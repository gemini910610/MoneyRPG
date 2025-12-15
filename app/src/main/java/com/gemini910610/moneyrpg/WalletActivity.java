package com.gemini910610.moneyrpg;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gemini910610.moneyrpg.dialogs.EditRecordDialog;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class RecordHelper extends SQLiteOpenHelper
{
    private static final String DB_NAME = "record.db";
    private static final int DB_VERSION = 1;

    public RecordHelper(Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE IF NOT EXISTS records (_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, date TEXT, money INTEGER, is_earn INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}

public class WalletActivity extends AppCompatActivity
{
    TextView balance_text;

    private RecordHelper helper;
    private RecordAdapter adapter;
    private ExecutorService executor;
    private int balance = 0;
    private int coin = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_wallet);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        balance_text = findViewById(R.id.balance_text);
        RecyclerView record_list = findViewById(R.id.record_list);

        executor = Executors.newSingleThreadExecutor();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed()
            {
                goBack();
            }
        });

        ArrayList<RecordAdapter.RecordData> datum = new ArrayList<>();

        record_list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecordAdapter(datum, data -> {
            EditRecordDialog dialog = new EditRecordDialog(this, new_data -> updateDataAsync(data, new_data, () -> {
                balance -= data.is_earn ? data.money : -data.money;
                balance += new_data.is_earn ? new_data.money : -new_data.money;
                balance_text.setText(MainActivity.stringFormat("%s $%d", getString(R.string.balance), balance));
            }));
            dialog.setData(data);
            dialog.enableDelete(() -> deleteDataAsync(data, () -> {
                balance -= data.is_earn ? data.money : -data.money;
                balance_text.setText(MainActivity.stringFormat("%s $%d", getString(R.string.balance), balance));
            }));
            dialog.show();
        });
        record_list.setAdapter(adapter);

        helper = new RecordHelper(this);

        loadDataAsync(() -> balance_text.setText(MainActivity.stringFormat("%s $%d", getString(R.string.balance), balance)));
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        executor.shutdown();
    }

    private void loadDataAsync(Runnable onLoaded)
    {
        executor.execute(() -> {
            int local_balance = 0;
            ArrayList<RecordAdapter.RecordData> datum = new ArrayList<>();

            SQLiteDatabase database = helper.getReadableDatabase();

            Cursor cursor = database.rawQuery("SELECT * FROM records ORDER BY date DESC", null);

            if (cursor.moveToFirst())
            {
                do
                {
                    int id = cursor.getInt(0);
                    String title = cursor.getString(1);
                    String date = cursor.getString(2);
                    int money = cursor.getInt(3);
                    boolean is_earn = cursor.getInt(4) == 1;

                    RecordAdapter.RecordData data = new RecordAdapter.RecordData(title, date, money, is_earn);
                    data.setID(id);
                    datum.add(data);

                    local_balance += is_earn ? money : -money;
                }
                while (cursor.moveToNext());
            }

            cursor.close();
            database.close();

            int final_balance = local_balance;
            runOnUiThread(() -> {
                balance = final_balance;
                adapter.setItems(datum);
                onLoaded.run();
            });
        });
    }

    private void updateDataAsync(RecordAdapter.RecordData old_date, RecordAdapter.RecordData new_data, Runnable onUpdated)
    {
        executor.execute(() -> {
            ContentValues values = new ContentValues();
            values.put("title", new_data.title);
            values.put("is_earn", new_data.is_earn ? 1 : 0);
            values.put("date", new_data.date);
            values.put("money", new_data.money);

            new_data.setID(old_date.getID());

            SQLiteDatabase database = helper.getWritableDatabase();
            database.update("records", values, "_id=?", new String[] { String.valueOf(new_data.getID()) });
            database.close();

            runOnUiThread(() -> {
                adapter.updateItem(old_date, new_data);
                onUpdated.run();
            });
        });
    }

    private void insertDataAsync(RecordAdapter.RecordData data, Runnable onInserted)
    {
        executor.execute(() -> {
            ContentValues values = new ContentValues();
            values.put("title", data.title);
            values.put("is_earn", data.is_earn ? 1 : 0);
            values.put("date", data.date);
            values.put("money", data.money);

            SQLiteDatabase database = helper.getWritableDatabase();
            long id = database.insert("records", null, values);
            database.close();

            data.setID(id);

            runOnUiThread(() -> {
                adapter.addItem(data);
                onInserted.run();
            });
        });
    }

    private void deleteDataAsync(RecordAdapter.RecordData data, Runnable onDeleted)
    {
        executor.execute(() -> {
            SQLiteDatabase database = helper.getWritableDatabase();
            database.delete("records", "_id=?", new String[] { String.valueOf(data.getID()) });
            database.close();

            runOnUiThread(() -> {
                adapter.removeItem(data);
                onDeleted.run();
            });
        });
    }

    public void goBack(View view)
    {
        goBack();
    }

    public void goBack()
    {
        Intent intent = new Intent();
        intent.putExtra("coin", coin);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void newRecord(View view)
    {
        EditRecordDialog dialog = new EditRecordDialog(this, data -> insertDataAsync(data, () -> {
            balance += data.is_earn ? data.money : -data.money;
            balance_text.setText(MainActivity.stringFormat("%s $%d", getString(R.string.balance), balance));

            Player.gainCoin(1000);
            coin += 1000;
        }));
        dialog.show();
    }
}
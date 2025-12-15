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
    RecordAdapter adapter;
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
            EditRecordDialog dialog = new EditRecordDialog(this, new_data -> {
                updateData(data, new_data);

                balance -= data.is_earn ? data.money : -data.money;
                balance += new_data.is_earn ? new_data.money : -new_data.money;
                balance_text.setText(MainActivity.stringFormat("%s $%d", getString(R.string.balance), balance));
            });
            dialog.setData(data);
            dialog.enableDelete(() -> {
                deleteData(data);

                balance -= data.is_earn ? data.money : -data.money;
                balance_text.setText(MainActivity.stringFormat("%s $%d", getString(R.string.balance), balance));
            });
            dialog.show();
        });
        record_list.setAdapter(adapter);

        helper = new RecordHelper(this);
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
                adapter.addItem(data);
                balance += is_earn ? money : -money;
            }
            while (cursor.moveToNext());
        }
        cursor.close();

        database.close();

        balance_text.setText(MainActivity.stringFormat("%s $%d", getString(R.string.balance), balance));
    }

    private void updateData(RecordAdapter.RecordData old_data, RecordAdapter.RecordData new_data)
    {
        ContentValues values = new ContentValues();
        values.put("title", new_data.title);
        values.put("is_earn", new_data.is_earn);
        values.put("date", new_data.date);
        values.put("money", new_data.money);

        new_data.setID(old_data.getID());

        SQLiteDatabase database = helper.getWritableDatabase();
        database.update("records", values, MainActivity.stringFormat("_id=%d", new_data.getID()), null);
        database.close();

        adapter.updateItem(old_data, new_data);
    }

    private void insertData(RecordAdapter.RecordData data)
    {
        ContentValues values = new ContentValues();
        values.put("title", data.title);
        values.put("is_earn", data.is_earn);
        values.put("date", data.date);
        values.put("money", data.money);

        SQLiteDatabase database = helper.getWritableDatabase();
        int id = Math.toIntExact(database.insert("records", null, values));
        database.close();

        data.setID(id);
        adapter.addItem(data);
    }

    private void deleteData(RecordAdapter.RecordData data)
    {
        SQLiteDatabase database = helper.getWritableDatabase();
        database.delete("records", MainActivity.stringFormat("_id=%d", data.getID()), null);
        database.close();

        adapter.removeItem(data);
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
        EditRecordDialog dialog = new EditRecordDialog(this, data -> {
            insertData(data);

            balance += data.is_earn ? data.money : -data.money;
            balance_text.setText(MainActivity.stringFormat("%s $%d", getString(R.string.balance), balance));

            coin += 1000;
        });
        dialog.show();
    }
}
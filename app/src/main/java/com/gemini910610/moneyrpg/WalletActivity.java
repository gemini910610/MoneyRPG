package com.gemini910610.moneyrpg;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;
import java.util.function.Consumer;

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

class DeleteCheckDialog extends Dialog
{
    public DeleteCheckDialog(Context context, Runnable onReset)
    {
        super(context);
        setContentView(R.layout.dialog_check);

        TextView message = findViewById(R.id.check_message);
        Button cancel_button = findViewById(R.id.cancel_button);
        Button ok_button = findViewById(R.id.ok_button);

        Window window = Objects.requireNonNull(getWindow());
        window.setBackgroundDrawableResource(R.drawable.rounded_corner_background);

        message.setText(R.string.check_delete);
        cancel_button.setOnClickListener(view -> cancel());
        ok_button.setOnClickListener(view -> {
            dismiss();
            onReset.run();
        });
    }
}

class EditRecordDialog extends Dialog
{
    EditText title_input, money_input;
    RadioGroup category_input;
    TextView date_input_text;
    Button delete_button;

    public EditRecordDialog(Context context, Consumer<RecordAdapter.RecordData> onOkClicked)
    {
        super(context);
        setContentView(R.layout.dialog_record_edit);

        title_input = findViewById(R.id.title_input);
        category_input = findViewById(R.id.category_input);
        date_input_text = findViewById(R.id.date_input_text);
        Button select_date_button = findViewById(R.id.select_date_button);
        money_input = findViewById(R.id.money_input);
        Button cancel_button = findViewById(R.id.cancel_button);
        delete_button = findViewById(R.id.delete_button);
        Button ok_button = findViewById(R.id.ok_button);

        Window window = Objects.requireNonNull(getWindow());
        window.setBackgroundDrawableResource(R.drawable.rounded_corner_background);

        select_date_button.setOnClickListener(view -> selectDate(context));
        cancel_button.setOnClickListener(view -> cancel());
        ok_button.setOnClickListener(view -> {
            String title = title_input.getText().toString();
            if (title.isEmpty())
            {
                title_input.requestFocus();
                return;
            }

            boolean is_earn = category_input.getCheckedRadioButtonId() == R.id.income_button;
            String date = date_input_text.getText().toString();

            String money_text = money_input.getText().toString();
            if (money_text.isEmpty())
            {
                money_input.requestFocus();
                return;
            }
            int money = Integer.parseInt(money_text);

            RecordAdapter.RecordData data = new RecordAdapter.RecordData(title, date, money, is_earn);
            onOkClicked.accept(data);
            dismiss();
        });
    }

    public void setData(RecordAdapter.RecordData data)
    {
        title_input.setText(data.title);
        title_input.requestFocus();
        category_input.check(data.is_earn ? R.id.income_button : R.id.outcome_button);
        date_input_text.setText(data.date);
        money_input.setText(String.valueOf(data.money));
    }

    private void selectDate(Context context)
    {
        Calendar calendar = Calendar.getInstance();
        int current_year = calendar.get(Calendar.YEAR);
        int current_month = calendar.get(Calendar.MONTH);
        int current_day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog date_picker = new DatePickerDialog(context, (picker, year, month, day) -> {
            month += 1;
            String date = MainActivity.stringFormat("%d-%02d-%02d", year, month, day);
            date_input_text.setText(date);
        }, current_year, current_month, current_day);
        date_picker.show();
    }

    public void enableDelete(Runnable onDeleteClicked)
    {
        delete_button.setVisibility(View.VISIBLE);
        delete_button.setOnClickListener(view -> {
            DeleteCheckDialog dialog = new DeleteCheckDialog(this.getContext(), () -> {
                dismiss();
                onDeleteClicked.run();
            });
            dialog.show();
        });
    }
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

        SQLiteDatabase database = helper.getReadableDatabase();
        database.update("records", values, MainActivity.stringFormat("_id=%d", old_data.getID()), null);
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

        SQLiteDatabase database = helper.getReadableDatabase();
        int id = Math.toIntExact(database.insert("records", null, values));
        database.close();

        data.setID(id);
        adapter.addItem(data);
    }

    private void deleteData(RecordAdapter.RecordData data)
    {
        SQLiteDatabase database = helper.getReadableDatabase();
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
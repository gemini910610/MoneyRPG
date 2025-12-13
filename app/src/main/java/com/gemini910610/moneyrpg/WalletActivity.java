package com.gemini910610.moneyrpg;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
    private RecordHelper helper;
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

        TextView money_text = findViewById(R.id.money_text);
        RecyclerView record_list = findViewById(R.id.record_list);
        Button back_button = findViewById(R.id.back_button);
        Button new_button = findViewById(R.id.new_button);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed()
            {
                goBack();
            }
        });

        ArrayList<RecordAdapter.RecordData> datum = new ArrayList<>();

        record_list.setLayoutManager(new LinearLayoutManager(this));
        RecordAdapter adapter = new RecordAdapter(datum);
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
                RecordAdapter.RecordData data = new RecordAdapter.RecordData(id, title, date, money, is_earn);
                adapter.addItem(data);
                balance += is_earn ? money : -money;
            }
            while (cursor.moveToNext());
        }
        cursor.close();

        database.close();

        money_text.setText(MainActivity.stringFormat("%s $%d", getString(R.string.balance), balance));
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
}
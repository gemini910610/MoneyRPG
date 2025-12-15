package com.gemini910610.moneyrpg.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.gemini910610.moneyrpg.MainActivity;
import com.gemini910610.moneyrpg.R;
import com.gemini910610.moneyrpg.RecordAdapter;

import java.util.Calendar;
import java.util.Objects;
import java.util.function.Consumer;

public class EditRecordDialog extends Dialog {
    EditText title_input, money_input;
    RadioGroup category_input;
    TextView date_input_text;
    Button delete_button;

    public EditRecordDialog(Context context, Consumer<RecordAdapter.RecordData> onOkClicked) {
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
            if (title.isEmpty()) {
                title_input.requestFocus();
                return;
            }

            boolean is_earn = category_input.getCheckedRadioButtonId() == R.id.income_button;
            String date = date_input_text.getText().toString();
            if (date.isEmpty())
            {
                return;
            }

            String money_text = money_input.getText().toString();
            if (money_text.isEmpty()) {
                money_input.requestFocus();
                return;
            }
            int money = Integer.parseInt(money_text);

            RecordAdapter.RecordData data = new RecordAdapter.RecordData(title, date, money, is_earn);
            onOkClicked.accept(data);
            dismiss();
        });
    }

    public void setData(RecordAdapter.RecordData data) {
        title_input.setText(data.title);
        title_input.requestFocus();
        category_input.check(data.is_earn ? R.id.income_button : R.id.outcome_button);
        date_input_text.setText(data.date);
        money_input.setText(String.valueOf(data.money));
    }

    private void selectDate(Context context) {
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

    public void enableDelete(Runnable onDeleteClicked) {
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

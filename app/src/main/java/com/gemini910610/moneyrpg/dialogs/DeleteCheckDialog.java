package com.gemini910610.moneyrpg.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.gemini910610.moneyrpg.R;

import java.util.Objects;

public class DeleteCheckDialog extends Dialog {
    public DeleteCheckDialog(Context context, Runnable onReset) {
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

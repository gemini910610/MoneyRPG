package com.gemini910610.moneyrpg.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gemini910610.moneyrpg.MainActivity;
import com.gemini910610.moneyrpg.R;

import java.util.Objects;

public class BattleResultDialog extends Dialog {
    public BattleResultDialog(Context context, boolean player_win, int exp, int coin, Runnable onClicked) {
        super(context);
        setContentView(R.layout.dialog_battle_result);

        LinearLayout dialog_view = findViewById(R.id.dialog_view);
        TextView result_text = findViewById(R.id.result_text);
        TextView exp_text = findViewById(R.id.exp_text);
        TextView coin_text = findViewById(R.id.coin_text);

        Window window = Objects.requireNonNull(getWindow());
        window.setBackgroundDrawableResource(R.drawable.rounded_corner_background);

        dialog_view.setOnClickListener(view -> {
            dismiss();
            onClicked.run();
        });
        result_text.setText(player_win ? R.string.win : R.string.lose);
        exp_text.setText(MainActivity.stringFormat("+%d", exp));
        coin_text.setText(MainActivity.stringFormat("+%d", coin));

        setOnCancelListener(dialog -> {
            dismiss();
            onClicked.run();
        });
    }
}

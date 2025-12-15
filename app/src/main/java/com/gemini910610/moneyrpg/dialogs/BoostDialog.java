package com.gemini910610.moneyrpg.dialogs;

import android.content.Context;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;

import com.gemini910610.moneyrpg.MainActivity;
import com.gemini910610.moneyrpg.Player;
import com.gemini910610.moneyrpg.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

public class BoostDialog extends BottomSheetDialog {
    private final Button random_button;
    private final Map<String, Button> boost_buttons = new Hashtable<>();

    private final Map<String, Integer> max_value;

    public BoostDialog(Context context, Map<String, Integer> max_value, Runnable onCanceled, BiConsumer<TextView, String> onItemClicked, Runnable onReset) {
        super(context);
        setContentView(R.layout.dialog_boost);
        setOnCancelListener(dialog -> onCanceled.run());

        Button reset_button = Objects.requireNonNull(findViewById(R.id.reset_button));
        random_button = Objects.requireNonNull(findViewById(R.id.random_button));

        this.max_value = max_value;

        Map<String, Integer> ability_view_ids = Map.of(
                "STR", R.id.str_boost,
                "DEX", R.id.dex_boost,
                "AGI", R.id.agi_boost,
                "VIT", R.id.vit_boost,
                "WIS", R.id.wis_boost,
                "LUC", R.id.luc_boost
        );

        for (String ability : ability_view_ids.keySet()) {
            int view_id = Objects.requireNonNull(ability_view_ids.get(ability));
            TableRow row = Objects.requireNonNull(findViewById(view_id));
            TextView text_view = (TextView) row.getChildAt(0);
            Button button = (Button) row.getChildAt(1);

            text_view.setText(MainActivity.stringFormat("%s(%d)", ability, Player.getAbility(ability)));
            button.setOnClickListener(view -> onItemClicked.accept(text_view, ability));
            boost_buttons.put(ability, button);
        }

        reset_button.setOnClickListener(view -> {
            ResetCheckDialog dialog = new ResetCheckDialog(context, () -> {
                dismiss();
                onReset.run();
            });
            dialog.show();
        });

        random_button.setOnClickListener(view -> {
            RandomCheckDialog dialog = new RandomCheckDialog(context, () -> {
                dismiss();
                onReset.run();
            });
            dialog.show();
        });
    }

    public void setupBoostButton() {
        int coin = Player.getCoin();

        for (Map.Entry<String, Integer> entry : max_value.entrySet()) {
            String ability = entry.getKey();
            int max_value = entry.getValue();

            int value = Player.getAbility(ability);
            int cost = calculateCost(ability, value);

            Button boost_button = Objects.requireNonNull(boost_buttons.get(ability));
            boost_button.setText(MainActivity.stringFormat("+($%d)", cost));
            boost_button.setEnabled(coin >= cost && value < max_value);
        }

        random_button.setEnabled(coin >= 500 && Player.getLevel() >= 10 && Player.getPokemonBox().size() > 1);
    }

    public int calculateCost(String ability, int current_value) {
        switch (ability) {
            case "STR":
            case "VIT":
                return (current_value + 1) * 50;
            case "DEX":
            case "AGI":
                return (current_value + 1) * 75;
            case "WIS":
            case "LUC":
                return (current_value + 1) * 100;
        }
        return 0;
    }
}

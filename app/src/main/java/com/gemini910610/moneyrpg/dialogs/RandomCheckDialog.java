package com.gemini910610.moneyrpg.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.gemini910610.moneyrpg.Player;
import com.gemini910610.moneyrpg.PokeDex;
import com.gemini910610.moneyrpg.R;

import java.util.Objects;

public class RandomCheckDialog extends Dialog {
    public RandomCheckDialog(Context context, Runnable onReset) {
        super(context);
        setContentView(R.layout.dialog_check);

        TextView message = findViewById(R.id.check_message);
        Button cancel_button = findViewById(R.id.cancel_button);
        Button ok_button = findViewById(R.id.ok_button);

        Window window = Objects.requireNonNull(getWindow());
        window.setBackgroundDrawableResource(R.drawable.rounded_corner_background);

        message.setText(R.string.check_random);
        cancel_button.setOnClickListener(view -> cancel());
        ok_button.setOnClickListener(view -> {
            Player.resetKeepBoxOnly();
//            PokeDex.Pokemon pokemon = PokeDex.randomPokemon(Player.getLevel(), Player.getPokemonBox());
            PokeDex.Pokemon pokemon = PokeDex.randomPokemon(Player.getLevel(), PokeDex.getBasicPokemons());
            Player.setPokemon(pokemon);
            Player.save();
            dismiss();
            onReset.run();
        });
    }
}

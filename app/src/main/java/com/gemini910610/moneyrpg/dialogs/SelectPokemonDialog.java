package com.gemini910610.moneyrpg.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.ImageView;

import com.gemini910610.moneyrpg.MainActivity;
import com.gemini910610.moneyrpg.PokeDex;
import com.gemini910610.moneyrpg.R;

import java.util.Objects;
import java.util.function.Consumer;

public class SelectPokemonDialog extends Dialog {
    public SelectPokemonDialog(Context context, Consumer<Integer> onSelected) {
        super(context);
        setContentView(R.layout.dialog_pokemon_select);

        ImageView pokemon_1 = findViewById(R.id.pokemon_1);
        ImageView pokemon_2 = findViewById(R.id.pokemon_2);
        ImageView pokemon_3 = findViewById(R.id.pokemon_3);

        Window window = Objects.requireNonNull(getWindow());
        window.setBackgroundDrawableResource(R.drawable.rounded_corner_background);
        setCancelable(false);

        MainActivity.summonPokemon(new PokeDex.Pokemon(1), pokemon_1, false);
        MainActivity.summonPokemon(new PokeDex.Pokemon(4), pokemon_2, false);
        MainActivity.summonPokemon(new PokeDex.Pokemon(7), pokemon_3, false);

        pokemon_1.setOnClickListener(view -> {
            onSelected.accept(1);
            dismiss();
        });
        pokemon_2.setOnClickListener(view -> {
            onSelected.accept(4);
            dismiss();
        });
        pokemon_3.setOnClickListener(view -> {
            onSelected.accept(7);
            dismiss();
        });
    }
}

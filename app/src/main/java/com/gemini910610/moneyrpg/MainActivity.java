package com.gemini910610.moneyrpg;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.Locale;
import java.util.function.Consumer;

class Player
{
    public static Player Instance;

    private int level, exp, needed_exp, coin;
    private int str, dex, agi, vit, wis, luc;
    private PokeDex.Pokemon pokemon;
    private final ArrayList<Integer> pokemon_box = new ArrayList<>();

    private final SharedPreferences preferences;

    public Player(Context context)
    {
        Instance = this;

        preferences = context.getSharedPreferences("player", Context.MODE_PRIVATE);
        load();
    }

    public int getLevel() { return level; }
    public int getEXP() { return exp; }
    public int getNeededExp() { return needed_exp; }
    public int getCoin() { return coin; }
    public int getSTR() { return str; }
    public int getDEX() { return dex; }
    public int getAGI() { return agi; }
    public int getVIT() { return vit; }
    public int getWIS() { return wis; }
    public int getLUC() { return luc; }
    public PokeDex.Pokemon getPokemon() { return pokemon; }
    public ArrayList<Integer> getPokemonBox() { return pokemon_box; }

    public void setSTR(int str) { this.str = str; }
    public void setDEX(int dex) { this.dex = dex; }
    public void setAGI(int agi) { this.agi = agi; }
    public void setVIT(int vit) { this.vit = vit; }
    public void setWID(int wis) { this.wis = wis; }
    public void setLUC(int luc) { this.luc = luc; }
    public void setPokemon(PokeDex.Pokemon pokemon) { this.pokemon = pokemon; }

    public void calculateNeededEXP()
    {
        needed_exp = (int)Math.pow(3 * level * level, 0.75);
    }

    public void gainEXP(int exp)
    {
        this.exp += exp;
        // check level up or evolution
    }

    public void gainCoin(int coin)
    {
        this.coin += coin;
    }

    public void gotchaPokemon(PokeDex.Pokemon pokemon)
    {
        int id = pokemon.id;

        // if pokemon is not basic pokemon, get its basic pokemon
        while (!PokeDex.Instance.basic_pokemons.contains(id))
        {
            id--;
        }

        if (!pokemon_box.contains(id))
        {
            pokemon_box.add(pokemon.id);

            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(String.valueOf(id), true);
            editor.apply();
        }
    }

    public void save()
    {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("level", level);
        editor.putInt("exp", exp);
        editor.putInt("coin", coin);
        editor.putInt("pokemon_id", pokemon.id);
        editor.putInt("STR", str);
        editor.putInt("DEX", dex);
        editor.putInt("AGI", agi);
        editor.putInt("VIT", vit);
        editor.putInt("WIS", wis);
        editor.putInt("LUC", luc);
        editor.apply();
    }

    public void load()
    {
        level = preferences.getInt("level", 1);
        exp = preferences.getInt("exp", 0);
        coin = preferences.getInt("coin", 1000);
        int pokemon_id = preferences.getInt("pokemon_id", -1);
        pokemon = pokemon_id == -1 ? null : new PokeDex.Pokemon(pokemon_id);
        str = preferences.getInt("STR", 3);
        dex = preferences.getInt("DEX", 0);
        agi = preferences.getInt("AGI", 0);
        vit = preferences.getInt("VIT", 3);
        wis = preferences.getInt("WIS", 0);
        luc = preferences.getInt("LUC", 0);

        calculateNeededEXP();

        for (int id = 1; id <= PokeDex.Instance.pokemon_count; id++)
        {
            if (preferences.getBoolean(String.valueOf(id), false))
            {
                pokemon_box.add(id);
            }
        }
    }

    public void reset()
    {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();

        load();
    }
}

class SelectPokemonDialog extends Dialog
{
    public SelectPokemonDialog(Context context, Consumer<Integer> onSelected)
    {
        super(context);
        setContentView(R.layout.dialog_select_pokemon);

        ImageView pokemon_1 = findViewById(R.id.pokemon_1);
        ImageView pokemon_2 = findViewById(R.id.pokemon_2);
        ImageView pokemon_3 = findViewById(R.id.pokemon_3);

        Window window = getWindow();
        assert window != null;
        window.setBackgroundDrawableResource(R.drawable.rounded_corner_background);
        setCancelable(false);

        MainActivity.summonPokemon(new PokeDex.Pokemon(1), pokemon_1);
        MainActivity.summonPokemon(new PokeDex.Pokemon(4), pokemon_2);
        MainActivity.summonPokemon(new PokeDex.Pokemon(7), pokemon_3);

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

public class MainActivity extends AppCompatActivity
{
    private TextView exp_text, coin_text;
    private RadarChart radar_chart;
    private ImageView pokemon_image;

    private Player player;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); // disable dark mode

        exp_text = findViewById(R.id.exp_text);
        coin_text = findViewById(R.id.coin_text);
        radar_chart = findViewById(R.id.radar_chart);
        pokemon_image = findViewById(R.id.pokemon_image);

        new PokeDex(this);

        player = new Player(this);
        setExpText();
        setCoinText();

        if (player.getPokemon() == null)
        {
            SelectPokemonDialog dialog = new SelectPokemonDialog(this, id -> {
                PokeDex.Pokemon pokemon = new PokeDex.Pokemon(id);
                player.gotchaPokemon(pokemon);
                player.setPokemon(pokemon);
                summonPokemon(pokemon, pokemon_image);
            });
            dialog.show();
        }
        else
        {
            summonPokemon(player.getPokemon(), pokemon_image);
        }

        radar_chart.update();
    }

    private void setExpText()
    {
        String text = stringFormat("LV. %d (%d/%d)", player.getLevel(), player.getEXP(), player.getNeededExp());
        exp_text.setText(text);
    }

    private void setCoinText()
    {
        String text = stringFormat("%s: %d", getString(R.string.coin), player.getCoin());
        coin_text.setText(text);
    }

    public static String stringFormat(String format, Object... args)
    {
        return String.format(Locale.getDefault(), format, args);
    }

    public static void summonPokemon(PokeDex.Pokemon pokemon, ImageView image)
    {
        String pokemon_image_url = stringFormat("file:///android_asset/pokemon/%d.gif", pokemon.id);
        Glide.with(image.getContext()).asGif().load(pokemon_image_url).override(Target.SIZE_ORIGINAL).into(image);
    }

    public void battle(View view) {
    }

    public void showDialog(View view) {
    }

    public void charge(View view) {
    }

    @Override
    protected void onStop() {
        super.onStop();
        player.save();
    }
}
package com.gemini910610.moneyrpg;

import android.os.Bundle;
import android.view.View;
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

import java.util.Locale;

class Player
{
    public static Player Instance;

    private int level, exp, needed_exp, coin;
    private int str, dex, agi, vit, wis, luc;
    private PokeDex.Pokemon pokemon;

    public Player()
    {
        Instance = this;

        level = 1;
        exp = 0;
        coin = 1000;
        str = 3;
        dex = 0;
        agi = 0;
        vit = 3;
        wis = 0;
        luc = 0;
        pokemon = null;

        calculateNeededEXP();
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

        player = new Player();
        setExpText();
        setCoinText();

        if (player.getPokemon() == null)
        {
            player.setPokemon(PokeDex.randomPokemon());
        }
        summonPokemon(player.getPokemon(), pokemon_image);

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

    private void summonPokemon(PokeDex.Pokemon pokemon, ImageView image)
    {
        String pokemon_image_url = stringFormat("file:///android_asset/pokemon/%d.gif", pokemon.id);
        Glide.with(this).asGif().load(pokemon_image_url).override(Target.SIZE_ORIGINAL).into(image);
    }

    public void battle(View view) {
    }

    public void showDialog(View view) {
    }

    public void charge(View view) {
    }
}
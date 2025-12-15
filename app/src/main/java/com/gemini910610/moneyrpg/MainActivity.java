package com.gemini910610.moneyrpg;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.gemini910610.moneyrpg.dialogs.BoostDialog;
import com.gemini910610.moneyrpg.dialogs.SelectPokemonDialog;
import com.google.android.material.snackbar.Snackbar;

import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity
{
    private TextView exp_text, coin_text;
    private ImageView pokemon_image;
    private BoostDialog boost_dialog;

    private Typeface message_font;

    private final ActivityResultLauncher<Intent> wallet_launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        Intent intent = result.getData();
        if (intent == null)
        {
            return;
        }

        int coin = intent.getIntExtra("coin", 0);
        if (coin > 0)
        {
            setCoinText();
            showMessage(stringFormat("%s +%d", getString(R.string.coin), coin));
        }
    });

    private final ActivityResultLauncher<Intent> battle_launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        Intent intent = result.getData();
        if (intent == null)
        {
            return;
        }

        int exp = intent.getIntExtra("exp", 0);
        int coin = intent.getIntExtra("coin", 0);
        boolean is_evolution = Player.gainEXP(exp);
        Player.gainCoin(coin);
        setExpText();
        setCoinText();

        if (is_evolution)
        {
            summonPokemon(Player.getPokemon(), pokemon_image, false);
        }

        String message = stringFormat("%s +%d  %s +%d", getString(R.string.coin), coin, getString(R.string.exp), exp);
        showMessage(message);
    });

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
        RadarChart radar_chart = findViewById(R.id.radar_chart);
        pokemon_image = findViewById(R.id.pokemon_image);

        message_font = ResourcesCompat.getFont(this, R.font.pixel);

        Map<String, Integer> max_value = Map.of(
                "STR", 100,
                "DEX", 50,
                "AGI", 50,
                "VIT", 100,
                "WIS", 20,
                "LUC", 20
        );
        radar_chart.setMaxValue(max_value);

        Player.load();
        setExpText();
        setCoinText();

        if (Player.getPokemon() == null)
        {
            SelectPokemonDialog dialog = new SelectPokemonDialog(this, id -> {
                PokeDex.Pokemon pokemon = new PokeDex.Pokemon(id);
                Player.gotchaPokemon(pokemon);
                Player.setPokemon(pokemon);
                summonPokemon(pokemon, pokemon_image, false);
            });
            dialog.show();
        }
        else
        {
            summonPokemon(Player.getPokemon(), pokemon_image, false);
        }

        radar_chart.update();

        boost_dialog = new BoostDialog(this, max_value, radar_chart::update, this::boostAbility, this::recreate);
    }

    private void setExpText()
    {
        String text = stringFormat("LV. %d (%d/%d)", Player.getLevel(), Player.getEXP(), Player.getNeededExp());
        exp_text.setText(text);
    }

    private void setCoinText()
    {
        String text = stringFormat("%s: %d", getString(R.string.coin), Player.getCoin());
        coin_text.setText(text);
    }

    public static String stringFormat(String format, Object... args)
    {
        return String.format(Locale.getDefault(), format, args);
    }

    public static void summonPokemon(PokeDex.Pokemon pokemon, ImageView image, boolean is_back)
    {
        String pokemon_image_url = stringFormat("file:///android_asset/%s/%d.gif", is_back ? "pokemon_back" : "pokemon", pokemon.id);
        Glide.with(image.getContext()).asGif().load(pokemon_image_url).override(Target.SIZE_ORIGINAL).into(image);
    }

    private void boostAbility(TextView text_view, String ability)
    {
        int value = Player.getAbility(ability);
        int cost = boost_dialog.calculateCost(ability, value);
        Player.setAbility(ability, value + 1);

        text_view.setText(stringFormat("%s(%d)", ability, value + 1));
        Player.gainCoin(-cost);
        setCoinText();
        boost_dialog.setupBoostButton();
    }

    private void showMessage(String message)
    {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.main), message, Snackbar.LENGTH_SHORT);
        snackbar.setAnchorView(R.id.boost_button);
        snackbar.setAnimationMode(Snackbar.ANIMATION_MODE_FADE);

        View view = snackbar.getView();
        TextView text = view.findViewById(com.google.android.material.R.id.snackbar_text);
        text.setTypeface(message_font);
        text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        snackbar.show();
    }

    public void gotoBattle(View view)
    {
        Intent intent = new Intent(this, BattleActivity.class);
        battle_launcher.launch(intent);
    }

    public void showBoostDialog(View view)
    {
        boost_dialog.setupBoostButton();
        boost_dialog.show();
    }

    public void gotoWallet(View view)
    {
        Intent intent = new Intent(this, WalletActivity.class);
        wallet_launcher.launch(intent);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        Player.save();
    }
}
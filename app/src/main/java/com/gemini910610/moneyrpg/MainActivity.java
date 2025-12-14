package com.gemini910610.moneyrpg;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

class Player
{
    private static Player Instance;

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

    public static int getLevel() { return Instance.level; }
    public static int getEXP() { return Instance.exp; }
    public static int getNeededExp() { return Instance.needed_exp; }
    public static int getCoin() { return Instance.coin; }
    public static int getSTR() { return Instance.str; }
    public static int getDEX() { return Instance.dex; }
    public static int getAGI() { return Instance.agi; }
    public static int getVIT() { return Instance.vit; }
    public static int getWIS() { return Instance.wis; }
    public static int getLUC() { return Instance.luc; }
    public static int getAbility(String ability)
    {
        switch (ability)
        {
            case "STR":
                return getSTR();
            case "DEX":
                return getDEX();
            case "AGI":
                return getAGI();
            case "VIT":
                return getVIT();
            case "WIS":
                return getWIS();
            case "LUC":
                return getLUC();
        }
        return 0;
    }
    public static PokeDex.Pokemon getPokemon() { return Instance.pokemon; }
    public static ArrayList<Integer> getPokemonBox() { return Instance.pokemon_box; }

    public static void setSTR(int str) { Instance.str = str; }
    public static void setDEX(int dex) { Instance.dex = dex; }
    public static void setAGI(int agi) { Instance.agi = agi; }
    public static void setVIT(int vit) { Instance.vit = vit; }
    public static void setWIS(int wis) { Instance.wis = wis; }
    public static void setLUC(int luc) { Instance.luc = luc; }
    public static void setAbility(String ability, int value)
    {
        switch (ability)
        {
            case "STR":
                setSTR(value);
                break;
            case "DEX":
                setDEX(value);
                break;
            case "AGI":
                setAGI(value);
                break;
            case "VIT":
                setVIT(value);
                break;
            case "WIS":
                setWIS(value);
                break;
            case "LUC":
                setLUC(value);
                break;
        }
    }
    public static void setPokemon(PokeDex.Pokemon pokemon) { Instance.pokemon = pokemon; }

    private void calculateNeededEXP()
    {
        needed_exp = (int) Math.pow(3 * level * level, 0.75);
    }

    public static void gainEXP(int exp)
    {
        Instance.exp += exp;
        // check level up or evolution
    }

    public static void gainCoin(int coin)
    {
        Instance.coin += coin;
    }

    public static void gotchaPokemon(PokeDex.Pokemon pokemon)
    {
        int id = pokemon.id;

        // if pokemon is not basic pokemon, get its basic pokemon
        while (!PokeDex.getBasicPokemons().contains(id))
        {
            id--;
        }

        if (!Instance.pokemon_box.contains(id))
        {
            Instance.pokemon_box.add(id);

            SharedPreferences.Editor editor = Instance.preferences.edit();
            editor.putBoolean(String.valueOf(id), true);
            editor.apply();
        }
    }

    public static void save()
    {
        SharedPreferences.Editor editor = Instance.preferences.edit();
        editor.putInt("level", Instance.level);
        editor.putInt("exp", Instance.exp);
        editor.putInt("coin", Instance.coin);
        PokeDex.Pokemon pokemon = Instance.pokemon;
        editor.putInt("pokemon_id", pokemon == null ? -1 : pokemon.id);
        editor.putInt("STR", Instance.str);
        editor.putInt("DEX", Instance.dex);
        editor.putInt("AGI", Instance.agi);
        editor.putInt("VIT", Instance.vit);
        editor.putInt("WIS", Instance.wis);
        editor.putInt("LUC", Instance.luc);
        editor.apply();
    }

    public static void load()
    {
        Instance.level = Instance.preferences.getInt("level", 1);
        Instance.exp = Instance.preferences.getInt("exp", 0);
        Instance.coin = Instance.preferences.getInt("coin", 1000);
        int pokemon_id = Instance.preferences.getInt("pokemon_id", -1);
        Instance.pokemon = pokemon_id == -1 ? null : new PokeDex.Pokemon(pokemon_id);
        Instance.str = Instance.preferences.getInt("STR", 3);
        Instance.dex = Instance.preferences.getInt("DEX", 0);
        Instance.agi = Instance.preferences.getInt("AGI", 0);
        Instance.vit = Instance.preferences.getInt("VIT", 3);
        Instance.wis = Instance.preferences.getInt("WIS", 0);
        Instance.luc = Instance.preferences.getInt("LUC", 0);

        Instance.calculateNeededEXP();

        Instance.pokemon_box.clear();
        for (int id = 1; id <= PokeDex.getPokemonCount(); id++)
        {
            if (Instance.preferences.getBoolean(String.valueOf(id), false))
            {
                Instance.pokemon_box.add(id);
            }
        }
    }

    public static void resetAll()
    {
        SharedPreferences.Editor editor = Instance.preferences.edit();
        editor.clear();
        editor.commit();

        load();
    }

    public static void resetKeepBoxOnly()
    {
        ArrayList<Integer> pokemon_box = new ArrayList<>(getPokemonBox());

        resetAll();

        for (int id: pokemon_box)
        {
            PokeDex.Pokemon pokemon = new PokeDex.Pokemon(id);
            gotchaPokemon(pokemon);
        }
    }
}

class SelectPokemonDialog extends Dialog
{
    public SelectPokemonDialog(Context context, Consumer<Integer> onSelected)
    {
        super(context);
        setContentView(R.layout.dialog_pokemon_select);

        ImageView pokemon_1 = findViewById(R.id.pokemon_1);
        ImageView pokemon_2 = findViewById(R.id.pokemon_2);
        ImageView pokemon_3 = findViewById(R.id.pokemon_3);

        Window window = Objects.requireNonNull(getWindow());
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

class ResetCheckDialog extends Dialog
{
    public ResetCheckDialog(Context context, Runnable onReset)
    {
        super(context);
        setContentView(R.layout.dialog_check);

        TextView message = findViewById(R.id.check_message);
        Button cancel_button = findViewById(R.id.cancel_button);
        Button ok_button = findViewById(R.id.ok_button);

        Window window = Objects.requireNonNull(getWindow());
        window.setBackgroundDrawableResource(R.drawable.rounded_corner_background);

        message.setText(R.string.check_reset);
        cancel_button.setOnClickListener(view -> cancel());
        ok_button.setOnClickListener(view -> {
            Player.resetAll();
            dismiss();
            onReset.run();
        });
    }
}

class RandomCheckDialog extends Dialog
{
    public RandomCheckDialog(Context context, Runnable onReset)
    {
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
//            PokeDex.Pokemon pokemon = PokeDex.randomPokemon(Player.getPokemonBox());
            PokeDex.Pokemon pokemon = PokeDex.randomPokemon(PokeDex.getBasicPokemons());
            Player.setPokemon(pokemon);
            Player.save();
            dismiss();
            onReset.run();
        });
    }
}

class BoostDialog extends BottomSheetDialog
{
    private final Button random_button;
    private final Map<String, Button> boost_buttons = new Hashtable<>();

    private final Map<String, Integer> max_value;

    public BoostDialog(Context context, Map<String, Integer> max_value, Runnable onCanceled, BiConsumer<TextView, String> onItemClicked, Runnable onReset)
    {
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

        for (String ability:ability_view_ids.keySet())
        {
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

    public void setupBoostButton()
    {
        int coin = Player.getCoin();

        for (Map.Entry<String, Integer> entry: max_value.entrySet())
        {
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

    public int calculateCost(String ability, int current_value)
    {
        switch (ability)
        {
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

public class MainActivity extends AppCompatActivity
{
    private TextView exp_text, coin_text;
    private ImageView pokemon_image;
    private BoostDialog boost_dialog;

    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        Intent intent = result.getData();
        if (intent == null)
        {
            return;
        }

        int coin = intent.getIntExtra("coin", 0);
        if (coin > 0)
        {
            Player.gainCoin(coin);
            setCoinText();
            showMessage(stringFormat("%s +%d", getString(R.string.coin), coin));
        }
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

        Map<String, Integer> max_value = Map.of(
                "STR", 100,
                "DEX", 50,
                "AGI", 50,
                "VIT", 100,
                "WIS", 20,
                "LUC", 20
        );
        radar_chart.setMaxValue(max_value);

        new PokeDex(this);
        new Player(this);

        setExpText();
        setCoinText();

        if (Player.getPokemon() == null)
        {
            SelectPokemonDialog dialog = new SelectPokemonDialog(this, id -> {
                PokeDex.Pokemon pokemon = new PokeDex.Pokemon(id);
                Player.gotchaPokemon(pokemon);
                Player.setPokemon(pokemon);
                summonPokemon(pokemon, pokemon_image);
            });
            dialog.show();
        }
        else
        {
            summonPokemon(Player.getPokemon(), pokemon_image);
        }

        radar_chart.update();

        boost_dialog = new BoostDialog(this, max_value, radar_chart::update, this::boostAbility, this::restart);
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

    public static void summonPokemon(PokeDex.Pokemon pokemon, ImageView image)
    {
        String pokemon_image_url = stringFormat("file:///android_asset/pokemon/%d.gif", pokemon.id);
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
        text.setTypeface(getResources().getFont(R.font.pixel));
        text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        snackbar.show();
    }

    public void restart()
    {
        Intent intent = new Intent(this, MainActivity.class);
        finish();
        startActivity(intent);
    }

    public void battle(View view)
    {
    }

    public void showBoostDialog(View view)
    {
        boost_dialog.setupBoostButton();
        boost_dialog.show();
    }

    public void gotoWallet(View view)
    {
        Intent intent = new Intent(this, WalletActivity.class);
        launcher.launch(intent);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        Player.save();
    }
}
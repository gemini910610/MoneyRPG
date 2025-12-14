package com.gemini910610.moneyrpg;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

class GameCharacter
{
    private int level, atk, life, max_life, miss_rate, cd;
    private final PokeDex.Pokemon pokemon;
    private final boolean belong_to_player;

    public GameCharacter(int level, PokeDex.Pokemon pokemon, int str, int dex, int agi, int vit, boolean belong_to_player)
    {
        this.level = level;
        this.pokemon = pokemon;
        this.belong_to_player = belong_to_player;

        initializeAbility(str, dex, agi, vit);
        ScaleAnimation animation = initializeAnimation();
    }

    private void initializeAbility(int str, int dex, int agi, int vit)
    {
        atk = str;
        max_life = vit * 2;
        life = max_life;
        miss_rate = dex;
        cd = 1000 - 10 * agi;
    }

    private ScaleAnimation initializeAnimation()
    {
        int pivot_x = belong_to_player ? 0 : 1;
        int pivot_y = belong_to_player ? 1 : 0;
        ScaleAnimation animation = new ScaleAnimation(1, 1.1f, 1, 1.1f, ScaleAnimation.RELATIVE_TO_SELF, pivot_x, Animation.RELATIVE_TO_SELF, pivot_y);
        animation.setDuration(100);
        animation.setRepeatCount(1);
        animation.setRepeatMode(Animation.REVERSE);
        return animation;
    }

    public void setupUI(BattleActivity activity, int pokemon_image_id, int level_text_id, int life_text_id, int life_bar_id)
    {
        ImageView pokemon_image = activity.findViewById(pokemon_image_id);
        TextView level_text = activity.findViewById(level_text_id);
        TextView life_text = activity.findViewById(life_text_id);
        ProgressBar life_bar = activity.findViewById(life_bar_id);

        MainActivity.summonPokemon(pokemon, pokemon_image, belong_to_player);
        level_text.setText(MainActivity.stringFormat("LV.%d", level));
        life_text.setText(MainActivity.stringFormat("%d/%d", life, max_life));
        life_bar.setMax(max_life);
        life_bar.setProgress(life);
        life_bar.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
    }
}

class PlayerCharacter extends GameCharacter
{
    public PlayerCharacter()
    {
        super(Player.getLevel(), Player.getPokemon(), Player.getSTR(), Player.getDEX(), Player.getAGI(), Player.getVIT(), true);
    }
}

class OpponentCharacter extends GameCharacter
{
    public OpponentCharacter(int level, PokeDex.Pokemon pokemon, int str, int dex, int agi, int vit, boolean belong_to_player)
    {
        super(level, pokemon, str, dex, agi, vit, belong_to_player);
    }

    public static OpponentCharacter create(int level)
    {
        if (level > 35)
        {
            level += (int) (Math.random() * 9) - 3; // -3 ~ +5
        }
        else if (level > 10)
        {
            level += (int) (Math.random() * 5) - 2; // -2 ~ +2
        }
        PokeDex.Pokemon pokemon = PokeDex.randomPokemon(level, PokeDex.getBasicPokemons());

        int str = 1;
        int dex = 0;
        int agi = 0;
        int vit = 1;

        int point = (int) Math.pow(level, 1.5) * (int) Math.ceil(level * 0.2) * (int) Math.log10(level * 10);
        while (point > 0)
        {
            switch ((int) (Math.random() * 4))
            {
                case 0:
                    str++;
                    point--;
                    break;
                case 1:
                    if (dex != 50)
                    {
                        dex++;
                        point--;
                    }
                    break;
                case 2:
                    if (agi != 50)
                    {
                        agi++;
                        point--;
                    }
                    break;
                case 3:
                    vit++;
                    point--;
                    break;
            }
        }

        return new OpponentCharacter(level, pokemon, str, dex, agi, vit, false);
    }
}

public class BattleActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_battle);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        PlayerCharacter player_character = new PlayerCharacter();
        player_character.setupUI(this, R.id.player_image, R.id.player_level_text, R.id.player_life_text, R.id.player_life_bar);

        OpponentCharacter opponent_character = OpponentCharacter.create(Player.getLevel());
        opponent_character.setupUI(this, R.id.opponent_image, R.id.opponent_level_text, R.id.opponent_life_text, R.id.opponent_life_bar);
    }
}
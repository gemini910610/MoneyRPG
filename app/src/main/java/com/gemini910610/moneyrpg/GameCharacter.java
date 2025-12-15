package com.gemini910610.moneyrpg;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class GameCharacter
{
    private ImageView pokemon_image;
    private TextView life_text;
    private ProgressBar life_bar;

    private ScaleAnimation animation;

    protected int level;
    private int atk, life, max_life, dodge_rate, cd;
    private final PokeDex.Pokemon pokemon;
    private final boolean belong_to_player;

    public GameCharacter(int level, PokeDex.Pokemon pokemon, int str, int dex, int agi, int vit, boolean belong_to_player)
    {
        this.level = level;
        this.pokemon = pokemon;
        this.belong_to_player = belong_to_player;

        initializeAbility(str, dex, agi, vit);
        initializeAnimation();
    }

    public int getLife() { return life; }

    public PokeDex.Pokemon getPokemon() { return pokemon; }

    public int getCD() { return cd; }

    private void initializeAbility(int str, int dex, int agi, int vit)
    {
        atk = str;
        max_life = vit * 2;
        life = max_life;
        dodge_rate = dex;
        cd = 1000 - 10 * agi;
    }

    private void initializeAnimation()
    {
        int pivot_x = belong_to_player ? 0 : 1;
        int pivot_y = belong_to_player ? 1 : 0;
        animation = new ScaleAnimation(1, 1.25f, 1, 1.25f, ScaleAnimation.RELATIVE_TO_SELF, pivot_x, Animation.RELATIVE_TO_SELF, pivot_y);
        animation.setDuration(100);
        animation.setRepeatCount(1);
        animation.setRepeatMode(Animation.REVERSE);
    }

    public void setupUI(BattleActivity activity, int pokemon_image_id, int level_text_id, int life_text_id, int life_bar_id)
    {
        pokemon_image = activity.findViewById(pokemon_image_id);
        TextView level_text = activity.findViewById(level_text_id);
        life_text = activity.findViewById(life_text_id);
        life_bar = activity.findViewById(life_bar_id);

        MainActivity.summonPokemon(pokemon, pokemon_image, belong_to_player);
        level_text.setText(MainActivity.stringFormat("LV.%d", level));
        life_text.setText(MainActivity.stringFormat("%d/%d", life, max_life));
        life_bar.setMax(max_life);
        life_bar.setProgress(life);
        life_bar.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
    }

    public void attack(GameCharacter target)
    {
        pokemon_image.startAnimation(animation);

        if (target.isMiss())
        {
            return;
        }

        target.life -= atk;
        if (target.life < 0)
        {
            target.life = 0;
        }
        target.updateLife();

        if (target.life <= atk)
        {
            target.life_bar.setProgressTintList(ColorStateList.valueOf(Color.RED));
        }
        else if ((float) target.life / target.max_life <= 0.5)
        {
            target.life_bar.setProgressTintList(ColorStateList.valueOf(Color.rgb(255, 125, 0)));
        }
    }

    private void updateLife()
    {
        life_text.setText(MainActivity.stringFormat("%d/%d", life, max_life));
        life_bar.setProgress(life);
    }

    private boolean isMiss()
    {
        return Math.random() * 100 < dodge_rate;
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

        int point = (int) (Math.pow(level, 1.5) * Math.ceil(level * 0.2) * Math.log10(level * 10));
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

    private int basicDropExp(int defeater_level)
    {
        int basic = (int) (1.5 * level);
        int level_distance = level - defeater_level;
        float reward = (float) level / defeater_level;
        return (int) (basic * reward / (1 - level_distance * 0.1));
    }

    public int[] getRewards(int defeater_level, int wis, int luc)
    {
        int reward_rate = basicDropExp(defeater_level);
        int exp = (int) (reward_rate * (1 + wis * 0.05));
        int coin = (int) (reward_rate * 50 * (1 + luc * 0.05));
        return new int[] {exp, coin};
    }
}

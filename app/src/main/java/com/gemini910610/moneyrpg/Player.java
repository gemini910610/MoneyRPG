package com.gemini910610.moneyrpg;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

public class Player
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

    public static boolean gainEXP(int exp)
    {
        // return if evolution
        Instance.exp += exp;

        boolean is_evolution = false;

        while (Instance.exp >= Instance.needed_exp)
        {
            Instance.level++;
            Instance.exp -= Instance.needed_exp;
            Instance.calculateNeededEXP();

            if (Instance.level == Instance.pokemon.evolution_level)
            {
                is_evolution = true;
                Instance.pokemon = Instance.pokemon.evolution();
            }
        }

        return is_evolution;
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
        editor.apply();

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

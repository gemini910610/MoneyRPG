package com.gemini910610.moneyrpg;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;

public class PokeDex
{
    private static PokeDex Instance;

    private final SQLiteDatabase database;
    private final ArrayList<Integer> basic_pokemons = new ArrayList<>();

    public PokeDex(Context context)
    {
        SQLiteAssetHelper helper = new SQLiteAssetHelper(context, "pokemon.db", null, 1);
        database = helper.getReadableDatabase();

        Cursor cursor = database.rawQuery("SELECT id FROM pokemon WHERE basic=1", null);
        cursor.moveToFirst();
        do
        {
            basic_pokemons.add(cursor.getInt(0));
        }
        while (cursor.moveToNext());
        cursor.close();

        cursor = database.rawQuery("SELECT count(*) FROM pokemon", null);
        cursor.moveToFirst();
        cursor.close();
    }
    public static void init(Context context)
    {
        if (Instance == null)
        {
            Instance = new PokeDex(context);
        }
    }

    public static ArrayList<Integer> getBasicPokemons() { return Instance.basic_pokemons; }

    public static class Pokemon
    {
        public int id;
        public ArrayList<Integer> evolutions = new ArrayList<>();
        public boolean is_basic;
        public int evolution_level;

        public Pokemon(int id)
        {
            this.id = id;

            Cursor cursor = PokeDex.Instance.database.rawQuery("SELECT * FROM pokemon WHERE id=?", new String[] { String.valueOf(id) });
            cursor.moveToFirst();
            String evolution_string = cursor.getString(1);
            if (evolution_string != null)
            {
                for (String evolution_id: evolution_string.split(","))
                {
                    evolutions.add(Integer.parseInt(evolution_id));
                }
            }
            is_basic = cursor.getInt(2) == 1;
            evolution_level = cursor.getInt(3);
            cursor.close();
        }

        public Pokemon evolution()
        {
            if (evolutions.isEmpty())
            {
                return this;
            }

            int id = evolutions.get(0);
            return new Pokemon(id);
        }
    }

    public static Pokemon randomPokemon(int level, ArrayList<Integer> pokemons)
    {
        int index = (int) (Math.random() * pokemons.size());
        int id = pokemons.get(index);
        Pokemon pokemon = new Pokemon(id);

        while (level >= pokemon.evolution_level)
        {
            if (pokemon.evolutions.isEmpty())
            {
                break;
            }
            pokemon = pokemon.evolution();
        }

        return pokemon;
    }
}

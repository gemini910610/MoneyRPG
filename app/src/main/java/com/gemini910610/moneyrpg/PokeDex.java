package com.gemini910610.moneyrpg;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;

public class PokeDex
{
    private static PokeDex Instance;

    private final SQLiteAssetHelper helper;
    private final ArrayList<Integer> basic_pokemons = new ArrayList<>();
    private final int pokemon_count;

    public PokeDex(Context context)
    {
        Instance = this;

        helper = new SQLiteAssetHelper(context, "pokemon.db", null, 1);
        SQLiteDatabase database = helper.getReadableDatabase();

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
        pokemon_count = cursor.getInt(0);
        cursor.close();

        database.close();
    }

    public static ArrayList<Integer> getBasicPokemons() { return Instance.basic_pokemons; }
    public static int getPokemonCount() { return Instance.pokemon_count; }

    public static class Pokemon
    {
        public int id;
        public ArrayList<Integer> evolution = new ArrayList<>();
        public boolean is_basic;
        public int evolution_level;

        public Pokemon(int id)
        {
            this.id = id;

            SQLiteDatabase database = PokeDex.Instance.helper.getReadableDatabase();

            String query = MainActivity.stringFormat("SELECT * FROM pokemon WHERE id=%d", id);
            Cursor cursor = database.rawQuery(query, null);
            cursor.moveToFirst();
            String evolution_string = cursor.getString(1);
            if (evolution_string != null)
            {
                for (String evolution_id: evolution_string.split(","))
                {
                    evolution.add(Integer.parseInt(evolution_id));
                }
            }
            is_basic = cursor.getInt(2) == 1;
            evolution_level = cursor.getInt(3);
            cursor.close();

            database.close();
        }
    }

    public static Pokemon randomPokemon(ArrayList<Integer> pokemons)
    {
        int index = (int) (Math.random() * pokemons.size());
        int id = pokemons.get(index);
        Pokemon pokemon = new Pokemon(id);

        while (Player.getLevel() >= pokemon.evolution_level)
        {
            if (pokemon.evolution.isEmpty())
            {
                break;
            }
            id = pokemon.evolution.get(0);
            pokemon = new Pokemon(id);
        }

        return pokemon;
    }
}

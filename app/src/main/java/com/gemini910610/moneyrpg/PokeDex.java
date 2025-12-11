package com.gemini910610.moneyrpg;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;

public class PokeDex
{
    public static PokeDex Instance;

    public SQLiteDatabase database;
    ArrayList<Integer> basic_pokemons = new ArrayList<>();

    public PokeDex(Context context)
    {
        Instance = this;

        SQLiteAssetHelper helper = new SQLiteAssetHelper(context, "pokemon.db", null, 1);
        database = helper.getReadableDatabase();

        Cursor cursor = database.rawQuery("select id from pokemon where basic=1", null);
        cursor.moveToFirst();
        do
        {
            basic_pokemons.add(cursor.getInt(0));
        }
        while (cursor.moveToNext());
        cursor.close();

        cursor = database.rawQuery("select count(*) from pokemon", null);
        cursor.moveToFirst();
        int pokemon_count = cursor.getInt(0);
        cursor.close();
    }

    public static class Pokemon
    {
        public int id;
        public ArrayList<Integer> evolution = new ArrayList<>();
        public boolean is_basic;
        public int evolution_level;

        public Pokemon(int id)
        {
            this.id = id;

            String query = MainActivity.stringFormat("select * from pokemon where id=%d", id);
            Cursor cursor = PokeDex.Instance.database.rawQuery(query, null);
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
        }
    }

    public static Pokemon randomPokemon()
    {
        int index = (int)(Math.random() * Instance.basic_pokemons.size());
        int id = Instance.basic_pokemons.get(index);
        Pokemon pokemon = new Pokemon(id);

        while (Player.Instance.getLevel() >= pokemon.evolution_level)
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

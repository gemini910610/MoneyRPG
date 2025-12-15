package com.gemini910610.moneyrpg;

import android.app.Application;

public class MoneyRPGApp extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();

        Player.init(this);
        PokeDex.init(this);
    }
}

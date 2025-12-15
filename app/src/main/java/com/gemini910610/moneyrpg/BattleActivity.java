package com.gemini910610.moneyrpg;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gemini910610.moneyrpg.dialogs.BattleResultDialog;

public class BattleActivity extends AppCompatActivity
{
    private enum Turn { PLAYER, OPPONENT }
    private Turn current_turn = Turn.PLAYER;
    private boolean battle_running = true;
    private Handler handler;
    private Runnable battle_loop;

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

        handler = new Handler();

        battle_loop = new Runnable()
        {
            @Override
            public void run()
            {
                if (!battle_running)
                {
                    return;
                }

                switch (current_turn)
                {
                    case PLAYER:
                        player_character.attack(opponent_character);
                        if (opponent_character.getLife() == 0)
                        {
                            endBattle(true, opponent_character);
                            return;
                        }

                        current_turn = Turn.OPPONENT;
                        handler.postDelayed(this, opponent_character.getCD());
                        break;
                    case OPPONENT:
                        opponent_character.attack(player_character);
                        if (player_character.getLife() == 0)
                        {
                            endBattle(false, opponent_character);
                            return;
                        }

                        current_turn = Turn.PLAYER;
                        handler.postDelayed(this, player_character.getCD());
                }
            }
        };
        handler.postDelayed(battle_loop, player_character.getCD());
    }

    private void endBattle(boolean player_win, OpponentCharacter opponent)
    {
        battle_running = false;
        handler.removeCallbacks(battle_loop);

        BattleResultDialog dialog;

        if (player_win)
        {
            Player.gotchaPokemon(opponent.getPokemon());
            int[] rewards = opponent.getRewards(Player.getLevel(), Player.getWIS(), Player.getLUC());
            int exp = rewards[0];
            int coin = rewards[1];
            dialog = new BattleResultDialog(this, true, exp, coin, () -> back(exp, coin));
        }
        else
        {
            dialog = new BattleResultDialog(this, false, 0, 0, this::finish);
        }
        dialog.show();
    }

    private void back(int exp, int coin)
    {
        Intent intent = new Intent();
        intent.putExtra("exp", exp);
        intent.putExtra("coin", coin);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        battle_running = false;
        handler.removeCallbacks(battle_loop);
    }
}
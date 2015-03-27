package ch.zuehlke.arscrabble.jmonkey;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jme3.app.AndroidHarness;
import com.jme3.system.android.AndroidConfigChooser;
import com.qualcomm.vuforia.Vuforia;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ch.zuehlke.arscrabble.R;

public class JMonkeyActivity extends AndroidHarness implements ScrabbleUI {

    private static final String DICTIONARY_FILE_NAME = "Dictionaries/german.dic";

    private TextView playerNameTextView;
    private TextView playerStonesTextView;
    private TextView playerNeedStonesInfo;

    private EditText playerNeedStonesEditText;

    private Button playerNeedStonesSaveButton;
    private Button finishRoundButton;

    public JMonkeyActivity() {
        appClass = "ch.zuehlke.arscrabble.jmonkey.JMonkeyApplication";
        eglConfigType = AndroidConfigChooser.ConfigType.BEST;
        eglConfigVerboseLogging = false;
        screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        mouseEventsInvertX = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Vuforia.setInitParameters(this, Vuforia.GL_20);
        Vuforia.init();

        getJMonkeyApplication().setUI(this);

        LayoutInflater li = LayoutInflater.from(this);
        RelativeLayout layout = (RelativeLayout) li.inflate(R.layout.activity_jmonkey, null);

        addContentView(layout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        playerNameTextView = (TextView) findViewById(R.id.textViewPlayerName);
        playerStonesTextView = (TextView) findViewById(R.id.textViewPlayerStones);

        playerNeedStonesEditText = (EditText) findViewById(R.id.editTextNewStones);
        playerNeedStonesInfo = (TextView) findViewById(R.id.textViewNotEnough);
        playerNeedStonesSaveButton = (Button) findViewById(R.id.setNewStones);

        playerNeedStonesSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getJMonkeyApplication().setNewStones(playerNeedStonesEditText.getText().toString());
                playerNeedStonesEditText.setText("");
                finishRoundButton.setEnabled(true);
            }
        });

        finishRoundButton = (Button) findViewById(R.id.finishRoundButton);
        finishRoundButton.setText("Spielzug abgeschlossen");
        finishRoundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getJMonkeyApplication().roundFinished();
            }
        });

        HashMap<String, String> players = new HashMap<String, String>();
        Intent intent = getIntent();

        addPlayerInfosFromIntent(players, intent, "player1Name", "player1NameStones");
        addPlayerInfosFromIntent(players, intent, "player2Name", "player2NameStones");
        addPlayerInfosFromIntent(players, intent, "player3Name", "player3NameStones");
        addPlayerInfosFromIntent(players, intent, "player4Name", "player4NameStones");


        getJMonkeyApplication().startGame(players, getWordList());
    }


    private void addPlayerInfosFromIntent(HashMap<String, String> players, Intent intent, String nameExtra, String stoneExtra) {
        String playerName = intent.getStringExtra(nameExtra);
        String playerStone = intent.getStringExtra(stoneExtra);

        if (playerName != null && !playerName.equals("") && playerStone != null && !playerStone.equals("")) {
            players.put(playerName, playerStone);
        }
    }

    private JMonkeyApplication getJMonkeyApplication() {
        return (JMonkeyApplication) app;
    }

    @Override
    public void UpdatePlayer(final String name) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                playerNameTextView.setText(name);

            }
        });
    }

    @Override
    public void UpdatePlayerStones(final String stones) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                playerStonesTextView.setText(stones);

            }
        });
    }

    @Override
    public void setPlayerNeedStones(final boolean value) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (value) {
                    playerNeedStonesEditText.setVisibility(View.VISIBLE);
                    playerNeedStonesInfo.setVisibility(View.VISIBLE);
                    playerNeedStonesSaveButton.setVisibility(View.VISIBLE);
                } else {
                    playerNeedStonesEditText.setVisibility(View.GONE);
                    playerNeedStonesInfo.setVisibility(View.GONE);
                    playerNeedStonesSaveButton.setVisibility(View.GONE);
                }

                finishRoundButton.setEnabled(!value);


            }
        });
    }

    private List<String> getWordList() {
        List<String> list = new ArrayList<>();

        list.add("ZUEHLKE");

        return list;
    }
}

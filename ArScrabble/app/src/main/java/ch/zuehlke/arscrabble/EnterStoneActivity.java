package ch.zuehlke.arscrabble;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import ch.zuehlke.arscrabble.jmonkey.JMonkeyActivity;


public class EnterStoneActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_stone);

        Button startGameButton = (Button) findViewById(R.id.startGame);
        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGame();
            }
        });

        updateTextView(R.id.textViewPlayer1, R.id.editTextPlayer1, "player1Name");
        updateTextView(R.id.textViewPlayer2, R.id.editTextPlayer2, "player2Name");
        updateTextView(R.id.textViewPlayer3, R.id.editTextPlayer3, "player3Name");
        updateTextView(R.id.textViewPlayer4, R.id.editTextPlayer4, "player4Name");
    }

    private void updateTextView(int textViewId, int editTextId, String extraName) {

        TextView textView = (TextView) findViewById(textViewId);

        String playerName = getIntent().getStringExtra(extraName);
        if (playerName != null && !playerName.equals("")) {
            textView.setText(playerName);
        } else {
            textView.setVisibility(View.INVISIBLE);
            findViewById(editTextId).setVisibility(View.INVISIBLE);
        }
    }

    private void startGame() {
        Intent intent = new Intent(this, JMonkeyActivity.class);
        intent.putExtras(getIntent().getExtras());

        EditText player1 = (EditText) findViewById(R.id.editTextPlayer1);
        intent.putExtra("player1NameStones", player1.getText().toString());

        EditText player2 = (EditText) findViewById(R.id.editTextPlayer2);
        intent.putExtra("player2NameStones", player2.getText().toString());

        EditText player3 = (EditText) findViewById(R.id.editTextPlayer3);
        intent.putExtra("player3NameStones", player3.getText().toString());

        EditText player4 = (EditText) findViewById(R.id.editTextPlayer4);
        intent.putExtra("player4NameStones", player4.getText().toString());

        startActivity(intent);
    }
}

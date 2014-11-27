package ch.zuehlke.arscrabble;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import ch.zuehlke.arscrabble.jmonkey.JMonkeyActivity;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startButton = (Button)findViewById(R.id.startArButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartARActivity();
            }
        });

        Button startMonkeyButton = (Button)findViewById(R.id.startjMonkeyButton);
        startMonkeyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGame();
            }
        });
    }

    private void startGame() {
        Intent intent = new Intent(this, EnterStoneActivity.class);

        EditText player1 =(EditText)findViewById(R.id.editTextName1);
        intent.putExtra("player1Name", player1.getText().toString());

        EditText player2 =(EditText)findViewById(R.id.editTextName2);
        intent.putExtra("player2Name", player2.getText().toString());

        EditText player3 =(EditText)findViewById(R.id.editTextName3);
        intent.putExtra("player3Name", player3.getText().toString());

        EditText player4 =(EditText)findViewById(R.id.editTextName4);
        intent.putExtra("player4Name", player4.getText().toString());

        startActivity(intent);
    }

    private void StartARActivity(){
        Intent intent = new Intent(this, ImageTargetsActivity.class);
        startActivity(intent);
    }
}

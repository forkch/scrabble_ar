package ch.zuehlke.arscrabble;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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
    }

    private void StartARActivity(){
        Intent intent = new Intent(this, ImageTargetsActivity.class);
        startActivity(intent);
    }
}

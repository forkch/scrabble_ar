package ch.zuehlke.arscrabble.jmonkey;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.jme3.app.AndroidHarness;
import com.jme3.system.android.AndroidConfigChooser;
import com.qualcomm.vuforia.Vuforia;

import ch.zuehlke.arscrabble.R;

public class JMonkeyActivity extends AndroidHarness {

    public JMonkeyActivity() {

        // Set the application class to run
        // appClass = "mygame.Main";
        appClass = "ch.zuehlke.arscrabble.jmonkey.JMonkeyApplication";

        // Try ConfigType.FASTEST; or ConfigType.LEGACY if you have problems
        eglConfigType = AndroidConfigChooser.ConfigType.BEST;

//        // Exit Dialog title & message
//        exitDialogTitle = "Exit?";
//        exitDialogMessage = "Press Yes";
//
        // Enable verbose logging
        eglConfigVerboseLogging = false;

        // Choose screen orientation
        screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        mouseEventsInvertX = true;

        // Invert the MouseEvents Y (default = true)
//        mouseEventsInvertY = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Vuforia.setInitParameters(this, Vuforia.GL_20);
        Vuforia.init();
    }
}

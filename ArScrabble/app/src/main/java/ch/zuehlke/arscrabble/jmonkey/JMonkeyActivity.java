package ch.zuehlke.arscrabble.jmonkey;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.jme3.app.AndroidHarness;
import com.jme3.system.android.AndroidConfigChooser;
import com.qualcomm.vuforia.Vuforia;

public class JMonkeyActivity extends AndroidHarness {

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
    }
}

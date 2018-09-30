package net.excession.mygame;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Jeremiah Biard on 9/30/18.
 */
public class MainActivity extends AppCompatActivity {
    private GameView myGameView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myGameView = new GameView(this);
        setContentView(myGameView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // The following call pauses the rendering thread.
        // If the OpenGL app is memory intensive,
        // we should consider de-allocating objects that
        // consume significant memory here.
        myGameView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // The following call resumes a paused rendering thread.
        // If we de-allocated graphic objects for onPause()
        // this is a good place to re-allocate themm.
        myGameView.onResume();
    }

}

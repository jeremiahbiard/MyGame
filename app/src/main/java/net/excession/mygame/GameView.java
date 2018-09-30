package net.excession.mygame;

import android.content.Context;
import android.opengl.GLSurfaceView;

/**
 * Created by Jeremiah Biard on 9/30/18.
 */
class GameView extends GLSurfaceView {

    private final GameRenderer gameRenderer;
    public GameView(Context context) {
        super(context);

        setEGLContextClientVersion(2);

        gameRenderer = new GameRenderer(context);

        setRenderer(gameRenderer);
    }
}

package net.excession.mygame;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Use getPointerCount() to detect multiple touch events
        // PointerCoords to retrieve pointer objects for detecting multi-touch input
        float x = event.getX();
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (x < getWidth() / 2) {
                    gameRenderer.setHeroMove(gameRenderer.getHeroMove() + .1f);
                }

                if (x > getWidth() / 2) {
                    gameRenderer.setHeroMove(gameRenderer.getHeroMove() - .1f);
                }
        }
        return true;
    }
}

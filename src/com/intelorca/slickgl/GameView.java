package com.intelorca.slickgl;

import java.util.ArrayList;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.InputEvent;
import android.view.MotionEvent;

public class GameView extends GLSurfaceView {
	
	protected static final long FRAME_DELAY = 0;
	private Game mGame;
	private GameRenderer mRenderer; 
	
	private ArrayList<InputEvent> mInputEventQueue = new ArrayList<InputEvent>();
	
	private long mFirstTick = 0;
	private long mSecondWait;
	private long mLastUpdate;
	private long mUpdateCount;
	private long mLastFrame;
	private long mDrawCount;
	private int mUPS, mFPS;
	
	public GameView(Context context) {
		super(context);
		
		mRenderer = new GameRenderer(this, getResources()); 
		setRenderer(mRenderer);
	}

	/**
	 * Called by the renderer when a frame should be drawn. This is also the
	 * update routine as frames are drawn repeatedly.
	 * @param g
	 */
	public void onDrawFrame(GameGraphics g) {
		// Get the game start tick count
		if (mFirstTick == 0)
			mFirstTick = System.currentTimeMillis();
		
		// Calculate what frame we 'should' be on
		long frame = (System.currentTimeMillis() - mFirstTick) * 60 / 1000;
		
		// Allow thread to be idle if we are over performing
		long sleepTime = Math.min(mLastUpdate - frame, mLastFrame - frame);
		if (sleepTime > 1) {
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) { }
		}
		
		// Keep updating until we have caught up with the number of updates we should of done
		while (frame > mLastUpdate) { 
			if (mGame != null)
				mGame.update();
			mUpdateCount++;
			mLastUpdate++;
			break;
		}
		
		// If we have enough time, draw the frame
		// if (frame > mLastFrame) { 
			if (mGame != null)
				mGame.draw(g);
			mDrawCount++;
			mLastFrame = frame;
		// }
		
		// Process input (we want to process on this thread!)
		processInput();
		
		// Refresh the window title displaying fps and ups
		if (System.currentTimeMillis() - mSecondWait > 1000) {
			mUPS = (int)mUpdateCount;
			mFPS = (int)mDrawCount;
			
			mSecondWait = System.currentTimeMillis();
			mDrawCount = 0;
			mUpdateCount = 0;
		}
	}
	
	private void processInput() {
		synchronized (mInputEventQueue) {
			for (InputEvent event : mInputEventQueue) {
				if (event instanceof MotionEvent)
					mGame.onTouchEvent((MotionEvent)event);
			}
			mInputEventQueue.clear();
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		synchronized (mInputEventQueue) {
			int[] location = new int[2];
			getLocationInWindow(location);
			event.offsetLocation(-location[0], -location[1]);
			mInputEventQueue.add(event);
		}
		return true;
	}
	
	
	public void setGame(Game game) {
		mGame = game;
	}
	
	public int getUPS() {
		return mUPS;
	}

	public int getFPS() {
		return mFPS;
	}
}

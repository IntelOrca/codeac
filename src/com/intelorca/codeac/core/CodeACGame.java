package com.intelorca.codeac.core;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;

import com.intelorca.codeac.R;
import com.intelorca.codeac.host.MainActivity;
import com.intelorca.slickgl.Game;
import com.intelorca.slickgl.GameGraphics;
import com.intelorca.slickgl.GameGraphics2D.BLENDING_MODE;
import com.intelorca.slickgl.GameGraphics2D.DrawOperation;

public class CodeACGame extends Game {
	public enum State {
		Normal,
		PlacingSymbol,
	}
	
	private static final String TAG = "CodeACGame";
	
	/** Singleton */
	public static CodeACGame Instance;
	
	private MainActivity mHostActivity;	
	private int mInitialisationState;
	private int mLastCanvasWidth, mLastCanvasHeight;
	private int mCanvasWidth, mCanvasHeight;
	private float mDensity;
	
	private Grid mGrid;
	private SymbolRepository mSymbolRepository;
	private Symbol mGrabSymbol;
	
	private State mState = State.Normal;
	
	public CodeACGame(MainActivity hostActivity) {
		// Set the singleton
		// if (Instance != null) Log.wtf(TAG, "Instance != null");
		Instance = this;
		
		mHostActivity = hostActivity;
		mDensity = mHostActivity.getResources().getDisplayMetrics().density;
	}
	
	private void updateInit() {
		mSymbolRepository = new SymbolRepository(2);
		mGrid = new Grid();
		
		Symbol symbol = new Symbol();
		symbol.setRandom(2, 1);
		mGrid.getCell(2, 3).setSymbol(symbol);
	}
	
	private void drawInit(GameGraphics g) {
		// Load textures
		g.renderer.loadTextures(new int[] { R.drawable.ic_launcher, R.drawable.symbols } );
	}
	
	@Override
	public void update() {
		// Check if the game has been initialised
		if ((mInitialisationState & 1) == 0) {
			mInitialisationState |= 1;
			updateInit();
		}
		
		// Update components
		mSymbolRepository.update();
		mGrid.update();
	}

	@Override
	public void draw(GameGraphics g) {
		// Check if there the game has been initialised
		if ((mInitialisationState & 1) == 0)
			return;
		
		// Check if the drawing has been initialised
		if ((mInitialisationState & 2) == 0) {
			mInitialisationState |= 2;
			drawInit(g);
		}
		
		// Check if the canvas size changed
		mCanvasWidth = g.renderer.getWidth();
		mCanvasHeight = g.renderer.getHeight();
		if (mCanvasWidth != mLastCanvasWidth || mCanvasHeight != mLastCanvasHeight) {
			mLastCanvasWidth = mCanvasWidth;
			mLastCanvasHeight = mCanvasHeight;
			onCanvasSizeChanged();
		}
		
		g.clear(Color.BLACK);
		g.gl2d.setupView();
		g.gl2d.beginSpriteBatch();
		
		// Draw components
		mSymbolRepository.draw(g);
		mGrid.draw(g);
		
		/*
		DrawOperation dop = new DrawOperation(R.drawable.symbols, new Rect(0, 0, 128, 128), new RectF(0, 0, 128, 128));
		dop.blendingMode = BLENDING_MODE.ALPHA;
		dop.colour = Color.RED;
		dop.centreX += 20;
		dop.z = 10;
		g.gl2d.addToBatch(dop);
		dop.colour = Color.YELLOW;
		dop.centreX += 20;
		dop.z = 20;
		g.gl2d.addToBatch(dop);
		dop.colour = Color.GREEN;
		dop.centreX += 20;
		dop.z = 30;
		g.gl2d.addToBatch(dop);
		dop.colour = Color.BLUE;
		dop.centreX += 20;
		dop.z = 40;
		g.gl2d.addToBatch(dop);
		
		dop.centreX = 64;
		dop.colour = Color.YELLOW;
		dop.centreY += 20;
		dop.z = 5;
		g.gl2d.addToBatch(dop);
		dop.colour = Color.GREEN;
		dop.centreY += 20;
		dop.z = 3;
		g.gl2d.addToBatch(dop);
		dop.colour = Color.BLUE;
		dop.centreY += 20;
		dop.z = 1;
		g.gl2d.addToBatch(dop);
		*/
		
		g.gl2d.endSpriteBatch();
	}
	
	private void onCanvasSizeChanged() {
		float symbolSize;
		
		symbolSize = (mCanvasHeight - 8.0f) / 9.0f; 
		
		// Calculate a suitable position for the symbol repository
		mSymbolRepository.setLocation(new Location(
				10.0f + (symbolSize / 2.0f), 10.0f + (symbolSize * mSymbolRepository.getMaxSymbols() / 2.0f), 16,
				symbolSize, symbolSize * mSymbolRepository.getMaxSymbols()));
		
		// Calculate a suitable position for the grid
		mGrid.setLocation(new Location(
				mCanvasWidth / 2.0f, mCanvasHeight / 2.0f, 32,
				symbolSize * 9, symbolSize * 9));
	}
	
	@Override
	public void onTouchEvent(MotionEvent event) {
		float x, y;
		
		// Log.w(TAG, Integer.toString(event.getActionMasked()));

		x = event.getX();
		y = event.getY();
		
		if (mGrid.getLocation().getBounds().contains(x, y)) {
			mGrid.onTouchEvent(event);
		} else {
			mGrid.highlightCell(-1, -1);
		}
		
		switch (mState) {
		case Normal:
			if (mSymbolRepository.getLocation().getBounds().contains(x, y))
				mSymbolRepository.onTouchEvent(event);
			break;
		case PlacingSymbol:
			if (mGrabSymbol == null) {
				mState = State.Normal;
				break;
			} else {
				if (event.getActionMasked() == MotionEvent.ACTION_UP || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
					mGrabSymbol.release();
					mState = State.Normal;
				} else {
					mGrabSymbol.grabMove(x, y);
				}
			}
			break;
		}
	}
	
	public void grabSymbol(Symbol symbol, float x, float y) {
		mGrabSymbol = symbol;
		mGrabSymbol.grab(x, y);
		mState = State.PlacingSymbol;
	}
	
	public void ungrabSymbol() {
		mGrabSymbol = null;
		mState = State.Normal;
	}
	
	public Symbol getGrabSymbol() {
		Symbol symbol = mGrabSymbol;
		return symbol;
	}
	
	public float getDensity() {
		return mDensity;
	}
	
	public State getState() {
		return mState;
	}
	
	public void setState(State value) {
		mState = value;
	}
}

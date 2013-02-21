package com.intelorca.codeac.core;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import com.intelorca.codeac.R;
import com.intelorca.codeac.host.MainActivity;
import com.intelorca.slickgl.Game;
import com.intelorca.slickgl.GameGraphics;
import com.intelorca.slickgl.GameGraphics2D.DrawOperation;

public class CodeACGame extends Game {
	private static final String TAG = "CodeACGame";
	
	/** Singleton */
	public static CodeACGame Instance;
	
	private MainActivity mHostActivity;	
	private int mInitialisationState;
	private int mLastCanvasWidth, mLastCanvasHeight;
	private int mCanvasWidth, mCanvasHeight;
	
	private Grid mGrid;
	private SymbolRepository mSymbolRepository;
	
	private float mDensity;
	
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
		
		g.clear(Color.RED);
		g.gl2d.setupView();
		g.gl2d.beginSpriteBatch();
		
		// Draw components
		mSymbolRepository.draw(g);
		mGrid.draw(g);

		g.gl2d.endSpriteBatch();
	}
	
	private void onCanvasSizeChanged() {
		RectF rect;
		float size;
		float symbolSize;
		
		symbolSize = (mCanvasHeight - 8.0f) / 9.0f; 
		
		// Calculate a suitable position for the symbol repository
		mSymbolRepository.setBounds(new RectF(10.0f, 10.0f,
				10.0f + symbolSize, 10.0f + (symbolSize * mSymbolRepository.getMaxSymbols())));
		
		// Calculate a suitable position for the grid
		size = symbolSize * 9.0f;
		rect = new RectF();
		rect.left = (mCanvasWidth - size) / 2.0f;
		rect.right = rect.left + size;
		rect.top = (mCanvasHeight - size) / 2.0f;
		rect.bottom = rect.top + size;
		mGrid.setBounds(rect);
	}
	
	public float getDensity() {
		return mDensity;
	}
}

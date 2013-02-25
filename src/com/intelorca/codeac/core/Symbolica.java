package com.intelorca.codeac.core;

import android.graphics.Color;
import android.view.MotionEvent;

import com.intelorca.codeac.R;
import com.intelorca.codeac.core.GameStateManager.State;
import com.intelorca.codeac.host.MainActivity;
import com.intelorca.slickgl.Game;
import com.intelorca.slickgl.GameGraphics;

public class Symbolica extends Game {
	private static final String TAG = "Symbolica";
	
	private MainActivity mHostActivity;	
	private int mInitialisationState;
	private int mLastCanvasWidth, mLastCanvasHeight;
	private int mCanvasWidth, mCanvasHeight;
	private float mDensity;
	
	// Abstract components
	private final GameStateManager mGameStateManager = new GameStateManager(this);
	private final SymbolController mSymbolController = new SymbolController(this);
	
	// Physical components
	private SymbolRepository mSymbolRepository;
	private Grid mGrid;
	
	public Symbolica(MainActivity hostActivity) {
		mHostActivity = hostActivity;
		mDensity = mHostActivity.getResources().getDisplayMetrics().density;
	}
	
	private void updateInit() {
		mSymbolRepository = new SymbolRepository(this, 2);
		mGrid = new Grid(this, 9, 7);
		
		Symbol symbol = new Symbol(this);
		symbol.setShape(Symbol.SPECIAL_WILD);
		mGrid.getCell(mGrid.getColumns() / 2, mGrid.getRows() / 2).setSymbol(symbol);
	}
	
	private void drawInit(GameGraphics g) {
		// Load textures
		g.renderer.loadTextures(new int[] {
				R.drawable.star,
				R.drawable.symbols,
		} );
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

		g.gl2d.endSpriteBatch();
	}
	
	private void onCanvasSizeChanged() {
		float symbolSize;
		
		// Calculate the optimal symbol size based on grid and canvas size
		float maxSymbolWidth = (float)mCanvasWidth / (mGrid.getColumns() + 3);
		float maxSymbolHeight = (mCanvasHeight - 2.0f) / mGrid.getRows();
		symbolSize = Math.min(maxSymbolWidth, maxSymbolHeight);
		
		// Calculate a suitable position for the symbol repository
		mSymbolRepository.setLocation(new Location(
				10.0f + (symbolSize / 2.0f), 10.0f + (symbolSize * mSymbolRepository.getMaxSymbols() / 2.0f), 16,
				symbolSize, symbolSize * mSymbolRepository.getMaxSymbols()));
		
		// Calculate a suitable position for the grid
		mGrid.setLocation(new Location(
				mCanvasWidth / 2.0f, mCanvasHeight / 2.0f, 32,
				symbolSize * mGrid.getColumns(), symbolSize * mGrid.getRows()));
	}
	
	@Override
	public void onTouchEvent(MotionEvent event) {
		// Are we moving a symbol 
		if (mGameStateManager.getState() == State.DRAGGING_SYMBOL) {
			if (event.getActionMasked() == MotionEvent.ACTION_UP)
				mSymbolController.drop(event.getX(), event.getY());
			else
				mSymbolController.move(event.getX(), event.getY());
		}

		// Propagate through sub components
		if (mSymbolRepository.getLocation().contains(event.getX(), event.getY()))
			mSymbolRepository.onTouchEvent(event);
	}
	
	public float getDensity() {
		return mDensity;
	}
		
	public MainActivity getHostActivity() {
		return mHostActivity;
	}
	
	public GameStateManager getStateManager() {
		return mGameStateManager;
	}
	
	public SymbolController getSymbolController() {
		return mSymbolController;
	}
	
	public Grid getGrid() {
		return mGrid;
	}
}

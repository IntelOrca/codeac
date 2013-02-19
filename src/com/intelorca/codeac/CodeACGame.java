package com.intelorca.codeac;

import android.graphics.Color;
import android.graphics.Rect;

import com.intelorca.slickgl.Game;
import com.intelorca.slickgl.GameGraphics;
import com.intelorca.slickgl.GameGraphics2D.DrawOperation;

public class CodeACGame extends Game {
	private MainActivity mHostActivity;	
	private int mInitialisationState;
	private int mCanvasWidth, mCanvasHeight;
	
	private float mDensity;
	
	public CodeACGame(MainActivity hostActivity) {
		mHostActivity = hostActivity;
		mDensity = mHostActivity.getResources().getDisplayMetrics().density;
	}
	
	private void updateInit() {
		
	}
	
	private void drawInit(GameGraphics g) {
		g.renderer.loadTextures(new int[] { R.drawable.ic_launcher, R.drawable.symbols } );
	}
	
	@Override
	public void update() {
		if ((mInitialisationState & 1) == 0) {
			mInitialisationState |= 1;
			updateInit();
		}
	}

	@Override
	public void draw(GameGraphics g) {
		if ((mInitialisationState & 2) == 0) {
			mInitialisationState |= 2;
			drawInit(g);
		}
		
		mCanvasWidth = g.renderer.getWidth();
		mCanvasHeight = g.renderer.getHeight();
		
		g.clear(Color.RED);
		g.gl2d.setupView();
		g.gl2d.beginSpriteBatch();
		
		drawGrid(g);

		g.gl2d.endSpriteBatch();
	}
	
	private void drawGrid(GameGraphics g) {
		int[] shape_colours = new int[] { Color.BLUE, Color.RED, Color.YELLOW, Color.GREEN, Color.WHITE, Color.MAGENTA };
		
		float size = (Math.min(mCanvasWidth, mCanvasHeight) - 8.0f) / 9.0f;
		
		DrawOperation drawop = new DrawOperation();
		drawop.width = size;
		drawop.height = size;
		drawop.src = new Rect(0, 0, (int)(128 * mDensity), (int)(128 * mDensity));
		
		float startX = (mCanvasWidth - (drawop.width * 9.0f)) / 2.0f;
		float startY = (mCanvasHeight - (drawop.height * 9.0f)) / 2.0f;
		
		for (int y = 0; y < 9; y++) {
			for (int x = 0; x < 9; x++) {
				drawop.centreX = startX + (x * drawop.width) + (drawop.width / 2);
				drawop.centreY = startY + (y * drawop.height) + (drawop.height / 2);
				
				drawop.z = 8;
				drawop.bitmapID = 0;
				drawop.colour = Color.GRAY;
				g.gl2d.addToBatch(drawop);
				
				drawop.z = 4;
				drawop.colour = shape_colours[(x * y ^ x ^ y) % shape_colours.length];
				drawop.bitmapID = R.drawable.symbols;
				g.gl2d.addToBatch(drawop);
			}
		}
	}
}

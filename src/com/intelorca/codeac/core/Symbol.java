package com.intelorca.codeac.core;

import java.util.Random;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;

import com.intelorca.codeac.R;
import com.intelorca.slickgl.GameGraphics;
import com.intelorca.slickgl.GameGraphics2D.DrawOperation;

class Symbol {
	public enum State {
		NULL,
		PLACABLE,
		PLACED,
		DESTROYING,
	}
	
	public static final int[] COLOURS = new int[] {
		Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.WHITE, Color.LTGRAY
	};
	public static final int MAX_SHAPES = 8;
	
	/** The maximum number of colours for a level. */
	public static final int[] LEVEL_MAX_COLOURS = new int[] {
		3, 3, 4, 4, 4, 5, 5, 5, 5, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 8
	};
	
	/** The maximum number of shapes for a level. */
	public static final int[] LEVEL_MAX_SHAPES = new int[] {
		3, 4, 4, 4, 5, 5, 6, 6, 6, 7, 7, 7, 8, 8, 8, 8
	};
	
	private static final Random mRandom = new Random();
	private RectF mBounds;
	private int mZ = 8;
	private int mColour;
	private int mShape;
	
	private State mState;
	
	public Symbol() {
	}
	
	public Symbol(int difficulty, int level) {
		int maxColour, maxShape;
		
		level += difficulty;
		
		maxColour = LEVEL_MAX_COLOURS[Math.min(level, LEVEL_MAX_COLOURS.length)];
		maxShape = LEVEL_MAX_SHAPES[Math.min(level, LEVEL_MAX_SHAPES.length)];
		setRandom(maxColour, maxShape);
	}
	
	public void setRandom(int maxColour, int maxShape) {
		mColour = mRandom.nextInt(maxColour);
		mShape =  mRandom.nextInt(maxShape);
	}
	
	public void translate(float x, float y) {
		mBounds.offset(x, y);
	}
	
	public void setSize(float width, float height) {
		if (mBounds.width() == width && mBounds.height() == height)
			return;
		
		float cx = mBounds.centerX();
		float cy = mBounds.centerX();
		mBounds.left = cx - (width / 2.0f);
		mBounds.top = cy - (height / 2.0f);
		mBounds.right = mBounds.left + width;
		mBounds.bottom = mBounds.bottom + height;
	}
	
	public void update() {
		
	}
	
	public void draw(GameGraphics g) {
		float density = CodeACGame.Instance.getDensity();
		
		// Calculate source rectangle
		int shapeWidth = (int)(128.0f * density);
		int shapeHeight = (int)(128.0f * density);
		Rect srcRect = new Rect(mShape * shapeWidth, 0, (mShape + 1) * shapeWidth, shapeHeight);
		
		// Create the draw operation
		DrawOperation drawOp = new DrawOperation(R.drawable.symbols, srcRect, mBounds);
		drawOp.colour = COLOURS[mColour];
		drawOp.z = mZ;
		
		// Add to the batch
		g.gl2d.addToBatch(drawOp);
	}
	
	public RectF getBounds() {
		return mBounds;
	}
	
	public void setBounds(RectF value) {
		mBounds = value;
	}
	
	public void setZ(int value) {
		mZ = value;
	}
	
	public int getColour() {
		return mColour;
	}

	public void setColour(int value) {
		mColour = value;
	}

	public int getShape() {
		return mShape;
	}

	public void setShape(int value) {
		mShape = value;
	}
	
	public State getState() {
		return mState;
	}
	
	public void setState(State value) {
		mState = value;
	}
}

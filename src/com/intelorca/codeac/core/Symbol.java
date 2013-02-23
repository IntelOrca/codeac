package com.intelorca.codeac.core;

import java.util.Random;

import android.graphics.Color;
import android.graphics.Rect;

import com.intelorca.codeac.R;
import com.intelorca.slickgl.GameGraphics;
import com.intelorca.slickgl.GameGraphics2D.BLENDING_MODE;
import com.intelorca.slickgl.GameGraphics2D.DrawOperation;

class Symbol {
	public enum State {
		NORMAL,
		GRABBED,
		RESTORING_LOCATION,
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
	private Location mLocation;
	private int mColour;
	private int mShape;
	
	private State mState;
	private float mGrabDX, mGrabDY;
	private int mRestoreLocationTime;
	
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
		mLocation.offset(x, y);
	}
	
	public void setSize(float width, float height) {
		mLocation.width = width;
		mLocation.height = height;
	}
	
	public void grab(float x, float y) {
		mLocation.z = 4;
		mGrabDX = x - mLocation.cx;
		mGrabDY = y - mLocation.cy;
		mState = State.GRABBED;
	}
	
	public void grabMove(float x, float y) {
		mLocation.cx = x + mGrabDX;
		mLocation.cy = y + mGrabDY;
	}
	
	public void release() {
		mState = State.NORMAL;
	}
	
	public void update() {
		mRestoreLocationTime++;
	}
	
	public void draw(GameGraphics g) {
		float density = CodeACGame.Instance.getDensity();
		
		// Calculate source rectangle
		int shapeWidth = (int)(128.0f * density);
		int shapeHeight = (int)(128.0f * density);
		Rect srcRect = new Rect(mShape * shapeWidth, 0, (mShape + 1) * shapeWidth, shapeHeight);
		
		// Create the draw operation
		DrawOperation drawOp = new DrawOperation(R.drawable.symbols, srcRect, mLocation.getBounds());
		drawOp.blendingMode = BLENDING_MODE.ALPHA;
		drawOp.colour = COLOURS[mColour];
		drawOp.z = mLocation.z;
		
		// Add to the batch
		g.gl2d.addToBatch(drawOp);
	}
	
	public Location getLocation() {
		return mLocation;
	}
	
	public void setLocation(Location value) {
		mLocation = value;
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
	
	public int getRestoreLocationTime() {
		return mRestoreLocationTime;
	}

	public void setRestoreLocationTime(int value) {
		mRestoreLocationTime = value;
	}
}

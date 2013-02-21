package com.intelorca.codeac.core;

import com.intelorca.slickgl.GameGraphics;
import com.intelorca.slickgl.GameGraphics2D.DrawOperation;

import android.graphics.Color;
import android.graphics.RectF;

class GridCell {
	public static final int Z = 64;
	
	private RectF mBounds;
	private boolean mCompleted;
	private Symbol mSymbol;
	
	public GridCell() {
		
	}
	
	public void update() {
		// Update the symbol
		if (mSymbol != null)
			mSymbol.update();
	}
	
	public void draw(GameGraphics g) {
		// Draw the cell background
		DrawOperation drawOp = new DrawOperation(mBounds);
		drawOp.colour = (mCompleted ? Color.YELLOW : Color.GRAY);
		drawOp.z = Z;
		g.gl2d.addToBatch(drawOp);
		
		// Draw the symbol
		if (mSymbol != null)
			mSymbol.draw(g);
	}
	
	public void setBounds(RectF value) {
		mBounds = value;
		if (mSymbol != null)
			mSymbol.setBounds(mBounds);
	}
	
	public void setSymbol(Symbol value) {
		mCompleted = true;
		mSymbol = value;
		
		mSymbol.setBounds(mBounds);
		mSymbol.setZ(16);
	}
}

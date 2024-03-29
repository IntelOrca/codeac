package com.intelorca.codeac.core;

import android.graphics.Color;

import com.intelorca.codeac.core.Symbol.State;
import com.intelorca.slickgl.GameGraphics;
import com.intelorca.slickgl.GameGraphics2D.DrawOperation;

class GridCell {
	private Location mLocation;
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
		DrawOperation drawOp = new DrawOperation(mLocation.getBounds());
		drawOp.colour = (mCompleted ? Color.YELLOW : Color.GRAY);
		drawOp.z = mLocation.z;
		g.gl2d.addToBatch(drawOp);
		
		// Draw the symbol
		if (mSymbol != null)
			mSymbol.draw(g);
	}
	
	public void setLocation(Location value) {
		mLocation = value;
		if (mSymbol != null)
			setSymbolLocation();
	}
	
	public Symbol getSymbol() {
		return mSymbol;
	}
	
	public void setSymbol(Symbol value) {
		if (value == null) {
			mSymbol = null;
		} else {		
			mCompleted = true;
			mSymbol = value;
			mSymbol.setState(State.PLACED);
			
			setSymbolLocation();
		}
	}
	
	private void setSymbolLocation() {
		if (mLocation == null)
			return;
		
		Location symbolLocation = (Location)mLocation.clone();
		symbolLocation.z--;
		mSymbol.setLocation(symbolLocation);
	}
}

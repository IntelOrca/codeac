package com.intelorca.codeac.core;

import android.graphics.Color;
import android.view.MotionEvent;

import com.intelorca.codeac.core.Symbol.State;
import com.intelorca.slickgl.GameGraphics;
import com.intelorca.slickgl.GameGraphics2D.DrawOperation;

class SymbolRepository {
	private Location mLocation;
	private float mSymbolWidth, mSymbolHeight;
	private int mMaxSymbols;
	private Symbol[] mSymbols;
	
	public SymbolRepository(int maxSymbols) {
		mMaxSymbols = maxSymbols;
		mSymbols = new Symbol[mMaxSymbols];
	}
	
	private Symbol getNewSymbol(int c) {
		Symbol symbol = new Symbol();
		symbol.setRandom(2, 1);
		symbol.setState(State.NORMAL);
		
		// Set symbol bounds
		symbol.setLocation(new Location(
				mLocation.cx, 0.0f - ((c + 1) * 2 * mSymbolHeight), 8,
				mSymbolWidth, mSymbolHeight));
		
		return symbol;
	}
	
	private void updateSymbolPhysics() {
		// First order the repository (bubble)
		Symbol temp;
		boolean sorted;
		do {
			sorted = true;
			for (int i = 0; i < mMaxSymbols - 1; i++) {
				if (mSymbols[i] == null || mSymbols[i + 1] == null)
					continue;
				
				// Calculate the x, y and component distance away
				float dx = getCellX(i) - mSymbols[i].getLocation().cx;
				float dy = getCellY(i) - mSymbols[i].getLocation().cy;
				float dc = (float)Math.sqrt((dx * dx) + (dy * dy));
				
				if ((mSymbols[i].getState() == State.GRABBED && dc > mSymbolWidth * 2) && mSymbols[i + 1].getState() == State.NORMAL) {
					temp = mSymbols[i];
					mSymbols[i] = mSymbols[i + 1];
					mSymbols[i + 1] = temp;
					sorted = false;
				}
			}
		} while (!sorted);
		
		// Move symbols to their targets
		for (int i = 0; i < mMaxSymbols; i++) {
			if (mSymbols[i] == null)
				continue;
			
			if (mSymbols[i].getState() == State.NORMAL || mSymbols[i].getState() == State.RESTORING_LOCATION)
				moveSymbolToTarget(mSymbols[i], getCellX(i), getCellY(i));
		}
	}
	
	private float getCellX(int i) {
		return mLocation.cx;
	}
	
	private float getCellY(int i) {
		return mLocation.getBounds().bottom - (i * mSymbolHeight) - (mSymbolHeight / 2.0f);
	}
	
	private void moveSymbolToTarget(Symbol symbol, float x, float y) {
		// Get symbol location
		Location symbolLocation = symbol.getLocation();
		
		// Calculate the x, y and component distance away
		float dx = x - symbolLocation.cx;
		float dy = y - symbolLocation.cy;
		float dc = (float)Math.sqrt((dx * dx) + (dy * dy));
		
		// First check if the symbol is already at its location or close enough
		if (dc < 4.0f) {
			// Set location to destination location
			symbolLocation.cx = x;
			symbolLocation.cy = y;
			symbolLocation.z = mLocation.z - 1;
			symbol.setState(State.NORMAL);
		} else {
			// Reset time from restore point to 0 if restore hasn't started yet
			if (symbol.getState() == State.NORMAL) {
				symbol.setState(State.RESTORING_LOCATION);
				symbol.setRestoreLocationTime(0);
			}
			
			// Calculate the distance that should be travelled in this tick
			int timeRemaining = (60 / 2) - symbol.getRestoreLocationTime();
			float speed = dc / timeRemaining; 
			
			// Translate by that distance in a direct direction
			double angle = Math.atan2(dy, dx);
			dx = (float)(Math.cos(angle) * speed);
			dy = (float)(Math.sin(angle) * speed);
			symbol.translate(dx, dy);
		}
	}
	
	public void update() {
		if (mLocation == null)
			return;
		
		updateSymbolPhysics();
		
		// Add any new symbols to fill up the stack
		int newSymbolCount = 0;
		for (int i = 0; i < mMaxSymbols; i++) {
			if (mSymbols[i] == null) {
				mSymbols[i] = getNewSymbol(newSymbolCount);
				newSymbolCount++;
			}
		}
		
		// Update the symbols
		for (Symbol symbol : mSymbols)
			if (symbol != null)
				symbol.update();
	}
	
	public void draw(GameGraphics g) {
		DrawOperation drawOp = new DrawOperation(mLocation.getBounds());
		drawOp.colour = Color.GRAY;
		drawOp.z = mLocation.z;
		g.gl2d.addToBatch(drawOp);
		
		// Draw the symbols
		for (Symbol symbol : mSymbols)
			if (symbol != null)
				symbol.draw(g);
	}
	
	public Location getLocation() {
		return mLocation;
	}
	
	public void setLocation(Location value) {
		mLocation = value;
		mSymbolWidth = mLocation.width;
		mSymbolHeight = mLocation.height / mMaxSymbols;
		
		// Update symbol sizes
		for (Symbol symbol : mSymbols)
			if (symbol != null)
				symbol.setSize(mSymbolWidth, mSymbolHeight);
	}
	
	public int getMaxSymbols() {
		return mMaxSymbols;
	}
	
	public void onTouchEvent(MotionEvent event) {
		float x, y;
		
		x = event.getX();
		y = event.getY();
		
		for (Symbol symbol : mSymbols) {
			if (symbol == null)
				continue;
			if (symbol.getState() == State.NORMAL && symbol.getLocation().getBounds().contains(x, y)) {
				CodeACGame.Instance.grabSymbol(symbol, x, y);
				break;
			}
		}
	}
}

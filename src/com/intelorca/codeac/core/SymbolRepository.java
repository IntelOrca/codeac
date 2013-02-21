package com.intelorca.codeac.core;

import android.graphics.Color;
import android.graphics.RectF;

import com.intelorca.codeac.core.Symbol.State;
import com.intelorca.slickgl.GameGraphics;
import com.intelorca.slickgl.GameGraphics2D.DrawOperation;

class SymbolRepository {
	private static final int Z = 64;
	
	private RectF mBounds;
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
		symbol.setState(State.NULL);
		
		// Set symbol bounds
		RectF bounds = new RectF(mBounds.left, mBounds.top - (((c * 2) + 1) * mSymbolHeight), 0, 0);
		bounds.right = bounds.left + mSymbolWidth;
		bounds.bottom = bounds.top + mSymbolHeight;
		symbol.setBounds(bounds);
		
		return symbol;
	}
	
	private void updateSymbolPhysics() {
		// First order the repository (bubble)
		Symbol temp;
		boolean sorted;
		do {
			sorted = true;
			for (int i = 0; i < mMaxSymbols - 1; i++) {
				if (mSymbols[i] == null)
					continue;
				
				if (mSymbols[i].getState() != State.NULL) {
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
			
			if (mSymbols[i].getState() == State.NULL) {
				moveSymbolToTarget(mSymbols[i], mBounds.centerX(),
						mBounds.bottom - (i * mSymbolHeight) - (mSymbolHeight / 2.0f));
			}
		}
	}
	
	private void moveSymbolToTarget(Symbol symbol, float x, float y) {
		final float maxDelta = 8.0f;
		
		RectF symbolBounds = symbol.getBounds();
		float dx = x - symbolBounds.centerX();
		float dy = y - symbolBounds.centerY();
		dx = Math.signum(dx) * Math.min(Math.abs(dx), maxDelta);
		dy = Math.signum(dy) * Math.min(Math.abs(dy), maxDelta);
		symbol.translate(dx, dy);
	}
	
	public void update() {
		if (mBounds == null)
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
		DrawOperation drawOp = new DrawOperation(mBounds);
		drawOp.colour = Color.GRAY;
		drawOp.z = Z;
		g.gl2d.addToBatch(drawOp);
		
		// Draw the symbols
		for (Symbol symbol : mSymbols)
			if (symbol != null)
				symbol.draw(g);
	}
	
	public void setBounds(RectF value) {
		mBounds = value;
		mSymbolWidth = mBounds.width();
		mSymbolHeight = mBounds.height() / mMaxSymbols;
		
		// Update symbol sizes
		for (Symbol symbol : mSymbols)
			if (symbol != null)
				symbol.setSize(mSymbolWidth, mSymbolHeight);
	}
	
	public int getMaxSymbols() {
		return mMaxSymbols;
	}
}

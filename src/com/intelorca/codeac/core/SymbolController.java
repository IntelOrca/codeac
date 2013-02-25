package com.intelorca.codeac.core;

import android.content.Context;
import android.os.Vibrator;

import com.intelorca.codeac.core.GameStateManager.State;

class SymbolController {
	private final Symbolica mGame;
	
	private Symbol mSymbol;
	private float mRestoreX, mRestoreY;
	
	public SymbolController(Symbolica game) {
		mGame = game;
	}
	
	public void pickup(Symbol symbol, float x, float y) {
		mSymbol = symbol;
		mSymbol.grab(x, y);
		
		mGame.getStateManager().setState(State.DRAGGING_SYMBOL);
	}
	
	public void move(float x, float y) {
		mSymbol.grabMove(x, y);
	}
	
	public void drop(float x, float y) {
		if (mGame.getGrid().canPlaceAt(mSymbol, x, y)) {
			mGame.getGrid().placeAt(mSymbol, x, y);
			
			// Vibrate the device
			Vibrator vibrator = (Vibrator)mGame.getHostActivity().getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(100);
		} else {
			mSymbol.setState(Symbol.State.NORMAL);
		}
		
		mSymbol = null;
		mGame.getStateManager().setState(State.IDLE);
	}
	
	public void finished() {
		mSymbol = null;
	}
	
	public void setRestoreLocation(float x, float y) {
		mRestoreX = x;
		mRestoreY = y;
	}
	
	public Symbol getSymbol() {
		return mSymbol;
	}
}

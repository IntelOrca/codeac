package com.intelorca.codeac.core;

class GameStateManager {
	public enum State {
		IDLE,
		DRAGGING_SYMBOL,
	}
	
	private final Symbolica mGame;
	private State mState = State.IDLE;
	
	public GameStateManager(Symbolica game) {
		mGame = game;
	}
	
	public void setState(State value) {
		mState = value;
	}
	
	public State getState() {
		return mState;
	}
}

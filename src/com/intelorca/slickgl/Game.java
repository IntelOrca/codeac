package com.intelorca.slickgl;

import android.view.MotionEvent;

public abstract class Game {
	
	public abstract void update();
	public abstract void draw(GameGraphics g);
	
	public void onTouchEvent(MotionEvent event) { }
}

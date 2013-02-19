package com.intelorca.codeac;

import com.intelorca.slickgl.GameView;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

public class MainActivity extends Activity {
	private CodeACGame mGame;
	private GameView mGameView;
	
	public MainActivity() {
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mGameView = new GameView(this);
		setContentView(mGameView);
		
		mGame = new CodeACGame(this);
		mGameView.setGame(mGame);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
}

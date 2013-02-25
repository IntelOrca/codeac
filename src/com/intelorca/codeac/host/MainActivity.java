package com.intelorca.codeac.host;

import com.intelorca.codeac.core.Symbolica;
import com.intelorca.slickgl.GameView;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

public class MainActivity extends Activity {
	private Symbolica mGame;
	private GameView mGameView;
	
	public MainActivity() {
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mGameView = new GameView(this);
		setContentView(mGameView);
		
		mGame = new Symbolica(this);
		mGameView.setGame(mGame);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
}

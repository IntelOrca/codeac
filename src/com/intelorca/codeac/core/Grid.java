package com.intelorca.codeac.core;

import android.graphics.Color;
import android.view.MotionEvent;

import com.intelorca.codeac.core.Symbol.State;
import com.intelorca.slickgl.GameGraphics;
import com.intelorca.slickgl.GameGraphics2D.DrawOperation;

class Grid {
	private Location mLocation;
	private int mColumns = 9;
	private int mRows = 9;
	private GridCell[] mCells;
	
	private int mHighlightCellX = -1, mHighlightCellY = -1;
	
	public Grid() {
		mCells = new GridCell[mRows * mColumns];
		for (int y = 0; y < mRows; y++)
			for (int x = 0; x < mColumns; x++)
				mCells[y * mColumns + x] = new GridCell();
	}
	
	public void update() {
		for (GridCell cell : mCells)
			cell.update();
	}
	
	public void draw(GameGraphics g) {
		for (GridCell cell : mCells)
			cell.draw(g);
		
		if (mHighlightCellX != -1)
			drawHighlightedCell(g);
	}
	
	private void drawHighlightedCell(GameGraphics g) {
		float cellWidth = mLocation.width / mColumns;
		float cellHeight = mLocation.height / mRows;
		
		float x = (mHighlightCellX * cellWidth) + (cellWidth / 2.0f);
		float y = (mHighlightCellY * cellHeight) + (cellHeight / 2.0f);
		
		DrawOperation drawOp = new DrawOperation();
		drawOp.colour = Color.WHITE;
		drawOp.centreX = mLocation.getBounds().left + x;
		drawOp.centreY = mLocation.cy;
		drawOp.width = 1;
		drawOp.height = mLocation.height;
		g.gl2d.addToBatch(drawOp);
		
		drawOp.centreX = mLocation.cx;
		drawOp.centreY = mLocation.getBounds().top + y;
		drawOp.width = mLocation.width;
		drawOp.height = 1;
		g.gl2d.addToBatch(drawOp);
	}
	
	public void onTouchEvent(MotionEvent event) {
		float x, y;
		
		x = event.getX();
		y = event.getY();
		
		float cellWidth = mLocation.width / mColumns;
		float cellHeight = mLocation.height / mRows;
		
		if (CodeACGame.Instance.getState() == CodeACGame.State.PlacingSymbol) {
			highlightCell((int)((x - mLocation.getBounds().left) / cellWidth),
					(int)((y - mLocation.getBounds().top) / cellHeight));
			
			if (event.getActionMasked() == MotionEvent.ACTION_UP) {
				 GridCell cell = getCell(mHighlightCellX, mHighlightCellY);
				 if (cell.getSymbol() == null) {
					 cell.setSymbol(CodeACGame.Instance.getGrabSymbol());
					 CodeACGame.Instance.ungrabSymbol();
				 }
				 highlightCell(-1, -1);
			}
		} else {
			highlightCell(-1, -1);
		}
	}
	
	public Location getLocation() {
		return mLocation;
	}
	
	public void setLocation(Location value) {
		mLocation = value;
		
		float cellWidth = mLocation.width / mColumns;
		float cellHeight = mLocation.height / mRows;
		for (int y = 0; y < mRows; y++) {
			for (int x = 0; x < mColumns; x++) {
				mCells[y * mColumns + x].setLocation(new Location(
						mLocation.getBounds().left + (x * cellWidth) + (cellWidth / 2.0f),
						mLocation.getBounds().top + (y * cellHeight) + (cellHeight / 2.0f),
						mLocation.z - 1,
						cellWidth, cellHeight));
			}
		}
	}
	
	public GridCell getCell(int x, int y) {
		return mCells[y * mColumns + x];
	}
	
	public void highlightCell(int x, int y) {
		mHighlightCellX = x;
		mHighlightCellY = y;
	}
}

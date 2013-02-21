package com.intelorca.codeac.core;

import com.intelorca.slickgl.GameGraphics;

import android.graphics.RectF;

class Grid {
	private RectF mBounds;
	private int mColumns = 9;
	private int mRows = 9;
	private GridCell[] mCells;
	
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
	}
	
	public void setBounds(RectF value) {
		mBounds = value;
		
		float cellWidth = mBounds.width() / mColumns;
		float cellHeight = mBounds.height() / mRows;
		for (int y = 0; y < mRows; y++)
			for (int x = 0; x < mColumns; x++)
				mCells[y * mColumns + x].setBounds(new RectF(x * cellWidth + mBounds.left, y * cellHeight + mBounds.top,
						(x + 1) * cellWidth + mBounds.left, (y + 1) * cellHeight + mBounds.top));
	}
	
	public GridCell getCell(int x, int y) {
		return mCells[y * mColumns + x];
	}
}

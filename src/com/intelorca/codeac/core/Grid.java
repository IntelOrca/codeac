package com.intelorca.codeac.core;

import com.intelorca.slickgl.GameGraphics;

class Grid {
	private Location mLocation;
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
}

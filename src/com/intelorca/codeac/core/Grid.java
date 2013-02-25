package com.intelorca.codeac.core;

import junit.framework.Assert;
import android.graphics.Color;

import com.intelorca.slickgl.GameGraphics;
import com.intelorca.slickgl.GameGraphics2D.BLENDING_MODE;
import com.intelorca.slickgl.GameGraphics2D.DrawOperation;

class Grid {
	private final Symbolica mGame;
	private Location mLocation;
	private int mColumns = 9;
	private int mRows = 9;
	private GridCell[] mCells;
	
	private int mHighlightCellX = -1, mHighlightCellY = -1;
	
	public Grid(Symbolica game) {
		mGame = game;
		init();
	}
	
	public Grid(Symbolica game, int columns, int rows) {
		mGame = game;
		mColumns = columns;
		mRows = rows;
		init();
	}
	
	private void init() {
		mCells = new GridCell[mRows * mColumns];
		for (int y = 0; y < mRows; y++)
			for (int x = 0; x < mColumns; x++)
				mCells[y * mColumns + x] = new GridCell();
	}
	
	public void update() {
		// Update the grid cells
		for (GridCell cell : mCells)
			cell.update();
		
		// Check if player is moving a symbol
		if (mGame.getStateManager().getState() == GameStateManager.State.DRAGGING_SYMBOL) {
			Symbol s = mGame.getSymbolController().getSymbol();
			Location symbolLocation = s.getLocation();
			int x = getColumnIndex(symbolLocation.cx);
			int y = getRowIndex(symbolLocation.cy);
			
			// if (canPlaceAt(s, symbolLocation.cx, symbolLocation.cy))
				highlightCell(x, y);
			// else
			//	highlightCell(-1, -1);
		} else {
			highlightCell(-1, -1);
		}
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
		drawOp.blendingMode = BLENDING_MODE.ADDITIVE;
		drawOp.colour = Color.rgb(16, 16, 16);
		drawOp.z = mLocation.z - 4;
		drawOp.centreX = mLocation.getBounds().left + x;
		drawOp.centreY = mLocation.cy;
		drawOp.width = cellWidth;
		drawOp.height = mLocation.height;
		g.gl2d.addToBatch(drawOp);
		
		drawOp.centreX = mLocation.cx;
		drawOp.centreY = mLocation.getBounds().top + y;
		drawOp.width = mLocation.width;
		drawOp.height = cellHeight;
		g.gl2d.addToBatch(drawOp);
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
		if (x >= 0 && x < mColumns && y >= 0 && y < mRows)
			return mCells[y * mColumns + x];
		else
			return null;
	}
	
	public Symbol getSymbolFromCell(int x, int y) {
		GridCell cell = getCell(x, y);
		return (cell != null ? cell.getSymbol() : null);
	}
	
	public void highlightCell(int x, int y) {
		mHighlightCellX = x;
		mHighlightCellY = y;
	}
	
	public int getColumnIndex(float x) {
		x = (x - mLocation.getBounds().left) / getCellWidth();
		return (x >= 0 && x < mColumns ? (int)x : -1);
	}
	
	public int getRowIndex(float y) {
		y = (y - mLocation.getBounds().top) / getCellHeight();
		return (y >= 0 && y < mRows ? (int)y : -1);
	}
	
	public boolean canPlaceAt(Symbol s, float x, float y) {
		int c = getColumnIndex(x);
		int r = getRowIndex(y);
		if (c == -1 || r == -1)
			return false;

		// Check if symbol can go in the cell
		if (getCell(c, r).getSymbol() != null)
			return false;
		
		// Check if there are any mismatch surrounding symbols
		Symbol[] surroundingSymbols = new Symbol[] {
				getSymbolFromCell(c, r - 1), getSymbolFromCell(c + 1, r),
				getSymbolFromCell(c, r + 1), getSymbolFromCell(c - 1, r) };
		int numSurroundingSymbols = 0;
		for (Symbol ss : surroundingSymbols) {
			if (ss != null) {
				if (!s.canBeTogether(ss))
					return false;
				numSurroundingSymbols++;
			}
		}
		if (numSurroundingSymbols == 0)
			return false;

		// Should be fine!
		return true;
	}
	
	public void placeAt(Symbol s, float x, float y) {
		int c = getColumnIndex(x);
		int r = getRowIndex(y);
		Assert.assertTrue(c != -1 && r != -1);
		
		// Set the cell to contain the symbol
		getCell(c, r).setSymbol(s);
		
		// Check if any lines have been made
		checkForLines();
	}
	
	private void checkForLines() {
		// Check for columns
		for (int x = 0; x < mColumns; x++) {
			int numSymbols = 0;
			for (int y = 0; y < mRows; y++)
				if (getSymbolFromCell(x, y) != null)
					numSymbols++;
			if (numSymbols == mRows)
				clearColumn(x);
		}
		
		// Check for rows
		for (int y = 0; y < mRows; y++) {
			int numSymbols = 0;
			for (int x = 0; x < mColumns; x++)
				if (getSymbolFromCell(x, y) != null)
					numSymbols++;
			if (numSymbols == mColumns)
				clearRow(y);
		}
	}
	
	private void clearColumn(int x) {
		for (int y = 0; y < mRows; y++) {
			getCell(x, y).setSymbol(null);
		}
	}
	
	private void clearRow(int y) {
		for (int x = 0; x < mColumns; x++) {
			getCell(x, y).setSymbol(null);
		}
	}
	
	private float getCellWidth() {
		return mLocation.width / mColumns;
	}
	
	private float getCellHeight() {
		return mLocation.height / mRows;	
	}
	
	public int getColumns() {
		return mColumns;
	}
	
	public int getRows() {
		return mRows;
	}
}

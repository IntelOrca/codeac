package com.intelorca.codeac.core;

import android.graphics.RectF;

public class Location implements Cloneable {
	public float cx, cy, z;
	public float width, height;
	
	public Location() { }
	
	public Location(float cx, float cy, float z, float width, float height) {
		this.cx = cx;
		this.cy = cy;
		this.z = z;
		this.width = width;
		this.height = height;
	}
	
	@Override
	public boolean equals(Object o) {
		Location ol = (Location)o;
		if (cx != ol.cx)
			return false;
		if (cy != ol.cy)
			return false;
		if (z != ol.z)
			return false;
		if (width != ol.width)
			return false;
		if (height != ol.height)
			return false;
		return true;
	}
	
	public Object clone() {
		return new Location(cx, cy, z, width, height);
	}
	
	public void offset(float x, float y) {
		cx += x;
		cy += y;
	}
	
	public boolean contains(float x, float y) {
		return getBounds().contains(x, y);
	}
	
	public RectF getBounds() {
		return new RectF(cx - (width / 2.0f), cy - (height / 2.0f),
				cx + (width / 2.0f), cy + (height / 2.0f));
	}
	
	public void setBounds(RectF value) {
		cx = value.centerX();
		cy = value.centerY();
		width = value.width();
		height = value.height();
	}
}

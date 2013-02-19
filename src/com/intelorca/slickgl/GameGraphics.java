package com.intelorca.slickgl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Color;

public class GameGraphics {
	public final GameRenderer renderer;
	public final GL10 gl;
	public final GameGraphics2D gl2d;
	public final GameGraphics3D gl3d;
	
	public GameGraphics(GameRenderer renderer, GL10 gl) {
		this.renderer = renderer;
		this.gl = gl;
		this.gl2d = new GameGraphics2D(renderer, gl);
		this.gl3d = new GameGraphics3D(renderer, gl);
	}
	
	public void clear(int colour) {
		gl.glClearColor(
				Color.red(colour) / 255.0f,
				Color.green(colour) / 255.0f,
				Color.blue(colour) / 255.0f,
				Color.alpha(colour) / 255.0f);
		clear();
	}
	
	public void clear() {
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();
	}
	
	public static ShortBuffer getShortBuffer(short[] values) {
		ByteBuffer ibb = ByteBuffer.allocateDirect(values.length * 2);
		ibb.order(ByteOrder.nativeOrder());
		ShortBuffer indexBuffer = ibb.asShortBuffer();
		indexBuffer.put(values);
		indexBuffer.position(0);
		return indexBuffer;
	}
	
	public static FloatBuffer getFloatBuffer(float[] values) {
		ByteBuffer vbb = ByteBuffer.allocateDirect(values.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		FloatBuffer vertexBuffer = vbb.asFloatBuffer();
		vertexBuffer.put(values);
		vertexBuffer.position(0);
		return vertexBuffer;
	}
}

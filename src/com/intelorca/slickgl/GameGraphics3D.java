package com.intelorca.slickgl;

import javax.microedition.khronos.opengles.GL10;

public class GameGraphics3D {
	private final GameRenderer mRenderer;
	private final GL10 mGL;
	
	public GameGraphics3D(GameRenderer renderer, GL10 gl) {
		mRenderer = renderer;
		mGL = gl;
	}
	
	public void drawCube() {
		float[] vertices = new float[] {
			-1.0f, -1.0f, -1.0f,
			-1.0f, +1.0f, -1.0f,
			+1.0f, +1.0f, -1.0f,
			+1.0f, -1.0f, -1.0f,
			-1.0f, -1.0f, +1.0f,
			-1.0f, +1.0f, +1.0f,
			+1.0f, +1.0f, +1.0f,
			+1.0f, -1.0f, +1.0f,
		};
		short[] indices = new short[] {
			0, 1, 2, 0, 2, 3,
			3, 2, 6, 3, 6, 7,
			7, 6, 5, 7, 5, 4,
			4, 5, 1, 4, 1, 0,
			1, 5, 6, 1, 6, 2,
			4, 0, 3, 4, 3, 7,
		};
		mGL.glVertexPointer(3, GL10.GL_FLOAT, 0, GameGraphics.getFloatBuffer(vertices));
		mGL.glDrawElements(GL10.GL_TRIANGLES, indices.length,
				GL10.GL_UNSIGNED_SHORT, GameGraphics.getShortBuffer(indices));
	}
	
	public void drawTriangle(float[] vertices) {
		short[] indices = new short[3];
		for (int i = 0; i < 3; i++)
			indices[i] = (short)i;
		
		mGL.glVertexPointer(3, GL10.GL_FLOAT, 0, GameGraphics.getFloatBuffer(vertices));
		mGL.glDrawElements(GL10.GL_TRIANGLES, indices.length,
				GL10.GL_UNSIGNED_SHORT, GameGraphics.getShortBuffer(indices));
	}
}

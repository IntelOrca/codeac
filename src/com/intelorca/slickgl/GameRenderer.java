package com.intelorca.slickgl;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLUtils;

public class GameRenderer implements Renderer {
	private GL10 mGL;
	private GameView mView;
	private Resources mResources;
	
	private int[] mBitmapIDs = new int[0];
	private int[] mTextureIDs = new int[0];
	private int[] mTextureWidths = new int[0];
	private int[] mTextureHeights = new int[0];
	
	private int mWidth;
	private int mHeight;
	
	public GameRenderer(GameView view, Resources resources) {
		mView = view;
		mResources = resources;
	}
	
	@Override
	public void onDrawFrame(GL10 gl) {
		mGL = gl;
		mView.onDrawFrame(new GameGraphics(this, mGL));
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		mGL = gl;
		this.mWidth = width;
		this.mHeight = height;
		mGL.glViewport(0, 0, width, height);
	}
	
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		mGL = gl;
		
		// Enable face culling.
		// mGL.glEnable(GL10.GL_CULL_FACE);
		
		// What faces to remove with the face culling.
		// mGL.glCullFace(GL10.GL_BACK);
		
		// Enabled the vertices buffer for writing and to be used during
		// rendering.
		mGL.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		
		// Tell OpenGL to enable the use of UV coordinates.
		mGL.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	}
	
	public void loadTextures(int[] bitmapIDs) {
		// Create a list of new bitmap IDs
		ArrayList<Integer> newBitmapIDs = new ArrayList<Integer>();
		for (int i = 0; i < bitmapIDs.length; i++) {
			boolean alreadyTexture = false;
			
			// Check if this bitmap is already a texture
			for (int j = 0; j < mBitmapIDs.length; j++) {
				if (mBitmapIDs[j] == bitmapIDs[i]) {
					alreadyTexture = true;
					break;
				}
			}
			
			if (!alreadyTexture)
				newBitmapIDs.add(bitmapIDs[i]);
		}
		
		int newIDsOffset = mBitmapIDs.length;
		
		// Extend texture and bitmap id array for new textures
		mBitmapIDs = resizeArray(mBitmapIDs, mBitmapIDs.length + newBitmapIDs.size());
		mTextureIDs = resizeArray(mTextureIDs, mTextureIDs.length + newBitmapIDs.size());
		mTextureWidths = resizeArray(mTextureWidths, mTextureWidths.length + newBitmapIDs.size());
		mTextureHeights = resizeArray(mTextureHeights, mTextureHeights.length + newBitmapIDs.size());
		
		// Set new IDs
		mGL.glGenTextures(mTextureIDs.length - newIDsOffset, mTextureIDs, newIDsOffset);
		for (int i = 0; i < newBitmapIDs.size(); i++) {
			mBitmapIDs[newIDsOffset + i] = newBitmapIDs.get(i);
			
			// Load the texture from the resource manager
			Bitmap bitmap = BitmapFactory.decodeResource(mResources, newBitmapIDs.get(i));
			mTextureWidths[newIDsOffset + i] = bitmap.getWidth();
			mTextureHeights[newIDsOffset + i] = bitmap.getHeight();
			
			// Load the texture into OpenGL
			bindTexture(mTextureIDs[newIDsOffset + i], bitmap);
		}
	}
	
	private void bindTexture(int textureId, Bitmap texture) {
		// Working with textureId
		mGL.glBindTexture(GL10.GL_TEXTURE_2D, textureId);

		// Texture attributes
		mGL.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		mGL.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		mGL.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
		mGL.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

		// Attach bitmap to current texture
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, texture, 0);
	}
	
	private int[] resizeArray(int[] old, int newSize) {
		int[] newArray = new int[newSize];
		for (int i = 0; i < Math.min(old.length, newSize); i++)
			newArray[i] = old[i];
		return newArray;
	}
	
	public int getTextureID(int bitmapID) {
		for (int i = 0; i < mBitmapIDs.length; i++)
			if (mBitmapIDs[i] == bitmapID)
				return mTextureIDs[i];
		return 0;
	}
	
	public int getTextureWidth(int bitmapID) {
		for (int i = 0; i < mBitmapIDs.length; i++)
			if (mBitmapIDs[i] == bitmapID)
				return mTextureWidths[i];
		return 0;
	}
	
	public int getTextureHeight(int bitmapID) {
		for (int i = 0; i < mBitmapIDs.length; i++)
			if (mBitmapIDs[i] == bitmapID)
				return mTextureHeights[i];
		return 0;
	}
	
	public int getWidth() {
		return mWidth;
	}
	
	public int getHeight() {
		return mHeight;
	}
}

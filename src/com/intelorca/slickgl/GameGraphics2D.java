package com.intelorca.slickgl;

import android.graphics.*;
import java.util.*;
import javax.microedition.khronos.opengles.*;

public class GameGraphics2D {
	private static final int DEFAULT_NEAR_DISTANCE = -1024;
	private static final int DEFAULT_FAR_DISTANCE = 1024;
	
	private final GameRenderer mRenderer;
	private final GL10 mGL;
	
	public GameGraphics2D(GameRenderer renderer, GL10 gl) {
		mRenderer = renderer;
		mGL = gl;
	}
	
	public void setupView() {
		setupView(DEFAULT_NEAR_DISTANCE, DEFAULT_FAR_DISTANCE);
	}
	
	public void setupView(int near, int far) {
		// Set an orthographic projection
		mGL.glMatrixMode(GL10.GL_PROJECTION);
		mGL.glLoadIdentity();
		mGL.glOrthof(0, mRenderer.getWidth(), -mRenderer.getHeight(), 0, near, far);
		
		// Select a clean model view
		mGL.glMatrixMode(GL10.GL_MODELVIEW);
		mGL.glLoadIdentity();
		
		// Rotate world by 180 around x axis so positive y is down
		mGL.glRotatef(-180, 1, 0, 0);
	}

	/**************************************************************************
	 * Sprite batching
	 **************************************************************************/
	private ArrayList<TextureBatch> mTextureBatches = new ArrayList<TextureBatch>();
	
	public void beginSpriteBatch() {
		mTextureBatches.clear();
	}
	
	public void addToBatch(DrawOperation drawOp) {
		// Add to existing texture batch
		for (TextureBatch texBatch : mTextureBatches) {
			if (texBatch.mBitmapID == drawOp.bitmapID &&
					texBatch.mColour == drawOp.colour &&
					texBatch.mBlendingMode == drawOp.blendingMode) {
				texBatch.addDrawOperation(drawOp);
				return;
			}
		}
		
		// Add a new texture batch
		TextureBatch texBatch = new TextureBatch(drawOp.bitmapID, drawOp.colour, drawOp.blendingMode);
		texBatch.addDrawOperation(drawOp);
		mTextureBatches.add(texBatch);
	}
	
	public void endSpriteBatch() {
		// Draw each texture batch
		for (TextureBatch texBatch : mTextureBatches)
			texBatch.draw();
	}
	
	public enum BLENDING_MODE {
		ALPHA,
		ADDITIVE,
	}
	
	private class TextureBatch {
		private int mBitmapID, mTextureID;
		private int mColour;
		private BLENDING_MODE mBlendingMode;
		private int mTextureWidth, mTextureHeight;
		private ArrayList<Float> mVertices = new ArrayList<Float>();
		private ArrayList<Float> mTextureCoords = new ArrayList<Float>();
		private ArrayList<Short> mIndices = new ArrayList<Short>();
		
		public TextureBatch(int bitmapID, int colour, BLENDING_MODE blendingMode) {
			GameRenderer renderer = GameGraphics2D.this.mRenderer;
			mColour = colour;
			mBlendingMode = blendingMode;
			
			if (bitmapID != 0) {
				mTextureID = renderer.getTextureID(bitmapID);
				mTextureWidth = renderer.getTextureWidth(bitmapID);
				mTextureHeight = renderer.getTextureHeight(bitmapID);
			}
		}
		
		public void addDrawOperation(DrawOperation drawOp) {
			// Add indices
			short startOffset = (short)(mVertices.size() / 3);
			for (short s : drawOp.getIndices())
				mIndices.add((short)(startOffset + s));
			
			// Add vertices
			for (float f : drawOp.getVertices())
				mVertices.add(f);
			
			if (mTextureID != 0) {
				// Add texture coordinates
				for (float f : drawOp.getTextureCoords(mTextureWidth, mTextureHeight))
					mTextureCoords.add(f);
			}
		}
		
		public void draw() {
			// Convert lists to arrays
			float[] vertices = new float[mVertices.size()];
			for (int i = 0; i < vertices.length; i++)
				vertices[i] = mVertices.get(i);
			
			float[] textureCoords = null;
			if (mTextureID != 0) {
				textureCoords = new float[mTextureCoords.size()];
				for (int i = 0; i < textureCoords.length; i++)
					textureCoords[i] = mTextureCoords.get(i);
			}
			
			short[] indices = new short[mIndices.size()];
			for (int i = 0; i < indices.length; i++)
				indices[i] = mIndices.get(i);
			
			// Get OpenGL
			GL10 gl = GameGraphics2D.this.mGL;
			
			// Set render state
			gl.glDisable(GL10.GL_LIGHTING);
			gl.glEnable(GL10.GL_DEPTH_TEST);
			gl.glEnable(GL10.GL_BLEND);

			if (mBlendingMode == BLENDING_MODE.ALPHA)
				gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			else if (mBlendingMode == BLENDING_MODE.ADDITIVE)
				gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE);
			
			if (mTextureID != 0)
				gl.glEnable(GL10.GL_TEXTURE_2D);
			else
				gl.glDisable(GL10.GL_TEXTURE_2D);
			
			// Draw the batch of textured triangles
			gl.glColor4f(Color.red(mColour) / 255.0f,
						 Color.green(mColour) / 255.0f,
						 Color.blue(mColour) / 255.0f,
						 Color.alpha(mColour) / 255.0f);
			
			if (mTextureID != 0) {
				gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureID);
				gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, GameGraphics.getFloatBuffer(textureCoords));
			}
			
			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, GameGraphics.getFloatBuffer(vertices));
			gl.glDrawElements(GL10.GL_TRIANGLES, indices.length,
					GL10.GL_UNSIGNED_SHORT, GameGraphics.getShortBuffer(indices));
		}
	}
	
	public static class DrawOperation {
		public int bitmapID;
		public int colour = Color.WHITE;
		public BLENDING_MODE blendingMode = BLENDING_MODE.ALPHA;
		public Rect src;
		public float centreX, centreY, z;
		public float width, height;
		public float angle;
		
		public DrawOperation() { }
		
		public DrawOperation(RectF dst) {
			setDestRect(dst);
		}
		
		public DrawOperation(int bitmapID, Rect src, float cx, float cy) {
			this.bitmapID = bitmapID;
			this.src = src;
			this.centreX = cx;
			this.centreY = cy;
			this.width = src.width();
			this.height = src.height();
		}
		
		public DrawOperation(int bitmapID, Rect src, RectF dst) {
			this.bitmapID = bitmapID;
			this.src = src;
			setDestRect(dst);
		}
				
		public void setDestRect(RectF rect) {
			width = rect.width();
			height = rect.height();
			centreX = rect.left + (width / 2);
			centreY = rect.top + (height / 2);
		}
		
		public float[] getVertices() {
			float[] vertices = new float[12];
			vertices[0] = centreX - (width / 2);
			vertices[1] = centreY - (height / 2);
			vertices[2] = z;
			vertices[3] = centreX - (width / 2);
			vertices[4] = centreY + (height / 2);
			vertices[5] = z;
			vertices[6] = centreX + (width / 2);
			vertices[7] = centreY + (height / 2);
			vertices[8] = z;
			vertices[9] = centreX + (width / 2);
			vertices[10] = centreY - (height / 2);
			vertices[11] = z;
			return vertices;
		}
		
		public float[] getTextureCoords(int textureWidth, int textureHeight) {
			float[] textureCoords = new float[8];
			textureCoords[0] = (float)src.left   / textureWidth;
			textureCoords[1] = (float)src.top    / textureHeight;
			textureCoords[2] = (float)src.left   / textureWidth;
			textureCoords[3] = (float)src.bottom / textureHeight;
			textureCoords[4] = (float)src.right  / textureWidth;
			textureCoords[5] = (float)src.bottom / textureHeight;
			textureCoords[6] = (float)src.right  / textureWidth;
			textureCoords[7] = (float)src.top    / textureHeight;
			return textureCoords;
		}
		
		public short[] getIndices() {
			short[] indices = new short[6];
			indices[0] = 0;
			indices[1] = 1;
			indices[2] = 2;
			indices[3] = 0;
			indices[4] = 2;
			indices[5] = 3;
			return indices;
		}
	}
	
	
	
	
	/*
	// ----------- ADD SPRITE METHODS --------------------

	// SIMPLE
	public void draw(GL10 gl, int bitmapId, Rect src, Rect dst) {
		// This is a simple class for doing straight src->dst draws
		// Look up bitmapId
		for (int i = 0; i < bitmapIds.length; i++) {
			if (bitmapId == bitmapIds[i]) {
				spriteData.get(i).addSprite(src, dst);
				return;
			}
		}
		Log.w("SpriteBatcher", "Warning: bitmapId not found");
	}

	public void draw(GL10 gl, int bitmapId, Rect src, Rect dst, int angle) {
		// This is a simple class for doing straight src->dst draws
		// Look up bitmapId
		for (int i = 0; i < bitmapIds.length; i++) {
			if (bitmapId == bitmapIds[i]) {
				spriteData.get(i).addSprite(src, dst, angle);
				return;
			}
		}
		Log.w("SpriteBatcher", "Warning: bitmapId not found");
	}

	// COMPLICATED
	public void draw(GL10 gl, int bitmapId, Rect src, int drawX, int drawY,
			Rect hotRect, int angle, float scale, float alpha) {
		// Just redirects to below with equal scale in x and y
		draw(gl, bitmapId, src, drawX, drawY, hotRect, angle, scale, scale,
				alpha);
	}

	public void draw(GL10 gl, int bitmapId, Rect src, int drawX, int drawY,
			Rect hotRect, int angle, float sizeX, float sizeY, float alpha) {
		// This class allows rotations but needs additional input
		// Look up bitmapId
		for (int i = 0; i < bitmapIds.length; i++) {
			if (bitmapId == bitmapIds[i]) {
				spriteData.get(i).addSprite(src, drawX, drawY, hotRect, angle,
						sizeX, sizeY, alpha);
				return;
			}
		}
		Log.w("SpriteBatcher", "Warning: bitmapId not found");
	}
	
	// Holds all the information for our batched GLDRAWELEMENT calls
	ArrayList<SpriteData> spriteData;

	public void batchDraw(GL10 gl) {
		// All the draw commands are already batched together for each separate
		// texture tile. Now we loop through each tile and make the draw calls
		// to OpenGL.
		// NOTE: You can call this method early to send a batch. This gives you
		// more control over layer order of the sprites.
		for (int i = 0; i < textureIds.length; i++) {
			// GRAB SPRITEDATA
			SpriteData thisSpriteData = spriteData.get(i);

			// CONVERT INTO ARRAY
			float[] vertices = thisSpriteData.getVertices();
			short[] indices = thisSpriteData.getIndices();
			float[] textureCoords = thisSpriteData.getTextureCoords();

			// ONLY DRAW IF ALL NOT NULL
			if (vertices != null && indices != null && textureCoords != null) {

				// CREATE BUFFERS - these are just containers for sending the
				// draw information we have already collected to OpenGL

				// Vertex buffer (position information of every draw command)
				ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
				vbb.order(ByteOrder.nativeOrder());
				FloatBuffer vertexBuffer = vbb.asFloatBuffer();
				vertexBuffer.put(vertices);
				vertexBuffer.position(0);

				// Index buffer (which vertices go together to make the
				// elements)
				ByteBuffer ibb = ByteBuffer.allocateDirect(indices.length * 2);
				ibb.order(ByteOrder.nativeOrder());
				ShortBuffer indexBuffer = ibb.asShortBuffer();
				indexBuffer.put(indices);
				indexBuffer.position(0);

				// How to paste the texture over each element so that the right
				// image is shown
				ByteBuffer tbb = ByteBuffer
						.allocateDirect(textureCoords.length * 4);
				tbb.order(ByteOrder.nativeOrder());
				FloatBuffer textureBuffer = tbb.asFloatBuffer();
				textureBuffer.put(textureCoords);
				textureBuffer.position(0);

				// DRAW COMMAND
				// Tell OpenGL where our texture is located.
				gl.glBindTexture(GL10.GL_TEXTURE_2D, textureIds[i]);
				// Telling OpenGL where our textureCoords are.
				gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
				// Specifies the location and data format of the array of vertex
				// coordinates to use when rendering.
				gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
				// Draw elements command using indices so it knows which
				// vertices go together to form each element
				gl.glDrawElements(GL10.GL_TRIANGLES, indices.length,
						GL10.GL_UNSIGNED_SHORT, indexBuffer);
			}
		}
		// Clear spriteData
		for (int i = 0; i < spriteData.size(); i++) {
			spriteData.get(i).clear();
		}
	}
	
	// DIRECT
	public void addVertices(int bitmapId, float[] f) {
		// Look up bitmapId
		for (int i = 0; i < bitmapIds.length; i++) {
			if (bitmapId == bitmapIds[i]) {
				spriteData.get(i).addVertices(f);
				return;
			}
		}
	}

	public void addIndices(int bitmapId, short[] s) {
		// Look up bitmapId
		for (int i = 0; i < bitmapIds.length; i++) {
			if (bitmapId == bitmapIds[i]) {
				spriteData.get(i).addIndices(s);
				return;
			}
		}
	}

	public void addTextureCoords(int bitmapId, float[] f) {
		// Look up bitmapId
		for (int i = 0; i < bitmapIds.length; i++) {
			if (bitmapId == bitmapIds[i]) {
				spriteData.get(i).addTextureCoords(f);
				return;
			}
		}
	}

	private class SpriteData {
		// This is a simple a container class to avoid unnecessary code in
		// SpriteBatcher. It holds a whole set of information for a single
		// GLDRAWELEMENTS call:
		private ArrayList<Float> vertices; // Positions of vertices
		private ArrayList<Short> indices; // Which verts go together to form
		// Ele's
		private ArrayList<Float> textureCoords; // Texture map coordinates

		private int textureWidth;
		private int textureHeight;

		public SpriteData(int width, int height) {
			vertices = new ArrayList<Float>();
			indices = new ArrayList<Short>();
			textureCoords = new ArrayList<Float>();
			textureWidth = width;
			textureHeight = height;
		}

		// Add sprite methods
		// DIRECT
		public void addVertices(float[] f) {
			for (int i = 0; i < f.length; i++) {
				vertices.add(f[i]);
			}
		}

		public void addIndices(short[] s) {
			for (int i = 0; i < s.length; i++) {
				indices.add(s[i]);
			}
		}

		public void addTextureCoords(float[] f) {
			for (int i = 0; i < f.length; i++) {
				textureCoords.add(f[i]);
			}
		}

		// SIMPLE
		public void addSprite(Rect src, Rect dst) {
			// This is a simple class for doing straight src->dst draws

			// VERTICES
			vertices.add((float) dst.left);
			vertices.add((float) dst.top);
			vertices.add(0f);
			vertices.add((float) dst.left);
			vertices.add((float) dst.bottom);
			vertices.add(0f);
			vertices.add((float) dst.right);
			vertices.add((float) dst.bottom);
			vertices.add(0f);
			vertices.add((float) dst.right);
			vertices.add((float) dst.top);
			vertices.add(0f);

			// INDICES - increment from last value
			short lastValue;
			if (!indices.isEmpty()) {
				// If not empty, find last value
				lastValue = indices.get(indices.size() - 1);
			} else
				lastValue = -1;
			indices.add((short) (lastValue + 1));
			indices.add((short) (lastValue + 2));
			indices.add((short) (lastValue + 3));
			indices.add((short) (lastValue + 1));
			indices.add((short) (lastValue + 3));
			indices.add((short) (lastValue + 4));

			// TEXTURE COORDS
			float[] srcX = { src.left, src.left, src.right, src.right };
			float[] srcY = { src.top, src.bottom, src.bottom, src.top };
			for (int i = 0; i < 4; i++) {
				textureCoords.add((float) (srcX[i] / textureWidth));
				textureCoords.add((float) (srcY[i] / textureHeight));
			}
		}

		public void addSprite(Rect src, Rect dst, int angle) {
			// This is a simple class for doing straight src->dst draws
			// It automatically rotates the images by angle about its centre

			// VERTICES
			// Trig
			double cos = Math.cos((double) angle / 180 * Math.PI);
			double sin = Math.sin((double) angle / 180 * Math.PI);

			// Width and height
			float halfWidth = (dst.right - dst.left) / 2;
			float halfHeight = (dst.top - dst.bottom) / 2;

			// Coordinates before rotation
			float[] hotX = { -halfWidth, -halfWidth, halfWidth, halfWidth };
			float[] hotY = { halfHeight, -halfHeight, -halfHeight, halfHeight };
			for (int i = 0; i < 4; i++) {
				// Coordinates after rotation
				float transformedX = (float) (cos * hotX[i] - sin * hotY[i]);
				float transformedY = (float) (sin * hotX[i] + cos * hotY[i]);
				// Pan by draw coordinates
				transformedX += dst.left + halfWidth;
				transformedY += dst.bottom + halfHeight;
				// Add to vertices array
				vertices.add(transformedX);
				vertices.add(transformedY);
				vertices.add(0f);
			}

			// INDICES - increment from last value
			short lastValue;
			if (!indices.isEmpty()) {
				// If not empty, find last value
				lastValue = indices.get(indices.size() - 1);
			} else
				lastValue = -1;
			indices.add((short) (lastValue + 1));
			indices.add((short) (lastValue + 2));
			indices.add((short) (lastValue + 3));
			indices.add((short) (lastValue + 1));
			indices.add((short) (lastValue + 3));
			indices.add((short) (lastValue + 4));

			// TEXTURE COORDS
			float[] srcX = { src.left, src.left, src.right, src.right };
			float[] srcY = { src.top, src.bottom, src.bottom, src.top };
			for (int i = 0; i < 4; i++) {
				textureCoords.add((float) (srcX[i] / textureWidth));
				textureCoords.add((float) (srcY[i] / textureHeight));
			}
		}

		// COMPLICATED
		public void addSprite(Rect src, int drawX, int drawY, Rect hotRect,
				int angle, float sizeX, float sizeY, float alpha) {
			// This class allows rotations but needs additional input
			// hotRect defines the corner coordinates from drawX and drawY
			// drawX and drawY is the draw point and centre of rotation

			// VERTICES
			// Trig
			double cos = Math.cos((double) angle / 180 * Math.PI);
			double sin = Math.sin((double) angle / 180 * Math.PI);

			// Coordinates before rotation
			float[] hotX = { hotRect.left, hotRect.left, hotRect.right,
					hotRect.right };
			float[] hotY = { hotRect.top, hotRect.bottom, hotRect.bottom,
					hotRect.top };
			for (int i = 0; i < 4; i++) {
				// Apply scale before rotation
				float x = hotX[i] * sizeX;
				float y = hotY[i] * sizeY;
				// Coordinates after rotation
				float transformedX = (float) (cos * x - sin * y);
				float transformedY = (float) (sin * x + cos * y);
				// Pan by draw coordinates
				transformedX += drawX;
				transformedY += drawY;
				// Add to vertices array
				vertices.add(transformedX);
				vertices.add(transformedY);
				vertices.add(0f);
			}

			// INDICES - increment from last value
			short lastValue;
			if (!indices.isEmpty()) {
				// If not empty, find last value
				lastValue = indices.get(indices.size() - 1);
			} else
				lastValue = -1;
			indices.add((short) (lastValue + 1));
			indices.add((short) (lastValue + 2));
			indices.add((short) (lastValue + 3));
			indices.add((short) (lastValue + 1));
			indices.add((short) (lastValue + 3));
			indices.add((short) (lastValue + 4));

			// TEXTURE COORDS
			float[] srcX = { src.left + 0.5f, src.left + 0.5f,
					src.right - 0.5f, src.right - 0.5f };
			float[] srcY = { src.top + 0.5f, src.bottom - 0.5f,
					src.bottom - 0.5f, src.top + 0.5f };
			for (int i = 0; i < 4; i++) {
				textureCoords.add((float) (srcX[i] / textureWidth));
				textureCoords.add((float) (srcY[i] / textureHeight));
			}
			// Log.d("SpriteBatcher", "Left = " + src.left);
			// Log.d("SpriteBatcher", "Top = " + src.top);
			// Log.d("SpriteBatcher", "Right = " + src.right);
			// Log.d("SpriteBatcher", "Bottom = " + src.bottom);
			// Log.d("SpriteBatcher", "LEFT U = "
			// + (float) ((float) src.left + 0.5 / textureWidth));
			// Log.d("SpriteBatcher", "TOP V = "
			// + (float) ((float) src.top + 0.5 / textureHeight));
			// textureCoords
			// .add((float) (((float) src.left + 0.5) / textureWidth));
			// textureCoords
			// .add((float) (((float) src.top + 0.5) / textureHeight));
			// textureCoords
			// .add((float) (((float) src.left + 0.5) / textureWidth));
			// textureCoords
			// .add((float) (((float) src.bottom - 0.5) / textureHeight));
			// textureCoords
			// .add((float) (((float) src.right - 0.5) / textureWidth));
			// textureCoords
			// .add((float) (((float) src.bottom - 0.5) / textureHeight));
			// textureCoords
			// .add((float) (((float) src.right - 0.5) / textureWidth));
			// textureCoords
			// .add((float) (((float) src.top + 0.5) / textureHeight));
		}

		public void clear() {
			vertices.clear();
			indices.clear();
			textureCoords.clear();
		}

		// GETTER/SETTER
		public float[] getVertices() {
			// Convert to float[] before returning
			return convertToPrimitive(vertices.toArray(new Float[vertices
					.size()]));
		}

		public short[] getIndices() {
			// Convert to short[] before returning
			return convertToPrimitive(indices
					.toArray(new Short[indices.size()]));
		}

		public float[] getTextureCoords() {
			// Convert to float[] before returning
			return convertToPrimitive(textureCoords
					.toArray(new Float[textureCoords.size()]));
		}

		private float[] convertToPrimitive(Float[] objectArray) {
			if (objectArray == null) {
				return null;
			} else if (objectArray.length == 0) {
				return null;
			}
			final float[] primitiveArray = new float[objectArray.length];
			for (int i = 0; i < objectArray.length; i++) {
				primitiveArray[i] = objectArray[i].floatValue();
			}
			return primitiveArray;
		}

		private short[] convertToPrimitive(Short[] objectArray) {
			if (objectArray == null) {
				return null;
			} else if (objectArray.length == 0) {
				return null;
			}
			final short[] primitiveArray = new short[objectArray.length];
			for (int i = 0; i < objectArray.length; i++) {
				primitiveArray[i] = objectArray[i].shortValue();
			}
			return primitiveArray;
		}
	}
	*/
}

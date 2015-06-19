package core;

import misc.Globals;
import misc.IUpdate;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Matrix4;

import entity.Player;

public final class TheCamera implements IUpdate {

	private static TheCamera instance; 
	
	private OrthographicCamera camera;
	
	public static TheCamera getInstance() {
		if(instance == null) {
			instance = new TheCamera();
		}
		
		return instance;
	}
	
	private TheCamera() {
		camera = new OrthographicCamera();
		
		// This will get adjusted when the window is resized.
		// 100 is used as a base that Box2D can handle efficiently.
		camera.setToOrtho(true, 100, 60);
	}

	@Override
	public boolean update() {
		Player player = Globals.getPlayer();
		camera.position.x = player.getCenterX();
		camera.position.y = player.getCenterY();
		
		camera.update();
		
		return false;
	}

	@Override
	public void done() {
	}
	
	public void resizeViewport(float screenWidth, float screenHeight) {
		// Adjust the camera viewport height, while keeping the width set at 100.
		camera.viewportHeight = (camera.viewportWidth / screenWidth) * screenHeight;
		camera.update();
	}
	
	public float getViewportWidth() {
		return camera.viewportWidth;
	}
	
	public float getViewportHeight() {
		return camera.viewportHeight;
	}
	
	public float getTileMapScale() {
		// Each tile is originally 32 x 32.
		// Each tile should be 1/16 of the screen's width.
		return (camera.viewportWidth / 16) / 32;
	}
	
	public float getTop() {
		return camera.position.y - (getViewportHeight() / 2);
	}
	
	public float getBottom() {
		return camera.position.y + (getViewportHeight() / 2);
	}
	
	public float getLeft() {
		return camera.position.x + (getViewportWidth() / 2);
	}
	
	public float getRight() {
		return camera.position.x + (getViewportWidth() / 2);
	}
	
	public float getCenterX() {
		return camera.position.x;
	}
	
	public float getCenterY() {
		return camera.position.y;
	}
	
	public OrthographicCamera getRawCamera() {
		return camera;
	}
	
	public boolean isVisible(float x, float y, float width, float height) {
		return camera.frustum.pointInFrustum(x, y, 0) ||
			   camera.frustum.pointInFrustum(x + width, y, 0) || 
			   camera.frustum.pointInFrustum(x, y + height, 0) ||
			   camera.frustum.pointInFrustum(x + width, y + height, 0);
	}
}

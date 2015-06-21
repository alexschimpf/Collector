package core;

import misc.Globals;
import misc.IUpdate;

import com.badlogic.gdx.graphics.OrthographicCamera;
import entity.Player;

public final class TheCamera implements IUpdate {

	private static TheCamera instance; 
	
	private final OrthographicCamera CAMERA = new OrthographicCamera();
	
	public static TheCamera getInstance() {
		if(instance == null) {
			instance = new TheCamera();
		}
		
		return instance;
	}
	
	private TheCamera() {
		// This will get adjusted when the window is resized.
		// 100 is used as a base that Box2D can handle efficiently.
		CAMERA.setToOrtho(true, 50, 50);
	}

	@Override
	public boolean update() {
		Player player = Globals.getPlayer();
		CAMERA.position.x = player.getCenterX();
		CAMERA.position.y = player.getCenterY();
		
		CAMERA.update();
		
		return false;
	}

	@Override
	public void done() {
	}
	
	public void resizeViewport(float screenWidth, float screenHeight) {
		// Adjust the camera viewport height, while keeping the width set at 100.
		CAMERA.viewportHeight = (CAMERA.viewportWidth / screenWidth) * screenHeight;
		CAMERA.update();
	}
	
	public float getViewportWidth() {
		return CAMERA.viewportWidth;
	}
	
	public float getViewportHeight() {
		return CAMERA.viewportHeight;
	}
	
	public float getTileMapScale() {
		// Each tile is originally 32 x 32.
		// Each tile should be 1/16 of the screen's width.
		return (CAMERA.viewportWidth / 16) / 32;
	}
	
	public float getTop() {
		return CAMERA.position.y - (getViewportHeight() / 2);
	}
	
	public float getBottom() {
		return CAMERA.position.y + (getViewportHeight() / 2);
	}
	
	public float getLeft() {
		return CAMERA.position.x + (getViewportWidth() / 2);
	}
	
	public float getRight() {
		return CAMERA.position.x + (getViewportWidth() / 2);
	}
	
	public float getCenterX() {
		return CAMERA.position.x;
	}
	
	public float getCenterY() {
		return CAMERA.position.y;
	}
	
	public OrthographicCamera getRawCamera() {
		return CAMERA;
	}
	
	public boolean isVisible(float x, float y, float width, float height) {
		return CAMERA.frustum.pointInFrustum(x, y, 0) ||
			   CAMERA.frustum.pointInFrustum(x + width, y, 0) || 
			   CAMERA.frustum.pointInFrustum(x, y + height, 0) ||
			   CAMERA.frustum.pointInFrustum(x + width, y + height, 0);
	}
}

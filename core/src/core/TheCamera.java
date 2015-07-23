package core;

import misc.Globals;
import misc.IUpdate;

import com.badlogic.gdx.graphics.OrthographicCamera;

import entity.special.Player;

public final class TheCamera implements IUpdate {

	public static final int NUM_TILES_PER_SCREEN_WIDTH = 18;
	
	private static TheCamera instance; 
	
	private final OrthographicCamera _camera = new OrthographicCamera();
	
	public static TheCamera getInstance() {
		if(instance == null) {
			instance = new TheCamera();
		}
		
		return instance;
	}
	
	private TheCamera() {
		// This will get adjusted when the window is resized.
		// 50 is used as a base that Box2D can handle efficiently.
		_camera.setToOrtho(true, 50, 50);
	}

	@Override
	public boolean update() {
		Player player = Globals.getPlayer();
		if(player.isRespawning()) {
			return false;
		}
		
		_camera.position.x = player.getCenterX();
		_camera.position.y = player.getCenterY();
		
		_camera.update();
		
		return false;
	}

	@Override
	public void done() {
	}
	
	public void resizeViewport(float screenWidth, float screenHeight) {
		// Adjust the camera viewport height, while keeping the width set at 100.
		_camera.viewportHeight = (_camera.viewportWidth / screenWidth) * screenHeight;
		_camera.update();
	}
	
	public float getViewportWidth() {
		return _camera.viewportWidth;
	}
	
	public float getViewportHeight() {
		return _camera.viewportHeight;
	}
	
	public float getTileMapScale() {
		// Each tile is originally 64 x 64.
		// Each tile should be 1/20 of the screen's width.
		return (_camera.viewportWidth / NUM_TILES_PER_SCREEN_WIDTH) / 64.0f;
	}
	
	public float getTop() {
		return _camera.position.y - (getViewportHeight() / 2);
	}
	
	public float getBottom() {
		return _camera.position.y + (getViewportHeight() / 2);
	}
	
	public float getLeft() {
		return _camera.position.x + (getViewportWidth() / 2);
	}
	
	public float getRight() {
		return _camera.position.x + (getViewportWidth() / 2);
	}
	
	public float getCenterX() {
		return _camera.position.x;
	}
	
	public float getCenterY() {
		return _camera.position.y;
	}
	
	public OrthographicCamera getRawCamera() {
		return _camera;
	}
	
	public boolean isVisible(float x, float y, float width, float height) {
		return _camera.frustum.pointInFrustum(x, y, 0) ||
			   _camera.frustum.pointInFrustum(x + width, y, 0) || 
			   _camera.frustum.pointInFrustum(x, y + height, 0) ||
			   _camera.frustum.pointInFrustum(x + width, y + height, 0);
	}
}

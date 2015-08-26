package background;

import misc.Globals;
import misc.IRender;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class ParallaxBackground implements IRender {

	protected final Vector2 _pos = new Vector2();
	protected final Vector2 _cachedCameraPos = new Vector2();
	protected final Matrix4 _cachedCameraMatrix = new Matrix4();

	protected Array<ParallaxLayer> _layers = new Array<ParallaxLayer>();
	
	public ParallaxBackground() {
		
	}
	
	public ParallaxBackground(ParallaxLayer ... layers) {
		for(ParallaxLayer layer : layers) {
			_layers.add(layer);
		}
	}

	@Override
	public void render(SpriteBatch spriteBatch) {
		for(ParallaxLayer layer : _layers) {
			_adjustCamera(spriteBatch, layer.getParallaxRatio());
			
			TextureRegion texture = layer.getTexture();
			_setStartPosition(texture);
			
			float width = texture.getRegionWidth();
			float height = texture.getRegionHeight();
			float cameraRight = Globals.getCamera().getRight();
			float cameraBottom = Globals.getCamera().getBottom();
			
			int numXRepeats = MathUtils.ceil(Math.abs(_pos.x - cameraRight) / width);
			int numYRepeats = MathUtils.ceil(Math.abs(_pos.y - cameraBottom) / height);		
			for(float x = _pos.x; x < _pos.x + (numXRepeats * width); x += width) {
				for(float y = _pos.y; y < _pos.y + (numYRepeats * height); y += height) {
					spriteBatch.draw(texture, x, y);
				}
			}
			
			_resetCamera(spriteBatch);
		}
	}
	
	public void addLayer(ParallaxLayer layer) {
		_layers.add(layer);
	}
	
	protected void _adjustCamera(SpriteBatch spriteBatch, float parallaxRatio) {
		OrthographicCamera camera = Globals.getCamera().getRawCamera();
		_cachedCameraMatrix.set(camera.combined);
		_cachedCameraPos.set(camera.position.x, camera.position.y);		
		camera.position.set(_cachedCameraPos.x * parallaxRatio, _cachedCameraPos.y * parallaxRatio, camera.position.z);
		camera.update();	
		
		spriteBatch.setProjectionMatrix(camera.combined);
	}
	
	protected void _resetCamera(SpriteBatch spriteBatch) {
		OrthographicCamera camera = Globals.getCamera().getRawCamera();
		camera.combined.set(_cachedCameraMatrix);
		camera.position.set(_cachedCameraPos.x, _cachedCameraPos.y, camera.position.z);
		camera.update();	
		
		spriteBatch.setProjectionMatrix(camera.combined);
	}
	
	protected void _setStartPosition(TextureRegion texture) {
		float width = texture.getRegionWidth();
		float height = texture.getRegionHeight();
		float cameraLeft = Globals.getCamera().getLeft();
		float cameraTop = Globals.getCamera().getTop();

		float x = MathUtils.floor(cameraLeft / width) * width;	
		float y = MathUtils.floor(cameraTop / height) * height;		
		_pos.set(x, y);
	}
}

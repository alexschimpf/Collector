package background;

import misc.Globals;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ParallaxLayer {

	protected float _parallaxRatio;
	protected TextureRegion _texture;
	
	public ParallaxLayer(String textureKey, float parallaxRatio) {
		_texture = Globals.getTextureManager().getImageTexture(textureKey);
		_parallaxRatio = parallaxRatio;
	}

	public float getParallaxRatio() {
		return _parallaxRatio;
	}
	
	public TextureRegion getTexture() {
		return _texture;
	}
}

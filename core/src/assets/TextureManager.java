package assets;

import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public final class TextureManager {

	private static TextureManager instance;
	
	private final TextureAtlas _atlas = new TextureAtlas(Gdx.files.internal("game.atlas"));
	private final HashMap<String, AtlasRegion> _imageTextureMap = new HashMap<String, AtlasRegion>();
	private final HashMap<String, Array<AtlasRegion>> _animationTexturesMap = new HashMap<String, Array<AtlasRegion>>();
	
	public static TextureManager getInstance() {
		if(instance == null) {
			instance = new TextureManager();
		}
		
		return instance;
	}
	
	private TextureManager() {		
		for(AtlasRegion region : _atlas.getRegions()) {
			String name = region.name;
			Array<AtlasRegion> regions = _atlas.findRegions(name);
			if(!_animationTexturesMap.containsKey(name)) {
				_animationTexturesMap.put(name, regions);
			}
			
			String imageKey = region.name + (region.index > 0 ? "_" + region.index : "");
			_imageTextureMap.put(imageKey, region);
		}
	}
	
	public Array<AtlasRegion> getAnimationTextures(String key) {
		return _animationTexturesMap.get(key);
	}
	
	public TextureRegion getImageTexture(String key) {
		return _imageTextureMap.get(key);
	}
	
	public TextureRegion getImageTexture(String key, int index) {
		return getImageTexture(key + "_" + index);
	}
	
	public Sprite getSprite(String key) {
		AtlasRegion region = _imageTextureMap.get(key);
		if(region == null) {
			return null;
		}
		
		Sprite sprite = new Sprite(region);
		sprite.setFlip(false, true);
		
		return sprite;
	}
}

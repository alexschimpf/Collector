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
	
	private final TextureAtlas ATLAS = new TextureAtlas(Gdx.files.internal("game.atlas"));
	private final HashMap<String, AtlasRegion> IMAGE_TEXTURE_MAP = new HashMap<String, AtlasRegion>();
	private final HashMap<String, Array<AtlasRegion>> ANIMATION_TEXTURES_MAP = new HashMap<String, Array<AtlasRegion>>();
	
	public static TextureManager getInstance() {
		if(instance == null) {
			instance = new TextureManager();
		}
		
		return instance;
	}
	
	private TextureManager() {		
		for(AtlasRegion region : ATLAS.getRegions()) {
			String name = region.name;
			Array<AtlasRegion> regions = ATLAS.findRegions(name);
			if(!ANIMATION_TEXTURES_MAP.containsKey(name)) {
				ANIMATION_TEXTURES_MAP.put(name, regions);
			}
			
			String imageKey = region.name + (region.index > 0 ? "_" + region.index : "");
			IMAGE_TEXTURE_MAP.put(imageKey, region);
		}
	}
	
	public Array<AtlasRegion> getAnimationTextures(String key) {
		return ANIMATION_TEXTURES_MAP.get(key);
	}
	
	public TextureRegion getImageTexture(String key) {
		return IMAGE_TEXTURE_MAP.get(key);
	}
	
	public TextureRegion getImageTexture(String key, int index) {
		return getImageTexture(key + "_" + index);
	}
	
	public Sprite getSprite(String key) {
		AtlasRegion region = IMAGE_TEXTURE_MAP.get(key);
		if(region == null) {
			return null;
		}
		
		Sprite sprite = new Sprite(region);
		sprite.setFlip(false, true);
		
		return sprite;
	}
}

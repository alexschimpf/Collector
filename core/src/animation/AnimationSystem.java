package animation;

import java.util.HashMap;

import misc.Globals;
import misc.IRender;
import misc.IUpdate;
import animation.Animation.State;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public final class AnimationSystem implements IRender, IUpdate, IAnimate {
	
	private final HashMap<String, Animation> ANIMATION_MAP  = new HashMap<String, Animation>();

	private final Sprite DEFAULT_SPRITE = new Sprite();
	
	private boolean useDefault = false;
	private Animation animation;
	
	public AnimationSystem() {	
	}
 	
	public AnimationSystem(TextureRegion defaultRegion, float width, float height) {
		if(defaultRegion != null) {
			DEFAULT_SPRITE.setRegion(defaultRegion);
			DEFAULT_SPRITE.setSize(width, height);
		}
	}
	
	public AnimationSystem(Animation[] animations, TextureRegion defaultRegion) {
		HashMap<String, Integer> suffixMap  = new HashMap<String, Integer>();
		for(Animation animation : animations) {
			String key = animation.getKey();
			if(ANIMATION_MAP.containsKey(key)) {
				int suffix = suffixMap.get(key);
				animation.setKey(key + "_" + suffix);
				ANIMATION_MAP.put(key + "_" + suffix, animation);
				suffixMap.put(key, suffix + 1);
			} else {
				ANIMATION_MAP.put(key, animation);
				suffixMap.put(key, 2);
			}
		}
		
		animation = animations[0];
		
		if(defaultRegion != null) {
			DEFAULT_SPRITE.setRegion(defaultRegion);
			DEFAULT_SPRITE.setSize(animation.getWidth(), animation.getHeight());
		}
	}
	
	@Override
	public boolean update() {
		animation.update();
		
		if(animation.isFinished() && DEFAULT_SPRITE.getTexture() != null) {
			useDefault = true;
		}
		
		return false;
	}

	@Override
	public void done() {

	}

	@Override
	public void render(SpriteBatch spriteBatch) {
		animation.render(spriteBatch);
	}
	
	/**
	 * This should only be used if this animation has a unique key.
	 */
	public void addAnimation(Animation animation) {
		ANIMATION_MAP.put(animation.getKey(), animation);
		
		if(this.animation == null) {
			this.animation = animation;
		}
	}
	
	/**
	 * This should only be used if this animation has a unique key.
	 */
	public void removeAnimation(Animation animation) {
		ANIMATION_MAP.remove(animation.getKey());
	}
	
	public void addAnimation(String key, Animation animation) {
		animation.setKey(key);
		ANIMATION_MAP.put(key, animation);
		
		if(this.animation == null) {
			this.animation = animation;
		}
	}
	
	public void removeAnimation(String key) {
		ANIMATION_MAP.remove(key);
	}
	
	public void setDefaultSprite(String imageKey, float width, float height) {
		TextureRegion defaultRegion = Globals.getImageTexture(imageKey);
		DEFAULT_SPRITE.setRegion(defaultRegion);
		DEFAULT_SPRITE.setSize(width, height);
	}
	
	@Override
	public Sprite getSprite() {
		if(useDefault) {
			return DEFAULT_SPRITE;
		}
		
		return animation.getSprite();
	}
	
	public Animation getAnimation() {
		return animation;
	}
	
	public String getAnimationKey() {
		return animation.getKey();
	}

	public void flipSprite(boolean hor, boolean vert) {
		if(useDefault) {
			DEFAULT_SPRITE.setFlip(hor, vert);
		} else {
			animation.flipSprite(hor, vert);
		}
	}
	
	public void switchToDefault() {
		stop();
		useDefault = true;
	}
	
	public void switchAnimation(String key, boolean pauseCurrent, boolean playOnSwitch) {
		useDefault = false;
		
		if(pauseCurrent) {
			animation.pause();
		}
		
		animation = ANIMATION_MAP.get(key);
		if(playOnSwitch) {
			animation.play();
		}
	}
	
	@Override
	public void play() {
		animation.play();
	}

	@Override
	public void resume() {
		animation.resume();
	}

	@Override
	public void pause() {
		animation.pause();
	}

	@Override
	public void stop() {
		animation.stop();
	}

	@Override
	public State getState() {
		return animation.getState();
	}

	@Override
	public boolean isPlaying() {
		return animation.isPlaying();
	}

	@Override
	public boolean isPaused() {
		return animation.isPaused();
	}

	@Override
	public boolean isFinished() {
		return animation.isFinished();
	}
}

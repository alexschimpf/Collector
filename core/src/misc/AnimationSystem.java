package misc;

import java.util.HashMap;

import misc.Animation.State;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public final class AnimationSystem implements IRender, IUpdate, IAnimate {
	
	private final HashMap<String, Animation> ANIMATION_MAP  = new HashMap<String, Animation>();

	private Animation animation;
	
	public AnimationSystem() {	
	}
	
	public AnimationSystem(Animation[] animations) {
		HashMap<String, Integer> suffixMap  = new HashMap<String, Integer>();
		for(Animation animation : animations) {
			String key = animation.getKey();
			if(ANIMATION_MAP.containsKey(key)) {
				int suffix = suffixMap.get(key);
				ANIMATION_MAP.put(key + "_" + suffix, animation);
				suffixMap.put(key, suffix + 1);
			} else {
				ANIMATION_MAP.put(key, animation);
				suffixMap.put(key, 2);
			}
		}
		
		animation = animations[0];
	}
	
	@Override
	public boolean update() {
		animation.update();
		
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
	}
	
	/**
	 * This should only be used if this animation has a unique key.
	 */
	public void removeAnimation(Animation animation) {
		ANIMATION_MAP.remove(animation.getKey());
	}
	
	public void addAnimation(String key, Animation animation) {
		ANIMATION_MAP.put(key, animation);
	}
	
	public void removeAnimation(String key) {
		ANIMATION_MAP.remove(key);
	}
	
	public Sprite getSprite() {
		return animation.getSprite();
	}
	
	public Animation getAnimation() {
		return animation;
	}
	
	public String getAnimationKey() {
		return animation.getKey();
	}
	
	public void switchAnimation(String key, boolean pauseCurrent, boolean playOnSwitch) {
		if(pauseCurrent) {
			animation.pause();
		}
		
		animation = ANIMATION_MAP.get(key);
		if(playOnSwitch) {
			// TODO: Do I need to reposition the new animation?
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

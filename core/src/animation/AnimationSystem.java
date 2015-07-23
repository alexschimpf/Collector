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
	
	private final HashMap<String, Animation> _animationMap = new HashMap<String, Animation>();
	private final Sprite _defaultSprite = new Sprite();
	
	private boolean _useDefault = false;
	private Animation _animation;
	
	public AnimationSystem() {	
	}
 	
	public AnimationSystem(TextureRegion defaultRegion, float width, float height) {
		if(defaultRegion != null) {
			_defaultSprite.setRegion(defaultRegion);
			_defaultSprite.setSize(width, height);
		}
	}
	
	public AnimationSystem(Animation[] animations, TextureRegion defaultRegion) {
		HashMap<String, Integer> suffixMap  = new HashMap<String, Integer>();
		for(Animation animation : animations) {
			String key = animation.getKey();
			if(_animationMap.containsKey(key)) {
				int suffix = suffixMap.get(key);
				animation.setKey(key + "_" + suffix);
				_animationMap.put(key + "_" + suffix, animation);
				suffixMap.put(key, suffix + 1);
			} else {
				_animationMap.put(key, animation);
				suffixMap.put(key, 2);
			}
		}
		
		_animation = animations[0];
		
		if(defaultRegion != null) {
			_defaultSprite.setRegion(defaultRegion);
			_defaultSprite.setSize(_animation.getWidth(), _animation.getHeight());
		}
	}
	
	@Override
	public boolean update() {
		_animation.update();
		
		if(_animation.isFinished() && _defaultSprite.getTexture() != null) {
			_useDefault = true;
		}
		
		return false;
	}

	@Override
	public void done() {

	}

	@Override
	public void render(SpriteBatch spriteBatch) {
		_animation.render(spriteBatch);
	}
	
	/**
	 * This should only be used if this animation has a unique key.
	 */
	public void addAnimation(Animation animation) {
		_animationMap.put(animation.getKey(), animation);
		
		if(this._animation == null) {
			this._animation = animation;
		}
	}
	
	/**
	 * This should only be used if this animation has a unique key.
	 */
	public void removeAnimation(Animation animation) {
		_animationMap.remove(animation.getKey());
	}
	
	public void addAnimation(String key, Animation animation) {
		animation.setKey(key);
		_animationMap.put(key, animation);
		
		if(this._animation == null) {
			this._animation = animation;
		}
	}
	
	public void removeAnimation(String key) {
		_animationMap.remove(key);
	}
	
	public void setDefaultSprite(String imageKey, float width, float height) {
		TextureRegion defaultRegion = Globals.getImageTexture(imageKey);
		_defaultSprite.setRegion(defaultRegion);
		_defaultSprite.setSize(width, height);
	}
	
	@Override
	public Sprite getSprite() {
		if(_useDefault) {
			return _defaultSprite;
		}
		
		return _animation.getSprite();
	}
	
	public Animation getAnimation() {
		return _animation;
	}
	
	public String getAnimationKey() {
		return _animation.getKey();
	}

	public void flipSprite(boolean hor, boolean vert) {
		if(_useDefault) {
			_defaultSprite.setFlip(hor, vert);
		} else {
			_animation.flipSprite(hor, vert);
		}
	}
	
	public void switchToDefault() {
		stop();
		_useDefault = true;
	}
	
	public void switchAnimation(String key, boolean pauseCurrent, boolean playOnSwitch) {
		_useDefault = false;
		
		if(pauseCurrent) {
			_animation.pause();
		}
		
		_animation = _animationMap.get(key);
		if(playOnSwitch) {
			_animation.play();
		}
	}
	
	@Override
	public void play() {
		_animation.play();
	}

	@Override
	public void resume() {
		_animation.resume();
	}

	@Override
	public void pause() {
		_animation.pause();
	}

	@Override
	public void stop() {
		_animation.stop();
	}

	@Override
	public State getState() {
		return _animation.getState();
	}

	@Override
	public boolean isPlaying() {
		return _animation.isPlaying();
	}

	@Override
	public boolean isPaused() {
		return _animation.isPaused();
	}

	@Override
	public boolean isFinished() {
		return _animation.isFinished();
	}
}

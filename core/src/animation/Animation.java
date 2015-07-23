package animation;

import misc.Globals;
import misc.IRender;
import misc.IUpdate;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public final class Animation implements IRender, IUpdate, IAnimate {

	public static enum State {
		PLAYING, PAUSED, STOPPED, FINISHED
	}
	
	private final boolean _loop;
	private final Sprite _sprite = new Sprite();
	
	private float _stateTime = 0;
	private State _state;
	private String _key;
	private com.badlogic.gdx.graphics.g2d.Animation _rawAnimation;
		
	private Animation(Builder builder) {
		_key = builder._animationKey;
		_loop = builder._looped;
		
		Array<AtlasRegion> regions = Globals.getAnimationTextures(builder._animationKey);
		float frameDuration = builder._totalDuration / regions.size;
		_rawAnimation = new com.badlogic.gdx.graphics.g2d.Animation(frameDuration, regions);
		
		_sprite.setPosition(builder._x, builder._y);
		_sprite.setSize(builder._width, builder._height);
		_sprite.setOrigin(builder._width / 2, builder._height / 2);
		
		if(builder._playOnCreate) {
			_state = State.PLAYING;
		} else {
			_state = State.STOPPED;
		}
	}
	
	@Override
	public void render(SpriteBatch spriteBatch) {
		_sprite.draw(spriteBatch);
	}
	
	@Override
	public boolean update() {
		if(isPlaying()) {
			_stateTime += Gdx.graphics.getDeltaTime();
		}
		
		_updateSprite();
		
		if(!_loop && _rawAnimation.isAnimationFinished(_stateTime)) {
			_state = State.FINISHED;
		}
		
		return false;
	}

	@Override
	public void done() {
	}
	
	@Override
	public Sprite getSprite() {
		return _sprite;
	}
	
	public Sprite getSprite(int frame) {
		TextureRegion textureRegion = _rawAnimation.getKeyFrames()[frame];
		_sprite.setRegion(textureRegion);
		_sprite.setFlip(false, true);
		
		return _sprite;
	}
	
	public void flipSprite(boolean hor, boolean vert) {
		_sprite.setFlip(hor, vert);
	}
	
	public float getX() {
		return _sprite.getX();
	}
	
	public float getY() {
		return _sprite.getY();
	}
	
	public float getWidth() {
		return _sprite.getWidth();
	}
	
	public float getHeight() {
		return _sprite.getHeight();
	}
	
	@Override
	public void play() {
		_stateTime = 0;
		_state = State.PLAYING;
	}
	
	@Override
	public void resume() {
		_state = State.PLAYING;
	}
	
	@Override
	public void pause() {
		_state = State.PAUSED;
	}
	
	@Override
	public void stop() {
		_stateTime = 0;
		_state = State.STOPPED;
	}
 
	@Override
	public State getState() {
		return _state;
	}
	
	@Override
	public boolean isPlaying() {
		return _state == State.PLAYING;
	}
	
	@Override
	public boolean isPaused() {
		return _state == State.PAUSED;
	}
	
	public boolean isStopped() {
		return _state == State.STOPPED;
	}
	
	@Override
	public boolean isFinished() {
		return _state == State.FINISHED;
	}
	
	public String getKey() {
		return _key;
	}
	
	public void setKey(String key) {
		this._key = key;
	}
	
	private void _updateSprite() {
		TextureRegion textureRegion = _rawAnimation.getKeyFrame(_stateTime, _loop);
		_sprite.setRegion(textureRegion);
	}
	
	public static class Builder {

		private final String _animationKey;
		private final float _totalDuration; 
		private final float _x;
		private final float _y;
		private final float _width;
		private final float _height;

		private boolean _looped = false;
		private boolean _playOnCreate = false;
		
		public Builder(String animationKey, Vector2 pos, Vector2 size, float totalDuration) {
			this._animationKey = animationKey;
			this._totalDuration = totalDuration;
			this._x = pos.x;
			this._y = pos.y;
			this._width = size.x;
			this._height = size.y;
		}
		
		public Builder loop(boolean loop) {
			_looped = loop;
			return this;
		}
		
		public Builder playOnCreate(boolean playOnCreate) {
			this._playOnCreate = playOnCreate;
			return this;
		}
		
		public Animation build() {
			return new Animation(this);
		}
	}
}

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
	
	private final Sprite SPRITE = new Sprite();
	
	private float stateTime = 0;
	private State state;
	private final boolean loop;
	private String key;
	private com.badlogic.gdx.graphics.g2d.Animation rawAnimation;
		
	private Animation(Builder builder) {
		key = builder.animationKey;
		loop = builder.loop;
		
		Array<AtlasRegion> regions = Globals.getTextureManager().getAnimationTextures(builder.animationKey);
		float frameDuration = builder.totalDuration / regions.size;
		rawAnimation = new com.badlogic.gdx.graphics.g2d.Animation(frameDuration, regions);
		
		SPRITE.setPosition(builder.x, builder.y);
		SPRITE.setSize(builder.width, builder.height);
		SPRITE.setOrigin(builder.width / 2, builder.height / 2);
		
		if(builder.playOnCreate) {
			state = State.PLAYING;
		} else {
			state = State.STOPPED;
		}
	}
	
	@Override
	public void render(SpriteBatch spriteBatch) {
		SPRITE.draw(spriteBatch);
	}
	
	@Override
	public boolean update() {
		if(isPlaying()) {
			stateTime += Gdx.graphics.getDeltaTime();
		}
		
		updateSprite();
		
		if(!loop && rawAnimation.isAnimationFinished(stateTime)) {
			state = State.FINISHED;
		}
		
		return false;
	}

	@Override
	public void done() {
	}
	
	@Override
	public Sprite getSprite() {
		return SPRITE;
	}
	
	public Sprite getSprite(int frame) {
		TextureRegion textureRegion = rawAnimation.getKeyFrames()[frame];
		SPRITE.setRegion(textureRegion);
		SPRITE.setFlip(false, true);
		
		return SPRITE;
	}
	
	public void flipSprite(boolean hor, boolean vert) {
		SPRITE.setFlip(hor, vert);
	}
	
	public float getX() {
		return SPRITE.getX();
	}
	
	public float getY() {
		return SPRITE.getY();
	}
	
	public float getWidth() {
		return SPRITE.getWidth();
	}
	
	public float getHeight() {
		return SPRITE.getHeight();
	}
	
	@Override
	public void play() {
		stateTime = 0;
		state = State.PLAYING;
	}
	
	@Override
	public void resume() {
		state = State.PLAYING;
	}
	
	@Override
	public void pause() {
		state = State.PAUSED;
	}
	
	@Override
	public void stop() {
		stateTime = 0;
		state = State.STOPPED;
	}
 
	@Override
	public State getState() {
		return state;
	}
	
	@Override
	public boolean isPlaying() {
		return state == State.PLAYING;
	}
	
	@Override
	public boolean isPaused() {
		return state == State.PAUSED;
	}
	
	public boolean isStopped() {
		return state == State.STOPPED;
	}
	
	@Override
	public boolean isFinished() {
		return state == State.FINISHED;
	}
	
	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	private void updateSprite() {
		TextureRegion textureRegion = rawAnimation.getKeyFrame(stateTime, loop);
		SPRITE.setRegion(textureRegion);
	}
	
	public static class Builder {

		private final String animationKey;
		private final float totalDuration; 
		private final float x;
		private final float y;
		private final float width;
		private final float height;

		private boolean loop = false;
		private boolean playOnCreate = false;
		
		public Builder(String animationKey, Vector2 pos, Vector2 size, float totalDuration) {
			this.animationKey = animationKey;
			this.totalDuration = totalDuration;
			this.x = pos.x;
			this.y = pos.y;
			this.width = size.x;
			this.height = size.y;
		}
		
		public Builder loop(boolean loop) {
			this.loop = loop;
			return this;
		}
		
		public Builder playOnCreate(boolean playOnCreate) {
			this.playOnCreate = playOnCreate;
			return this;
		}
		
		public Animation build() {
			return new Animation(this);
		}
	}
}

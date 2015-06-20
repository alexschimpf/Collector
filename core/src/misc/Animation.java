package misc;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;

public final class Animation implements IRender, IUpdate {

	public static enum State {
		PLAYING,
		PAUSED,
		STOPPED,
		FINISHED
	}
	
	private final Sprite SPRITE = new Sprite();
	
	private float stateTime = 0;
	private State state;
	private final boolean loop;
	private com.badlogic.gdx.graphics.g2d.Animation rawAnimation;
		
	private Animation(AnimationBuilder builder) {
		if(builder.playOnCreate) {
			state = State.PLAYING;
		} else {
			state = State.STOPPED;
		}
		
		loop = builder.loop;
		
		Array<AtlasRegion> regions = Globals.getTextureManager().getAnimationTextures(builder.animationKey);
		int numFrames = regions.size;
		float frameDuration = builder.totalDuration / numFrames;		
		rawAnimation = new com.badlogic.gdx.graphics.g2d.Animation(frameDuration, regions);
		
		SPRITE.setPosition(builder.x, builder.y);
		SPRITE.setSize(builder.width, builder.height);
		SPRITE.setOrigin(builder.width / 2, builder.height / 2);
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
		
		return !loop && rawAnimation.isAnimationFinished(stateTime);
	}

	@Override
	public void done() {
		state = State.FINISHED;
	}
	
	public Sprite getSprite() {
		return SPRITE;
	}
	
	public Sprite getSprite(int frame) {
		TextureRegion textureRegion = rawAnimation.getKeyFrames()[frame];
		SPRITE.setRegion(textureRegion);
		
		return SPRITE;
	}
	
	public void play() {
		stateTime = 0;
		state = State.PLAYING;
	}
	
	public void resume() {
		state = State.PLAYING;
	}
	
	public void pause() {
		state = State.PAUSED;
	}
	
	public void stop() {
		stateTime = 0;
		state = State.STOPPED;
	}
 
	public State getState() {
		return state;
	}
	
	public boolean isPlaying() {
		return state == State.PLAYING;
	}
	
	public boolean isPaused() {
		return state == State.PAUSED;
	}
	
	public boolean isStopped() {
		return state == State.STOPPED;
	}
	
	public boolean isFinished() {
		return state == State.FINISHED;
	}
	
	private void updateSprite() {
		TextureRegion textureRegion = rawAnimation.getKeyFrame(stateTime, loop);
		SPRITE.setRegion(textureRegion);
	}
	
	public static class AnimationBuilder {
		
		// Required
		private final String animationKey;
		private final float totalDuration; 
		private final float x;
		private final float y;
		private final float width;
		private final float height;
		
		// Optional
		private boolean loop = false;
		private boolean playOnCreate = false;
		
		public AnimationBuilder(String animationKey, float x, float y, float width, float height, 
				                float totalDuration) {
			this.animationKey = animationKey;
			this.totalDuration = totalDuration;
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}
		
		public AnimationBuilder loop(boolean loop) {
			this.loop = loop;
			return this;
		}
		
		public AnimationBuilder playOnCreate(boolean playOnCreate) {
			this.playOnCreate = playOnCreate;
			return this;
		}
		
		public Animation build() {
			return new Animation(this);
		}
	}
}

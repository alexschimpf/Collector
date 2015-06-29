package particle;

import java.util.Iterator;

import misc.Globals;
import misc.IRender;
import misc.IUpdate;
import misc.Utils;
import misc.Vector2Pool;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public final class ParticleEffect implements IRender, IUpdate {
	
	private final Array<Particle> PARTICLES = new Array<Particle>();
	
	private ParticleEffect(Builder builder) {
		float numParticles = MathUtils.round(getRandomFromRange(builder.minMaxParticles));
		for(int i = 0; i < numParticles; i++) {
			String imageKey = builder.imageKey;
			float size = getRandomFromRange(builder.minMaxSize);
			
			float duration = getRandomFromRange(builder.minMaxDuration);
			float vx = getRandomFromRange(builder.minVelocity.x, builder.maxVelocity.x, builder.velocitySplits.x);
			float vy = getRandomFromRange(builder.minVelocity.y, builder.maxVelocity.y, builder.velocitySplits.y);
			
			float offsetX = getRandomFromRange(builder.minOffsets.x, builder.maxOffsets.x, 0);
			offsetX *= Utils.choose(1, -1);
			float offsetY = getRandomFromRange(builder.minOffsets.y, builder.maxOffsets.y, 0);
			offsetY *= Utils.choose(1, -1);			
			builder.pos.add(offsetX, offsetY);
			
			Particle particle = new Particle.Builder(imageKey, builder.pos.x, builder.pos.y, size, vx, vy, duration)
			.fadeIn(builder.fadeIn)
			.keepProportions(builder.keepProportions)
			.sizeScale(builder.sizeScale.x, builder.sizeScale.y)
			.startEndAlphas(builder.startEndAlphas.x, builder.startEndAlphas.y)
			.startEndColors(builder.startColor, builder.endColor)
			.build();
			
			PARTICLES.add(particle);
		}
	}
	
	@Override
	public void render(SpriteBatch spriteBatch) {
		for(Particle particle : PARTICLES) {
			particle.render(spriteBatch);
		}
	}
	
	@Override
	public boolean update() {
		Iterator<Particle> particleIter = PARTICLES.iterator();
		while(particleIter.hasNext()) {
			Particle particle = particleIter.next();
			if(particle.update()) {
				particle.done();
				
				particleIter.remove();
			}
		}
		
		return PARTICLES.size == 0;
	}

	@Override
	public void done() {

	}
	
	public void start() {
		Globals.getGameScreen().addParticleEffect(this);
	}
	
	public void setTint(float r, float g, float b) {
		for(Particle particle : PARTICLES) {
			particle.setTint(r, g, b);
		}
	}

	private float getRandomFromRange(Vector2 range) {
		return MathUtils.random(range.x, range.y);
	}

	private float getRandomFromRange(float a, float b, float split) {
		if(split == 0) {
			return MathUtils.random(a, b);
		}
		
		return Utils.choose(MathUtils.random(a, -split), MathUtils.random(split, b));
	}
	
	public static class Builder {
		
		public final String imageKey;
		public final Vector2 pos;
		public final Vector2 minMaxSize;
		public final Vector2 minMaxDuration;
		public final Vector2 minMaxParticles;
		public final Vector2 minVelocity;
		public final Vector2 maxVelocity;
		
		private boolean fadeIn = false;
		private boolean keepProportions = true;
		private Color startColor = null;
		private Color endColor = null;
		private Vector2 startEndAlphas = new Vector2(1, 0);
		private Vector2 sizeScale = new Vector2(1, 1);
		private Vector2 velocitySplits = new Vector2(0, 0);
		private Vector2 minOffsets = new Vector2(0, 0);
		private Vector2 maxOffsets = new Vector2(0, 0);
		
		/**
		 * All Vector2 parameters should be obtained from the Vector2Pool instance.
		 * They will be freed automatically, so the caller does not have to.
		 */
		public Builder(String imageKey, Vector2 pos, Vector2 minMaxSize, Vector2 minVelocity, Vector2 maxVelocity,
                	   Vector2 minMaxDuration, Vector2 minMaxParticles) {
			this.imageKey = imageKey;
			this.pos = pos;
			this.minMaxSize = minMaxSize;
			this.minVelocity = minVelocity;
			this.maxVelocity = maxVelocity;
			this.minMaxDuration = minMaxDuration;
			this.minMaxParticles = minMaxParticles;
		}

		public Builder startEndAlphas(float startAlpha, float endAlpha) {
			startEndAlphas.set(startAlpha, endAlpha);
			return this;
		}
		
		public Builder sizeScale(float scaleX, float scaleY) {
			sizeScale.set(scaleX, scaleY);
			return this;
		}
		
		public Builder minOffsets(float minOffsetX, float minOffsetY) {
			minOffsets.set(minOffsetX, minOffsetY);
			return this;
		}

		public Builder maxOffsets(float maxOffsetX, float maxOffsetY) {
			maxOffsets.set(maxOffsetX, maxOffsetY);
			return this;
		}
		
		public Builder vSplits(float vxSplit, float vySplit) {
			velocitySplits.set(vxSplit, vySplit);
			return this;
		}
		
		public Builder startEndColors(Color startColor, Color endColor) {
			this.startColor = startColor;
			this.endColor = endColor;
			return this;
		}
		
		public Builder fadeIn(boolean fadeIn) {
			this.fadeIn = fadeIn;
			return this;
		}
		
		public Builder keepProportions(boolean keepProportions) {
			this.keepProportions = keepProportions;
			return this;
		}

		public ParticleEffect build() {
			ParticleEffect particleEffect = new ParticleEffect(this);
			
			freeResources();
			
			return particleEffect;
		}
		
		private void freeResources() {
			Vector2Pool pool = Globals.getVector2Pool();
			pool.free(pos);
			pool.free(this.maxOffsets);
			pool.free(this.maxVelocity);
			pool.free(this.minMaxDuration);
			pool.free(this.minMaxParticles);
			pool.free(this.minMaxSize);
			pool.free(this.minOffsets);
			pool.free(this.minVelocity);
			pool.free(this.pos);
			pool.free(this.startEndAlphas);
			pool.free(this.velocitySplits);
			pool.free(this.sizeScale);
		}
	}
}

package particle;

import misc.Globals;
import misc.IRender;
import misc.IUpdate;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.TimeUtils;

public final class Particle implements IRender, IUpdate {

	private static final Pool<Particle> PARTICLE_POOL = new Pool<Particle>() {
	    @Override
	    protected Particle newObject() {
	        return new Particle();
	    }
	};
	
	private float vx;
	private float vy;
	private float startAlpha;
	private float endAlpha;
	private float duration;
	private Color endColor;
	private boolean fadeIn;
	private long startTime;
	private Sprite sprite;
	
	public Particle() {
	}
	
	public void set(Builder builder) {
		vx = builder.vx;
		vy = builder.vy;
		duration = builder.duration;		
		sprite = Globals.getTextureManager().getSprite(builder.imageKey);
		sprite.setPosition(builder.x, builder.y);
		sprite.setSize(builder.size, builder.size);
		
		if(builder.startColor != null) {
			sprite.setColor(builder.startColor);
		}
		
		startAlpha = builder.startAlpha;
		endAlpha = builder.endAlpha;
		
		fadeIn = builder.fadeIn;
		endColor = builder.endColor;
				
		startTime = TimeUtils.millis();
	}
	
	@Override
	public void render(SpriteBatch spriteBatch) {
		sprite.draw(spriteBatch);
	}
	
	@Override
	public boolean update() {
		long age = TimeUtils.timeSinceMillis(startTime);
		float ageDurationRatio = Math.min(1, age / duration);
		
		float x = sprite.getX();
		float y = sprite.getY();
		sprite.setPosition(x + (Gdx.graphics.getDeltaTime() * vx), y + (Gdx.graphics.getDeltaTime() * vy));
		
		if(fadeIn) {
			float fadeTimeRatio = Math.min(1, age / (duration / 2));
			float alpha = fadeTimeRatio * startAlpha;
			alpha = alpha < 0 ? 0 : (alpha > 1 ? 1 : alpha);
			sprite.setAlpha(alpha);
			
			if(age >= duration / 2) {
				fadeIn = false;
				startTime = TimeUtils.millis();
				age = 0;
				duration /= 2;
			}
		} else {
			float alpha = startAlpha + ((endAlpha - startAlpha) * ageDurationRatio);
			alpha = alpha < 0 ? 0 : (alpha > 1 ? 1 : alpha);

			sprite.setAlpha(alpha);
		}
		
		Color color = sprite.getColor();
		if(color != null && endColor != null && !color.equals(endColor)) {
			float dr = endColor.r - color.r;
			float dg = endColor.g - color.g;
			float db = endColor.b - color.b;			
			float r = color.r + (ageDurationRatio * dr);
			float g = color.g + (ageDurationRatio * dg);
			float b = color.b + (ageDurationRatio * db);		
			r = r < 0 ? 0 : (r > 1 ? 1 : r);
			g = g < 0 ? 0 : (g > 1 ? 1 : g);
			b = b < 0 ? 0 : (b > 1 ? 1 : b);
			
			sprite.setColor(r, g, b, sprite.getColor().a);
		}

		return age > duration;
	}

	@Override
	public void done() {
		PARTICLE_POOL.free(this);
	}
	
	public static class Builder {
				
		public final String imageKey;
		public final float x;
		public final float y;
		public final float size;
		public final float vx;
		public final float vy;
		public final float duration;

		private boolean fadeIn = false;
		private float startAlpha = 1;
		private float endAlpha = 0;
		private Color startColor = null;
		private Color endColor = null;

		public Builder(String imageKey, float x, float y, float size, float vx, float vy, float duration) {
			this.imageKey = imageKey;
			this.x = x;
			this.y = y;
			this.size = size;
			this.vx = vx;
			this.vy = vy;
			this.duration = duration;
		}
		
		public Particle build() {
			Particle particle = PARTICLE_POOL.obtain();
			particle.set(this);
			
			return particle;
		}
		
		public Builder fadeIn(boolean fadeIn) {
			this.fadeIn = fadeIn;
			return this;
		}
		
		public Builder startEndColors(Color startColor, Color endColor) {
			this.startColor = startColor;
			this.endColor = endColor;
			return this;
		}
		
		public Builder startEndAlphas(float startAlpha, float endAlpha) {
			this.startAlpha = startAlpha;
			this.endAlpha = endAlpha;
			return this;
		}
	}
}

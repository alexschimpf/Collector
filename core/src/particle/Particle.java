package particle;

import misc.Globals;
import misc.IRender;
import misc.IUpdate;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.TimeUtils;

public final class Particle implements IRender, IUpdate, Poolable {

	private static final Pool<Particle> PARTICLE_POOL = new Pool<Particle>() {
	    @Override
	    protected Particle newObject() {
	        return new Particle();
	    }
	};
	
	private float startX;
	private float startY;
	private float startWidth;
	private float startHeight;
	private float vx;
	private float vy;
	private float startAlpha;
	private float endAlpha;
	private float scaleX;
	private float scaleY;
	private float duration;
	private Color startColor;
	private Color endColor;
	private boolean fadeIn;
	private boolean keepCenter;
	private long startTime;
	private Sprite sprite;
	
	public Particle() {
	}
	
	public void set(Builder builder) {
		vx = builder.vx;
		vy = builder.vy;
		duration = builder.duration;		
		sprite = Globals.getTextureManager().getSprite(builder.imageKey);
		
		startWidth = builder.size;
		startHeight = builder.size;
		
		if(builder.keepProportions) {
			startHeight = ((float)sprite.getRegionHeight() / (float)sprite.getRegionWidth()) * startWidth;
		} 
		sprite.setSize(startWidth, startHeight);
		
		if(builder.startColor != null) {
			sprite.setColor(builder.startColor);
		}

		startX = builder.x;
		startY = builder.y;
		sprite.setPosition(builder.x - (startWidth / 2), builder.y - (startHeight / 2));
		
		startAlpha = builder.startAlpha;
		endAlpha = builder.endAlpha;
		
		scaleX = builder.scaleX;
		scaleY = builder.scaleY;
		
		fadeIn = builder.fadeIn;
		keepCenter = builder.keepCenter;
		startColor = builder.startColor;
		endColor = builder.endColor;
				
		startTime = TimeUtils.millis();
	}
	
	@Override
	public void render(SpriteBatch spriteBatch) {
		// HACK: Couldn't figure out how alphas were getting set to 1
		//       at start of fade-in.
		long age = TimeUtils.timeSinceMillis(startTime);
		if(fadeIn && age < 50 && sprite.getColor().a > 0.1f) {
			sprite.setAlpha(0);
		}
		
		sprite.draw(spriteBatch);
	}
	
	@Override
	public boolean update() {
		long age = TimeUtils.timeSinceMillis(startTime);
		float ageDurationRatio = Math.min(1, age / duration);
		
		float endWidth = startWidth * scaleX;
		float endHeight = startHeight * scaleY;
		float width = startWidth + ((endWidth - startWidth) * ageDurationRatio);		
		float height = startHeight + ((endHeight - startHeight) * ageDurationRatio);
		sprite.setSize(width, height);
		
		if(keepCenter) {
			float dx = age * vx;
			float dy = age * vy;
			sprite.setPosition(startX - (width / 2) + dx, startY - (height / 2) + dy);
		} else {
			float x = sprite.getX();
			float y = sprite.getY();
			sprite.setPosition(x + (Gdx.graphics.getDeltaTime() * vx), y + (Gdx.graphics.getDeltaTime() * vy));
		}
		
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
		} else if(startAlpha != endAlpha) {
			float alpha = startAlpha + ((endAlpha - startAlpha) * ageDurationRatio);
			alpha = alpha < 0 ? 0 : (alpha > 1 ? 1 : alpha);

			sprite.setAlpha(alpha);
		}
		
		Color currColor = sprite.getColor();
		if(startColor != null && endColor != null && !currColor.equals(endColor)) {
			float dr = endColor.r - startColor.r;
			float dg = endColor.g - startColor.g;
			float db = endColor.b - startColor.b;			
			float r = startColor.r + (ageDurationRatio * dr);
			float g = startColor.g + (ageDurationRatio * dg);
			float b = startColor.b + (ageDurationRatio * db);		
			r = r < 0 ? 0 : (r > 1 ? 1 : r);
			g = g < 0 ? 0 : (g > 1 ? 1 : g);
			b = b < 0 ? 0 : (b > 1 ? 1 : b);
			
			sprite.setColor(r, g, b, currColor.a);
		}

		return age > duration;
	}

	@Override
	public void done() {
		PARTICLE_POOL.free(this);
	}
	
	@Override
	public void reset() {
		startColor = null;
		endColor = null;
		sprite = null;
	}
	
	public void setTint(float r, float g, float b) {
		sprite.setColor(r, g, b, sprite.getColor().a);
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
		private boolean keepCenter = false;
		private boolean keepProportions = true;
		private float startAlpha = 1;
		private float endAlpha = 0;
		private float scaleX = 1;
		private float scaleY = 1;
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
		
		public Builder keepCenter(boolean keepCenter) {
			this.keepCenter = keepCenter;
			return this;
		}
		
		public Builder keepProportions(boolean keepProportions) {
			this.keepProportions = keepProportions;
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
		
		public Builder sizeScale(float scaleX, float scaleY) {
			this.scaleX = scaleX;
			this.scaleY = scaleY;
			return this;
		}
	}
}

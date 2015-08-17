package particle;

import misc.Globals;
import misc.IRender;
import misc.IUpdate;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.TimeUtils;

public class Particle implements IRender, IUpdate, Poolable {

	public float startX;
	public float startY;
	public float startWidth;
	public float startHeight;
	public float vx;
	public float vy;
	public float angularVelocity;
	public float startAlpha;
	public float endAlpha;
	public float scaleX;
	public float scaleY;
	public float duration;
	public Color startColor;
	public Color endColor;
	public boolean fadeIn;
	public boolean keepCenter;
	public long startTime;
	public Sprite sprite;

	public Particle() {		
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
		
		sprite.rotate(Gdx.graphics.getDeltaTime() * angularVelocity);
		
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
	}

	@Override
	public void reset() {
		startTime = TimeUtils.millis();
		startColor = null;
		endColor = null;
		sprite = null;
	}
	
	public boolean isVisible() {
		return Globals.getCamera().isVisible(sprite.getX(), sprite.getY(), sprite.getWidth(), sprite.getHeight());
	}
}

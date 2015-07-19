package particle;

import java.util.Iterator;

import misc.Globals;
import misc.IRender;
import misc.IUpdate;
import misc.Utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.TimeUtils;

public class ParticleEffect implements IRender, IUpdate {

	private static final Pool<Particle> PARTICLE_POOL = new Pool<Particle>() {
	    @Override
	    protected Particle newObject() {
	        return new Particle();
	    }
	};
	
	private final Array<Particle> PARTICLES = new Array<Particle>();
	
	// Required
	public String  imageKey;
	public Vector2 position        = new Vector2();
	public Vector2 minMaxSize      = new Vector2();
	public Vector2 minMaxDuration  = new Vector2();
	public Vector2 minMaxParticles = new Vector2();
	public Vector2 minVelocity     = new Vector2();
	public Vector2 maxVelocity     = new Vector2();
	
	// Optional
	public boolean fadeIn          = false;
	public boolean keepCenter      = false;
	public boolean keepProportions = true;
	public Color   startColor      = null;
	public Color   endColor        = null;
	public Vector2 startEndAlphas  = new Vector2(1, 0);
	public Vector2 sizeScale       = new Vector2(1, 1);
	public Vector2 velocitySplits  = new Vector2(0, 0);
	public Vector2 minOffsets      = new Vector2(0, 0);
	public Vector2 maxOffsets      = new Vector2(0, 0);
	
	public ParticleEffect() {
		
	}
	
	@Override
	public ParticleEffect clone() {
		ParticleEffect particleEffect = new ParticleEffect();
		particleEffect.imageKey = imageKey;
		particleEffect.fadeIn = fadeIn;
		particleEffect.keepCenter = keepCenter;
		particleEffect.keepProportions = keepProportions;
		particleEffect.startColor = startColor;
		particleEffect.endColor = endColor;
		particleEffect.sizeScale = sizeScale;		
		particleEffect.position.set(position);
		particleEffect.minMaxSize.set(minMaxSize);
		particleEffect.minMaxDuration.set(minMaxDuration);
		particleEffect.minMaxParticles.set(minMaxParticles);
		particleEffect.minVelocity.set(minVelocity);
		particleEffect.maxVelocity.set(maxVelocity);
		particleEffect.startEndAlphas.set(startEndAlphas);
		particleEffect.velocitySplits.set(velocitySplits);
		particleEffect.minOffsets.set(minOffsets);
		particleEffect.maxOffsets.set(maxOffsets);
		
		return particleEffect;
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
				PARTICLE_POOL.free(particle);			
				particleIter.remove();
			}
		}
		
		return PARTICLES.size == 0;
	}

	@Override
	public void done() {
	}
	
	public void addToScreen() {
		buildParticles();
		Globals.getGameScreen().addParticleEffect(this);
	}
	
	public void buildParticles() {
		float numParticles = MathUtils.round(Utils.getRandomFromRange(minMaxParticles));
		for(int i = 0; i < numParticles; i++) {
			float offsetX = Utils.getRandomFromRange(minOffsets.x, maxOffsets.x, 0) * Utils.choose(1, -1);
			float offsetY = Utils.getRandomFromRange(minOffsets.y, maxOffsets.y, 0) * Utils.choose(1, -1);	

			Particle particle = PARTICLE_POOL.obtain();
			particle.sprite = Globals.getSprite(imageKey);
			float startWidth = Utils.getRandomFromRange(minMaxSize);
			float startHeight = startWidth;
			if(keepProportions) {
				startHeight = ((float)particle.sprite.getRegionHeight() / (float)particle.sprite.getRegionWidth()) * startWidth;
			}
			particle.sprite.setSize(startWidth, startHeight);
			if(startColor != null) {
				particle.sprite.setColor(startColor);
			}
			
			float startX = position.x + offsetX;
			float startY = position.y + offsetY;
			particle.startX = startX;
			particle.startY = startY;
			particle.sprite.setPosition(startX - (startWidth / 2), startY - (startHeight / 2));
			
			particle.startWidth = startWidth;
			particle.startHeight = startHeight;
			particle.duration = Utils.getRandomFromRange(minMaxDuration);
			particle.startAlpha = startEndAlphas.x;
			particle.endAlpha = startEndAlphas.y;
			particle.startColor = startColor;
			particle.endColor = endColor;
			particle.fadeIn = fadeIn;
			particle.keepCenter = keepCenter;
			particle.scaleX = sizeScale.x;
			particle.scaleY = sizeScale.y;			
			particle.vx = Utils.getRandomFromRange(minVelocity.x, maxVelocity.x, velocitySplits.x);
			particle.vy = Utils.getRandomFromRange(minVelocity.y, maxVelocity.y, velocitySplits.y);			
			particle.startTime = TimeUtils.millis();

			PARTICLES.add(particle);
		}
	}
	
	public void imageKey(String imageKey) {
		this.imageKey = imageKey;
	}
	
	public void position(Float x, Float y) {
		position.set(x, y);
	}
	
	public void minMaxSize(Float x, Float y) {
		minMaxSize.set(x, y);
	}
	
	public void minMaxDuration(Float x, Float y) {
		minMaxDuration.set(x, y);
	}
	
	public void minMaxParticles(Float x, Float y) {
		minMaxParticles.set(x, y);
	}
	
	public void minVelocity(Float x, Float y) {
		minVelocity.set(x, y);
	}
	
	public void maxVelocity(Float x, Float y) {
		maxVelocity.set(x, y);
	}
	
	public void minOffsets(Float x, Float y) {
		minOffsets.set(x, y);
	}
	
	public void maxOffsets(Float x, Float y) {
		maxOffsets.set(x, y);
	}
	
	public void velocitySplits(Float x, Float y) {
		this.velocitySplits.set(x, y);
	}

	public void fadeIn(Boolean fadeIn) {
		this.fadeIn = fadeIn;
	}
	
	public void keepCenter(Boolean keepCenter) {
		this.keepCenter = keepCenter;
	}
	
	public void keepProportions(Boolean keepProportions) {
		this.keepProportions = keepProportions;
	}
	
	public void startColor(Color startColor) {
		this.startColor = startColor;
	}
	
	public void endColor(Color endColor) {
		this.endColor = endColor;
	}
	
	public void print() {
		System.out.println();
		System.out.println("IMAGE_KEY: " + imageKey);    
        System.out.println("POSITION: " + position);     
        System.out.println("MIN_MAX_SIZE: " + minMaxSize);     
        System.out.println("MIN_MAX_DURATION: " + minMaxDuration); 
        System.out.println("MIN_MAX_PARTICLES: " + minMaxParticles); 
        System.out.println("MIN_VELOCITY: " + minVelocity); 
        System.out.println("MAX_VELOCITY: " + maxVelocity);   
        System.out.println("FADE_IN: " + fadeIn); 
        System.out.println("KEEP_CENTER: " + keepCenter);      
        System.out.println("KEEP_PROPORTIONS: " + keepProportions); 
        System.out.println("START_COLOR: " + startColor); 
        System.out.println("END_COLOR: " + endColor);       
        System.out.println("START_END_ALPHAS: " + startEndAlphas);  
        System.out.println("SIZE_SCALE: " + sizeScale); 
        System.out.println("VELOCITY_SPLITS: " + velocitySplits);  
        System.out.println("MIN_OFFSETS: " + minOffsets);  
        System.out.println("MAX_OFFSETS: " + maxOffsets);
        System.out.println();
	} 
}
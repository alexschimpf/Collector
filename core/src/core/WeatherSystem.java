package core;

import java.util.Iterator;

import misc.Globals;
import misc.IRender;
import misc.IUpdate;
import misc.Utils;
import particle.ParticleEffect;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class WeatherSystem implements IRender, IUpdate {
	
	public float TIME_SCALE = 0.02f;
	
	public static WeatherSystem instance;
	
	private final Array<ParticleEffect> CLOUDS = new Array<ParticleEffect>();
	
	private float time = 0;
	private float light = 1;
	
	public static WeatherSystem getInstance() {
		if(instance == null) {
			instance = new WeatherSystem();
		}
		
		return instance;
	}
	
	public WeatherSystem() {
		time = MathUtils.random(0.0f, 1.0f);
		
		tryCreateClouds(true);
	}
	
	@Override
	public void render(SpriteBatch spriteBatch) {
//		for(ParticleEffect cloud : CLOUDS) {
//			cloud.render(spriteBatch);
//		}
	}
	
	@Override
	public boolean update() {
//		time += TIME_SCALE * Gdx.graphics.getDeltaTime();		
//		light = Math.min(Math.max(Math.abs(MathUtils.sin(time)), 0.3f), 1f);
		
		Iterator<ParticleEffect> cloudIter = CLOUDS.iterator();
		while(cloudIter.hasNext()) {
			ParticleEffect cloud = cloudIter.next();
			if(cloud.update()) {
				cloud.done();
				cloudIter.remove();
			}
		}

		tryCreateClouds(false);
		
		return false;
	}

	@Override
	public void done() {

	}
	
	public float getLight() {
		return light;
	}
	
	private void tryCreateClouds(boolean randomFadeIn) {
		GameWorld gameWorld = Globals.getGameWorld();
		while(CLOUDS.size < gameWorld.getWidth() * gameWorld.getHeight() / Globals.getCamera().getViewportWidth() / 20) {
			float screenWidth = Globals.getCamera().getViewportWidth();
			float screenHeight = Globals.getCamera().getViewportHeight();
			float x = MathUtils.random(gameWorld.getLeft() - (screenWidth / 2), gameWorld.getRight());
			float y = MathUtils.random(gameWorld.getTop(), gameWorld.getBottom() - (24 * Globals.getTileSize()));
			ParticleEffect cloud = Globals.getParticleEffectManager().getParticleEffect("cloud", x, y);
			cloud.minMaxSize(screenWidth * 0.8f, screenWidth);
			cloud.fadeIn(randomFadeIn ? Utils.choose(true, false) : true);
            cloud.buildParticles();
			
			CLOUDS.add(cloud);
		}
	}
}

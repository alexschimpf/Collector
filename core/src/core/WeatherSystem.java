package core;

import java.util.Iterator;

import misc.Globals;
import misc.IRender;
import misc.IUpdate;
import misc.Vector2Pool;
import particle.ParticleEffect;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import entity.Player;

public class WeatherSystem implements IRender, IUpdate {
	
	public float TIME_SCALE = 0.02f;
	
	public static WeatherSystem instance;
	
	private final Array<ParticleEffect> CLOUDS = new Array<ParticleEffect>();
	
	private float time = 0;
	private float light = 0;
	
	public static WeatherSystem getInstance() {
		if(instance == null) {
			instance = new WeatherSystem();
		}
		
		return instance;
	}
	
	public WeatherSystem() {
		time = MathUtils.random(0.0f, 1.0f);
		
		tryCreateClouds(false);
	}
	
	@Override
	public void render(SpriteBatch spriteBatch) {
		for(ParticleEffect cloud : CLOUDS) {
			cloud.render(spriteBatch);
		}
	}
	
	@Override
	public boolean update() {
		time += TIME_SCALE * Gdx.graphics.getDeltaTime();		
		light = Math.min(Math.max(Math.abs(MathUtils.sin(time)), 0.3f), 0.8f);
		
		Iterator<ParticleEffect> cloudIter = CLOUDS.iterator();
		while(cloudIter.hasNext()) {
			ParticleEffect cloud = cloudIter.next();
			cloud.setTint(light, light, light);
			if(cloud.update()) {
				cloud.done();
				cloudIter.remove();
			}
		}

		tryCreateClouds(true);
		
		return false;
	}

	@Override
	public void done() {

	}
	
	public float getLight() {
		return light;
	}
	
	private void tryCreateClouds(boolean fadeIn) {
		GameWorld gameWorld = Globals.getGameWorld();
		while(CLOUDS.size < gameWorld.getWidth() * gameWorld.getHeight() / Globals.getCamera().getViewportWidth() / 15) {
			createCloud(fadeIn);
		}
	}
	
	private void createCloud(boolean fadeIn) {
		GameWorld gameWorld = Globals.getGameWorld();
		float screenWidth = Globals.getCamera().getViewportWidth();
		float screenHeight = Globals.getCamera().getViewportHeight();
		float x = MathUtils.random(gameWorld.getLeft() - (screenWidth / 2), gameWorld.getRight() + (screenWidth / 3));
		float y = MathUtils.random(gameWorld.getTop() - (screenHeight / 2), gameWorld.getBottom() + (screenHeight / 3));
		
		Vector2Pool pool = Globals.getVector2Pool();
		Vector2 pos = pool.obtain(x, y);
		Vector2 minMaxSize = pool.obtain(screenWidth * 0.8f, screenWidth);
		Vector2 minVelocity = pool.obtain(-0.1f, 0);
		Vector2 maxVelocity = pool.obtain(0.1f, 0);
		Vector2 minMaxDuration = pool.obtain(30000, 150000);
		Vector2 minMaxParticles = pool.obtain(1, 1);
		ParticleEffect cloud = new ParticleEffect.Builder("cloud_1", pos, minMaxSize, minVelocity, maxVelocity, 
				                                          minMaxDuration, minMaxParticles)
		.fadeIn(fadeIn)
		.vSplits(0.05f, 0)
		.build();
		cloud.start();
		
		CLOUDS.add(cloud);
	}
}

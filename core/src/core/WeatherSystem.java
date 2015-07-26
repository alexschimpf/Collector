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
	
	private final Array<ParticleEffect> _clouds = new Array<ParticleEffect>();
	
	private float _time = 0;
	private float _light = 1;
	private boolean _enabled = true;
	
	public static WeatherSystem getInstance() {
		if(instance == null) {
			instance = new WeatherSystem();
		}
		
		return instance;
	}
	
	public WeatherSystem() {
		_time = MathUtils.random(0.0f, 1.0f);
		
		_tryCreateClouds(true);
	}
	
	@Override
	public void render(SpriteBatch spriteBatch) {
		if(!_enabled) {
			return;
		}		
		
		for(ParticleEffect cloud : _clouds) {
			cloud.render(spriteBatch);
		}
	}
	
	@Override
	public boolean update() {	
		if(!_enabled) {
			return false;
		}
		
//		time += TIME_SCALE * Gdx.graphics.getDeltaTime();		
//		light = Math.min(Math.max(Math.abs(MathUtils.sin(time)), 0.3f), 1f);
		
		Iterator<ParticleEffect> cloudIter = _clouds.iterator();
		while(cloudIter.hasNext()) {
			ParticleEffect cloud = cloudIter.next();
			if(cloud.update()) {
				cloud.done();
				cloudIter.remove();
			}
		}

		_tryCreateClouds(false);
		
		return false;
	}

	@Override
	public void done() {
	}
	
	public void setEnabled(boolean enabled){
		_enabled = enabled;
	}
	
	public void resetClouds(boolean randomFadeIn) {
		clearClouds();
		_tryCreateClouds(randomFadeIn);
	}
	
	public void setClouds(Array<ParticleEffect> clouds) {
		clearClouds();
		_clouds.addAll(clouds);
	}
	
	public void clearClouds() {
		_clouds.clear();
	}
	
	public Array<ParticleEffect> getClouds() {
		return _clouds;
	}
	
	public float getLight() {
		return _light;
	}
	
	private void _tryCreateClouds(boolean randomFadeIn) {
		GameWorld gameWorld = Globals.getGameWorld();
		while(_clouds.size < gameWorld.getWidth() * gameWorld.getHeight() / Globals.getCamera().getViewportWidth() / 20) {
			float screenWidth = Globals.getCamera().getViewportWidth();
			float screenHeight = Globals.getCamera().getViewportHeight();
			float x = MathUtils.random(gameWorld.getLeft(), gameWorld.getRight());
			float y = MathUtils.random(gameWorld.getTop(), gameWorld.getBottom());
			ParticleEffect cloud = Globals.getParticleEffectManager().getParticleEffect("cloud", x, y);
			cloud.minMaxSize(screenWidth * 0.8f, screenWidth);
			cloud.fadeIn(randomFadeIn ? Utils.choose(true, false) : true);
            cloud.buildParticles();
			
			_clouds.add(cloud);
		}
	}
}

package core;

import java.util.Iterator;

import misc.Globals;
import misc.IRender;
import misc.IUpdate;
import misc.Utils;
import particle.ParticleEffect;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class WeatherSystem implements IRender, IUpdate {

	public static WeatherSystem instance;
	
	private final Array<ParticleEffect> _clouds = new Array<ParticleEffect>();
	
	private boolean _enabled = true;	
	private ParticleEffect[][] _cloudMap;
	
	private WeatherSystem() {
		_resetCloudMap();
		
		if(!Globals.getCurrentRoom().isLobby()) {
			_tryCreateClouds(true);
		}		
	}
	
	private WeatherSystem(WeatherSystem weatherSystem) {
		set(weatherSystem);
	}
	
	public static WeatherSystem getInstance() {
		if(instance == null) {
			instance = new WeatherSystem();
		}
		
		return instance;
	}
	
	public WeatherSystem clone() {
		return new WeatherSystem(this);
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

		Iterator<ParticleEffect> cloudIter = _clouds.iterator();
		while(cloudIter.hasNext()) {
			ParticleEffect cloud = cloudIter.next();
			if(cloud.update()) {
				_removeCloudFromMap(cloud);
				
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
	
	public void set(WeatherSystem weatherSystem) {
		clearClouds();
		_clouds.addAll(weatherSystem._clouds);
		_cloudMap = weatherSystem._cloudMap;
	}
	
	public void setEnabled(boolean enabled){
		_enabled = enabled;
	}
	
	public void resetClouds(boolean randomFadeIn) {
		clearClouds();
		_tryCreateClouds(randomFadeIn);
	}

	public void clearClouds() {
		_resetCloudMap();
		_clouds.clear();
	}
	
	public Array<ParticleEffect> getClouds() {
		return _clouds;
	}
	
	private void _resetCloudMap() {
		int numRows = MathUtils.ceil(Globals.getGameWorld().getHeight() / (Globals.getCamera().getViewportHeight() / 3));
		int numCols = MathUtils.ceil(Globals.getGameWorld().getWidth() / (Globals.getCamera().getViewportWidth() / 3));
		_cloudMap = new ParticleEffect[numRows][numCols];
		for(int i = 0; i < _cloudMap.length; i++) {
			for(int j = 0; j < _cloudMap[0].length; j++) {
				_cloudMap[i][j] = null;
			}
		}
	}

	private void _tryCreateClouds(boolean randomFadeIn) {
		int maxNumClouds = _cloudMap.length * _cloudMap[0].length / 4;
		while(_clouds.size < maxNumClouds) {
			Vector2 pos = null;
			while(pos == null) {
				int row = MathUtils.random(_cloudMap.length - 1);
				int col = MathUtils.random(_cloudMap[0].length - 1);
				if(_cloudMap[row][col] == null) {
					pos = _getCloudPosition(row, col);
					ParticleEffect cloud = Globals.getParticleEffectManager().getParticleEffect("cloud", pos.x, pos.y);
					cloud.imageKey("cloud_" + MathUtils.random(1, 3));
					cloud.minMaxSize(Globals.getCamera().getViewportWidth() * 0.5f, Globals.getCamera().getViewportWidth());
					cloud.fadeIn(randomFadeIn ? MathUtils.randomBoolean(0.3f) : true);
		            cloud.buildParticles();
					
					_cloudMap[row][col] = cloud;
					_clouds.add(cloud);
				}
			}
		}
	}
	
	private void _removeCloudFromMap(ParticleEffect cloud) {
		for(int i = 0; i < _cloudMap.length; i++) {
			for(int j = 0; j < _cloudMap[0].length; j++) {
				if(_cloudMap[i][j] == cloud) {
					_cloudMap[i][j] = null;
				}
			}
		}
	}
	
	private Vector2 _getCloudPosition(int mapRow, int mapCol) {
		return new Vector2(mapCol * (Globals.getCamera().getViewportWidth() / 2), 
				           mapRow * (Globals.getCamera().getViewportHeight() / 2));
	}
}

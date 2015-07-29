package screen;

import misc.Globals;
import misc.InputListener;
import particle.ParticleEffect;
import particle.ParticleEffectLoader;
import animation.Animation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;

import core.TheCamera;
import core.TheGame;
import core.TileMap;
import core.TileMap.TileMapLayerType;
import core.WeatherSystem;

public final class GameScreen implements Screen {

	private final TheGame _theGame;
	private final Matrix4 _debugMatrix = new Matrix4();
	private final SpriteBatch _spriteBatch = new SpriteBatch();
	private final Box2DDebugRenderer _debugRenderer = new Box2DDebugRenderer();
	private final Array<ParticleEffect> _particleEffects = new Array<ParticleEffect>();
	private final Array<Animation> _animations = new Array<Animation>();	
	
	private TileMap _tileMap;

	public GameScreen(TheGame theGame) {
		_theGame = theGame;
		
		// HACK: Why does this work?
		if(TheGame.FULLSCREEN) {
			Gdx.graphics.setDisplayMode(Gdx.graphics.getDesktopDisplayMode().width, 
					                    Gdx.graphics.getDesktopDisplayMode().height, true);	
		}
		
		Globals.getSoundManager();
		Globals.getMusicManager();
		Globals.getTextureManager();	
		new ParticleEffectLoader().load();
	}
	
	@Override
	public void show() {
		Globals.setGameScreen(this);
		
		Globals.getGameWorld().loadRoom("world_1/world_start_tile_map.tmx", true);
		Globals.getWeatherSystem().setEnabled(false);
		
		if(TheGame.MUSIC) {
			// TODO: How to implement music?
		}
	}

	@Override
	public void render(float delta) {
		if(TheGame.PRINT_FPS) {
			Gdx.app.log("FPS", "" + Gdx.graphics.getFramesPerSecond());
		}	
		
		Globals.getCamera().update();
		
		_update();
		_render(delta);	
	}

	@Override
	public void resize(int width, int height) {
		Globals.getCamera().resizeViewport(width, height);		
		Globals.getHUD().resize(width, height);
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void hide() {
	}

	@Override
	public void dispose() {
	}
	
	public void setTileMap(TileMap tileMap) {
		_tileMap = tileMap;
	}
	
	public void addParticleEffect(ParticleEffect particleEffect) {
		_particleEffects.add(particleEffect);
	}
	
	public void addAnimation(Animation animation) {
		_animations.add(animation);
	}
	
	private void _update() {
		Globals.getGameWorld().update();
		Globals.getHUD().update();
		
		for(Animation animation : _animations) {
			if(animation.update()) {
				animation.done();
			}
		}
		
		for(ParticleEffect particleEffect : _particleEffects) {
			if(particleEffect.update()) {
				particleEffect.done();
			}
		}
		
		Globals.getWeatherSystem().update();
	}
	
	private void _render(float delta) {
		Gdx.gl.glClearColor((240 / 255.0f) * 0.8f, (250 / 255.0f) * 0.8f, (255 / 255.0f) * 0.8f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		OrthographicCamera camera = Globals.getCamera().getRawCamera();

		_tileMap.setView(camera);
		_spriteBatch.setProjectionMatrix(camera.combined);
		
		Globals.getHUD().render(_spriteBatch);

		_renderLayers();
		
		if(TheGame.PHYSICS_DEBUG) {
			_debugMatrix.set(camera.combined);
			_debugRenderer.render(Globals.getPhysicsWorld(), _debugMatrix);
		}
	}
	
	private void _renderLayers() {
		_renderBackgroundTiles();
		_renderWeather();
		_renderEnclosingTiles();
		_renderParticleEffects();
		_renderNormalTiles();
		_renderWorldAndAnimations();
		_renderForeground();
	}
	
	private void _renderBackgroundTiles() {
		_tileMap.render(TileMapLayerType.BACKGROUND, _spriteBatch);
	}
	
	private void _renderWeather() {
		_spriteBatch.begin();
		
		Globals.getWeatherSystem().render(_spriteBatch);
		
		_spriteBatch.end();
	}
	
	private void _renderEnclosingTiles() {
		_tileMap.render(TileMapLayerType.ENCLOSING, _spriteBatch);
	}
	
	private void _renderNormalTiles() {
		_tileMap.render(TileMapLayerType.NORMAL, _spriteBatch);
	}
	
	private void _renderParticleEffects() {
		_spriteBatch.begin();
		
		for(ParticleEffect particleEffect : _particleEffects) {
			particleEffect.render(_spriteBatch);
		}
		
		_spriteBatch.end();
	}
	
	private void _renderWorldAndAnimations() {		
		_spriteBatch.begin();
		
		for(Animation animation : _animations) {
			animation.render(_spriteBatch);
		}
		
		Globals.getGameWorld().render(_spriteBatch);	
		
		_spriteBatch.end();
	}
	
	private void _renderForeground() {
		_tileMap.render(TileMapLayerType.FOREGROUND, _spriteBatch);
	}
}

package screen;

import misc.Globals;
import particle.ParticleEffect;
import particle.ParticleEffectLoader;
import animation.Animation;
import background.ParallaxBackground;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.utils.Array;

import core.TheGame;
import core.TileMap;
import core.TileMap.TileMapLayerType;

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
		clearScreen();
		
		OrthographicCamera camera = Globals.getCamera().getRawCamera();

		_tileMap.setView(camera);
		_spriteBatch.setProjectionMatrix(camera.combined);
		
		_spriteBatch.begin();
		//Globals.getCurrentRoom().renderBackground(_spriteBatch);
		_spriteBatch.end();
			
		if(!Globals.isGameLoading()) {
			_renderLayers();
			
			if(TheGame.PHYSICS_DEBUG) {
				_debugMatrix.set(camera.combined);
				_debugRenderer.render(Globals.getPhysicsWorld(), _debugMatrix);
			}
			
			Globals.getHUD().render(_spriteBatch);
		} else {
			_doFlash();
			if(Globals.getPlayer() != null) {
				_spriteBatch.begin();
				Globals.getPlayer().render(_spriteBatch);
				_spriteBatch.end();
			}
		}
	}
	
	private void _renderLayers() {
		_renderBackgroundTiles();
		_renderWeather();
		_renderEnclosingTiles();
		_renderEnclosingWorld();
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
	
	private void _renderEnclosingWorld() {
		_spriteBatch.begin();
		Globals.getGameWorld().renderEnclosing(_spriteBatch);	
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
	
    private float r = (240 / 255.0f), g = (250 / 255.0f), b = (250 / 255.0f);
	private void clearScreen() {
		Gdx.gl.glClearColor(r, g, b, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	}
	
	// TODO: REFACTOR THIS UGLY FLASHING CRAP.
	private float flashAlpha = 1;
	private boolean once = false;
	private void _doFlash() {
		if(!once && flashAlpha > 0) {
			flashAlpha -= Gdx.graphics.getDeltaTime() * 2; 
		} else {
			once = true;
			flashAlpha += Gdx.graphics.getDeltaTime() * 2;
		}

		flashAlpha = Math.max(Math.min(flashAlpha, 1), 0);
		r = (240 / 255.0f) * flashAlpha;
		g = (250 / 255.0f) * flashAlpha;
		b = (250 / 255.0f) * flashAlpha;
		_spriteBatch.setColor(flashAlpha, flashAlpha, flashAlpha, 1);
		_tileMap.getBatch().setColor(flashAlpha, flashAlpha, flashAlpha, 1);
		
		if(once && flashAlpha >= 1) {
			once = false;
			Globals.state = Globals.State.RUNNING;
		}
	}
}

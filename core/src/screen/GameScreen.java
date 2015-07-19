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

	private final TheGame THE_GAME;
	private final SpriteBatch SPRITE_BATCH = new SpriteBatch();
	private final Box2DDebugRenderer DEBUG_RENDERER = new Box2DDebugRenderer();
	private final Matrix4 DEBUG_MATRIX = new Matrix4();
	private final Stage HUD_STAGE;
	private final InputListener INPUT_LISTENER = new InputListener();
	private final Array<ParticleEffect> PARTICLE_EFFECTS = new Array<ParticleEffect>();
	private final Array<Animation> ANIMATIONS = new Array<Animation>();	
	
	private TileMap tileMap;

	public GameScreen(TheGame theGame) {
		THE_GAME = theGame;
		
		// HACK: Why does this work?
		if(TheGame.FULLSCREEN) {
			Gdx.graphics.setDisplayMode(Gdx.graphics.getDesktopDisplayMode().width, 
					                    Gdx.graphics.getDesktopDisplayMode().height, true);	
		}
		
		Globals.getSoundManager();
		Globals.getMusicManager();
		Globals.getTextureManager();	
		new ParticleEffectLoader().load();
		
		HUD_STAGE = new Stage(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
		HUD_STAGE.addListener(INPUT_LISTENER);
		Gdx.input.setInputProcessor(HUD_STAGE);
	}
	
	@Override
	public void show() {
		Globals.setGameScreen(this);
		
		Globals.getGameWorld().loadRoom("world_1/world_start_tile_map.tmx", true);
		
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
		
		update();
		_render(delta);	
	}

	@Override
	public void resize(int width, int height) {
		TheCamera.getInstance().resizeViewport(width, height);		
		HUD_STAGE.getViewport().update(width, height, false);
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
		this.tileMap = tileMap;
	}
	
	public void addParticleEffect(ParticleEffect particleEffect) {
		PARTICLE_EFFECTS.add(particleEffect);
	}
	
	public void addAnimation(Animation animation) {
		ANIMATIONS.add(animation);
	}
	
	private void update() {
		INPUT_LISTENER.update();
		Globals.getGameWorld().update();
		HUD_STAGE.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		
		for(Animation animation : ANIMATIONS) {
			if(animation.update()) {
				animation.done();
			}
		}
		
		for(ParticleEffect particleEffect : PARTICLE_EFFECTS) {
			if(particleEffect.update()) {
				particleEffect.done();
			}
		}
		
		Globals.getWeatherSystem().update();
	}
	
	private void _render(float delta) {
		WeatherSystem weatherSystem = Globals.getWeatherSystem();
		float light = weatherSystem.getLight();
		
//		Gdx.gl.glClearColor(224 / 255.0f, 245 / 255.0f, 255 / 255.0f, 1);
		Gdx.gl.glClearColor(240 / 255.0f, 250 / 255.0f, 255 / 255.0f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		OrthographicCamera camera = Globals.getCamera().getRawCamera();
		
//		SPRITE_BATCH.setColor(light, light, light, 1);
		tileMap.setView(camera);
		SPRITE_BATCH.setProjectionMatrix(camera.combined);

		renderLayers();
		
		if(TheGame.PHYSICS_DEBUG) {
			DEBUG_MATRIX.set(camera.combined);
			DEBUG_RENDERER.render(Globals.getPhysicsWorld(), DEBUG_MATRIX);
		}
	}
	
	private void renderLayers() {
		renderBackgroundTiles();
		renderWeather();
		renderEnclosingTiles();
		renderParticleEffects();
		renderNormalTiles();
		renderWorldAndAnimations();
		renderForeground();
	}
	
	private void renderBackgroundTiles() {
		tileMap.render(TileMapLayerType.BACKGROUND, SPRITE_BATCH);
	}
	
	private void renderWeather() {
		SPRITE_BATCH.begin();
		
		Globals.getWeatherSystem().render(SPRITE_BATCH);
		
		SPRITE_BATCH.end();
	}
	
	private void renderEnclosingTiles() {
		tileMap.render(TileMapLayerType.ENCLOSING, SPRITE_BATCH);
	}
	
	private void renderNormalTiles() {
		tileMap.render(TileMapLayerType.NORMAL, SPRITE_BATCH);
	}
	
	private void renderParticleEffects() {
		SPRITE_BATCH.begin();
		
		for(ParticleEffect particleEffect : PARTICLE_EFFECTS) {
			particleEffect.render(SPRITE_BATCH);
		}
		
		SPRITE_BATCH.end();
	}
	
	private void renderWorldAndAnimations() {		
		SPRITE_BATCH.begin();
		
		for(Animation animation : ANIMATIONS) {
			animation.render(SPRITE_BATCH);
		}
		
		Globals.getGameWorld().render(SPRITE_BATCH);	
		
		SPRITE_BATCH.end();
	}
	
	private void renderForeground() {
		tileMap.render(TileMapLayerType.FOREGROUND, SPRITE_BATCH);
	}
}

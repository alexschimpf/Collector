package screen;

import misc.Globals;
import misc.InputListener;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.TmxMapLoader.Parameters;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;

import core.GameWorld;
import core.GameWorldLoader;
import core.TheCamera;
import core.TheGame;

public final class GameScreen implements Screen {

	private final SpriteBatch SPRITE_BATCH = new SpriteBatch();
	private final Box2DDebugRenderer DEBUG_RENDERER = new Box2DDebugRenderer();
	private final Matrix4 DEBUG_MATRIX = new Matrix4();
	private final TiledMap TILE_MAP;
	private final OrthogonalTiledMapRenderer TILE_MAP_RENDERER;
	private final Stage HUD_STAGE;
	private final InputListener INPUT_LISTENER = new InputListener();
	
	public GameScreen() {
		// music
		// background

		Parameters tileMapParams = new Parameters();
		tileMapParams.flipY = false;
		TILE_MAP = new TmxMapLoader().load("tile_map_1.tmx", tileMapParams);
		TILE_MAP_RENDERER = new OrthogonalTiledMapRenderer(TILE_MAP, Globals.getCamera().getTileMapScale());
		
		HUD_STAGE = new Stage(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
		HUD_STAGE.addListener(INPUT_LISTENER);
		Gdx.input.setInputProcessor(HUD_STAGE);
		
		GameWorldLoader gameWorldLoader = new GameWorldLoader(TILE_MAP);
		gameWorldLoader.load();
	}
	
	@Override
	public void show() {
		if(TheGame.MUSIC) {
			// TODO: How to implement music?
		}
		
		// Why does this work?
		if(TheGame.FULLSCREEN) {
			Gdx.graphics.setDisplayMode(Gdx.graphics.getDesktopDisplayMode().width, Gdx.graphics.getDesktopDisplayMode().height, true);	
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
	
	private void update() {
		INPUT_LISTENER.update();
		Globals.getGameWorld().update();
		HUD_STAGE.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
	}
	
	private void _render(float delta) {
		Gdx.gl.glClearColor((201.0f / 255), (238.0f / 255), (255.0f / 255), 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		OrthographicCamera camera = Globals.getCamera().getRawCamera();
		
		SPRITE_BATCH.setProjectionMatrix(camera.combined);
		
		TILE_MAP_RENDERER.setView(camera);
		TILE_MAP_RENDERER.render();
		
		SPRITE_BATCH.begin(); {
			Globals.getGameWorld().render(SPRITE_BATCH);		
		} SPRITE_BATCH.end();
		
		if(TheGame.PHYSICS_DEBUG) {
			DEBUG_MATRIX.set(camera.combined);
			DEBUG_RENDERER.render(Globals.getPhysicsWorld(), DEBUG_MATRIX);
		}
	}
}

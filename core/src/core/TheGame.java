package core;

import screen.GameScreen;

import com.badlogic.gdx.Game;

public final class TheGame extends Game {

	public static boolean MUSIC = true;
	public static boolean FULLSCREEN = true;
	public static boolean PHYSICS_DEBUG = false;
	public static boolean PRINT_FPS = false;
	public static boolean PACK_TEXTURES = false;
	
	private GameScreen gameScreen;
	
	@Override
	public void create() {
		gameScreen = new GameScreen(this);
		
		setScreen(gameScreen);
	}

	@Override
	public void render() {
		super.render();
	}
}

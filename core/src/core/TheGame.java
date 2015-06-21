package core;

import screen.GameScreen;

import com.badlogic.gdx.Game;

public final class TheGame extends Game {

	public static final boolean MUSIC = true;
	public static final boolean FULLSCREEN = true;
	public static final boolean PHYSICS_DEBUG = false;
	public static final boolean PRINT_FPS = false;
	
	private GameScreen gameScreen;
	
	@Override
	public void create() {
		gameScreen = new GameScreen();
		
		setScreen(gameScreen);
	}

	@Override
	public void render() {
		super.render();
	}
}

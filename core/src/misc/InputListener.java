package misc;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.InputEvent;

import core.TheGame;
import entity.Player;

public final class InputListener extends com.badlogic.gdx.scenes.scene2d.InputListener implements IUpdate {

	public InputListener() {
		Gdx.input.setCatchBackKey(true);
	}
	
	@Override
	public boolean keyDown(InputEvent event, int keyCode) {
		if(keyCode == Keys.ESCAPE) {
			Gdx.app.exit();
		}
		
		if(!Globals.isGameRunning()) {
			return true;
		}
		
		Player player = Globals.getPlayer();
		switch(keyCode) {
			case Keys.BACK:
				Globals.getGameWorld().loadLobbyRoom();
				break;
			case Keys.SPACE:
				player.jump();
				break;
			case Keys.A:
				player.interact();
				break;
				
			// For testing:
			case Keys.D:
				TheGame.PHYSICS_DEBUG = !TheGame.PHYSICS_DEBUG;
				break;
			case Keys.F:
				TheGame.PRINT_FPS = !TheGame.PRINT_FPS;
				break;
			case Keys.UP:
				String tileMapName = Globals.getCurrentRoom().checkForRoomEntrance();
				if(tileMapName != null) {
					Globals.getGameWorld().loadRoom(tileMapName, false);
				}
				break;
			case Keys.Q:
				if(!Globals.isGameLoading()) {
					Globals.getGameWorld().loadLobbyRoom();
				}
				
				break;
			case Keys.C:
				Globals.getCamera().getRawCamera().zoom = 1;
				break;
			case Keys.V:
				Globals.getWeatherSystem().resetClouds(true);
				break;
		}
		
		return true;
	}
	
	@Override
	public boolean keyUp(InputEvent event, int keyCode) {
		if(!Globals.isGameRunning()) {
			return true;
		}
		
		Player player = Globals.getPlayer();
		switch(keyCode) {
			case Keys.SPACE:
				player.stopJump();
				break;
		}
		
		return true;
	}

	@Override
	public boolean update() {
		if(!Globals.isGameRunning()) {
			return true;
		}
		
		Player player = Globals.getPlayer();
		if(Gdx.input.isKeyPressed(Keys.RIGHT)) {
			player.moveRight();
		} else if(Gdx.input.isKeyPressed(Keys.LEFT)) {
			player.moveLeft();
		} else if(!Utils.usingAndroidContext()){
			player.stopMove();
		}
		
		if(Gdx.input.isKeyPressed(Keys.Z)) {
			Globals.getCamera().getRawCamera().zoom += 0.05f;
		} else if(Gdx.input.isKeyPressed(Keys.X)) {
			Globals.getCamera().getRawCamera().zoom -= 0.05f;
		}
		
		return false;
	}

	@Override
	public void done() {
	}
}

package misc;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.InputEvent;

import core.GameWorld;
import entity.Player;

public final class InputListener extends com.badlogic.gdx.scenes.scene2d.InputListener implements IUpdate {

	public InputListener() {
	}
	
	@Override
	public boolean keyDown(InputEvent event, int keyCode) {
		if(keyCode == Keys.ESCAPE) {
			Gdx.app.exit();
		}
		
		if(Globals.getGameState() != GameWorld.State.RUNNING) {
			return false;
		}
		
		Player player = Globals.getPlayer();
		switch(keyCode) {
			case Keys.SPACE:
				player.jump();
				break;
			case Keys.A:
				player.shoot();
				break;
		}
		
		return true;
	}
	
	@Override
	public boolean keyUp(InputEvent event, int keyCode) {
		if(Globals.getGameState() != GameWorld.State.RUNNING) {
			return false;
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
		if(Globals.getGameState() != GameWorld.State.RUNNING) {
			return false;
		}
		
		Player player = Globals.getPlayer();
		if(Gdx.input.isKeyPressed(Keys.RIGHT)) {
			player.moveRight();
		} else if(Gdx.input.isKeyPressed(Keys.LEFT)) {
			player.moveLeft();
		} else if(!Utils.usingAndroidContext()){
			player.stopMove();
		}
		
//		if(Gdx.input.isKeyPressed(Keys.A)) {
//			player.shoot();
//		}	
		
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

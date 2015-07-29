package core;

import misc.Globals;
import misc.IRender;
import misc.IUpdate;
import misc.InputListener;
import misc.Utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;

import entity.special.Player;

public final class HUD implements IRender, IUpdate {

	private static HUD instance;
	
	private final Stage _stage;
	private final InputListener _inputListener = new InputListener();
	
	private Button _moveButton;
	private Button _jumpButton;
	private Button _interactButton;	
	private Integer _movePointer;
	
	private HUD() {
		_stage = new Stage(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
		_stage.addListener(_inputListener);
		Gdx.input.setInputProcessor(_stage);
		
		if(Utils.usingAndroidContext()) {
			buildMobileUI();
		}
	}
	
	public static HUD getInstance() {
		if(instance == null) {
			instance = new HUD();
		}
		
		return instance;
	}

	@Override
	public boolean update() {
		if(Utils.usingAndroidContext()) {
			checkPressedButtons();
		} else {
			_inputListener.update();
		}
		
		_stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
		
		return false;
	}

	@Override
	public void done() {
	}

	@Override
	public void render(SpriteBatch spriteBatch) {
		_stage.draw();
	}	
	
	public void resize(int width, int height) {
		_stage.getViewport().update(width, height, false);
	}
	
	public void setVisible(boolean visible) {
		// TODO: Best way to do this?
	} 
	
	public Stage getStage() {
		return _stage;
	}
	
	public InputListener getInputListener() {
		return _inputListener;
	}
	
	private void checkPressedButtons() {
		if(_movePointer == null) {
			return;
		}
		
		float moveCenterX = _moveButton.getX() + (_moveButton.getWidth() / 2);
		float x = Gdx.input.getX(_movePointer);	
		Player player = Globals.getPlayer();
		if(_moveButton.isPressed()) {	
			if(x < moveCenterX) {
				player.moveLeft();
			} else {
				player.moveRight();
			}
		} else {
			player.stopMove();
		}
	}
	
	private void buildMobileUI() {
		Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
		createMoveButton(skin);
		createJumpButton(skin);
		createInteractButton(skin);
	}
	
	private void createMoveButton(Skin skin) {
		float screenWidth = Gdx.graphics.getWidth();
		float screenHeight = Gdx.graphics.getHeight();
		
		_moveButton = new Button(skin);
		_moveButton.setColor(1, 1, 1, 0.3f);
		_moveButton.setSize(screenWidth / 2.75f, screenHeight / 5f);
		_moveButton.setPosition(0, screenHeight / 32f);
		
		_moveButton.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				if(Globals.isGameRunning()) {
					_movePointer = pointer;
				}
				 
				return true;
			}
		});
		
		_stage.addActor(_moveButton);
	}
	
	private void createJumpButton(Skin skin) {
		float screenWidth = Gdx.graphics.getWidth();
		float screenHeight = Gdx.graphics.getHeight();
		
		_jumpButton = new Button(skin);
		_jumpButton.setColor(1, 1, 1, 0.3f);
		_jumpButton.setSize(screenWidth / 7f, screenHeight / 5f);
		float buttonWidth = _jumpButton.getWidth();
		
		_jumpButton.setPosition(screenWidth - (buttonWidth * 2.2f), screenHeight / 32f);

		_jumpButton.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				if(Globals.isGameRunning()) { 
					Globals.getPlayer().jump();
				}
				
				return true;
			}
			
			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				if(Globals.isGameRunning()) { 
					Globals.getPlayer().stopJump();
				}
			}
		});

		_stage.addActor(_jumpButton);
	}
	
	private void createInteractButton(Skin skin) {
		float screenWidth = Gdx.graphics.getWidth();
		float screenHeight = Gdx.graphics.getHeight();
		
		_interactButton = new Button(skin);
		_interactButton.setColor(1, 1, 1, 0.3f);
		_interactButton.setSize(screenWidth / 7f, screenHeight / 5f);
		float buttonWidth = _interactButton.getWidth();
		
		_interactButton.setPosition(screenWidth - (buttonWidth * 1.1f), screenHeight / 32f);

		_interactButton.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				if(Globals.isGameRunning()) { 
					if(Globals.getCurrentRoom() != null && Globals.getCurrentRoom().isLobby()) {
						String tileMapName = Globals.getCurrentRoom().checkForRoomEntrance();
						if(tileMapName != null) {
							Globals.getGameWorld().loadRoom(tileMapName, false);
						}
					} else {
						Globals.getPlayer().interact();
					}
				}
				
				return true;
			}
		});

		_stage.addActor(_interactButton);
	}
}

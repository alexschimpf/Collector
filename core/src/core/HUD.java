package core;

import misc.Globals;
import misc.IRender;
import misc.IUpdate;
import misc.InputListener;
import misc.Utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.badlogic.gdx.utils.viewport.FitViewport;

import entity.Player;

public final class HUD implements IRender, IUpdate {

	public static final int DEFAULT_NARRATION_DURATION = 8000;
	
	private static final String[] NARRATION_TEXT = new String[] {
		"There is so much more...",
		"You've only just begun, you know.",
		"Purpose is what moves you forward.",
		"You exist to continue.",
		"Before there is tomorrow, there is now."
	};
	
	private static HUD instance;
	
	private final Label _textLabel;
	private final Skin _skin;
	private final Stage _stage;
	private final InputListener _inputListener = new InputListener();	
	
	private Button _moveButton;
	private Button _jumpButton;
	private Button _interactButton;	
	private Integer _movePointer;	
	private long _showTextStartTime;
	private float _showTextDuration;
	private long _nextRandomNarrationTime = TimeUtils.millis() + getRandomNarrationDelay();
	private int _narrationTextIndex = 0;
	       
	private HUD() {
		_stage = new Stage(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
		_stage.addListener(_inputListener);
		Gdx.input.setInputProcessor(_stage);
		
		_skin = new Skin(Gdx.files.internal("uiskin.json"));
		
		_textLabel = new Label("", _skin);		
		_setTextLabel();
		
		if(Utils.usingAndroidContext()) {
			buildMobileUI();
		}
		
		Utils.shuffleArray(NARRATION_TEXT);
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
		
		_updateText();
		
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
	
	public void showRandomText(float duration) {
		String text = NARRATION_TEXT[_narrationTextIndex];
		showText(text, duration);
		
		_narrationTextIndex = (_narrationTextIndex + 1) % NARRATION_TEXT.length;
	}
	
	public void showText(String text, float duration) {
		_showTextStartTime = TimeUtils.millis();
		_showTextDuration = duration;		
		showText(text);
	}
	
	public void showText(String text) {
		_textLabel.getStyle().background = _skin.newDrawable("default-pane", 0, 0, 0, 0.2f);
		_textLabel.setText(text);
	}
	
	public void clearText() {
		_textLabel.getStyle().background = null;
		_textLabel.setText("");
	}
	
	private void _setTextLabel() {
		float screenWidth = Gdx.graphics.getWidth();
		float screenHeight = Gdx.graphics.getHeight();
		
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("font.ttf"));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = (int)(screenWidth / 25);
		BitmapFont font = generator.generateFont(parameter);
		LabelStyle labelStyle = new LabelStyle(font, Color.WHITE);	
		
		_textLabel.setAlignment(Align.center);
		_textLabel.setWrap(true);
		_textLabel.setPosition(0, 0.8f * screenHeight);
		_textLabel.setStyle(labelStyle);
		_textLabel.setColor(Color.WHITE);
		_textLabel.setSize(screenWidth, screenHeight / 5);
		_stage.addActor(_textLabel);
	}
	
	private void _updateText() {
		_trySetRandomText();
		
		if(_showTextStartTime == 0) {
			return;
		}
		
		float timeSinceStart = TimeUtils.timeSinceMillis(_showTextStartTime);
		if(timeSinceStart > _showTextDuration) {
			clearText();
			_showTextStartTime = 0;
			_nextRandomNarrationTime = TimeUtils.millis() + getRandomNarrationDelay();
			
		} else {
			float ratio = timeSinceStart / _showTextDuration;
			Color color = _textLabel.getColor();
			if(ratio < 0.2f) {
				_textLabel.setColor(color.r, color.g, color.b, ratio * 5);
			} else if(ratio > 0.8f) {
				_textLabel.setColor(color.r, color.g, color.b, (1 - ratio) * 5);
			}
		}
	}
	
	private void _trySetRandomText() {
		if(_showTextStartTime == 0 && TimeUtils.millis() >= _nextRandomNarrationTime) {
			showRandomText(DEFAULT_NARRATION_DURATION);
		}		
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
		createMoveButton();
		createJumpButton();
		createInteractButton();
	}
	
	private void createMoveButton() {
		float screenWidth = Gdx.graphics.getWidth();
		float screenHeight = Gdx.graphics.getHeight();
		
		_moveButton = new Button(_skin);
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
	
	private void createJumpButton() {
		float screenWidth = Gdx.graphics.getWidth();
		float screenHeight = Gdx.graphics.getHeight();
		
		_jumpButton = new Button(_skin);
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
	
	private void createInteractButton() {
		float screenWidth = Gdx.graphics.getWidth();
		float screenHeight = Gdx.graphics.getHeight();
		
		_interactButton = new Button(_skin);
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
	
	private int getRandomNarrationDelay() {
		return MathUtils.random(3 * 60000, 5 * 60000);
	}
}

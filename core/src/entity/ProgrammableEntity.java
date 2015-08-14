package entity;

import misc.Globals;
import misc.Utils;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;

import core.GameRoom;

public final class ProgrammableEntity extends Entity {

	protected enum MoveState {
		LEFT, RIGHT, UP, DOWN, NONE
	}
	
	private final float _leftLimit;
	private final float _rightLimit;
	private final float _upLimit;
	private final float _downLimit;
	private final Vector2 _origPos = new Vector2();
	private final String[] _otherIds;
	
	private MoveState _state;
	
	public ProgrammableEntity(EntityBodyDef bodyDef, TextureMapObject object, MapObject bodySkeleton) {
		super(bodyDef, object, bodySkeleton);

		int maxLeft = Utils.getPropertyInt(object, "max_left");
		int maxRight = Utils.getPropertyInt(object, "max_right");
		int maxUp = Utils.getPropertyInt(object, "max_up");
		int maxDown = Utils.getPropertyInt(object, "max_down");
		
		_leftLimit = getLeft() - (maxLeft * Globals.getTileSize());
		_rightLimit = getLeft() + (maxRight * Globals.getTileSize());
		_upLimit = getTop() - (maxUp * Globals.getTileSize());
		_downLimit = getTop() + (maxDown * Globals.getTileSize());
		
		_state = MoveState.NONE;		
		_origPos.set(bodyDef.position);
		
		_otherIds = Utils.getPropertyStringArray(object, "other_ids", ",");
	}
	
	@Override
	public String getType() {
		return "programmable";
	}
	
	@Override
	public boolean update() {
		if(_isPlayerTouching()) {
			onBeginContact(null, Globals.getPlayer());
		}
		
		return super.update();
	}
	
	@Override
	public void onBeginContact(Contact contact, Entity entity) {
		if(!Utils.isPlayer(entity)) {
			return;
		}
		
		float LEFT = getLeft();
		float width = getWidth();
		float playerX = entity.getCenterX();
		if(playerX < LEFT + (width / 4)) {
			_state = MoveState.LEFT;
		} else if(playerX < LEFT + (width / 2)) {
			_state = MoveState.DOWN;
		} else if(playerX < LEFT + ((3.0f / 4.0f) * width)) {
			_state = MoveState.UP;
		} else {
			_state = MoveState.RIGHT;
		}
		
		_updateSprite();
		_deactivateOthers();
	}

	public void move() {
		if(!_isMoveValid()) {
			return;
		}
		
		float amt = getHeight();
		switch(_state) {
			case DOWN:
				setPosition(getCenterX(), getCenterY() + amt);
				break;
			case LEFT:
				setPosition(getCenterX() - amt, getCenterY());
				break;
			case RIGHT:
				setPosition(getCenterX() + amt, getCenterY());
				break;
			case UP:
				setPosition(getCenterX(), getCenterY() - amt);
				break;
			default:
				break;
		}
	}
	
	public void reset() {
		_state = MoveState.NONE;
		setPosition(_origPos.x, _origPos.y);
		_updateSprite();
	}
	
	public boolean isActivated() {
		return _state != MoveState.NONE;
	}
	
	public void deactivate() {
		_state = MoveState.NONE;
		_updateSprite();
	}

	private boolean _isMoveValid() {
		GameRoom currRoom = Globals.getCurrentRoom();
		
		float newTop, newLeft;
		switch(_state) {
			case DOWN:
				newTop = getTop() + Globals.getTileSize();
				return !currRoom.isEntityAt(getLeft(), newTop, getWidth(), getHeight(), this) && newTop <= _downLimit;
			case LEFT:
				newLeft = getLeft() - Globals.getTileSize();
				return !currRoom.isEntityAt(newLeft, getTop(), getWidth(), getHeight(), this) && newLeft >= _leftLimit;
			case RIGHT:
				newLeft = getLeft() + Globals.getTileSize();
				return !currRoom.isEntityAt(newLeft, getTop(), getWidth(), getHeight(), this) && newLeft <= _rightLimit;
			case UP:
				newTop = getTop() - Globals.getTileSize();
				return !currRoom.isEntityAt(getLeft(), newTop, getWidth(), getHeight(), this) && newTop >= _upLimit;
			default:
				return false;
		}
	}
	
	private int _getStateIndex() {
		switch(_state) {
			case NONE:
				return 1;
			case DOWN:
				return 3;
			case LEFT:
				return 2;
			case RIGHT:
				return 5;
			case UP:
				return 4;
			default:
				return -1;	
		}
	}
	
	private void _deactivateOthers() {
		for(String otherId : _otherIds) {
			ProgrammableEntity other = (ProgrammableEntity)Globals.getCurrentRoom().getEntityById(otherId);
			other.deactivate();
		}
	}
	
	private void _updateSprite() {
		int index = _getStateIndex();
		TextureRegion textureRegion = Globals.getImageTexture("programmable", index);
		_sprite.setRegion(textureRegion);
	}
	
    private boolean _isPlayerTouching() {
    	Player player = Globals.getPlayer();
    	Fixture fixture = getBody().getFixtureList().get(0);
    	return fixture.testPoint(player.getCenterX(), player.getBottom());
    }
}

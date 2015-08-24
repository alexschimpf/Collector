package entity;

import misc.Globals;
import misc.Utils;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;

import core.GameRoom;

public final class ProgrammableEntity extends Entity {

	protected enum MoveState {
		LEFT, RIGHT, UP, DOWN, NONE
	}
	
	private final String _areaId;
	private final Vector2 _origPos = new Vector2();
	private final String[] _otherIds;
	
	private MoveState _state;
	
	public ProgrammableEntity(EntityBodyDef bodyDef, TextureMapObject object, MapObject bodySkeleton) {
		super(bodyDef, object, bodySkeleton);

		_areaId = Utils.getPropertyString(object, "area_id");
		
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
		return _isMoveClearOfEntities() && _isMoveWithinArea();
	}
	
	private boolean _isMoveClearOfEntities() {
		GameRoom currRoom = Globals.getCurrentRoom();
		
		float newTop, newLeft;
		switch(_state) {
			case DOWN:
				newTop = getTop() + Globals.getTileSize();
				return !currRoom.isEntityAt(getLeft(), newTop, getWidth(), getHeight(), this);
			case LEFT:
				newLeft = getLeft() - Globals.getTileSize();
				return !currRoom.isEntityAt(newLeft, getTop(), getWidth(), getHeight(), this);
			case RIGHT:
				newLeft = getLeft() + Globals.getTileSize();
				return !currRoom.isEntityAt(newLeft, getTop(), getWidth(), getHeight(), this);
			case UP:
				newTop = getTop() - Globals.getTileSize() * 0f;
				return !currRoom.isEntityAt(getLeft(), getBottom() - Globals.getTileSize(), getWidth(), getHeight(), this);
			default:
				return false;
		}
	}
	
	private boolean _isMoveWithinArea() {
		BasicEntity area = _getArea();
		Rectangle newBorderRect = new Rectangle(getBorderRectangle());
		switch(_state) {
			case DOWN:
				newBorderRect.y += Globals.getTileSize();
				break;
			case LEFT:
				newBorderRect.x -= Globals.getTileSize();
				break;
			case RIGHT:
				newBorderRect.x += Globals.getTileSize();
				break;
			case UP:
				newBorderRect.y -= Globals.getTileSize();
				break;
			default:
				break;
		}
		
		return area.getBorderRectangle().contains(newBorderRect);
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
    
    private BasicEntity _getArea() {
    	return (BasicEntity)Globals.getCurrentRoom().getEntityById(_areaId);
    }
}

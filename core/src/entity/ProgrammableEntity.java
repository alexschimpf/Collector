package entity;

import misc.Globals;
import misc.IInteractive;
import misc.Utils;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;

import core.GameWorld;
import entity.special.Player;

public final class ProgrammableEntity extends Entity {

	protected enum MoveState {
		LEFT, RIGHT, UP, DOWN, NONE
	}
	
	private final float LEFT_LIMIT;
	private final float RIGHT_LIMIT;
	private final float UP_LIMIT;
	private final float DOWN_LIMIT;
	private final Vector2 ORIG_POS = new Vector2();
	private final String[] OTHER_IDS;
	
	private MoveState state;
	
	public ProgrammableEntity(EntityBodyDef bodyDef, TextureMapObject object, MapObject bodySkeleton) {
		super(bodyDef, object, bodySkeleton);
		
		MapProperties properties = object.getProperties();
		int maxLeft = Utils.getPropertyInt(properties, "max_left");
		int maxRight = Utils.getPropertyInt(properties, "max_right");
		int maxUp = Utils.getPropertyInt(properties, "max_up");
		int maxDown = Utils.getPropertyInt(properties, "max_down");
		
		LEFT_LIMIT = getLeft() - (maxLeft * Globals.getTileSize());
		RIGHT_LIMIT = getLeft() + (maxRight * Globals.getTileSize());
		UP_LIMIT = getTop() - (maxUp * Globals.getTileSize());
		DOWN_LIMIT = getTop() + (maxDown * Globals.getTileSize());
		
		state = MoveState.NONE;		
		ORIG_POS.set(bodyDef.position);
		
		OTHER_IDS = Utils.getPropertyStringArray(properties, "other_ids", ",");
	}
	
	@Override
	public String getType() {
		return "programmable";
	}
	
	@Override
	public boolean update() {
		if(isPlayerTouching()) {
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
			state = MoveState.LEFT;
		} else if(playerX < LEFT + (width / 2)) {
			state = MoveState.DOWN;
		} else if(playerX < LEFT + ((3.0f / 4.0f) * width)) {
			state = MoveState.UP;
		} else {
			state = MoveState.RIGHT;
		}
		
		updateSprite();
		deactivateOthers();
	}
	
	public void move() {
		if(!isMoveValid()) {
			return;
		}
		
		float amt = getHeight();
		switch(state) {
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
		state = MoveState.NONE;
		setPosition(ORIG_POS.x, ORIG_POS.y);
	}
	
	public boolean isActivated() {
		return state != MoveState.NONE;
	}
	
	public void deactivate() {
		state = MoveState.NONE;
		updateSprite();
	}

	private boolean isMoveValid() {
		GameWorld world = Globals.getGameWorld();
		
		float newTop, newLeft;
		switch(state) {
			case DOWN:
				newTop = getTop() + Globals.getTileSize();
				return !world.isEntityAt(getLeft(), newTop, getWidth(), getHeight(), this) && newTop <= DOWN_LIMIT;
			case LEFT:
				newLeft = getLeft() - Globals.getTileSize();
				return !world.isEntityAt(newLeft, getTop(), getWidth(), getHeight(), this) && newLeft >= LEFT_LIMIT;
			case RIGHT:
				newLeft = getLeft() + Globals.getTileSize();
				return !world.isEntityAt(newLeft, getTop(), getWidth(), getHeight(), this) && newLeft <= RIGHT_LIMIT;
			case UP:
				newTop = getTop() - Globals.getTileSize();
				return !world.isEntityAt(getLeft(), newTop, getWidth(), getHeight(), this) && newTop >= UP_LIMIT;
			default:
				return false;
		}
	}
	
	private int getStateIndex() {
		switch(state) {
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
	
	private void deactivateOthers() {
		for(String otherId : OTHER_IDS) {
			ProgrammableEntity other = (ProgrammableEntity)Globals.getGameWorld().getEntityById(otherId);
			other.deactivate();
		}
	}
	
	private void updateSprite() {
		int index = getStateIndex();
		TextureRegion textureRegion = Globals.getImageTexture("programmable", index);
		sprite.setRegion(textureRegion);
	}
	
    private boolean isPlayerTouching() {
    	Player player = Globals.getPlayer();
    	Fixture fixture = getBody().getFixtureList().get(0);
    	return fixture.testPoint(player.getCenterX(), player.getBottom());
    }
}

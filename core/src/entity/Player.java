package entity;

import misc.EntityBodyDef;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;

public class Player extends Entity {
	
	public Player(EntityBodyDef bodyDef, TextureMapObject object, MapObject bodySkeleton) {
		super(bodyDef, object, bodySkeleton);
	}

	@Override
	public String getType() {
		return "player";
	}
	
	public void jump() {
		
	}
	
	public void stopJump() {
		
	}
	
	public void moveLeft() {
		
	}
	
	public void moveRight() {
		
	}
	
	public void stopMove() {
		
	}
	
	public void shoot() {
		
	}
	
	public void incrementFootContacts() {
		
	}
	
	public void decrementFootContacts() {
		
	}
}

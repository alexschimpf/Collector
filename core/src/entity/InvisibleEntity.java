package entity;

import misc.Utils;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.physics.box2d.Contact;

public class InvisibleEntity extends Entity {

	public InvisibleEntity(EntityBodyDef bodyDef, TextureMapObject object, MapObject bodySkeleton) {
		super(bodyDef, object, bodySkeleton);
		
		_sprite.setAlpha(0);
	}
	
	@Override
	public String getType() {
		return "invisible";
	}
	
	@Override
	public void onBeginContact(Contact contact, Entity entity) {
		if(Utils.isPlayer(entity)) {
			_sprite.setAlpha(1);
		}
	}
	
	@Override
	public void onEndContact(Contact contact, Entity entity) {
		if(Utils.isPlayer(entity)) {
			_sprite.setAlpha(0);
		}
	}
}

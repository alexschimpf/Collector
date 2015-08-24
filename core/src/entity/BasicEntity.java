package entity;

import misc.Utils;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.physics.box2d.Fixture;


public class BasicEntity extends Entity {

	public BasicEntity(EntityBodyDef bodyDef, TextureMapObject object, MapObject bodySkeleton) {
		super(bodyDef, object, bodySkeleton);
		
		boolean isSensor = Utils.getPropertyBoolean(object, "is_sensor");
		
		Fixture fixture = _body.getFixtureList().get(0);
		fixture.setSensor(isSensor);
	}
	
	@Override
	public String getType() {
		return "basic";
	}
}

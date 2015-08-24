package entity;

import misc.Globals;
import misc.Utils;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.TimeUtils;

public class ProjectileEntity extends Entity {

	private static final float LIFE_TIME = 3500;
	
	protected final Vector2 _velocity;
	
	protected long _startTime;
	
	public ProjectileEntity(String textureKey, EntityBodyDef bodyDef, FixtureDef fixtureDef, Vector2 velocity) {
		super(null, textureKey, bodyDef, fixtureDef);
		
		_velocity = velocity;
		
		_body.setGravityScale(0);
		_body.setBullet(true);
	}
	
	public void onPostCreate(EntityBodyDef bodyDef, TextureMapObject object, MapObject bodySkeleton) {
		setBodyData();
		setVisible(true);
	}
	
	@Override
	public String getType() {
		return "projectile";
	}
	
	@Override
	public boolean update() {
		if(TimeUtils.timeSinceMillis(_startTime) > LIFE_TIME) {
			return true;
		}
		
		return super.update();
	}
	
	@Override
	public void onBeginContact(Contact contact, Entity entity) {
		if(Utils.isPlayer(entity)) {
			Globals.getPlayer().respawn("die", true, 0.5f, null);
		}
		
		markDone();
	}
	
	public void shoot() {
		_startTime = TimeUtils.millis();
		setLinearVelocity(_velocity.x, _velocity.y);
	}
}

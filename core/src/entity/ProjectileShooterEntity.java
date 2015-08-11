package entity;

import misc.Globals;
import misc.Utils;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.TimeUtils;

public class ProjectileShooterEntity extends Entity{

	protected final float _shootCooldown;
	protected final Vector2 _shootVelocity;
	protected final EntityBodyDef _entityBodyDef;
	protected final FixtureDef _fixtureDef;
	
	protected long _lastShootTime = 0;
	
	public ProjectileShooterEntity(EntityBodyDef bodyDef, TextureMapObject object, MapObject bodySkeleton) {
		super(bodyDef, object, bodySkeleton);
		
		_shootCooldown = Utils.getPropertyFloat(object, "shoot_cooldown");
		_shootVelocity = Utils.getPropertyVector2(object, "shoot_velocity");
				
		Vector2 pos = new Vector2();
		Vector2 size = new Vector2();
		_entityBodyDef = new EntityBodyDef(pos, size, BodyType.KinematicBody);
		
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(Globals.getTileSize() / 4, Globals.getTileSize() / 4);
		_fixtureDef = new FixtureDef();
		_fixtureDef.density = 1;
		_fixtureDef.friction = 0;
		_fixtureDef.restitution = 0;
		_fixtureDef.shape = shape;	
		
		if(_shootVelocity.x > 0) {
			_entityBodyDef.position.set(getCenterX() + getWidth(), getCenterY());
		} else if(_shootVelocity.x < 0) {
			_entityBodyDef.position.set(getCenterX() - getWidth(), getCenterY());
		} else if(_shootVelocity.y > 0) {
			_entityBodyDef.position.set(getCenterX(), getCenterY() + getHeight());
		} else if(_shootVelocity.y < 0) {
			_entityBodyDef.position.set(getCenterX(), getCenterY() - getHeight());
		}
	}
	
	@Override
	public String getType() {
		return "projectile_shooter";
	}
	
	@Override
	public boolean update() {
		if(TimeUtils.timeSinceMillis(_lastShootTime) > _shootCooldown && isPlayerInRange()) {
			_lastShootTime = TimeUtils.millis();
			shoot();
		}
		
		return super.update();
	}

	protected boolean isPlayerInRange() {
		Player player = Globals.getPlayer();
		return Utils.calcDistance(getCenter(), player.getCenter()) < Globals.getCamera().getViewportWidth();
	}
	
	protected void shoot() {
		ProjectileEntity projectile = new ProjectileEntity("projectile", _entityBodyDef, _fixtureDef, _shootVelocity);
		projectile.shoot();
	}
}

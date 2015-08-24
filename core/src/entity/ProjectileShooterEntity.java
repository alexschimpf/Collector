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

	private static final float SIZE = Globals.getTileSize() / 2;
	
	protected final float _shootCooldown;
	protected final Vector2 _shootVelocity;
	protected final EntityBodyDef _entityBodyDef;
	
	protected long _lastShootTime = 0;
	
	public ProjectileShooterEntity(EntityBodyDef bodyDef, TextureMapObject object, MapObject bodySkeleton) {
		super(bodyDef, object, bodySkeleton);
		
		_shootCooldown = Utils.getPropertyFloat(object, "shoot_cooldown");
		_shootVelocity = Utils.getPropertyVector2(object, "shoot_velocity");
				
		Vector2 pos = new Vector2();
		Vector2 size = new Vector2(SIZE, SIZE);
		_entityBodyDef = new EntityBodyDef(pos, size, BodyType.KinematicBody);
		
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
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(SIZE / 2, SIZE / 2);
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.density = 1;
		fixtureDef.friction = 0;
		fixtureDef.restitution = 0;
		fixtureDef.shape = shape;	
		
		ProjectileEntity projectile = new ProjectileEntity("projectile", _entityBodyDef, fixtureDef, _shootVelocity);
		projectile.onPostCreate(null, null, null);
		Globals.getCurrentRoom().addEntity(projectile);
		projectile.shoot();
	}
}

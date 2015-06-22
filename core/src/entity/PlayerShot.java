package entity;

import misc.Globals;
import misc.Vector2Pool;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

public final class PlayerShot extends Entity {

	public static final float SPEED = Player.MOVE_SPEED * 4;
	public static final EntityBodyDef BODY_DEF = new EntityBodyDef();
	public static final FixtureDef FIXTURE_DEF = new FixtureDef();
	
	static {
		BODY_DEF.bodyType = BodyType.DynamicBody;
		FIXTURE_DEF.density = 1f;
		FIXTURE_DEF.friction = 0.2f;
		FIXTURE_DEF.restitution = 0.2f;
		FIXTURE_DEF.filter.categoryBits = Globals.PLAYER_NO_COLLIDE_MASK;
	}

	public PlayerShot() {
		TextureRegion textureRegion = Globals.getTextureManager().getImageTexture("shot");
		
		float size = BODY_DEF.size.x;
		
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(size / 2 * 0.7f, size / 2 * 0.7f);
		
		FIXTURE_DEF.shape = shape;
		
		createSprite(BODY_DEF, textureRegion);	
		createBody(BODY_DEF, FIXTURE_DEF);
		
		body.setBullet(true);
		body.setGravityScale(0);
	}
	
	public static void shootShot() {
		Player player = Globals.getPlayer();
		
		float size = player.getHeight() / 3;
		float x = player.getFrontX();
		float y = player.getCenterY();
		
		if(player.isFacingRight()) {
			x -= size / 2;
		} else {
			x += size / 2;
		}
		
		Vector2Pool vector2Pool = Vector2Pool.getIntance();
		Vector2 posVec = vector2Pool.obtain(x, y);
		Vector2 sizeVec = vector2Pool.obtain(size, size);
		BODY_DEF.position = posVec;
		BODY_DEF.size =  sizeVec;
		
		PlayerShot shot = new PlayerShot();
		shot.setId(String.valueOf(shot.hashCode()));
		shot.setBodyData();
		shot.shoot();
		
		vector2Pool.free(posVec);
		vector2Pool.free(sizeVec);
	}

	@Override
	public String getType() {
		return "player_shot";
	}
	
	@Override
	public void onBeginContact(Entity entity) {
		if(entity == null) {
			// TODO: particle effect
			markDone();
			return;
		}

		Body entityBody = entity.getBody();
		Fixture fixture = entityBody.getFixtureList().get(0);
		if(fixture != null && fixture.isSensor()) {
			return;
		}
		
		// TODO: particle effect

		markDone();
	}
	
	public void shoot() {
		Globals.getGameWorld().addEntity(this);
		
		Player player = Globals.getPlayer();
		
		float vy = Math.min(player.getLinearVelocity().y, 2.8f);
		float vx = PlayerShot.SPEED;

		if(!player.isFacingRight()) {
			vx = 0 - vx;
		}
	
		setLinearVelocity(vx, vy);
	}
}

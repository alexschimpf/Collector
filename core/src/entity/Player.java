package entity;

import misc.Animation;
import misc.AnimationSystem;
import misc.EntityBodyDef;
import misc.Globals;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.MassData;
import com.badlogic.gdx.utils.TimeUtils;

public final class Player extends Entity {
	
	public static final float MOVE_SPEED = 18;
	public static final float JUMP_IMPULSE = -100;
	public static final float SHOOT_PERIOD = 200;
	public static final float JUMP_PERIOD = 100;
	public static final float MASS = 5.7f;
	
	private final AnimationSystem ANIMATION_SYSTEM = new AnimationSystem();
	
	private boolean isJumping = false;
	private boolean isFacingRight = true;
	private int numFootContacts = 0;
	private long lastShotTime = 0;
	private long lastJumpTime = 0;
	private long lastBlinkTime = TimeUtils.millis();
	private float blinkPeriod = MathUtils.random(1000, 6000);
	
	public Player(EntityBodyDef bodyDef, TextureMapObject object, MapObject bodySkeleton) {
		super(bodyDef, object, bodySkeleton);
		
		body.setBullet(true);
		
		MassData massData = body.getMassData();
		massData.mass = MASS;
		body.setMassData(massData);
		
		attachFootSensors();
	}

	@Override
	public String getType() {
		return "player";
	}
	
	@Override
	public boolean update() {
		tryBlink();
		
		ANIMATION_SYSTEM.update();
		sprite = ANIMATION_SYSTEM.getSprite();
		
		ANIMATION_SYSTEM.getAnimation().flipSprite(!isFacingRight, false);
		
		return super.update();
	}
	
	public boolean jump() {
		if(isJumping || TimeUtils.timeSinceMillis(lastJumpTime) < JUMP_PERIOD) {
			return false;
		}
		
		// TODO: Vertically moving block hack
		
		lastJumpTime = TimeUtils.millis();
		
		Globals.getSoundManager().playSound("jump");
		
		float x = body.getWorldCenter().x;
		float y = body.getWorldCenter().y;
		body.applyLinearImpulse(0, JUMP_IMPULSE, x, y, true);
		
		ANIMATION_SYSTEM.switchAnimation("jump", false, true);
		
		return true;
	}
	
	public void stopJump() {
		if(getLinearVelocity().y < 0) {
			setLinearVelocity(getLinearVelocity().x, 0.02f);
		}
	}
	
	public void moveLeft() {
		move(false);
	}
	
	public void moveRight() {
		move(true);
	}
	
	public void stopMove() {
		if(isJumpAnimationPlaying() || isBlinking() || isShooting()) {
			return;
		}
		
		ANIMATION_SYSTEM.stop();
		
		setLinearVelocity(0, getLinearVelocity().y);
	}
	
	public void shoot() {
		if(TimeUtils.timeSinceMillis(lastShotTime) < SHOOT_PERIOD) {
			return;
		}
		
		lastShotTime = TimeUtils.millis();
		
		Globals.getSoundManager().playSound("shoot");
		
		PlayerShot.shootShot();
		
		ANIMATION_SYSTEM.switchAnimation("shoot", false, true);
	}
	
	public boolean isFacingRight() {
		return isFacingRight;
	}
	
	public boolean isJumping() {
		return isJumping;
	}
	
	public float getFrontX() {
		return isFacingRight ? getRight() : getLeft();
	}
	
	public float getBackX() {
		return isFacingRight ? getLeft() : getRight();
	}
	
	public void incrementFootContacts() {
		numFootContacts++;
		isJumping = numFootContacts < 1;
	}
	
	public void decrementFootContacts() {
		numFootContacts--;
		if(numFootContacts < 0) {
			numFootContacts = 0;
		}
		
		isJumping = numFootContacts < 1;
	}
	
	@Override
	protected void createSprite(EntityBodyDef bodyDef, TextureRegion textureRegion) {
		super.createSprite(bodyDef, textureRegion);
		
		Vector2 pos = bodyDef.position;
		Vector2 size = bodyDef.size;
		
		Animation blinkAnimation = new Animation.AnimationBuilder("player_blink", pos, size, 0.56f).build();
		Animation jumpAnimation = new Animation.AnimationBuilder("player_jump", pos, size, 0.27f).build();		
		Animation moveAnimation = new Animation.AnimationBuilder("player_move", pos, size, 0.2f).loop(true).build();
		Animation shootAnimation = new Animation.AnimationBuilder("player_shoot", pos, size, 0.05f).build();
		
		ANIMATION_SYSTEM.addAnimation("blink", blinkAnimation);
		ANIMATION_SYSTEM.addAnimation("jump", jumpAnimation);
		ANIMATION_SYSTEM.addAnimation("move", moveAnimation);
		ANIMATION_SYSTEM.addAnimation("shoot", shootAnimation);
	}
	
	private void move(boolean right) {
		isFacingRight = right;
		
		float vx = Player.MOVE_SPEED;
		if(!right) {
			vx = 0 - vx;
		}
		
		if(numContacts > 0 && !isJumping()) {
			if(!isMoving()) {
				ANIMATION_SYSTEM.switchAnimation("move", false, true);
			}
			
			// particle effects
		} else if(!isJumpAnimationPlaying() && !isShooting()) {
			ANIMATION_SYSTEM.switchAnimation("move", false, false);
		}
	}
	
	private void attachFootSensors() {
		CircleShape shape = new CircleShape();

		float radius = getWidth() / 6f;
		shape.setRadius(radius);
		shape.setPosition(new Vector2(0.65f, 1.45f));
		Fixture fixture1 = body.createFixture(shape, 0);
		fixture1.setSensor(true);
		
		shape.setPosition(new Vector2(-0.65f, 1.45f));
		Fixture fixture2 = body.createFixture(shape, 0);
		fixture2.setSensor(true);
		
		shape.dispose();
	}
	
	private void tryBlink() {
		if(isJumping && TimeUtils.timeSinceMillis(lastBlinkTime) > blinkPeriod) {
			lastBlinkTime = TimeUtils.millis();
			blinkPeriod = MathUtils.random(1000, 5000);
			ANIMATION_SYSTEM.switchAnimation("blink", false, true);
		}
	}
	
	private boolean isBlinking() {
		return ANIMATION_SYSTEM.getAnimationKey().equals("blink");
	}
	
	/**
	 * Note: This is different from isJumping().
	 */
	private boolean isJumpAnimationPlaying() {
		return ANIMATION_SYSTEM.getAnimationKey().equals("jump");
	}
	
	private boolean isShooting() {
		return ANIMATION_SYSTEM.getAnimationKey().equals("shoot");
	}
	
	private boolean isMoving() {
		return ANIMATION_SYSTEM.getAnimationKey().equals("move");
	}
}

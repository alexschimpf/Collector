package entity;

import misc.Globals;
import misc.Utils;
import animation.Animation;
import animation.AnimationSystem;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.MassData;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.TimeUtils;

public final class Player extends Entity {
	
	public static final float MOVE_SPEED = 15;
	public static final float JUMP_IMPULSE = -95;
	public static final float SHOOT_PERIOD = 150;
	public static final float MASS = 5.69f;
	
	private final AnimationSystem ANIMATION_SYSTEM = new AnimationSystem();
	
	private boolean isJumping = false;
	private boolean isFacingRight = true;
	private int numFootContacts = 0;
	private long lastShotTime = 0;
	private long lastBlinkTime = TimeUtils.millis();
	private float blinkPeriod = MathUtils.random(1000, 6000);
	
	public Player(EntityBodyDef bodyDef, TextureMapObject object, MapObject bodySkeleton) {
		TextureRegion textureRegion = object.getTextureRegion();
		createSprite(bodyDef, textureRegion);	
		createBody(bodyDef, bodySkeleton);
		
		body.setBullet(true);
		
		MassData massData = body.getMassData();
		massData.mass = MASS;
		body.setMassData(massData);
		
		attachFootSensors(bodyDef);
		
		Filter filter = new Filter();
		filter.categoryBits = 0x0001;
		filter.maskBits = (short)~Globals.PLAYER_NO_COLLIDE_MASK;
		body.getFixtureList().get(0).setFilterData(filter);
	}

	@Override
	public String getType() {
		return "player";
	}
	
	@Override
	public String getId() {
		return "player";
	}
	
	@Override
	public boolean update() {
		tryBlink();
		
		ANIMATION_SYSTEM.update();
		sprite = ANIMATION_SYSTEM.getSprite();
		ANIMATION_SYSTEM.flipSprite(!isFacingRight, true);
			
		return super.update();
	}
	
	public boolean jump() {
		if(isJumping) {
			return false;
		}
		
		isJumping = true;
		
		// TODO: Vertically moving block hack

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
		setLinearVelocity(0, getLinearVelocity().y);
		
		if(!isJumping && !isShooting() && !isBlinking()) {
			ANIMATION_SYSTEM.switchToDefault();
		}
	}
	
	public void shoot() {
		if(TimeUtils.timeSinceMillis(lastShotTime) < SHOOT_PERIOD) {
			return;
		}
		
		lastShotTime = TimeUtils.millis();
		
		Globals.getSoundManager().playSound("shoot");
		
		PlayerShot.shootShot();
		
		if(!isShooting() && !isJumpAnimationPlaying()) {
			ANIMATION_SYSTEM.switchAnimation("shoot", false, true);
		}
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
		
		Animation blinkAnimation = new Animation.Builder("player_blink", pos, size, 0.56f).build();
		Animation jumpAnimation = new Animation.Builder("player_jump", pos, size, 0.27f).build();		
		Animation moveAnimation = new Animation.Builder("player_move", pos, size, 0.2f).loop(true).build();
		Animation shootAnimation = new Animation.Builder("player_shoot", pos, size, 0.1f).build();
		
		ANIMATION_SYSTEM.addAnimation("blink", blinkAnimation);
		ANIMATION_SYSTEM.addAnimation("jump", jumpAnimation);
		ANIMATION_SYSTEM.addAnimation("move", moveAnimation);
		ANIMATION_SYSTEM.addAnimation("shoot", shootAnimation);
		
		ANIMATION_SYSTEM.setDefaultSprite("player", size.x, size.y);
	}
	
	protected void createBody(EntityBodyDef bodyDef, MapObject bodySkeleton) {
		FixtureDef fixtureDef = Utils.getScaledFixtureDefFromBodySkeleton(bodySkeleton, 0.95f);
		createBody(bodyDef, fixtureDef);
	}
	
	private void move(boolean right) {
		isFacingRight = right;
		
		float vx = Player.MOVE_SPEED;
		if(!right) {
			vx = 0 - vx;
		}
		
		setLinearVelocity(vx, getLinearVelocity().y);
		
		if(numFootContacts > 0 && !isJumping && !isMovingAnimationPlaying() && !isJumpAnimationPlaying() && !isShooting()) {
			ANIMATION_SYSTEM.switchAnimation("move", false, true);
			
			// TODO: particle effects
		}
		
		if(numFootContacts == 0 && !isJumpAnimationPlaying() && !isShooting() && !isBlinking()) {
			ANIMATION_SYSTEM.switchToDefault();
		}
	}
	
	private void attachFootSensors(EntityBodyDef bodyDef) {	
		Vector2 localBottom = body.getLocalPoint(new Vector2(getCenterX(), getBottom())).sub(0, getHeight() / 20);
		
		float width = bodyDef.size.x;
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(width / 2 * 0.8f, getHeight() / 19.9f, localBottom, 0);
		
		Fixture fixture = body.createFixture(shape, 0);
		fixture.setSensor(true);
		
		shape.dispose();
	}
	
	private void tryBlink() {
		if(!isJumping && !isMovingAnimationPlaying() && TimeUtils.timeSinceMillis(lastBlinkTime) > blinkPeriod) {
			lastBlinkTime = TimeUtils.millis();
			blinkPeriod = MathUtils.random(1000, 5000);
			ANIMATION_SYSTEM.switchAnimation("blink", false, true);
		}
	}
	
	private boolean isBlinking() {
		return ANIMATION_SYSTEM.getAnimationKey().equals("blink") && ANIMATION_SYSTEM.isPlaying();
	}
	
	/**
	 * Note: This is different from isJumping().
	 */
	private boolean isJumpAnimationPlaying() {
		return ANIMATION_SYSTEM.getAnimationKey().equals("jump") && ANIMATION_SYSTEM.isPlaying();
	}
	
	private boolean isShooting() {
		return ANIMATION_SYSTEM.getAnimationKey().equals("shoot") && ANIMATION_SYSTEM.isPlaying();
	}
	
	private boolean isMovingAnimationPlaying() {
		return ANIMATION_SYSTEM.getAnimationKey().equals("move") && ANIMATION_SYSTEM.isPlaying();
	}
}

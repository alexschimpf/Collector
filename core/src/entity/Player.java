package entity;

import particle.ParticleEffect;
import misc.Globals;
import misc.Globals.State;
import misc.Utils;
import misc.Vector2Pool;
import animation.Animation;
import animation.AnimationSystem;

import com.badlogic.gdx.graphics.Color;
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
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

public final class Player extends Entity {
	
	public static final float MOVE_SPEED = 15;
	public static final float JUMP_IMPULSE = -95;
	public static final float SHOOT_PERIOD = 150;
	public static final float MASS = 5.69f;
	public static final float FALL_HEIGHT_LIMIT = Globals.getTileSize() * 6f;
	
	private final AnimationSystem ANIMATION_SYSTEM = new AnimationSystem();
	
	private boolean isJumping = false;
	private boolean isFacingRight = true;
	private int numFootContacts = 0;
	private long lastShotTime = 0;
	private long lastBlinkTime = TimeUtils.millis();
	private float blinkPeriod = MathUtils.random(1000, 6000);
	private Vector2 lastValidPos = new Vector2();
	private boolean isLastValidDirectionRight = true;
	
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
		ANIMATION_SYSTEM.flipSprite(isFacingLeft(), true);
		
		if(isMoveAnimationPlaying() && Math.abs(getLinearVelocity().x) == MOVE_SPEED) {
			startMoveParticleEffect();
		}
		
		// TODO: Remove this... maybe.
		if(getLinearVelocity().y > 60) {
			respawnPlayer();
		}
			
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
		
		if(!isJumpAnimationPlaying() && !isShootAnimationPlaying() && !isBlinkAnimationPlaying()) {
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
		
		if(!isShootAnimationPlaying() && !isJumpAnimationPlaying()) {
			ANIMATION_SYSTEM.switchAnimation("shoot", false, true);
		}
	}
	
	public boolean isFacingRight() {
		return isFacingRight;
	}
	
	public boolean isFacingLeft() {
		return !isFacingRight;
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
		
		if(!isJumping && getCenterY() - lastValidPos.y > FALL_HEIGHT_LIMIT) {
			respawnPlayer();
		} else if(numFootContacts >= 1) {
			isLastValidDirectionRight = isFacingRight();
			lastValidPos.set(getCenterX(), getCenterY());
		}
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
		
		lastValidPos.set(pos.x, pos.y);
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
		
		if(numFootContacts > 0 && !isJumping && !isMoveAnimationPlaying() && !isJumpAnimationPlaying() && !isShootAnimationPlaying()) {
			ANIMATION_SYSTEM.switchAnimation("move", false, true);
		}
		
		if(numFootContacts == 0 && !isJumpAnimationPlaying() && !isShootAnimationPlaying() && !isBlinkAnimationPlaying()) {
			ANIMATION_SYSTEM.switchToDefault();
		}
	}
	
	private void respawnPlayer() {
		Globals.state = State.PAUSED;
		Timer timer = new Timer();
		timer.scheduleTask(new Task() {
			@Override
			public void run() {
				Globals.state = State.RUNNING;
				Globals.getSoundManager().playSound("transport");
				isFacingRight = isLastValidDirectionRight;
				setLinearVelocity(0, 0);
				setPosition(lastValidPos.x, lastValidPos.y);
			}
		}, 1);
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
		if(!isJumping && !isMoveAnimationPlaying() && TimeUtils.timeSinceMillis(lastBlinkTime) > blinkPeriod) {
			lastBlinkTime = TimeUtils.millis();
			blinkPeriod = MathUtils.random(1000, 5000);
			ANIMATION_SYSTEM.switchAnimation("blink", false, true);
		}
	}
	
	private boolean isBlinkAnimationPlaying() {
		return ANIMATION_SYSTEM.getAnimationKey().equals("blink") && ANIMATION_SYSTEM.isPlaying();
	}
	
	/**
	 * Note: This is different from isJumping().
	 */
	private boolean isJumpAnimationPlaying() {
		return ANIMATION_SYSTEM.getAnimationKey().equals("jump") && ANIMATION_SYSTEM.isPlaying();
	}
	
	private boolean isShootAnimationPlaying() {
		return ANIMATION_SYSTEM.getAnimationKey().equals("shoot") && ANIMATION_SYSTEM.isPlaying();
	}
	
	private boolean isMoveAnimationPlaying() {
		return ANIMATION_SYSTEM.getAnimationKey().equals("move") && ANIMATION_SYSTEM.isPlaying();
	}
	
	private void startMoveParticleEffect() {
		Vector2 v = getLinearVelocity();
		float minVx = -v.x / 13;
		float maxVx = -v.x / 7;
		if(v.x > 0) {
			float temp = minVx;
			minVx = maxVx;
			maxVx = temp;
		}
		
		float x = getLeft();
		if(isFacingLeft()) {
			x = getRight() - getWidth() / 2;
		}

		Vector2Pool pool = Globals.getVector2Pool();
		Vector2 pos = pool.obtain(x, getBottom() - (getHeight() / 12));
		Vector2 minMaxSize = pool.obtain(getWidth() / 6, getWidth() / 2);
		Vector2 minVelocity = pool.obtain(minVx, 0);
		Vector2 maxVelocity = pool.obtain(maxVx,  -Math.abs(v.x) / 3);
		Vector2 minMaxDuration = pool.obtain(250, 500);
		Vector2 minMaxParticles = pool.obtain(0, 1);
		ParticleEffect particleEffect = new ParticleEffect.Builder("shot", pos, minMaxSize, minVelocity, maxVelocity, 
				                                                   minMaxDuration, minMaxParticles)
		.startEndColors(Color.WHITE, Color.LIGHT_GRAY)
		.build();
		particleEffect.start();
	}
}

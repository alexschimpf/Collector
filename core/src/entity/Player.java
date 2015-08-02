	package entity;

import misc.Globals;
import misc.Globals.State;
import misc.IInteractive;
import misc.Utils;
import particle.ParticleEffect;
import animation.Animation;
import animation.AnimationSystem;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.MassData;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

public final class Player extends Entity {
	
	public static final float MOVE_SPEED = 10;
	public static final float JUMP_IMPULSE = -98;
	public static final float MOVE_PARTICLE_DELAY = 100;
	public static final float MASS = 5.69f;
	public static final float FALL_HEIGHT_LIMIT = Globals.getTileSize() * 6f;
	
	private final AnimationSystem _animationSystem = new AnimationSystem();
	
	private boolean _isJumping = false;
	private boolean _isFacingRight = true;
	private boolean _isRespawning = false;
	private int _numFootContacts = 0;
	private long _lastBlinkTime = TimeUtils.millis();
	private float _blinkPeriod = MathUtils.random(5000, 10000);
	private Vector2 _lastValidPos = new Vector2();
	private Vector2 _lastActualPos = new Vector2();
	private boolean _isLastValidDirectionRight = true;
	
	public Player(EntityBodyDef bodyDef, TextureMapObject object, MapObject bodySkeleton) {
		TextureRegion textureRegion = object.getTextureRegion();
		_createSprite(bodyDef, textureRegion);	
		_createBody(bodyDef, bodySkeleton);
		
		_body.setBullet(true);
		
		MassData massData = _body.getMassData();
		massData.mass = MASS;
		_body.setMassData(massData);
		
		_attachFootSensors(bodyDef);
		
		Filter filter = new Filter();
		filter.categoryBits = 0x0001;
		filter.maskBits = (short)~Globals.PLAYER_NO_COLLIDE_MASK;
		_body.getFixtureList().get(0).setFilterData(filter);
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
		_tryBlink();
		
		_animationSystem.update();
		_sprite = _animationSystem.getSprite();
		_animationSystem.flipSprite(isFacingLeft(), true);
		
		if(getLinearVelocity().y > 50) {
			respawn(false, null);
		}

		return super.update();
	}
	
	public boolean jump() {
		if(_isJumping) {
			return false;
		}

		// HACK: If moving vertically, set vy to 0 to reduce jump height.
		float vy = getLinearVelocity().y;
		if(vy > 0.01f || vy < -0.01f) {
			setLinearVelocity(getLinearVelocity().x, 0);
		}

		Globals.getSoundManager().playSound("jump");
		
		float x = _body.getWorldCenter().x;
		float y = _body.getWorldCenter().y;
		_body.applyLinearImpulse(0, JUMP_IMPULSE, x, y, true);
		
		_animationSystem.switchAnimation("jump", false, true);
		
		return true;
	}
	
	public void stopJump() {
		if(getLinearVelocity().y < 0) {
			setLinearVelocity(getLinearVelocity().x, 0.02f);
		}
	}
	
	public void moveLeft() {
		_move(false);
	}
	
	public void moveRight() {
		_move(true);
	}
	
	public void stopMove() {
		setLinearVelocity(0, getLinearVelocity().y);
		
		if(!_isJumpAnimationPlaying() && !_isBlinkAnimationPlaying()) {
			_animationSystem.switchToDefault();
		}
	}

	public void interact() {
		float dist = Globals.getTileSize() / 4;
		
		float upperX, lowerX;
		if(isFacingRight()) {
			lowerX = getRight();
			upperX = getRight() + dist;
		} else {
			lowerX = getLeft() - dist;
			upperX = getLeft();
		}
		
		float correction = getHeight() / 10;

		Globals.getPhysicsWorld().QueryAABB(new QueryCallback() {
			@Override
			public boolean reportFixture(Fixture fixture) {
				if(!Utils.isFromEntity(fixture) || fixture.equals(this)) {
					return true;
				}
				
				Entity entity = Utils.getEntity(fixture);
				if(entity != null && entity instanceof IInteractive) {
					((IInteractive)entity).onInteraction();
				}
				
				return true;
			}			
		}, lowerX, getTop() + correction, upperX, getBottom() - correction);
	}
	
	public void respawn(boolean collided, final Vector2 respawnPos) {
		if(_isRespawning) {
			return;
		}
 		
		_isRespawning = true;
		
		Globals.state = State.PAUSED;
		
		if(collided) {
			Globals.getSoundManager().playSound("die");
			_startDieParticleEffect();
		}
		
		setVisible(false);
		setLinearVelocity(0, 0);
		
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				_body.setActive(false);
			}		
		});
		
		float respawnDelay = collided ? 1 : 0;
		Timer timer = new Timer();
		timer.scheduleTask(new Task() {
			@Override
			public void run() {
				_isRespawning = false;
				
				Globals.getSoundManager().playSound("transport");
				
				_isFacingRight = _isLastValidDirectionRight;
				_animationSystem.switchToDefault();
				
				_body.setActive(true);
				
				if(respawnPos != null) {
					_lastActualPos.set(respawnPos.x, respawnPos.y);
					setPosition(respawnPos.x, respawnPos.y);
				} else {
					_lastActualPos.set(_lastValidPos.x, _lastValidPos.y);
					setPosition(_lastValidPos.x, _lastValidPos.y);
				}

				
				setVisible(true);
				
				Globals.state = State.RUNNING;
			}
		}, respawnDelay);
	}
	
	public boolean isFacingRight() {
		return _isFacingRight;
	}
	
	public boolean isFacingLeft() {
		return !_isFacingRight;
	}
	
	public boolean isJumping() {
		return _isJumping;
	}
	
	public float getFrontX() {
		return _isFacingRight ? getRight() : getLeft();
	}
	
	public float getBackX() {
		return _isFacingRight ? getLeft() : getRight();
	}
	
	public int getNumFootContacts() {
		return _numFootContacts;
	}

	public void incrementFootContacts(Contact contact) {
		_numFootContacts++;
		_isJumping = _numFootContacts < 1;

		Fixture fixture = contact.getFixtureA();
		if(fixture.getBody().equals(_body)) {
			fixture = contact.getFixtureB();
		}
		
		Entity entity = Utils.getEntity(fixture);
		BodyType bodyType = fixture.getBody().getType();
		if(!_isJumping && getCenterY() - _lastActualPos.y > FALL_HEIGHT_LIMIT) {
			respawn(true, null);
		} else if(_numFootContacts >= 1 && !fixture.isSensor()) {	 
			if(bodyType != BodyType.DynamicBody && (bodyType != BodyType.KinematicBody || entity.getBody().getLinearVelocity().isZero()) &&
			  (entity == null || entity.isValidForPlayerRespawn())) {
				_isLastValidDirectionRight = isFacingRight();
				_lastValidPos.set(getCenterX(), getCenterY());
			}
			
			_lastActualPos.set(getCenterX(), getCenterY());
		}
	}
	
	public void decrementFootContacts(Contact contact) {
		_numFootContacts--;
		if(_numFootContacts < 0) {
			_numFootContacts = 0;
		}
		
		_isJumping = _numFootContacts < 1;
	}
	
	public boolean isRespawning() {
		return _isRespawning;
	}
	
	@Override
	protected void _createSprite(EntityBodyDef bodyDef, TextureRegion textureRegion) {
		super._createSprite(bodyDef, textureRegion);
		
		Vector2 pos = bodyDef.position;
		Vector2 size = bodyDef.size;
		
		Animation blinkAnimation = new Animation.Builder("player_blink", pos, size, 0.5f).build();
		Animation jumpAnimation = new Animation.Builder("player_jump", pos, size, 0.3f).build();		
		Animation moveAnimation = new Animation.Builder("player_move", pos, size, 0.2f).loop(true).build();

		_animationSystem.addAnimation("blink", blinkAnimation);
		_animationSystem.addAnimation("jump", jumpAnimation);
		_animationSystem.addAnimation("move", moveAnimation);
		
		_animationSystem.setDefaultSprite("player", size.x, size.y);
		
		_lastActualPos.set(pos.x, pos.y);
		_lastValidPos.set(pos.x, pos.y);
	}
	
	@Override
	protected void _createBody(EntityBodyDef bodyDef, MapObject bodySkeleton) {
		FixtureDef fixtureDef = Utils.getScaledFixtureDefFromBodySkeleton(bodySkeleton, 0.98f);
		_createBodyFromDef(bodyDef, fixtureDef);
	}
	
	private void _move(boolean right) {
		_isFacingRight = right;
		
		float vx = Player.MOVE_SPEED;
		if(!right) {
			vx = 0 - vx;
		}
		
		setLinearVelocity(vx, getLinearVelocity().y);
		
		if(_numFootContacts > 0 && !_isJumping && !_isMoveAnimationPlaying() && !_isJumpAnimationPlaying()) {
			_animationSystem.switchAnimation("move", false, true);
		}
		
		if(_numFootContacts == 0 && !_isJumpAnimationPlaying() && !_isBlinkAnimationPlaying()) {
			_animationSystem.switchToDefault();
		}
	}
	
	private void _attachFootSensors(EntityBodyDef bodyDef) {	
		Vector2 localBottom = _body.getLocalPoint(new Vector2(getCenterX(), getBottom() - ((4.0f / 64.0f) * getHeight())));
		
		float width = bodyDef.size.x;
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(width / 2 * 0.88f, 0.1f, localBottom, 0);
		
		Fixture fixture = _body.createFixture(shape, 0);
		fixture.setSensor(true);
		
		shape.dispose();
	}
	
	private void _tryBlink() {
		if(!_isJumping && !_isMoveAnimationPlaying() && TimeUtils.timeSinceMillis(_lastBlinkTime) > _blinkPeriod) {
			_lastBlinkTime = TimeUtils.millis();
			_blinkPeriod = MathUtils.random(5000, 10000);
			_animationSystem.switchAnimation("blink", false, true);
		}
	}
	
	private boolean _isBlinkAnimationPlaying() {
		return _animationSystem.getAnimationKey().equals("blink") && _animationSystem.isPlaying();
	}

	private boolean _isJumpAnimationPlaying() {
		return _animationSystem.getAnimationKey().equals("jump") && _animationSystem.isPlaying();
	}

	private boolean _isMoveAnimationPlaying() {
		return _animationSystem.getAnimationKey().equals("move") && _animationSystem.isPlaying();
	}
	
	private void _startDieParticleEffect() {
		float x = getCenterX();
		float y = getBottom() - getHeight() / 5;
		ParticleEffect particleEffect = Globals.getParticleEffect("player_dying", x, y);
		particleEffect.minMaxSize(getWidth() * 0.9f, getWidth());
		particleEffect.addToScreen();
	}
}

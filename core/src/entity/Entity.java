package entity;

import misc.BodyData;
import misc.Globals;
import misc.ICollide;
import misc.IRender;
import misc.IUpdate;
import misc.Utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public abstract class Entity implements IRender, IUpdate, ICollide {

	protected final String _id;
	protected final Vector2 _leftTop = new Vector2();
	private final Rectangle _borderRect = new Rectangle();
	
	protected int _numContacts = 0;
	protected boolean _markedDone = false;
	protected boolean _isVisible = true;
	protected boolean _isActive = true;
	protected Body _body;
	protected Sprite _sprite;
	
	public Entity() {
		String id;
		do {
			id = String.valueOf(getClass().getName() + MathUtils.random());
		} while(Globals.getCurrentRoom().entityIdExists(id));
		
		_id = id;
	}
	
	public Entity(EntityBodyDef bodyDef, TextureMapObject object, MapObject bodySkeleton) {
		if(object.getName() != null && !object.getName().isEmpty()) {
			_id = object.getName();
		} else {
			_id = String.valueOf(object.hashCode());
		}
		
		MapProperties properties = object.getProperties();
		
		TextureRegion textureRegion;
		if(properties.containsKey("image_key")) {
			String imageKey = Utils.getPropertyString(object, "image_key");
			textureRegion = Globals.getImageTexture(imageKey);
		} else {
			textureRegion = object.getTextureRegion();
		}		

		_createSprite(bodyDef, textureRegion);	
		_createBody(bodyDef, bodySkeleton);
	}
	
	public abstract String getType();
	
	public void onPostCreate() {
	}
	
	@Override
	public void render(SpriteBatch spriteBatch) {
		if(isVisible() && _sprite.getTexture() != null) {
			_sprite.draw(spriteBatch);
		}
	}
	
	@Override
	public boolean update() {
		_sprite.setPosition(getLeft(), getTop());
		_sprite.setRotation(MathUtils.radiansToDegrees * _body.getAngle());
		
		return _markedDone;
	}
	
	@Override
	public void done() {
		Globals.getPhysicsWorld().destroyBody(_body);
	}
	
	@Override
	public void onBeginContact(Contact contact, Entity entity) {
		_numContacts++;
	}
	
	@Override
	public void onEndContact(Contact contact, Entity entity) {
		_numContacts--;
	}
	
	public String getId() {
		return _id;
	}

	public float getWidth() {
		return _sprite.getWidth();
	}
	
	public float getHeight() {
		return _sprite.getHeight();
	}
	
	public float getLeft() {
		return getCenterX() - (getWidth() / 2);
	}
	
	public float getRight() {
		return getCenterX() + (getWidth() / 2);
	}
	
	public float getTop() {
		return getCenterY() - (getHeight() / 2);
	}
	
	public float getBottom() {
		return getCenterY() + (getHeight() / 2);
	}
	
	public float getCenterX() {
		return _body.getPosition().x;
	}
	
	public float getCenterY() {	
		return _body.getPosition().y;
	}
	
	public Vector2 getCenter() {
		return _body.getPosition();
	}
	
	public Vector2 getLeftTop() {
		return _leftTop.set(getLeft(), getTop());
	}
	
	/**
	 * Used in function isEntityAt(...).
	 */
	public boolean isActive() {
		return _isActive;
	}
	
	public void setActive(boolean active) {
		_isActive = active;
	}
	
	public boolean isVisible() {
		return _isVisible && Globals.getCamera().isVisible(getLeft(), getTop(), getWidth(), getHeight());
	}
	
	public void setPosition(final float centerX, final float centerY) {
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				_body.setTransform(centerX, centerY, _body.getAngle());
			}
		});
	}
	
	public void setRotation(final float angle) {
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				_body.setTransform(getCenterX(), getCenterY(), angle);
			}
		});
	}
	
	public void setLinearVelocity(float vx, float vy) {
		_body.setLinearVelocity(vx, vy);
	}
	
	public Vector2 getLinearVelocity() {
		return _body.getLinearVelocity();
	}
	
	public void setVisible(boolean visible) {
		_isVisible = visible;
	}
	
	public BodyData getBodyData() {
		if(_body == null) {
			return null;
		}
		
		return (BodyData)_body.getUserData();
	}
	
	public void setBodyData() {
		BodyData bodyData = new BodyData(this);
		_body.setUserData(bodyData);
	}
	
	public void markDone() {
		_markedDone = true;
	}
	
	public Body getBody() {
		return _body;
	}
	
	public Sprite getSprite() {
		return _sprite;
	}
	
	public boolean isValidForPlayerRespawn() {
		return true;
	}
	
	public Rectangle getBorderRectangle() {
		return _borderRect.set(getLeft(), getTop(), getWidth(), getHeight());
	}
	
	public boolean overlapsEntity(Entity entity) {
		return getBorderRectangle().overlaps(entity.getBorderRectangle());
	}
	
	public boolean containsEntity(Entity entity) {
		return getBorderRectangle().contains(entity.getBorderRectangle());
	}

	protected void _createSprite(EntityBodyDef bodyDef, TextureRegion textureRegion) {
		Vector2 position = bodyDef.position;
		Vector2 size = bodyDef.size;
		_sprite = new Sprite(textureRegion);	
		_sprite.flip(false, true);
		_sprite.setPosition(position.x, position.y);
		_sprite.setSize(size.x, size.y);
		_sprite.setOrigin(size.x / 2, size.y / 2);
	}

	protected void _createBody(EntityBodyDef bodyDef, MapObject bodySkeleton) {
		FixtureDef fixtureDef = Utils.getFixtureDefFromBodySkeleton(bodySkeleton);
		_createBodyFromDef(bodyDef, fixtureDef);
	}
	
	protected void _createBodyFromDef(EntityBodyDef entityBodyDef, FixtureDef fixtureDef) {
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = entityBodyDef.bodyType;
		
		bodyDef.position.set(entityBodyDef.position);

		_body = Globals.getPhysicsWorld().createBody(bodyDef);		
		_body.setFixedRotation(true);

		_attachFixture(fixtureDef);		
	}
	
	protected void _attachFixture(FixtureDef fixtureDef) {
		_body.createFixture(fixtureDef);
		fixtureDef.shape.dispose();
	}
}

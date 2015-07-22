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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public abstract class Entity implements IRender, IUpdate, ICollide {

	protected final String ID;
	protected final Vector2 LEFT_TOP = new Vector2();
	
	protected int numContacts = 0;
	protected boolean markedDone = false;
	protected boolean isVisible = true;
	protected boolean isActive = true;
	protected Body body;
	protected Sprite sprite;
	
	public Entity() {
		String id;
		do {
			id = String.valueOf(getClass().getName() + MathUtils.random());
		} while(Globals.getCurrentRoom().entityIdExists(id));
		
		ID = id;
	}
	
	public Entity(EntityBodyDef bodyDef, TextureMapObject object, MapObject bodySkeleton) {
		if(object.getName() != null && !object.getName().isEmpty()) {
			ID = object.getName();
		} else {
			ID = String.valueOf(object.hashCode());
		}
		
		MapProperties properties = object.getProperties();
		
		TextureRegion textureRegion;
		if(properties.containsKey("image_key")) {
			String imageKey = Utils.getPropertyString(object, "image_key");
			textureRegion = Globals.getImageTexture(imageKey);
		} else {
			textureRegion = object.getTextureRegion();
		}		

		createSprite(bodyDef, textureRegion);	
		createBody(bodyDef, bodySkeleton);
	}
	
	public abstract String getType();
	
	public void onPostCreate() {
	}
	
	@Override
	public void render(SpriteBatch spriteBatch) {
		if(isVisible() && sprite.getTexture() != null) {
			sprite.draw(spriteBatch);
		}
	}
	
	@Override
	public boolean update() {
		sprite.setPosition(getLeft(), getTop());
		sprite.setRotation(MathUtils.radiansToDegrees * body.getAngle());
		
		return markedDone;
	}
	
	@Override
	public void done() {
		Globals.getPhysicsWorld().destroyBody(body);
	}
	
	@Override
	public void onBeginContact(Contact contact, Entity entity) {
		numContacts++;
	}
	
	@Override
	public void onEndContact(Contact contact, Entity entity) {
		numContacts--;
	}
	
	public String getId() {
		return ID;
	}

	public float getWidth() {
		return sprite.getWidth();
	}
	
	public float getHeight() {
		return sprite.getHeight();
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
		return body.getPosition().x;
	}
	
	public float getCenterY() {	
		return body.getPosition().y;
	}
	
	public Vector2 getCenter() {
		return body.getPosition();
	}
	
	public Vector2 getLeftTop() {
		return LEFT_TOP.set(getLeft(), getTop());
	}
	
	/**
	 * Used in function isEntityAt(...).
	 */
	public boolean isActive() {
		return isActive;
	}
	
	public void setActive(boolean active) {
		isActive = active;
	}
	
	public boolean isVisible() {
		return isVisible && Globals.getCamera().isVisible(getLeft(), getTop(), getWidth(), getHeight());
	}
	
	public void setPosition(final float centerX, final float centerY) {
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				body.setTransform(centerX, centerY, body.getAngle());
			}
		});
	}
	
	public void setLinearVelocity(float vx, float vy) {
		body.setLinearVelocity(vx, vy);
	}
	
	public Vector2 getLinearVelocity() {
		return body.getLinearVelocity();
	}
	
	public void setVisible(boolean visible) {
		isVisible = visible;
	}
	
	public BodyData getBodyData() {
		if(body == null) {
			return null;
		}
		
		return (BodyData)body.getUserData();
	}
	
	public void setBodyData() {
		BodyData bodyData = new BodyData(this);
		body.setUserData(bodyData);
	}
	
	public void markDone() {
		markedDone = true;
	}
	
	public Body getBody() {
		return body;
	}
	
	public Sprite getSprite() {
		return sprite;
	}
	
	public boolean isValidForPlayerRespawn() {
		return true;
	}

	protected void createSprite(EntityBodyDef bodyDef, TextureRegion textureRegion) {
		Vector2 position = bodyDef.position;
		Vector2 size = bodyDef.size;
		sprite = new Sprite(textureRegion);	
		sprite.flip(false, true);
		sprite.setPosition(position.x, position.y);
		sprite.setSize(size.x, size.y);
		sprite.setOrigin(size.x / 2, size.y / 2);
	}

	protected void createBody(EntityBodyDef bodyDef, MapObject bodySkeleton) {
		FixtureDef fixtureDef = Utils.getFixtureDefFromBodySkeleton(bodySkeleton);
		createBody(bodyDef, fixtureDef);
	}
	
	protected void createBody(EntityBodyDef entityBodyDef, FixtureDef fixtureDef) {
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = entityBodyDef.bodyType;
		
		bodyDef.position.set(entityBodyDef.position);

		body = Globals.getPhysicsWorld().createBody(bodyDef);		
		body.setFixedRotation(true);

		attachFixture(fixtureDef);		
	}
	
	protected void attachFixture(FixtureDef fixtureDef) {
		body.createFixture(fixtureDef);
		fixtureDef.shape.dispose();
	}
}

package entity;

import misc.Globals;
import misc.Utils;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;

public class PressureButtonEntity extends Entity {

	private final String _scriptId;
	
	private boolean _needsReleased;
	private Body _anchorBody;

	public PressureButtonEntity(EntityBodyDef bodyDef, TextureMapObject object, MapObject bodySkeleton) {
		super(bodyDef, object, bodySkeleton);
		
		_scriptId = Utils.getPropertyString(object, "script_id");
	}
	
	@Override
	public String getType() {
		return "pressure_button";
	}
	
	@Override
	public boolean update() {
		if(_isPressed() && !_needsReleased) {
			_needsReleased = true;		
			Globals.getCurrentRoom().startScript(_scriptId);
		}
		
		_checkReleased();
		
		return super.update();
	}
	
	@Override
	protected void _createBodyFromDef(EntityBodyDef entityBodyDef, FixtureDef fixtureDef, boolean fixedRotation) {
		super._createBodyFromDef(entityBodyDef, fixtureDef, fixedRotation);

		_body.setGravityScale(0);
		
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.StaticBody;
		bodyDef.position.set(entityBodyDef.position.x, entityBodyDef.position.y + getHeight());	

		_anchorBody = Globals.getPhysicsWorld().createBody(bodyDef);
		
		FixtureDef anchorFixtureDef = new FixtureDef();
		PolygonShape anchorShape = new PolygonShape();
		anchorShape.setAsBox(getWidth() / 2, getHeight() / 2);
		anchorFixtureDef.shape = anchorShape;	
		anchorFixtureDef.isSensor = true;
		_anchorBody.createFixture(anchorFixtureDef);
		
		anchorShape.dispose();
		
		_createJoint();
	}
	
	protected void _createJoint() {
		PrismaticJointDef jointDef = new PrismaticJointDef();
		Vector2 axis = new Vector2(0, 1);
		jointDef.initialize(_anchorBody, _body, _body.getWorldCenter(), axis);		
		jointDef.motorSpeed = -1000;
		jointDef.maxMotorForce = .42f * Globals.getPlayer().getBody().getMass() * Globals.getPhysicsWorld().getGravity().y;
		jointDef.enableMotor = true;
		jointDef.lowerTranslation = 0;
		jointDef.upperTranslation = getHeight();
		jointDef.enableLimit = true;
		jointDef.collideConnected = false;
		
		Globals.getPhysicsWorld().createJoint(jointDef);
	}
	
	protected void _checkReleased() {
		if(getCenterY() <= _anchorBody.getPosition().y - getHeight()) {
			_needsReleased = false;
		}
	}
	
	protected boolean _isPressed() {
		return getCenterY() >= _anchorBody.getPosition().y;
	}
}
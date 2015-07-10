package entity;

import misc.Globals;
import misc.IInteractive;
import misc.Utils;
import animation.Animation;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;

public final class ProgrammableTriggerEntity extends Entity {

	private enum TriggerType {
		RESET, MOVE
	}
	
	private final TriggerType TRIGGER_TYPE;
	private final String[] TARGET_IDS;
	
	private Animation animation;
	
	public ProgrammableTriggerEntity(EntityBodyDef bodyDef, TextureMapObject object, MapObject bodySkeleton) {
		super(bodyDef, object, bodySkeleton);
		
		MapProperties properties = object.getProperties();
		String triggerTypeStr = Utils.getPropertyString(properties, "trigger_type");
		if(triggerTypeStr.equals("reset")) {
			TRIGGER_TYPE = TriggerType.RESET;
		} else if(triggerTypeStr.equals("move")) {
			TRIGGER_TYPE = TriggerType.MOVE;
		} else {
			throw new NullPointerException("Trigger type '" + triggerTypeStr + "' is not valid");
		}
		
		TARGET_IDS = Utils.getPropertyStringArray(properties, "target_ids", ",");
	}
	
	@Override
	public String getType() {
		return "programmable_trigger";
	}
	
	@Override
	public boolean update() {
		animation.update();
		sprite = animation.getSprite();
		sprite.setFlip(false, true);
		
		return super.update();
	}
	
	@Override
	public void onBeginContact(Contact contact, Entity entity) {
		if(!Utils.isPlayerShot(entity)) {
			return;
		}
		
		animation.play();
		
		for(String targetId : TARGET_IDS) {
			ProgrammableEntity target = (ProgrammableEntity)Globals.getGameWorld().getEntityById(targetId);
			if(target.isActivated()) {
				target.move();
			}
		}
	}
	
	@Override
	protected void createSprite(EntityBodyDef bodyDef, TextureRegion textureRegion) {
		super.createSprite(bodyDef, textureRegion);
		
		Vector2 pos = bodyDef.position;
		Vector2 size = bodyDef.size;		
		animation = new Animation.Builder("programmable_trigger", pos, size, 0.2f).build();
	}
}

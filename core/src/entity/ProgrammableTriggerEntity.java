package entity;

import misc.Globals;
import misc.IInteractive;
import misc.Utils;
import animation.Animation;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.math.Vector2;

public final class ProgrammableTriggerEntity extends Entity implements IInteractive {

	private enum TriggerType {
		RESET, MOVE
	}
	
	private final TriggerType _triggerType;
	private final String[] _targetIds;
	
	private Animation _animation;
	
	public ProgrammableTriggerEntity(EntityBodyDef bodyDef, TextureMapObject object, MapObject bodySkeleton) {
		super(bodyDef, object, bodySkeleton);
		
		String triggerTypeStr = Utils.getPropertyString(object, "trigger_type");
		if(triggerTypeStr.equals("reset")) {
			_triggerType = TriggerType.RESET;
		} else if(triggerTypeStr.equals("move")) {
			_triggerType = TriggerType.MOVE;
		} else {
			throw new NullPointerException("Trigger type '" + triggerTypeStr + "' is not valid");
		}
		
		_targetIds = Utils.getPropertyStringArray(object, "target_ids", ",");
	}
	
	@Override
	public String getType() {
		return "programmable_trigger";
	}
	
	@Override
	public boolean update() {
		_animation.update();
		_sprite = _animation.getSprite();
		_sprite.setFlip(false, true);
		
		return super.update();
	}
	
	@Override
	public void onInteraction() {
		_animation.play();
		
		for(String targetId : _targetIds) {
			ProgrammableEntity target = (ProgrammableEntity)Globals.getCurrentRoom().getEntityById(targetId);
			if(target.isActivated()) {
				target.move();
			}
		}
	}
	
	@Override
	protected void _createSprite(EntityBodyDef bodyDef, TextureRegion textureRegion) {
		super._createSprite(bodyDef, textureRegion);
		
		Vector2 pos = bodyDef.position;
		Vector2 size = bodyDef.size;		
		_animation = new Animation.Builder("programmable_trigger", pos, size, 0.2f).build();
	}
}

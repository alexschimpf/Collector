package entity;

import misc.Globals;
import misc.Utils;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.physics.box2d.Contact;

public class DisappearingTriggerEntity extends Entity {

	private String[] _targetIds;
	
	public DisappearingTriggerEntity(EntityBodyDef bodyDef, TextureMapObject object, MapObject bodySkeleton) {
		super(bodyDef, object, bodySkeleton);
		
		_targetIds = Utils.getPropertyStringArray(object, "target_ids", ",");
	}
	
	@Override
	public String getType() {
		return "disappearing_trigger";
	}
	
	@Override
	public void onBeginContact(Contact contact, Entity entity) {
		if(Utils.isPlayer(entity)) {
			_makeTargetsDisappear();
		}
	}	
	
	private void _makeTargetsDisappear() {
		for(String targetId : _targetIds) {
			if(Globals.getCurrentRoom().entityIdExists(targetId)) {
				DisappearingEntity target = (DisappearingEntity)Globals.getCurrentRoom().getEntityById(targetId);
				if(!target.isDisappearing()) {
					target.disappear();
				}
			}	
		}
	}
}

package script;

import misc.Globals;
import misc.Utils;

import com.badlogic.gdx.maps.MapObject;

import entity.ProgrammableEntity;

public class UpdateProgrammablesScript extends Script {

	private final String _updateType; // 'reset' or 'move'
	private final String[] _targetIds;
	
	protected UpdateProgrammablesScript(MapObject object) {
		super(object);
		
		_targetIds = Utils.getPropertyStringArray(object, "target_ids", ",");
		_updateType = Utils.getPropertyString(object, "update_type");
	}

	@Override
	public boolean update() {
		return false;
	}

	@Override
	public String getType() {
		return "update_programmables";
	}
	
	@Override
	public void onStart() {
		for(String targetId : _targetIds) {
			ProgrammableEntity target = (ProgrammableEntity)Globals.getCurrentRoom().getEntityById(targetId);		
			if(target.isActivated() && _updateType.equals("move")) {
				target.move();
			} else if(_updateType.equals("reset")) {
				target.reset();
			}
		}	
	}
}

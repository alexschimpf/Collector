package script;

import misc.Globals;
import misc.Utils;

import com.badlogic.gdx.maps.MapObject;

import entity.ProgrammableEntity;

public class UpdateProgrammableScript extends Script {

	private final String _updateType; // 'reset' or 'move'
	private final String _targetId;
	
	protected UpdateProgrammableScript(MapObject object) {
		super(object);
		
		_targetId = Utils.getPropertyString(object, "target_id");
		_updateType = Utils.getPropertyString(object, "update_type");
	}

	@Override
	public boolean update() {
		return false;
	}

	@Override
	public String getType() {
		return "update_programmable";
	}
	
	@Override
	public void onStart() {
		ProgrammableEntity target = (ProgrammableEntity)Globals.getCurrentRoom().getEntityById(_targetId);
		
		if(_updateType.equals("move")) {
			target.move();
		} else {
			target.reset();
		}		
	}
}

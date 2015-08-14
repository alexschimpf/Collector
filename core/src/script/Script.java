package script;

import misc.IUpdate;
import misc.Utils;

import com.badlogic.gdx.maps.MapObject;

public abstract class Script implements IUpdate {

	protected final String _id;
	protected final MapObject _mapObject;

	public static Script build(MapObject object) {
		String type = Utils.getPropertyString(object, "type");
		if(type.equals("move_entity")) {
			return new MoveEntityScript(object);
		} else if(type.equals("update_programmables")) {
			return new UpdateProgrammablesScript(object);
		}
		
		throw new NullPointerException("Script type '" + type + "' is not valid");
	}
	
	protected Script(MapObject object) {
		_mapObject = object;
		
		if(object.getName() != null && !object.getName().isEmpty()) {
			_id = object.getName();
		} else {
			_id = String.valueOf(object.hashCode());
		}
	}

	public abstract String getType();

	@Override
	public void done() {
	}
	
	public void onStart() {
	}
	
	public String getId() {
		return _id;
	}
}

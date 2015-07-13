package script;

import com.badlogic.gdx.maps.MapObject;

import misc.Globals;
import misc.IUpdate;
import misc.Utils;

public abstract class Script implements IUpdate {

	protected final String ID;
	protected final MapObject MAP_OBJECT;

	public static Script build(MapObject object) {
		String type = Utils.getPropertyString(object, "type");
		if(type.equals("move_entity")) {
			return new MoveEntityScript(object);
		}
		
		throw new NullPointerException("Script type '" + type + "' is not valid");
	}
	
	protected Script(MapObject object) {
		MAP_OBJECT = object;
		
		if(object.getName() != null && !object.getName().isEmpty()) {
			ID = object.getName();
		} else {
			ID = String.valueOf(object.hashCode());
		}
	}

	public abstract String getType();

	@Override
	public void done() {
	}
	
	public void onStart() {
	}
	
	public String getId() {
		return ID;
	}
}

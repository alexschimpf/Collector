package misc;

import entity.Entity;

public final class BodyData {

	private boolean _validForRespawn = true;
	private Entity _entity;

	public BodyData(Entity entity) {
		_entity = entity;;
	}
	
	public BodyData(Entity entity, boolean validForRespawn) {
		_entity = entity;
		_validForRespawn = validForRespawn;
	}

	public void markEntityDone() {
		_entity.markDone();
	}
	
	public Entity getEntity() {
		return _entity;
	}
	
	public boolean isValidForRespawn() {
		return _validForRespawn;
	}
}

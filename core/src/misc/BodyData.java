package misc;

import entity.Entity;

public final class BodyData {

	private Entity _entity;

	public BodyData(Entity entity) {
		_entity = entity;;
	}

	public void markEntityDone() {
		_entity.markDone();
	}
	
	public Entity getEntity() {
		return _entity;
	}
}

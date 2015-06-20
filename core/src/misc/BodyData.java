package misc;

import entity.Entity;

public final class BodyData {

	private Entity entity;
	
	public BodyData(Entity entity) {
		this.entity = entity;;
	}

	public void markEntityDone() {
		entity.markDone();
	}
	
	public Entity getEntity() {
		return this.entity;
	}
}

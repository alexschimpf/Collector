package misc;

import entity.Entity;

public interface ICollide {

	public void onBeginContact(Entity entity);
	
	public void onEndContact(Entity entity);
}

package misc;

import com.badlogic.gdx.physics.box2d.Contact;

import entity.Entity;

public interface ICollide {

	public void onBeginContact(Contact contact, Entity entity);
	
	public void onEndContact(Contact contact, Entity entity);
}

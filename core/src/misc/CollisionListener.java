package misc;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

import entity.Entity;
import entity.Player;

public final class CollisionListener implements ContactListener {

	@Override
	public void beginContact(Contact contact) {
		onContact(contact, true);
	}

	@Override
	public void endContact(Contact contact) {
		onContact(contact, false);
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {

	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {

	}
	
	private void onContact(Contact contact, boolean beginContact) {
		Fixture fixA = contact.getFixtureA();
		Fixture fixB = contact.getFixtureB();
		
		Body bodyA = fixA.getBody();
		Body bodyB = fixB.getBody();

		BodyData dataA = (BodyData)bodyA.getUserData();
		BodyData dataB = (BodyData)bodyB.getUserData();
		if(dataA == null || dataB == null) {
			if(beginContact) {
				if(dataA == null || dataB == null) {
					// Shot may have hit a non-entity body.
					if(dataA != null || dataB != null) {
						Entity entity = dataA != null ? dataA.getEntity() : dataB.getEntity();
						if(entity.getType().equals("shot")) {
							entity.onBeginContact(null);
						}
					}
					
					return;
				}
			} else {
				return;
			}
		}
		
		Entity a = dataA.getEntity();
		Entity b = dataB.getEntity();
		if(a == null || b == null) {
			return;
		}
		
		checkFootContacts(fixA, fixB, a, b, beginContact);
		
		if(beginContact) {
			a.onBeginContact(b);
			b.onBeginContact(a);
		} else {
			a.onEndContact(b);
			b.onEndContact(a);
		}		
	}
	
	private void checkFootContacts(Fixture fixA, Fixture fixB, Entity a, Entity b, boolean beginContact) {
		if(fixA.isSensor() && fixB.isSensor()) {
			return;
		}
		
		boolean footContact = (fixA.isSensor() && a.getType().equals("player")) ||
	                          (fixB.isSensor() && b.getType().equals("player")); 
        if(footContact) {
        	Player player = Globals.getPlayer();      	
        	if(beginContact) {
        		player.incrementFootContacts();
        	} else {
        		player.decrementFootContacts();
        	}
        }
	}
}

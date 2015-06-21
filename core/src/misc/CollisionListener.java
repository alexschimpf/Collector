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
		Entity a = dataA.getEntity();
		Entity b = dataB.getEntity();
		checkPlayerFootContacts(fixA, fixB, a, b, beginContact);
		
		if(a == null || b == null) {
			if(Utils.isPlayerShot(a)) {
				if(beginContact) {
					a.onBeginContact(b);
				} else {
					a.onEndContact(b);
				}
			} else if(Utils.isPlayerShot(b)) {
				if(beginContact) {
					b.onBeginContact(a);
				} else {
					b.onEndContact(a);
				}
			}
			return;
		}

		if(beginContact) {
			a.onBeginContact(b);
			b.onBeginContact(a);
		} else {
			a.onEndContact(b);
			b.onEndContact(a);
		}		
	}
	
	private void checkPlayerFootContacts(Fixture fixA, Fixture fixB, Entity a, Entity b, boolean beginContact) {
		if((fixA != null && fixA.isSensor() && Utils.isPlayer(a) && (fixB == null || !fixB.isSensor())) ||
		   (fixB != null && fixB.isSensor() && Utils.isPlayer(b) && (fixA == null || !fixA.isSensor()))) {
			Player player = Globals.getPlayer();      	
        	if(beginContact) {
        		player.incrementFootContacts();
        	} else {
        		player.decrementFootContacts();
        	}
		} 
	}
}

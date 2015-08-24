package misc;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

import entity.Entity;
import entity.Player;
import entity.ProjectileEntity;

public final class CollisionListener implements ContactListener {

	@Override
	public void beginContact(Contact contact) {
		_onContact(contact, true);
	}

	@Override
	public void endContact(Contact contact) {
		_onContact(contact, false);
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {

	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {

	}
	
	private void _onContact(Contact contact, boolean beginContact) {
		Fixture fixA = contact.getFixtureA();
		Fixture fixB = contact.getFixtureB();
		
		Body bodyA = fixA.getBody();
		Body bodyB = fixB.getBody();
		BodyData dataA = (BodyData)bodyA.getUserData();
		BodyData dataB = (BodyData)bodyB.getUserData();
		if(dataA == null || dataB == null) {
			return;
		}
		
		Entity a = dataA.getEntity();
		Entity b = dataB.getEntity();
		_checkPlayerFootContacts(contact, a, b, beginContact);
		
		if(_checkProjectileContact(contact, a, b, beginContact) || a == null || b == null) {
			return;
		}

		if(beginContact) {
			a.onBeginContact(contact, b);
			b.onBeginContact(contact, a);
		} else {
			a.onEndContact(contact, b);
			b.onEndContact(contact, a);
		}		
	}
	
	private void _checkPlayerFootContacts(Contact contact, Entity a, Entity b, boolean beginContact) {
		Fixture fixA = contact.getFixtureA();
		Fixture fixB = contact.getFixtureB();
		if((fixA.isSensor() && Utils.isPlayer(a) && !fixB.isSensor()) ||
		   (fixB.isSensor() && Utils.isPlayer(b) && !fixA.isSensor())) {
			Player player = Globals.getPlayer();
        	if(beginContact) {
        		player.incrementFootContacts(contact);
        	} else {
        		player.decrementFootContacts(contact);
        	}
		} 
	}
	
	private boolean _checkProjectileContact(Contact contact, Entity a, Entity b, boolean beginContact) {
		Entity projectile;
		Entity other;
		if(a != null && a instanceof ProjectileEntity) {
			projectile = a;
			other = b;
		} else if(b != null && b instanceof ProjectileEntity) {
			projectile = b;
			other = a;
		} else {
			return false;
		}

		if(beginContact) {
			projectile.onBeginContact(contact, other);
		} else {
			projectile.onEndContact(contact, other);
		}
		
		return true;
	}
}

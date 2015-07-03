package entity;

import misc.Globals;
import misc.Utils;
import misc.Vector2Pool;
import particle.ParticleEffect;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;

import entity.special.Player;

public class CollectableEntity extends Entity {

	public CollectableEntity(EntityBodyDef bodyDef, TextureMapObject object, MapObject bodySkeleton) {
		super(bodyDef, object, bodySkeleton);
	}
	
	@Override
	public String getType() {
		return "collectable";
	}

	@Override
	public void onBeginContact(Contact contact, Entity entity) {
		if(!Utils.isPlayer(entity)) {
			return;
		}
		
		contact.setEnabled(false);
		Globals.incrementNumCollected();

		ParticleEffect particleEffect = Globals.getParticleEffectManager().getParticleEffect("collectable", getCenterX(), getCenterY());
		particleEffect.minMaxSize(getWidth() / 5, getWidth());
		particleEffect.addToScreen();
		
		markDone();
	}
}
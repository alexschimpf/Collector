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
		
		startContactParticleEffect();
		
		markDone();
	}
	
	private void startContactParticleEffect() {
		float v = Player.MOVE_SPEED / 5;
		Vector2Pool pool = Globals.getVector2Pool();
		Vector2 pos = pool.obtain(getCenterX(), getCenterY());
		Vector2 minMaxSize = pool.obtain(getWidth() / 5, getWidth());
		Vector2 minVelocity = pool.obtain(-v, -v);
		Vector2 maxVelocity = pool.obtain(v, v);
		Vector2 minMaxDuration = pool.obtain(500, 1000);
		Vector2 minMaxParticles = pool.obtain(5, 10);
		new ParticleEffect.Builder("player_shot", pos, minMaxSize, minVelocity, maxVelocity, 
				                   minMaxDuration, minMaxParticles)
		.vSplits(v / 4, v / 4)
		.startEndColors(Color.WHITE, Color.RED)
		.build()
		.start();
	}
}
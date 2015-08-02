package entity;

import misc.Globals;
import misc.Utils;
import particle.ParticleEffect;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.physics.box2d.Contact;

import core.HUD;

public class CollectableEntity extends Entity {

	private final String _narrationText;
	
	public CollectableEntity(EntityBodyDef bodyDef, TextureMapObject object, MapObject bodySkeleton) {
		super(bodyDef, object, bodySkeleton);
		
		_narrationText = Utils.getPropertyString(object, "narration_text");
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
		
		Globals.getSoundManager().playSound("collect");
		
		contact.setEnabled(false);
		Globals.incrementNumCollected();

		ParticleEffect particleEffect = Globals.getParticleEffect("collectable", getCenterX(), getCenterY());
		particleEffect.minMaxSize(getWidth() / 5, getWidth());
		particleEffect.addToScreen();
		
		Globals.getHUD().showText(_narrationText, HUD.DEFAULT_NARRATION_DURATION);
		
		markDone();
	}
	
	@Override
	public boolean isValidForPlayerRespawn() {
		return false;
	}
}
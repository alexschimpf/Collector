package entity;

import misc.Globals;
import misc.Utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;

import entity.special.Player;

public final class BumpEnableEntity extends Entity {

	public BumpEnableEntity(EntityBodyDef bodyDef, TextureMapObject object, MapObject bodySkeleton) {
		super(bodyDef, object, bodySkeleton);
		
		boolean enabled = Utils.getPropertyBoolean(object, "enabled");	
		setEnabled(enabled);
	}
	
	@Override
	public String getType() {
		return "bump_enable";
	}

	@Override
	public void onBeginContact(Contact contact, Entity entity) {
		if(!Utils.isPlayer(entity)) {
			return;
		}
		
		Player player = Globals.getPlayer();
		if(player.getTop() >= getBottom() - (getHeight() / 5) &&
		   player.getCenterX() > getLeft() && player.getCenterX() < getRight())	{
			Gdx.app.postRunnable(new Runnable() {
				@Override
				public void run() {
					Fixture fixture = body.getFixtureList().get(0);
					setEnabled(fixture.isSensor());
				}				
			});
		}
	}
	
	private void setEnabled(boolean enabled) {
		int index = enabled ? 2 : 1;
		sprite.setRegion(Globals.getImageTexture("bump_enable", index));
		sprite.setFlip(false, true);
		
		Fixture fixture = body.getFixtureList().get(0);
		fixture.setSensor(!enabled);
	}
}

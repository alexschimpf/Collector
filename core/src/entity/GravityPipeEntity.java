package entity;

import misc.Globals;
import misc.Utils;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;

import core.GameWorld;

public class GravityPipeEntity extends Entity {

	private final Rectangle _playerRect = new Rectangle();

	private Vector2 _gravity;
	
	public GravityPipeEntity(EntityBodyDef bodyDef, TextureMapObject object, MapObject bodySkeleton) {
		super(bodyDef, object, bodySkeleton);
		
		_gravity = Utils.getPropertyVector2(object, "gravity");
		
		Fixture fixture = _body.getFixtureList().get(0);
		fixture.setSensor(true);
	}
	
	@Override
	public String getType() {
		return "gravity_pipe";
	}
	
	@Override
	public boolean update() {
		Player player = Globals.getPlayer();
		_playerRect.set(player.getLeft() + (player.getWidth() * 0.05f), player.getTop() + (player.getHeight() * 0.05f), 
				        player.getWidth() * 0.95f, player.getHeight() * 0.95f);
		if(getBorderRectangle().overlaps(_playerRect)) {
			Globals.getPhysicsWorld().setGravity(_gravity);
			player.setGravityPipe(this);
		} else if(player.getGravityPipe() == this) {
			Globals.getPhysicsWorld().setGravity(new Vector2(0, GameWorld.DEFAULT_GRAVITY));
			player.setGravityPipe(null);
		}
		
		return super.update();
	}

	public void setGravity(float x, float y) {
		_gravity.set(x, y);
	}
}

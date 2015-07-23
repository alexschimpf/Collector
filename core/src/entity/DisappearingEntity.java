package entity;

import misc.Globals;
import misc.Utils;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

public final class DisappearingEntity extends Entity {

	private EntityBodyDef _entityBodyDef;
	private TextureMapObject _mapObject;
	private MapObject _bodySkeleton;
	private final float _disappearDuration;
	private final float _recreateDelay;
	private final boolean disappearOnTouch;
	private final boolean _recreate;
	
	private long _disappearStartTime;
	private boolean _disappearing = false;
	
	public DisappearingEntity(EntityBodyDef bodyDef, TextureMapObject object, MapObject bodySkeleton) {
		super(bodyDef, object, bodySkeleton);
		
		_entityBodyDef = bodyDef;
		_mapObject = object;
		_bodySkeleton = bodySkeleton;
		
		_disappearDuration = Utils.getPropertyFloat(object, "disappear_duration");
		_recreateDelay = Utils.getPropertyFloat(object, "recreate_delay");
		disappearOnTouch = Utils.getPropertyBoolean(object, "disappear_on_touch");
		_recreate = Utils.getPropertyBoolean(object, "recreate");
	}
	
	@Override
	public String getType() {
		return "disappearing";
	}
	
	@Override
	public boolean update() {
		if(_disappearing) {
			float timeSinceDisappearStart = TimeUtils.millis() - _disappearStartTime;			
			_sprite.setAlpha(1 - (timeSinceDisappearStart / _disappearDuration));
			
			if(timeSinceDisappearStart > _disappearDuration) {
				_disappearing = false;
				markDone();
			}
		}
		
		return super.update();
	}
	
	@Override
	public void done() {
		if(_recreate) {
			_recreate();
		}
		
		super.done();
	}

	@Override
	public void onBeginContact(Contact contact, Entity entity) {
		if(disappearOnTouch && Utils.isPlayer(entity)) {
			_disappear();
		}
	}	
	
	@Override
	public boolean isValidForPlayerRespawn() {
		return false;
	}
	
	private void _disappear() {
		if(!_disappearing) {
			_disappearing = true;
			_disappearStartTime = TimeUtils.millis();
		}
	}
	
	private void _recreate() {
		Timer timer = new Timer();
		timer.scheduleTask(new Task() {
			@Override
			public void run() {
//				Player player = Globals.getPlayer();
//				if(Globals.getGameWorld().isEntityAt2(getLeft(), getBottom(), getWidth(), getHeight(), player)) {
//					player.respawnPlayer();
//				}
				
				DisappearingEntity entity = new DisappearingEntity(_entityBodyDef, _mapObject, _bodySkeleton);
				entity.setBodyData();
				Globals.getCurrentRoom().addEntity(entity);
			}			
		}, _recreateDelay / 1000);	
	}
}

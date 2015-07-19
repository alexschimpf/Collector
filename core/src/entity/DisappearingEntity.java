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

	private EntityBodyDef ENTITY_BODY_DEF;
	private TextureMapObject MAP_OBJECT;
	private MapObject BODY_SKELETON;
	private final float DISAPPEAR_DURATION;
	private final float RECREATE_DELAY;
	private final boolean DISAPPEAR_ON_TOUCH;
	private final boolean RECREATE;
	
	private long disappearStartTime;
	private boolean disappearing = false;
	
	public DisappearingEntity(EntityBodyDef bodyDef, TextureMapObject object, MapObject bodySkeleton) {
		super(bodyDef, object, bodySkeleton);
		
		ENTITY_BODY_DEF = bodyDef;
		MAP_OBJECT = object;
		BODY_SKELETON = bodySkeleton;
		
		DISAPPEAR_DURATION = Utils.getPropertyFloat(object, "disappear_duration");
		RECREATE_DELAY = Utils.getPropertyFloat(object, "recreate_delay");
		DISAPPEAR_ON_TOUCH = Utils.getPropertyBoolean(object, "disappear_on_touch");
		RECREATE = Utils.getPropertyBoolean(object, "recreate");
	}
	
	@Override
	public String getType() {
		return "disappearing";
	}
	
	@Override
	public boolean update() {
		if(disappearing) {
			float timeSinceDisappearStart = TimeUtils.millis() - disappearStartTime;			
			sprite.setAlpha(1 - (timeSinceDisappearStart / DISAPPEAR_DURATION));
			
			if(timeSinceDisappearStart > DISAPPEAR_DURATION) {
				disappearing = false;
				markDone();
			}
		}
		
		return super.update();
	}
	
	@Override
	public void done() {
		if(RECREATE) {
			recreate();
		}
		
		super.done();
	}

	@Override
	public void onBeginContact(Contact contact, Entity entity) {
		if(DISAPPEAR_ON_TOUCH && Utils.isPlayer(entity)) {
			disappear();
		}
	}	
	
	@Override
	public boolean isValidForPlayerRespawn() {
		return false;
	}
	
	private void disappear() {
		if(!disappearing) {
			disappearing = true;
			disappearStartTime = TimeUtils.millis();
		}
	}
	
	private void recreate() {
		Timer timer = new Timer();
		timer.scheduleTask(new Task() {
			@Override
			public void run() {
//				Player player = Globals.getPlayer();
//				if(Globals.getGameWorld().isEntityAt2(getLeft(), getBottom(), getWidth(), getHeight(), player)) {
//					player.respawnPlayer();
//				}
				
				DisappearingEntity entity = new DisappearingEntity(ENTITY_BODY_DEF, MAP_OBJECT, BODY_SKELETON);
				entity.setBodyData();
				Globals.getCurrentRoom().addEntity(entity);
			}			
		}, RECREATE_DELAY / 1000);	
	}
}

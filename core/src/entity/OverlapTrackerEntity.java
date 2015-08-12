package entity;

import misc.Globals;
import misc.Utils;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.TimeUtils;

public class OverlapTrackerEntity extends Entity {

	protected final float _duration;
	
	protected boolean _done = false;
	protected Long _startTime = null;
	
	public OverlapTrackerEntity(EntityBodyDef bodyDef, TextureMapObject object, MapObject bodySkeleton) {
		super(bodyDef, object, bodySkeleton);
		
		_duration = Utils.getPropertyFloat(object, "duration");
		
		_setSolid(false);
	}
	
	@Override
	public String getType() {
		return "overlap_tracker";
	}
	
	@Override
	public boolean update() {
		if(_done && !_isEnclosing) {
			return super.update();
		}
		
		if(_startTime != null && TimeUtils.timeSinceMillis(_startTime) > _duration) {
			_done = true;
		}
		
		if(_startTime != null && !_done) {
			float ratio = Math.min(TimeUtils.timeSinceMillis(_startTime) / _duration, 1);
			setColor(1 - ratio, 1 - ratio, 1 - ratio, 1);
		} else if(_startTime == null) {
			setColor(1, 1, 1, 1);
		}
		
		if(overlapsEntity(Globals.getPlayer())) {
			if(_startTime == null) {
				_startTime = TimeUtils.millis();
			}
		} else if(_done) {
			_setSolid(true);
		} else {
			_startTime = null;
		}
		
		return super.update();
	}
	
	protected void _setSolid(boolean solid) {
		_isEnclosing = !solid;
		Fixture fixture = _body.getFixtureList().get(0);
		fixture.setSensor(!solid);
	}
}

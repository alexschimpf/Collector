package entity;

import misc.Globals;
import misc.Utils;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

public class DiscreteMovingEntity extends Entity implements IMovingEntity {

	protected boolean _restartOnBlocked;
	protected final int[] _rotations;
	protected final Array<Vector2> _path;
	
	protected float[] _intervals;
	protected long _lastMoveTime;
	protected int _pathPos;
	protected boolean _started = false;
	
	public DiscreteMovingEntity(EntityBodyDef bodyDef, TextureMapObject object, MapObject bodySkeleton) {
		super(bodyDef, object, bodySkeleton);
		
		_rotations = Utils.getPropertyIntArray(object, "rotations", ",");
		_restartOnBlocked = Utils.getPropertyBoolean(object, "restart_on_blocked");

		_intervals = Utils.getPropertyFloatArray(object, "intervals", ",");
		
		String[] path = Utils.getPropertyStringArray(object, "path", " ");
		_path = _buildPath(path);
		
		_started = Utils.getPropertyBoolean(object, "start_on_create");
		
		if(_started) {
			start();
		}
	}

	@Override
	public String getType() {
		return "discrete_moving";
	}

	@Override
	public boolean update() {
		if(!_started) {
			return super.update();
		}
		
		_checkNextMove();
		
		return super.update();
	}
	
	@Override
	public void start() {
		_pathPos = 0;
		_lastMoveTime = TimeUtils.millis();
		_started = true;
	}
	
	@Override
	public void pause() {
		_started = false;
	}
	
	@Override
	public void setPath(String[] serializedPath) {
		_pathPos = 0;
		_path.clear();
		_path.addAll(_buildPath(serializedPath));
	}
	
	@Override
	public void setIntervals(float[] intervals) {
		this._intervals = intervals;
	}
	
	protected Array<Vector2> _buildPath(String[] serializedPath) {
		Array<Vector2> path = new Array<Vector2>();
		
		float x = getLeft();
		float y = getTop();
		path.add(new Vector2(x, y));
		
		for(String link : serializedPath) {
			int separatorIdx = link.indexOf(",");
			float dx = Float.parseFloat(link.substring(1, separatorIdx));
			float dy = Float.parseFloat(link.substring(separatorIdx + 1, link.length() - 1));
			x += (dx * Globals.getTileSize());
			y += (dy * Globals.getTileSize());
			path.add(new Vector2(x, y));
		}

		return path;
	}

	protected void _checkNextMove() {
		if(_rotations.length > 0) {
			int angleDeg = _rotations[_pathPos] * 90;
			float angleRad = MathUtils.degreesToRadians * angleDeg;
			if(_body.getAngle() != angleRad) {
				setRotation(angleRad);
			}
		}
		
		float interval = _intervals[_pathPos];		
		if(TimeUtils.timeSinceMillis(_lastMoveTime) > interval) {
			int nextPathPos = _getNextPathPos();
			
			Vector2 nextPos = _path.get(nextPathPos);
			float nextX = nextPos.x + (getWidth() / 2);
			float nextY = nextPos.y + (getHeight() / 2);
			if(!Globals.getCurrentRoom().isEntityAt(nextX, nextY, getWidth(), getHeight(), "player")) {
				setPosition(nextX, nextY);				
				_pathPos = nextPathPos;
				_lastMoveTime = TimeUtils.millis();
				
				// HACK: Setting the position breaks the simulation and may
				//       leave playing hanging in mid-air.
				if(Globals.getPlayer().getLinearVelocity().isZero()) {
					Globals.getPlayer().setLinearVelocity(0, 0.00001f);
				}
			} else if(_restartOnBlocked) {
				nextPos = _path.get(0);
				nextX = nextPos.x + (getWidth() / 2);
				nextY = nextPos.y + (getHeight() / 2);
				setPosition(nextX, nextY);				
				_pathPos = 0;
				_lastMoveTime = TimeUtils.millis();
			}
		}
	}
	
	protected int _getNextPathPos() {
		return (_pathPos + 1) % _path.size;
	}
}

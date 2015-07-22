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

	protected boolean RESTART_ON_BLOCKED;
	protected final int[] ROTATIONS;
	protected final Array<Vector2> PATH;
	
	protected float[] intervals;
	protected long lastMoveTime;
	protected int pathPos;
	protected boolean started = false;
	
	public DiscreteMovingEntity(EntityBodyDef bodyDef, TextureMapObject object, MapObject bodySkeleton) {
		super(bodyDef, object, bodySkeleton);
		
		ROTATIONS = Utils.getPropertyIntArray(object, "rotations", ",");
		RESTART_ON_BLOCKED = Utils.getPropertyBoolean(object, "restart_on_blocked");

		intervals = Utils.getPropertyFloatArray(object, "intervals", ",");
		
		String[] path = Utils.getPropertyStringArray(object, "path", " ");
		PATH = buildPath(path);
		
		started = Utils.getPropertyBoolean(object, "start_on_create");
		
		if(started) {
			start();
		}
	}

	@Override
	public String getType() {
		return "discrete_moving";
	}

	@Override
	public boolean update() {
		if(!started) {
			return super.update();
		}
		
		checkNextMove();
		
		return super.update();
	}
	
	@Override
	public void start() {
		pathPos = 0;
		lastMoveTime = TimeUtils.millis();
		started = true;
	}
	
	@Override
	public void pause() {
		started = false;
	}
	
	@Override
	public void setPath(String[] serializedPath) {
		pathPos = 0;
		PATH.clear();
		PATH.addAll(buildPath(serializedPath));
	}
	
	@Override
	public void setIntervals(float[] intervals) {
		this.intervals = intervals;
	}
	
	protected Array<Vector2> buildPath(String[] serializedPath) {
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

	protected void checkNextMove() {
		if(ROTATIONS.length > 0) {
			int angleDeg = ROTATIONS[pathPos] * 90;
			float angleRad = MathUtils.degreesToRadians * angleDeg;
			if(body.getAngle() != angleRad) {
				setRotation(angleRad);
			}
		}
		
		float interval = intervals[pathPos];		
		if(TimeUtils.timeSinceMillis(lastMoveTime) > interval) {
			int nextPathPos = getNextPathPos();
			
			Vector2 nextPos = PATH.get(nextPathPos);
			if(!Globals.getCurrentRoom().isEntityAt(nextPos.x, nextPos.y, getWidth(), getHeight())) {
				setPosition(nextPos.x + (getWidth() / 2), nextPos.y + (getHeight() / 2));				
				pathPos = nextPathPos;
				lastMoveTime = TimeUtils.millis();
			} else if(RESTART_ON_BLOCKED) {
				nextPos = PATH.get(0);
				setPosition(nextPos.x + (getWidth() / 2), nextPos.y + (getHeight() / 2));				
				pathPos = 0;
				lastMoveTime = TimeUtils.millis();
			}
		}
	}
	
	protected int getNextPathPos() {
		return (pathPos + 1) % PATH.size;
	}
}

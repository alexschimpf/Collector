package entity;

import misc.Globals;
import misc.Utils;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class MovingEntity extends Entity {

	protected final boolean LOOP;
	protected final float INTERVAL;
	protected final Array<Vector2> PATH;
	
	protected int pathPos = 0;
	protected boolean started = false;
	
	public MovingEntity(EntityBodyDef bodyDef, TextureMapObject object, MapObject bodySkeleton) {
		super(bodyDef, object, bodySkeleton);
		
		INTERVAL = Utils.getPropertyFloat(object, "interval");
		LOOP = Utils.getPropertyBoolean(object, "loop");
		
		String[] path = Utils.getPropertyStringArray(object, "path", " ");
		PATH = buildPath(path);

		started = Utils.getPropertyBoolean(object, "start_on_create");
		
		if(started) {
			updateVelocity();
		}
	}
	
	@Override
	public String getType() {
		return "moving";
	}

	@Override
	public boolean update(){
		if(!started || (atLastPos() && !LOOP)) {
			return super.update();
		}

		checkNextPosReached();
		
		return super.update();
	}
	
	@Override
	public boolean isValidForPlayerRespawn() {
		return false;
	}
	
	public void start() {
		pathPos = 0;
		updateVelocity();
		started = true;
	}
	
	public void setPath(String[] serializedPath) {
		pathPos = 0;
		PATH.clear();
		PATH.addAll(buildPath(serializedPath));
	}
	
	private Array<Vector2> buildPath(String[] serializedPath) {
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
		
		if(LOOP) {
			for(int i = path.size - 2; i > 0; i--) {
				path.add(path.get(i));
			}
		}
		
		return path;
	}
	
	private void checkNextPosReached() {
		float vx = getLinearVelocity().x;
		float vy = getLinearVelocity().y;
		
		float x = getLeft();
		float y = getTop();
		
		Vector2 nextPos = PATH.get(getNextPathPos());
		if((vx == 0 || (vx > 0 && x >= nextPos.x) || (vx < 0 && x <= nextPos.x)) &&
		   (vy == 0 || (vy > 0 && y >= nextPos.y) || (vy < 0 && y <= nextPos.y))) {
			pathPos = getNextPathPos();
			updateVelocity();
		}
	}
	
	private void updateVelocity() {
		if(atLastPos() && !LOOP) {
			setLinearVelocity(0, 0);
			return;
		}
		
		Vector2 a = PATH.get(pathPos);
		Vector2 b = PATH.get(getNextPathPos());			
		float intervalSeconds = INTERVAL / 1000;
		float vx = (b.x - a.x) / intervalSeconds;
		float vy = (b.y - a.y) / intervalSeconds;
		
		setLinearVelocity(vx, vy);		
	}
	
	private boolean atLastPos() {
		return pathPos >= PATH.size - 1;
	}
	
	private int getNextPathPos() {
		int nextIdx = pathPos + 1;
		if(LOOP && nextIdx > PATH.size - 1) {
			nextIdx = 0;
		}
		
		return nextIdx;
	}
}

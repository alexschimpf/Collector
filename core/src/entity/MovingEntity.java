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
		
		String[] path = Utils.getPropertyStringArray(object, "path", " ");
		PATH = buildPath(path);
		
		// TODO: Add support for the other optional properties.
		
		INTERVAL = Utils.getPropertyFloat(object, "interval");
		LOOP = Utils.getPropertyBoolean(object, "loop");
		
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
		if(!started || pathPos >= PATH.size - 1) {
			return super.update();
		}

		checkNextPosReached();
		
		return super.update();
	}
	
	public void start() {
		pathPos = 0;
		updateVelocity();
		started = true;
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
		
		return path;
	}
	
	private void checkNextPosReached() {
		float vx = getLinearVelocity().x;
		float vy = getLinearVelocity().y;
		
		float x = getLeft();
		float y = getTop();
		Vector2 nextPos = PATH.get(pathPos + 1);

		if((vx == 0 || (vx > 0 && x >= nextPos.x) || (vx < 0 && x <= nextPos.x)) &&
		   (vy == 0 || (vy > 0 && y >= nextPos.y) || (vy < 0 && y <= nextPos.y))) {
			pathPos++;
			updateVelocity();
		}
	}
	
	private void updateVelocity() {
		if(pathPos >= PATH.size - 1) {
			setLinearVelocity(0, 0);
			return;
		}
		
		Vector2 a = PATH.get(pathPos);
		Vector2 b = PATH.get(pathPos + 1);			
		float intervalSeconds = INTERVAL / 1000;
		float vx = (b.x - a.x) / intervalSeconds;
		float vy = (b.y - a.y) / intervalSeconds;
		
		setLinearVelocity(vx, vy);
		
	}
}

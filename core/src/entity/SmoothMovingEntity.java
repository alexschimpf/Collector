package entity;

import misc.Globals;
import misc.Utils;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.utils.Array;

import entity.special.Player;

public class SmoothMovingEntity extends Entity implements IMovingEntity {

	protected final boolean LOOP;
	protected final Array<Vector2> PATH;
	protected final boolean IS_FATAL;
	
	protected int pathPos = 0;
	protected boolean started = false;
	protected float[] intervals;
	
	public SmoothMovingEntity(EntityBodyDef bodyDef, TextureMapObject object, MapObject bodySkeleton) {
		super(bodyDef, object, bodySkeleton);
		
		intervals = Utils.getPropertyFloatArray(object, "intervals", ",");
		LOOP = Utils.getPropertyBoolean(object, "loop");
		
		String[] path = Utils.getPropertyStringArray(object, "path", " ");
		PATH = buildPath(path);
		
		IS_FATAL = Utils.getPropertyBoolean(object, "is_fatal");

		started = Utils.getPropertyBoolean(object, "start_on_create");
		
		if(started) {
			updateVelocity();
		}
	}
	
	@Override
	public String getType() {
		return "smooth_moving";
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
	public void onBeginContact(Contact contact, Entity entity) {
		Player player = Globals.getPlayer();
		if(IS_FATAL && Utils.isPlayer(entity) && player.getTop() <= getBottom()) {
			// TODO: Need a known respawn point.
			player.respawn(true, null);
		}
	}
	
	@Override
	public boolean isValidForPlayerRespawn() {
		return false;
	}
	
	@Override
	public void start() {
		pathPos = 0;
		updateVelocity();
		started = true;
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
		
		if(LOOP) {
			for(int i = path.size - 2; i > 0; i--) {
				path.add(path.get(i));
			}
		}
		
		return path;
	}
	
	protected void checkNextPosReached() {
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
	
	protected void updateVelocity() {
		if(atLastPos() && !LOOP) {
			setLinearVelocity(0, 0);
			return;
		}
		
		Vector2 a = PATH.get(pathPos);
		Vector2 b = PATH.get(getNextPathPos());		
		
		int intervalPos = pathPos;
		if(LOOP && atLastPos()) {
			intervalPos = 0;
		}
		float intervalSeconds = intervals[intervalPos] / 1000;
		float vx = (b.x - a.x) / intervalSeconds;
		float vy = (b.y - a.y) / intervalSeconds;
		
		setLinearVelocity(vx, vy);		
	}
	
	protected boolean atLastPos() {
		return pathPos >= PATH.size - 1;
	}
	
	protected int getNextPathPos() {
		int nextIdx = pathPos + 1;
		if(LOOP && nextIdx > PATH.size - 1) {
			nextIdx = 0;
		}
		
		return nextIdx;
	}
}

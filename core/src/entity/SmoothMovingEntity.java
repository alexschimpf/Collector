package entity;

import misc.Globals;
import misc.Utils;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.utils.Array;

public class SmoothMovingEntity extends Entity implements IMovingEntity {

	protected final boolean _loop;
	protected final Array<Vector2> _path;
	protected final boolean _isFatal;
	
	protected int _pathPos = 0;
	protected boolean _started = false;
	protected float[] _intervals;
	protected Vector2 _respawnPos;
	
	public SmoothMovingEntity(EntityBodyDef bodyDef, TextureMapObject object, MapObject bodySkeleton) {
		super(bodyDef, object, bodySkeleton);
		
		_intervals = Utils.getPropertyFloatArray(object, "intervals", ",");
		_loop = Utils.getPropertyBoolean(object, "loop");
		
		String[] path = Utils.getPropertyStringArray(object, "path", " ");
		_path = _buildPath(path);
		
		_isFatal = Utils.getPropertyBoolean(object, "is_fatal");
		
		if(!Utils.isPropertyEmpty(object, "respawn_pos")) {
			Vector2 respawnOffset = Utils.getPropertyVector2(object, "respawn_pos");
			_respawnPos = respawnOffset.scl(Globals.getTileSize()).add(getCenter());
		} else {
			_respawnPos = null;
		}
		
		_started = Utils.getPropertyBoolean(object, "start_on_create");
		
		if(_started) {
			_updateVelocity();
		}
	}
	
	@Override
	public String getType() {
		return "smooth_moving";
	}

	@Override
	public boolean update(){
		if(!_started || (_atLastPos() && !_loop)) {
			return super.update();
		}

		_checkNextPosReached();
		
		return super.update();
	}

	@Override
	public void onBeginContact(Contact contact, Entity entity) {
		Player player = Globals.getPlayer();
		if(_isFatal && Utils.isPlayer(entity) && getLinearVelocity().y > 0 && _isPlayerBelow()) {
			player.respawn(true, _respawnPos);
		}
	}
	
	@Override
	public boolean isValidForPlayerRespawn() {
		return false;
	}
	
	@Override
	public void start() {
		_pathPos = 0;
		_updateVelocity();
		_started = true;
	}
	
	@Override
	public void pause() {
		_started = false;
		setLinearVelocity(0, 0);
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
		
		if(_loop) {
			for(int i = path.size - 2; i > 0; i--) {
				path.add(path.get(i));
			}
		}
		
		return path;
	}
	
	protected void _checkNextPosReached() {
		float vx = getLinearVelocity().x;
		float vy = getLinearVelocity().y;
		
		float x = getLeft();
		float y = getTop();
		
		Vector2 nextPos = _path.get(_getNextPathPos());
		if((vx == 0 || (vx > 0 && x >= nextPos.x) || (vx < 0 && x <= nextPos.x)) &&
		   (vy == 0 || (vy > 0 && y >= nextPos.y) || (vy < 0 && y <= nextPos.y))) {
			_pathPos = _getNextPathPos();
			_updateVelocity();
		}
	}
	
	protected void _updateVelocity() {
		if(_atLastPos() && !_loop) {
			setLinearVelocity(0, 0);
			return;
		}
		
		Vector2 a = _path.get(_pathPos);
		Vector2 b = _path.get(_getNextPathPos());
		
		int intervalPos = _pathPos;
		if(_loop && _atLastPos()) {
			intervalPos = 0;
		}
		float intervalSeconds = _intervals[intervalPos] / 1000;
		float vx = (b.x - a.x) / intervalSeconds;
		float vy = (b.y - a.y) / intervalSeconds;	
		if(getLinearVelocity().epsilonEquals(vx, vy, 0)) {
			return;
		}
		
//		// HACK: Make player stick to this when changing directions.
//		Player player = Globals.getPlayer();
//		if(getLinearVelocity().y < 0 && vy > 0 && _isPlayerAbove() && !player.isJumping() && player.getNumFootContacts() > 0) {
//			player.setLinearVelocity(player.getLinearVelocity().x, vy);
//		}
		
		setLinearVelocity(vx, vy);	
	}
	
	protected boolean _atLastPos() {
		return _pathPos >= _path.size - 1;
	}
	
	protected int _getNextPathPos() {
		int nextIdx = _pathPos + 1;
		if(_loop && nextIdx > _path.size - 1) {
			nextIdx = 0;
		}
		
		return nextIdx;
	}
	
	protected boolean _isPlayerAbove() {
		Player player = Globals.getPlayer();
		return _isPlayerAboveOrBelow() && player.getBottom() <= getTop() + (getHeight() / 10);
	}
	
	protected boolean _isPlayerBelow() {
		Player player = Globals.getPlayer();
		return _isPlayerAboveOrBelow() && player.getTop() >= getBottom() - (getHeight() / 10);
	}
	
	protected boolean _isPlayerAboveOrBelow() {
		Player player = Globals.getPlayer();
		float correction = player.getWidth() / 20;
		return (player.getRight() - correction > getLeft() && player.getLeft() < getLeft()) ||
               (player.getLeft() - correction > getLeft() && player.getRight() > getRight()) ||
               (player.getLeft() >= getLeft() && player.getRight() <= getRight());		
	}
}

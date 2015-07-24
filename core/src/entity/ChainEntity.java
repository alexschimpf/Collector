package entity;

import misc.Globals;
import misc.Utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.utils.TimeUtils;

public final class ChainEntity extends Entity {

	private final float _activatedDuration;
	private final String _chainStartId;
	private final String[] _stateMachine;
	private final String[] _chainIds;
	private final Color _activeColor = new Color(1, 0.4f, 0.4f, 1);
	private final Color _chainStartColor = new Color(0.4f, 1, 0.4f, 1);
	
	private int _state = 0;
	private boolean _activated = false;
	private long _activatedStartTime = 0;
	
	public ChainEntity(EntityBodyDef bodyDef, TextureMapObject object, MapObject bodySkeleton) {
		super(bodyDef, object, bodySkeleton);
		
		_activatedDuration = Utils.getPropertyFloat(object, "activated_duration");
		_chainStartId = Utils.getPropertyString(object, "chain_start_id");
		_stateMachine = Utils.getPropertyStringArray(object, "state_machine", ",");
		_chainIds = Utils.getPropertyStringArray(object, "chain_ids", ",");
		
		if(_isChainStart()) {
			activate();
		}
	}
	
	@Override
	public String getType() {
		return "chain";
	}
	
	@Override
	public boolean update() {
		if(!_activated || (_isChainStart() && _state == 0)) {
			return super.update();
		}
		
		float timeSinceActivated = TimeUtils.timeSinceMillis(_activatedStartTime);
		float ratio = Math.min(1, _activeColor.g + ((1 - _activeColor.g) * (timeSinceActivated / _activatedDuration)));
		_sprite.setColor(1, ratio, ratio, 1);
		
		if(timeSinceActivated > _activatedDuration) {
			_restartChain();
		}

		return super.update();
	}
	
	@Override
	public void onBeginContact(Contact contact, Entity entity) {
		if(!Utils.isPlayer(entity) || !_activated) {
			return;
		}
		
		if(Globals.getPlayer().getBottom() - (getHeight() / 25) <= getTop()) {
			_deactivate();
			_activateNext();
		}
	}
	
	@Override
	public boolean isValidForPlayerRespawn() {
		return false;
	}
	
	public void activate() {	
		if(_isChainStart() && _state == 0) {
			_sprite.setColor(_chainStartColor);
		} else {
			_sprite.setColor(_activeColor);
		}
		
		_activatedStartTime = TimeUtils.millis();
		_activated = true;
	}
	
	private void _deactivate() {
		_sprite.setColor(Color.WHITE);
		_activated = false;
	}
	
	private void _activateNext() {
		String nextId = _stateMachine[_state];
		Entity nextEntity = Globals.getCurrentRoom().getEntityById(nextId);
		if(nextEntity != null && nextEntity.getType().equals("chain")) {
			((ChainEntity)nextEntity).activate();
		} else {
			Globals.getCurrentRoom().startScript(nextId);
		}
		
		_state++;
	}
	
	private void _restartChain() {
		_state = 0;
		if(_isChainStart()) {
			activate();
		} else {
			_deactivate();
		}
		
		for(String chainId : _chainIds) {
			ChainEntity chainEntity = (ChainEntity)Globals.getCurrentRoom().getEntityById(chainId);
			chainEntity._state = 0;
			
			if(chainEntity._isChainStart()) {
				chainEntity.activate();
			} else {
				chainEntity._deactivate();
			}
		}
	}
	
	private boolean _isChainStart() {
		return _id.equals(_chainStartId);
	}
}

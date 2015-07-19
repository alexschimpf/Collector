package entity;

import misc.Globals;
import misc.Utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.utils.TimeUtils;

public final class ChainEntity extends Entity {

	private final float ACTIVATED_DURATION;
	private final String CHAIN_START_ID;
	private final String[] STATE_MACHINE;
	private final String[] CHAIN_IDS;
	private final Color ACTIVE_COLOR = new Color(1, 0.4f, 0.4f, 1);
	private final Color CHAIN_START_COLOR = new Color(0.4f, 1, 0.4f, 1);
	
	private int state = 0;
	private boolean activated = false;
	private long activatedStartTime = 0;
	
	public ChainEntity(EntityBodyDef bodyDef, TextureMapObject object, MapObject bodySkeleton) {
		super(bodyDef, object, bodySkeleton);
		
		ACTIVATED_DURATION = Utils.getPropertyFloat(object, "activated_duration");
		CHAIN_START_ID = Utils.getPropertyString(object, "chain_start_id");
		STATE_MACHINE = Utils.getPropertyStringArray(object, "state_machine", ",");
		CHAIN_IDS = Utils.getPropertyStringArray(object, "chain_ids", ",");
		
		if(isChainStart()) {
			activate();
		}
	}
	
	@Override
	public String getType() {
		return "chain";
	}
	
	@Override
	public boolean update() {
		if(!activated || (isChainStart() && state == 0)) {
			return super.update();
		}
		
		float timeSinceActivated = TimeUtils.timeSinceMillis(activatedStartTime);
		float ratio = Math.min(1, ACTIVE_COLOR.g + ((1 - ACTIVE_COLOR.g) * (timeSinceActivated / ACTIVATED_DURATION)));
		sprite.setColor(1, ratio, ratio, 1);
		
		if(timeSinceActivated > ACTIVATED_DURATION) {
			restartChain();
		}

		return super.update();
	}
	
	@Override
	public void onBeginContact(Contact contact, Entity entity) {
		if(!Utils.isPlayer(entity) || !activated) {
			return;
		}
		
		if(Globals.getPlayer().getBottom() - (getHeight() / 25) <= getTop()) {
			deactivate();
			activateNext();
		}
	}
	
	public void activate() {	
		if(isChainStart() && state == 0) {
			sprite.setColor(CHAIN_START_COLOR);
		} else {
			sprite.setColor(ACTIVE_COLOR);
		}
		
		activatedStartTime = TimeUtils.millis();
		activated = true;
	}
	
	private void deactivate() {
		sprite.setColor(Color.WHITE);
		activated = false;
	}
	
	private void activateNext() {
		String nextId = STATE_MACHINE[state];
		Entity nextEntity = Globals.getCurrentRoom().getEntityById(nextId);
		if(nextEntity != null && nextEntity.getType().equals("chain")) {
			((ChainEntity)nextEntity).activate();
		} else {
			Globals.getCurrentRoom().startScript(nextId);
		}
		
		state++;
	}
	
	private void restartChain() {
		state = 0;
		if(isChainStart()) {
			activate();
		} else {
			deactivate();
		}
		
		for(String chainId : CHAIN_IDS) {
			ChainEntity chainEntity = (ChainEntity)Globals.getCurrentRoom().getEntityById(chainId);
			chainEntity.state = 0;
			
			if(chainEntity.isChainStart()) {
				chainEntity.activate();
			} else {
				chainEntity.deactivate();
			}
		}
	}
	
	private boolean isChainStart() {
		return ID.equals(CHAIN_START_ID);
	}
}

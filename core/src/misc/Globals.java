package misc;

import assets.MusicManager;
import assets.SoundManager;
import assets.TextureManager;

import com.badlogic.gdx.physics.box2d.World;

import core.GameWorld;
import core.TheCamera;
import entity.Player;

public final class Globals {

	public static short PLAYER_COLLISION_MASK = 0x0002;
	
	public static GameWorld getGameWorld() {
		return GameWorld.getInstance();
	}
	
	public static World getPhysicsWorld() {
		return GameWorld.getInstance().getWorld();
	}
	
	public static TheCamera getCamera() {
		return TheCamera.getInstance();
	}
	
	public static TextureManager getTextureManager() {
		return TextureManager.getInstance();
	}
	
	public static SoundManager getSoundManager() {
		return SoundManager.getInstance();
	}
	
	public static MusicManager getMusicManager() {
		return MusicManager.getInstance();
	}
	
	public static EntityPropertyValidator getEntityPropertyValidator() {
		return EntityPropertyValidator.getInstance();
	}
	
	public static Player getPlayer() {
		return getGameWorld().getPlayer();
	}
	
	public static GameWorld.State getGameState() {
		return getGameWorld().getState();
	}
}

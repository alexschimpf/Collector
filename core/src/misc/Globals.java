package misc;

import particle.ParticleEffect;
import particle.ParticleEffectManager;
import screen.GameScreen;
import assets.MusicManager;
import assets.SoundManager;
import assets.TextureManager;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import core.EntityPropertyValidator;
import core.GameRoom;
import core.GameWorld;
import core.HUD;
import core.TheCamera;
import core.WeatherSystem;
import entity.Player;

public final class Globals {

	public static enum State {
		RUNNING, PAUSED, LOADING
	};
	
	public static final short PLAYER_NO_COLLIDE_MASK = 0x0002;
	
	public static State state = State.RUNNING;
	
	private static int numCollected = 0;
	private static GameScreen gameScreen;
	
	public static void setGameScreen(GameScreen gameScreen) {
		Globals.gameScreen = gameScreen;
	}
	
	public static GameScreen getGameScreen() {
		return gameScreen;
	}
	
	public static HUD getHUD() {
		return HUD.getInstance();
	}
	
	public static GameWorld getGameWorld() {
		return GameWorld.getInstance();
	}
	
	public static GameRoom getCurrentRoom() {
		return getGameWorld().getCurrentRoom();
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
	
	public static ParticleEffectManager getParticleEffectManager() {
		return ParticleEffectManager.getInstance();
	}
	
	public static WeatherSystem getWeatherSystem() {
		return WeatherSystem.getInstance();
	}
	
	public static Vector2Pool getVector2Pool() {
		return Vector2Pool.getIntance();
	}
	
	public static EntityPropertyValidator getEntityPropertyValidator() {
		return EntityPropertyValidator.getInstance();
	}
	
	public static Player getPlayer() {
		return getGameWorld().getPlayer();
	}
	
	public static float getTileSize() {
		return getCamera().getViewportWidth() / TheCamera.NUM_TILES_PER_SCREEN_WIDTH;
	}
	
	public static int getNumCollected() {
		return numCollected;
	}
	
	public static void setNumCollected(int numCollected) {
		Globals.numCollected = numCollected;
	}
	
	public static void incrementNumCollected() {
		numCollected++;
	}

	public static Sprite getSprite(String textureKey) {
		return getTextureManager().getSprite(textureKey);
	}
	
	public static TextureRegion getImageTexture(String textureKey) {
		return getTextureManager().getImageTexture(textureKey);
	}
	
	public static TextureRegion getImageTexture(String textureKey, int index) {
		return getTextureManager().getImageTexture(textureKey, index);
	}
	
	public static Array<AtlasRegion> getAnimationTextures(String animationKey) {
		return getTextureManager().getAnimationTextures(animationKey);
	}
	
	public static ParticleEffect getParticleEffect(String particleEffectKey, float x, float y) {
		return getParticleEffectManager().getParticleEffect(particleEffectKey, x, y);
	}
	
	public static boolean isGameRunning() {
		return state == State.RUNNING;
	}
	
	public static boolean isGamePaused() {
		return state == State.PAUSED;
	}
	
	public static boolean isGameLoading() {
		return state == State.LOADING;
	}
}

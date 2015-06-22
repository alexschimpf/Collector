package animation;

import com.badlogic.gdx.graphics.g2d.Sprite;

public interface IAnimate {

	public void play();
	
	public void resume();
	
	public void pause();
	
	public void stop();
	
	public Animation.State getState();
	
	public boolean isPlaying();
	
	public boolean isPaused();
	
	public boolean isFinished();
	
	public Sprite getSprite();
}

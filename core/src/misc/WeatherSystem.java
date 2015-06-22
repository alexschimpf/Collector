package misc;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class WeatherSystem implements IRender, IUpdate {

	public static WeatherSystem instance;
	
	public static WeatherSystem getInstance() {
		if(instance == null) {
			instance = new WeatherSystem();
		}
		
		return instance;
	}
	
	public WeatherSystem() {
		
	}
	
	@Override
	public void render(SpriteBatch spriteBatch) {

	}
	
	@Override
	public boolean update() {
		return false;
	}

	@Override
	public void done() {

	}
}

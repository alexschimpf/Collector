package misc;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;

public final class Vector2Pool extends Pool<Vector2> {

	private static Vector2Pool instance = new Vector2Pool();
	
	public static Vector2Pool getIntance() {
		if(instance == null) {
			instance = new Vector2Pool();
		}
		
		return instance;
	}
	
	public Vector2 obtain(float x, float y) {
		return instance.obtain().set(x, y);
	}
	
	@Override
	protected Vector2 newObject() {
		return new Vector2();
	}
}

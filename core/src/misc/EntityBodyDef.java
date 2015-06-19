package misc;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class EntityBodyDef {

	public Vector2 position;
	public Vector2 size;
	public BodyType bodyType;
	
	public EntityBodyDef(Vector2 position, Vector2 size, BodyType bodyType) {
		this.position = position;
		this.size = size;
		this.bodyType = bodyType;
	}
	
	@Override
	public String toString() {
		return "Position: " + position.x + ", " + position.y + "\n" +
	           "Size: " + size.x + " x " + size.y + "\n" +
			   "Body type: " + bodyType.toString();
	}
}

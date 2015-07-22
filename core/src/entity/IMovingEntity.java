package entity;

public interface IMovingEntity {

	public void start();
	
	public void setPath(String[] serializedPath);
	
	public void setIntervals(float[] intervals);
}

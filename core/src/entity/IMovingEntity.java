package entity;

public interface IMovingEntity {

	public void start();
	
	public void pause();
	
	public void setPath(String[] serializedPath);
	
	public void setIntervals(float[] intervals);
}

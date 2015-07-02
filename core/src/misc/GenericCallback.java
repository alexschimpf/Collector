package misc;

public abstract class GenericCallback<T> {

	public GenericCallback() {
		
	}
	
	public abstract void onCallback(T t); 
}

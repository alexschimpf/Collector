package misc;

public final class Tuple<X, Y> {

	public X x;
	public Y y;
	
	public Tuple(X x, Y y) {
		this.x = x;
		this.y = y;
	}
	
	public void set(X x, Y y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public int hashCode() {
		return 0;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof Tuple<?, ?>) {
			@SuppressWarnings("unchecked")
			Tuple<X, Y> other = (Tuple<X, Y>)o;
			return x.equals(other.x) && y.equals(other.y);
		}

		return false;
	}
}

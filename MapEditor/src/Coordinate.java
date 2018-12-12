
public class Coordinate implements Comparable<Coordinate> {
	public int x = 0;
	public int y = 0;
	
	public Coordinate() {
		
	}
	
	public Coordinate(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public boolean equals(Object c) {
		if (c instanceof Coordinate) {
			Coordinate c2 = (Coordinate) c;
			return (c2.x == x && c2.y == y);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return x + (1000 * y);
	}

	@Override
	public int compareTo(Coordinate o) {
		
		return 0;
	}
}

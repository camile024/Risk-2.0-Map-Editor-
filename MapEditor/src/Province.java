import java.util.ArrayList;
import java.util.LinkedList;

import javafx.scene.paint.Color;

public class Province {

	public String id;
	public Coordinate coord;
	public Color colour;
	private boolean isCapital = false;
	private boolean isPort = false;
	private int attack = 10;
	private int defence = 5;
	private int neighbourDefence = 5;
	private float resources = (float) 0.2;
	private int loyalty = 50;
	private ArrayList<Integer> seaList;
	
	private LinkedList<Province> neighbours;
	
	public Province(Color colour, int x, int y) {
		seaList = new ArrayList<Integer>();
		this.id = String.format( "0x%02X%02X%02X",
	            (int)( colour.getRed() * 255 ),
	            (int)( colour.getGreen() * 255 ),
	            (int)( colour.getBlue() * 255 ));
		this.colour = colour;
		coord = new Coordinate(x,y);
		neighbours = new LinkedList<Province>();
	}
	
	public void addNeighbour(Province p) {
		if (!neighbours.contains(p) && p != null) {
			neighbours.add(p);
		}
	}
	
	public LinkedList<Province> getNeighbours() {
		return neighbours;
	}
	
	@Override
	public int hashCode() {
		return colour.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Province) {
			return ((Province)o).id.equals(id);
		}
		return false;
	}
	

	public int getAttack() {
		return attack;
	}

	public void setAttack(int attack) {
		this.attack = attack;
	}

	public int getDefence() {
		return defence;
	}

	public void setDefence(int defence) {
		this.defence = defence;
	}

	public float getResources() {
		return resources;
	}

	public void setResources(float resources) {
		this.resources = resources;
	}

	public int getLoyalty() {
		return loyalty;
	}

	public void setLoyalty(int loyalty) {
		this.loyalty = loyalty;
	}

	public void setNeighbours(LinkedList<Province> neighbours) {
		this.neighbours = neighbours;
	}

	public boolean isCapital() {
		return isCapital;
	}

	public void setCapital(boolean isCapital) {
		this.isCapital = isCapital;
	}

	public int getNeighbourDefence() {
		return neighbourDefence;
	}

	public void setNeighbourDefence(int neighbourDefence) {
		this.neighbourDefence = neighbourDefence;
	}

	public void removeSea(int seaNum) {
		seaList.remove(Integer.valueOf(seaNum));
		if (seaList.isEmpty()) {
			isPort = false;
		}
	}
	
	public void addSea(int seaNum) {
		if (!seaList.contains(seaNum)) {
			seaList.add(seaNum);
		}
		isPort = true;
	}

	public boolean isPort() {
		return isPort;
	}

	public void setPort(boolean isPort) {
		this.isPort = isPort;
	}
}

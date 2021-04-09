
import java.util.ArrayList;

public class Door {
	
	ArrayList<Coordinates> coords = new ArrayList<Coordinates>();
	ArrayList<Coordinates> doorCoords = new ArrayList<Coordinates>();
	
	private boolean leadsToCheckedRoom = false;
	private boolean visited = false;
	
	public Door() {
		
	}
	
	
	
	public void setVisited() {
		this.visited = true;
	}
	
	public boolean getVisited() {
		return this.visited;
	}
	
	public void setDoorCoords(ArrayList<Coordinates> coords) {
		this.coords = coords;
	}
	
	public Door(Coordinates coords) {
		this.coords.add(coords);
	}
	
	public boolean getLeadsToCheckedRoom() {
		return this.leadsToCheckedRoom;
	}
	
	public void setDoorLeadsToCheckedRoom(boolean c) {
		this.leadsToCheckedRoom = c;
	}
	
	public void addCoordinates(Coordinates coords) {
		this.coords.add(coords);
	}
	public void addDoorCoordinates(ArrayList<Coordinates> coords) {
		this.coords.addAll(coords);
		
	}
	public Coordinates getCoordsFromDoorSpace(int i) {
		
		for (int j = 0; j < coords.size(); j++) {
			if (j == i) return coords.get(j);
		}
		return null;
	}
	
	public ArrayList<Coordinates> getDoorCoordinates() {
		return this.coords;
	}
	
	public void clearDoorCoordinates() {
		this.coords.clear();
	}
	
}

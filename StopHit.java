
public class StopHit {

	private Coordinates coords;
	private int direction;
	private int roomNr;
	private int count = 0;
	
	public StopHit(Coordinates coords, int direction) {
		this.coords = coords;
		this.direction = direction;		
	}
	
	public void incCount() {
		this.count++;
	}
	
	public int getCount() {
		return this.count;
	}
	
	public void setCount(int c) {
		this.count = c;
	}
	
	public int getDirection() {
		return this.direction;
	}
	
	public Coordinates getCoordinates() {
		return this.coords;
	}

	public void setRoomNr(int nr) {
		this.roomNr = nr;
	}
	
	public int getRoomNr() {
		return this.roomNr;
	}
}


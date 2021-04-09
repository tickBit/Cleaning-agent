
import java.util.ArrayList;

public class DetectedRooms {

	private int ID = -1;
	private int numberOfDoors = -1;
	private int doorsInRoom = 0;
	
	ArrayList<Integer> connectsRoomNr = new ArrayList<Integer>();
	
	ArrayList<Door> checkedDoors = new ArrayList<Door>();
	ArrayList<Coordinates> doorCoordinates = new ArrayList<Coordinates>();
	ArrayList<Coordinates> roomLocations = new ArrayList<Coordinates>();
	
	ArrayList<Coordinates> room = new ArrayList<Coordinates>();
	ArrayList<Door> door = new ArrayList<Door>();
	ArrayList<Coordinates> cleaningLocationHistory = new ArrayList<Coordinates>();
	
	
	
	private int leftWallX = -1;
	private int rightWallX = -1;
	private int UpWallY = -1;
	private int DownWallY = -1;
	private boolean roomMapped = false;
	private boolean roomCleaned = false;
	
	private int area = -1;
	
	public DetectedRooms() {
		
	}
	
	public int returnMinY() {
		int min = Integer.MAX_VALUE;
		for (int i = 0; i < this.door.size(); i++) {
			for (int ii = 0; ii < this.door.get(i).getDoorCoordinates().size(); ii++)
			if (this.door.get(i).getDoorCoordinates().get(ii).getY() < min) min = this.door.get(i).getDoorCoordinates().get(ii).getY();
		
		}

		return min;
	}
	
	public int returnMaxY() {
		int max = Integer.MIN_VALUE;
		for (int i = 0; i < this.door.size(); i++) {
			for (int ii = 0; ii < this.door.get(i).getDoorCoordinates().size(); ii++)
			if (this.door.get(i).getDoorCoordinates().get(ii).getY() > max) max = this.door.get(i).getDoorCoordinates().get(ii).getY();
		
		}

		return max;
	}
	
	public void setRoomCleaned (boolean val) {
		this.roomCleaned = true;
	}
	
	public boolean getRoomCleaned() {
		return this.roomCleaned;
	}
	
	public int getWidth() {
		return (((rightWallX - leftWallX ) / 32));
	}
	
	public int getHeight() {
		return ((DownWallY - UpWallY ) / 32);
	}
	public void addRoomLocation(Coordinates coords) {
		this.roomLocations.add(coords);
	}
	
	public ArrayList<Coordinates> getRoomLocations() {
		return this.roomLocations;
	}
	
	public void setArea() {
		this.area = (((DownWallY - UpWallY ) / 32) * ((rightWallX - leftWallX ) / 32)) ;
	}
	
	public int getArea() {
		return this.area;
	}
	
	public void addConnectedRoomNr(int nr) {
		this.connectsRoomNr.add(nr);
	}
	
	public void setRoomMapped(boolean mapped) {
		this.roomMapped = mapped;
	}
	
	public boolean getRoomMapped() {
		return this.roomMapped;
	}
	
	public void setRoomID(int id) {
		this.ID = id;
	}
	
	public int getRoomID() {
		return this.ID;
	}
	
	
	public int getNrDoors() {
		return this.door.size();
	}
	
	public ArrayList<Coordinates> getRoomCleaningLocationHistory() {
		return this.cleaningLocationHistory;
	}
	
	public void addRoomCleaningLocation(Coordinates coords) {
		this.cleaningLocationHistory.add(coords);
	}
	
	public void setDoorsInRoom(int nrDoors) {
		this.doorsInRoom = nrDoors;
	}
	
	public int getDoorsInRoom() {
		return this.door.size();
	}
	
	public void addDoor(Door door) {
		this.door.add(door);
	}
	
	public ArrayList<Door> getDoors() {
		return this.door;
	}

	public void addcheckedDoorLocation(Door door) {
		this.checkedDoors.add(door);
	}
	
	public ArrayList<Door> getUncheckedDoorLocations() {
		return this.checkedDoors;
	}
	
	public ArrayList<Coordinates> getRoom() {
		return this.room;
	}

	public void addRoomCoordinates(Coordinates coords) {
		this.room.add(coords);
		
	}
	
	
	public ArrayList<Coordinates> getCoordinates() {
		return this.room;
	}
	
	public void setLeftWallX(int x) {
		this.leftWallX = x;
	}
	
	public int getLeftWallX() {
		return this.leftWallX;
	}
	
	public void setRightWallX(int x) {
		this.rightWallX = x;
	}
	
	public int getRightWallX() {
		return this.rightWallX;
	}
	
	public void setUpWallY(int y) {
		this.UpWallY = y;
	}
	
	public int getUpWallY() {
		return this.UpWallY;
	}
	
	public void setDownWallY(int y) {
		this.DownWallY = y;
	}
	
	public int getDownWallY() {
		return this.DownWallY;
	}
	
	public void addDoorCoordinates(Coordinates coords) {
		this.doorCoordinates.add(coords);
	}
	
	public ArrayList<Coordinates> getDoorCoordinates() {
		return this.doorCoordinates;
	}
}

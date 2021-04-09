
import java.util.ArrayList;

// Intelligent agent's system
public class IntelligentAgent {
	
	final private int UP = 0;
	final private int DOWN = 1;
	final private int LEFT = 2;
	final private int RIGHT = 3;
	
	final private int FINDWALL = 0;
	final private int DETECTROOM = 1;	// checks also for the doors of the room
	final private int CREATEROOMMAP = 2;
	final private int CLEANROOM = 3;
	final private int FINDDROOR = 4;
	final private int FINDUNCHECKEDROOM = 5;
	private int x = 32*6;
	private int y = 32*16;
	
	private int direction = (int)(Math.random() * 100) % 4;
	
	// starting operation
	private int operation = FINDWALL;
	
	private boolean permissionToWalkThrougPassedDoors = false;
	
	private int roomsFound = 0;
	
	private int targetX = -1;
	private int targetY = -1;
	
	
	private int possibleRoomsAtleast = 0;
	
	
	private int doorsFound = 0;
	
	private boolean allRoomsFound = false;
	
	private ArrayList<StopHit> wallLocations = new ArrayList<StopHit>();
	private ArrayList<Coordinates> doorLocations = new ArrayList<Coordinates>();
	private ArrayList<Coordinates> fullLocationHistory = new ArrayList<Coordinates>();
	private ArrayList<DetectedRooms> detectedRooms = new ArrayList<DetectedRooms>();
	
	private ArrayList<Door> visitedDoors = new ArrayList<Door>();
	private ArrayList<Door> foundDoors = new ArrayList<Door>();
	
	private boolean possibleNeRoomJustDetected = false;
	
	private int previousDirection = -1;
	
	private Door lastDoorCheckedBeforeStopHit = null;
	
	public IntelligentAgent() {
		
	}
	
	public void addDoorLocation(Coordinates coords) {
		this.doorLocations.add(coords);
	}
	
	public ArrayList<Coordinates> getDoorCoordsLocations() {
		return this.doorLocations;
	}
	
	public void addVisitedDoor(Door door) {
		this.visitedDoors.add(door);
	}
	
	public ArrayList<Door> getVisitedDoors() {
		return this.visitedDoors;
	}
	
	public Door getLastDoorCheckedBeforeStopHit() {
		return this.lastDoorCheckedBeforeStopHit;
	}
	
	public void setLastDoorCheckedBeforeStopHit(Door door) {
		this.lastDoorCheckedBeforeStopHit = door;
	}
	
	public void addFoundDoor(Door door) {
		
		boolean result = false;
		
		if (door.getDoorCoordinates().size() > 0) {
			for (int i = 0; i < this.foundDoors.size(); i++) {
			
				if (sameDoors(this.foundDoors.get(i), door) == true) result = true;
			}
			if (result == false) {
				System.out.println("NEW found door added.");
				this.foundDoors.add(door);
			}
		}
	}
	
	public void deleteDoor(int index) {
		this.foundDoors.remove(index);
	}
	
	public ArrayList<Door> getFoundDoors() {
		return this.foundDoors;
	}
	
	
	public void setPrevDir(int dir) {
		this.previousDirection = dir;
	}
	
	public int getPrevDir() {
		return this.previousDirection;
	}
	
	public int getDirection() {
		return direction;
	}
	
	public void setTargetX(int toX) {
		
		this.targetX = toX;
	}
	
	public void setTargetY(int toY) {
		this.targetY = toY;
	}

	public boolean atTargetX(int toX) {
		if (this.x == toX) return true;
		return false;
	}
	
	public boolean atTargetY(int toY) {
		if (this.y == toY) return true;
		return false;
	}
	public void turn90degrees() {
		this.previousDirection = this.direction;
		if (direction == UP) {
			direction = LEFT;
			return;
		}
		if (direction == DOWN) {
			direction = RIGHT;
			return;
		}
		if (direction == LEFT) {
			direction = DOWN;
			return;
		}
		if (direction == RIGHT) {
			direction = UP;
			return;
		}
	
	}
	
	public void turnMinus90degrees() {
		
		
		if (direction == UP) {
			direction = RIGHT;
			return;
		}
		if (direction == DOWN) {
			direction = LEFT;
			return;
		}
		if (direction == LEFT) {
			direction = UP;
			return;
		}
		if (direction == RIGHT) {
			direction = DOWN;
			return;
		}
	
	}
	
	public void moveStep() {
    	switch(direction) {
    	
    	case UP: 
    		y-=32;
    		break;
    	case DOWN:
    		y+=32;
    		break;
    	case LEFT:
    		x-=32;
    		break;
    	case RIGHT:
    		x+=32;
    		break;
    	
    	}
	}
	
	public int getX() {
		return this.x;
	}
	
	public int getY() {
		return this.y;
	}
	
	// return wall locations known by the agent
	public ArrayList<StopHit> returnWallHistory() {
		return this.wallLocations;
	}
	
	// return door locations known by the agent
	public ArrayList<Coordinates> returnDoorHistory() {
		return this.doorLocations;
	}
	// return agent's location history
	public ArrayList<Coordinates> returnLocationHistory() {
		return this.fullLocationHistory;
	}


	public DetectedRooms getRoomByID(int id) {
		for (int i = 0; i < detectedRooms.size(); i++) {
			if (detectedRooms.get(i).getRoomID() == id) return detectedRooms.get(i);
		}
		return null;
	}
	
	// store agent location
	public void storeLocation(Coordinates coords) {
		this.fullLocationHistory.add(coords);
	}
	

	public Coordinates getCoordinates() {
		return new Coordinates(this.x, this.y);
	}
	
	public ArrayList<DetectedRooms> getDetectedRooms() {
		return this.detectedRooms;
	}
	public int getRoomsFound() {
		return this.detectedRooms.size();
	}
	
	public void addDetectedRoom(DetectedRooms dr) {
		this.detectedRooms.add(dr);
	}
	
	public DetectedRooms getDetectedRoom(int room) {
		return this.detectedRooms.get(room);
	}
	
	public void setPossibleRoomDetection() {
		this.possibleNeRoomJustDetected = true;
	}
	
	public void unSetPossibleRoomDetection() {
		this.possibleNeRoomJustDetected = false;
	}
	
	public int getOperation() {
		return this.operation;
	}
	
	public void setOperation(int op) {
		this.operation = op;
	}

	public void setDoorLocation(Coordinates coords) {
		this.doorLocations.add(coords);
	}
	
	public ArrayList<DetectedRooms> getRooms() {
		return this.detectedRooms;
	}
	
	public ArrayList<Coordinates> getDoorLocations() {
		return this.doorLocations;
	}
	
	public void incRoomsFound() {
		this.roomsFound++;
	}
	
	public void setDirection(int dir) {
		this.direction = dir;
	}
	
	public int getTargetX() {
		return this.targetX;
	}
	
	public int getTargetY() {
		return this.targetY;
	}
	
	private boolean doorAlreadySaved(Door door) {

		for (int i = 0; i < this.getFoundDoors().size(); i++) {
			Door door2 = this.getFoundDoors().get(i);
			
			if (door2.equals(door)) return true;
		}
		
		return false;
		
	}
	
	public void setPermission(boolean perm) {
		this.permissionToWalkThrougPassedDoors = perm;
	}
	
	public boolean getPermission() {
		return this.permissionToWalkThrougPassedDoors;
	}
	
	public boolean sameDoors(Door door1, Door door2) {
		  
			  
		  if (door1.getDoorCoordinates().size() != door2.getDoorCoordinates().size()) {
			  //System.out.println("door1 size: "+door1.getDoorCoordinates().size() + " door2 size: "+door2.getDoorCoordinates().size());
			  return false;
		  }
		  
		  int size = door1.getDoorCoordinates().size();
		   boolean result = true;
		   
		   
		
		  
		   for (int i = 0; i < door1.getDoorCoordinates().size(); i++) {
			   
			   
			   
			   if (!((door1.getDoorCoordinates().get(i).getX() == door2.getDoorCoordinates().get(i).getX() && door1.getDoorCoordinates().get(i).getY() == door2.getDoorCoordinates().get(i).getY()) ||
				(door1.getDoorCoordinates().get(size - 1 - i).getX() == door2.getDoorCoordinates().get(size - 1 - i).getX() && door1.getDoorCoordinates().get(i).getY() == door2.getDoorCoordinates().get(i).getY()) ||
				(door1.getDoorCoordinates().get(i).getX() == door2.getDoorCoordinates().get(i).getX() && door1.getDoorCoordinates().get(size - 1 - i).getY() == door2.getDoorCoordinates().get(size - 1 - i).getY())
				)) result = false;
		   }
		   
		   
		   /*
		   if (result == false) {
			   for (int i = 0; i < door1.getDoorCoordinates().size(); i++) {
				   System.out.println("Door1: x: " + door1.getDoorCoordinates().get(i).getX() + " Door2: x: " + door2.getDoorCoordinates().get(i).getX() + "Door1: y: " + door1.getDoorCoordinates().get(i).getY() + " Door2: y: " + door2.getDoorCoordinates().get(i).getY());
			   }
		   }
		   */
		   return result;
	   }
	  
	  public boolean atTargetDoor(Door door) {
		  
		  for (int i = 0; i < door.getDoorCoordinates().size(); i++) {
			  if (this.x == door.getDoorCoordinates().get(i).getX() && this.y == door.getDoorCoordinates().get(i).getY()) {
				  return true;
			  }
		  }
		  return false;
	  }
}

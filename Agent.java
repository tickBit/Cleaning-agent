import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.io.BufferedReader;


/**
 * 
 * @author Random Student
 * 
 * This is just a prototype made in a hurry. Hardly no comments.. May still have some bugs..
 * But since programming was not required for the assignment, this prototype is just meant to
 * demonstrate the idea presented in the TIES4320 research paper..
 *
 * So, this is some kind of prototype for a cleaning agent...
 * 
 * I didn't plan this implementation, just tried out the idea in my research paper...
 * This is why this whole implementation is quite a mess...
 * 
 * There are not-used parts in the application..
 * 
 * The cleaning agent is "blind" and doesn't know at fist anything about its environment...
 * 
 * The text output of the application tells what the agent has in its mind...
 * 
 */
public class Agent {

	JFrame frame;
	DrawPanel drawPanel;


	final private int UP = 0;
	final private int DOWN = 1;
	final private int LEFT = 2;
	final private int RIGHT = 3;

	final private int FINDWALL = 0;
	final private int DETECTROOM = 1; // checks also for the doors of the room
	final private int CREATEROOMMAP = 2;
	final private int CLEANROOM = 3;
	final private int FINDDOOR = 4;
	final private int TASKDONE = 5;
	
	
	private int currentRoom = 0;
	private int roomNr = 0;
	private int lastStopHits = 0;

	int tempRoomLeft = -1;
	int tempRoomRight = -1;
	int tempRoomUp = -1;
	int tempRoomDown = -1;
	

	private Door targetDoor = new Door();
	

	private int roomHistoryStart = -1;
	
	private boolean allRoomsCleaned = false;

	private IntelligentAgent ia = new IntelligentAgent();

	private ArrayList<Coordinates> tempDoorCoords = new ArrayList<Coordinates>();
	private ArrayList<StopHit> stopHits = new ArrayList<StopHit>();

	private ArrayList<Coordinates> possibleNewRoomDetections = new ArrayList<Coordinates>();
	private ArrayList<Door> forbiddenDoor = new ArrayList<Door>();
	private Door previousDoor = new Door();
	
	static private int[][] room = new int[43][20];

	boolean initSweep = false;
	boolean sweep = false;
	int sweepStartX = -1;
	int sweepStopX = -1;
	int sweepStartY = -1;
	int sweepStopY = -1;

	int roomsMapped = 0;
	int doorsVisited = 0;
	int roomsEntered = 0;

	int tempNrDoors = 0;
	int temp = 0;
	int colsCleaned = 0;
	int rowsCleaned = 0;

	
	boolean firstRowOrCol = true;
	int cleaningDir = -1;

	int everySecond = 1;

	Coordinates rmStart;


	public static void main(String[] args) {

		Charset encoding = Charset.defaultCharset();

		File file = new File("rooms//room7.txt");
		try {
			handleFile(file, encoding);
		} catch (IOException e) {

			e.printStackTrace();
		}

		new Agent().go();

	}

	private static void handleFile(File file, Charset encoding) throws IOException {
		try (InputStream in = new FileInputStream(file);
				Reader reader = new InputStreamReader(in, encoding);
				// buffer for efficiency
				Reader buffer = new BufferedReader(reader)) {
			handleCharacters(buffer);
		}
	}

	private static void handleCharacters(Reader reader) throws IOException {
		int r;
		int x = 0;
		int y = 0;

		while ((r = reader.read()) != -1) {

			char ch = (char) r;

			if (ch == 'x') {
				room[x][y] = 1;
				x++;
			}

			if (ch == ' ') {
				room[x][y] = 0;
				x++;
			}

			if (r == 13) {
				x = 0;
				y++;
			}

		}
	}

	private void go() {

		frame = new JFrame("Cleaning agent");

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		drawPanel = new DrawPanel();

		frame.getContentPane().add(drawPanel);

		frame.setResizable(false);
		frame.setSize(1384, 680);
		frame.setLocationByPlatform(true);
		frame.setVisible(true);

		try {
			Thread.sleep(35);
		} catch (Exception e) {
			e.printStackTrace();
		}

		int dir = (int) (Math.random() * 100) % 4;
		//dir = LEFT;
		
		ia.setDirection(dir);
		ia.setOperation(FINDWALL);

		moveIt();

	}

	class DrawPanel extends JPanel {

		private static final long serialVersionUID = 1L;

		public void paintComponent(Graphics g) {

			g.setColor(Color.BLACK);
			g.fillRect(0, 0, this.getWidth(), this.getHeight());
			g.setColor(Color.WHITE);
			g.fillRect(32, 32, this.getWidth() - 64, this.getHeight() - 64);

			

		
			for (int j = 0; j < room[0].length; j++) {
				for (int i = 0; i < room.length; i++) {
					g.setColor(Color.BLACK);
					if (room[i][j] == 1) g.fillRect(i * 32, j * 32, 32, 32);

				}
			}


			g.setColor(Color.LIGHT_GRAY);
			for (int i = 0; i < ia.returnLocationHistory().size(); i++) {

				g.fillRect(ia.returnLocationHistory().get(i).getX(), ia.returnLocationHistory().get(i).getY(), 32, 32);

			}

			
			g.setColor(Color.CYAN);
			for (int i = 0; i < ia.getFoundDoors().size(); i++) {
				for (int ii = 0; ii < ia.getFoundDoors().get(i).getDoorCoordinates().size(); ii++) {
				  g.fillRect(ia.getFoundDoors().get(i).getDoorCoordinates().get(ii).getX(), ia.getFoundDoors().get(i).getDoorCoordinates().get(ii).getY(), 32, 32);
				}
			}
			
			
			  g.setColor(Color.YELLOW); 
			  for (int i = 0; i < ia.getVisitedDoors().size(); i++) {
			  
				  for (int ii = 0; ii < ia.getVisitedDoors().get(i).getDoorCoordinates().size(); ii++) {
					
					  g.fillRect(ia.getVisitedDoors().get(i).getDoorCoordinates().get(ii).getX(), ia.getVisitedDoors().get(i).getDoorCoordinates().get(ii).getY(), 32, 32);
				}
			}
			if (targetDoor != null) {
				  g.setColor(Color.GREEN);
			 
				  for (int ii = 0; ii < targetDoor.getDoorCoordinates().size(); ii++) {
			  
					  g.fillRect(targetDoor.getDoorCoordinates().get(ii).getX(), targetDoor.getDoorCoordinates().get(ii).getY(), 32, 32);
			  
			  	}
			}
			 
			
			g.setColor(Color.ORANGE);
			for (int i = 0; i < forbiddenDoor.size(); i++) {
				for (int ii = 0; ii < forbiddenDoor.get(i).getDoorCoordinates().size(); ii++) {
				  g.fillRect(forbiddenDoor.get(i).getDoorCoordinates().get(ii).getX(), forbiddenDoor.get(i).getDoorCoordinates().get(ii).getY(), 32, 32);
				}
			}
			

			g.setColor(Color.PINK);
			 for (int i = 0; i < stopHits.size(); i++) {
				 
				 g.fillRect(stopHits.get(i).getCoordinates().getX(), stopHits.get(i).getCoordinates().getY(), 32, 32);	 
			 }
			 

			// draw the agent			
			 g.setColor(Color.RED);
			g.fillRect(ia.getX(), ia.getY(), 32, 32);

			if (allRoomsCleaned == true) {
				g.setColor(Color.BLUE);
				g.setFont(new Font("TimesRoman", Font.PLAIN, 78));
				g.drawString("ALL ROOMS CLEANED!", 200, 200);
				
			}
		}
	}

	private void moveIt() {
		while (true) {

			if (ia.getOperation() == TASKDONE) {
				
				return;
			}
			
			if (ia.getOperation() == CLEANROOM) {

				if (cleanRoom(ia.getRoomByID(currentRoom)) == true) {
					this.lastStopHits = 0;
					this.stopHits.clear();

					System.out.println("Room cleaned.");
						//System.out.println(this.possibleNewRoomDetections.size() + " : " + ia.getVisitedDoors().size() + " : " + ia.getFoundDoors().size());
					   if (this.possibleNewRoomDetections.size() == 0 && ia.getVisitedDoors().size() == ia.getFoundDoors().size()) {
						   this.allRoomsCleaned = true;
						   System.out.println("All rooms cleaned!");
						   ia.setOperation(TASKDONE);
						   return;
					   }
					   
					initFindDoor();
				}

			}
			if (ia.getOperation() == FINDWALL) {

				// targetDoor = null;
				ia.setPrevDir(-1);
				if (findWall() == true) {
					System.out.println("a wall found or a known door found!");

					if (this.lastStopHits >= 4) {
						if (this.doesTheHistoryMakeARectangle() == true) {
							System.out.println("Checking room's borders...");
							ia.setOperation(DETECTROOM);
						} else {
							
							ia.turn90degrees();
						}
					} else {
						
						ia.turn90degrees();
					}
				}
			}

			if (ia.getOperation() == DETECTROOM) {
				if (findRoomBorders() == true) {

					ia.setOperation(CREATEROOMMAP);

				}
			}

			if (ia.getOperation() == CREATEROOMMAP) {

				createRoomMap();

			}

			if (ia.getOperation() == FINDDOOR) {

				moveToDoor();

			}

			try {
				Thread.sleep(45);	// 100 == slow, 15 == fast
			} catch (Exception e) {
				e.printStackTrace();
			}

			ia.storeLocation(new Coordinates(ia.getX(), ia.getY()));

			frame.repaint();
		}
	}

	private void createRoomMap() {

		ArrayList<Coordinates> history = ia.returnLocationHistory();

		int leftWallX = Integer.MAX_VALUE;
		int rightWallX = Integer.MIN_VALUE;
		int upWallY = Integer.MAX_VALUE;
		int downWallY = Integer.MIN_VALUE;
		int k = -1;

		
		DetectedRooms room = whichRoom();
		if (room != null) {
			
			System.out.println("PANIC!");	// This should never happen.. Just left from an unfinished implementation..
		}


		DetectedRooms dr = new DetectedRooms();

		Coordinates roomStart = new Coordinates(stopHits.get(stopHits.size() - 1).getCoordinates().getX(),
				stopHits.get(stopHits.size() - 1).getCoordinates().getY());

		// starting point the first hit in the wall in the room
		for (int j = history.size() - 2; j > -1; j--) {

			if (history.get(j).getY() <= upWallY)
				upWallY = history.get(j).getY();

			dr.addRoomCoordinates(new Coordinates(history.get(j).getX(), history.get(j).getY()));
			dr.addRoomLocation(new Coordinates(history.get(j).getX(), history.get(j).getY()));

			if (history.get(j).getX() < leftWallX)
				leftWallX = history.get(j).getX();
			if (history.get(j).getX() > rightWallX)
				rightWallX = history.get(j).getX();
			if (history.get(j).getY() > downWallY)
				downWallY = history.get(j).getY();
			if (history.get(j).getY() < upWallY)
				upWallY = history.get(j).getY();

			if (history.get(j).getX() == roomStart.getX() && history.get(j).getY() == roomStart.getY())
				break;

		}

		dr.setDownWallY(downWallY);
		dr.setLeftWallX(leftWallX);
		dr.setRightWallX(rightWallX);
		dr.setUpWallY(upWallY);
		dr.setRoomMapped(true);
		dr.setRoomID(roomNr);
/*
		System.out.println("up: " + upWallY);
		System.out.println("down: " + downWallY);
		System.out.println("left: " + leftWallX);
		System.out.println("right: " + rightWallX);
*/
		currentRoom = roomNr;

		System.out.println("Room mapped.");

		// delete unnecessary doors, that is: doors that are in practice one, must be a single door
		
		// horizontal doors
		for (int i = 0; i < ia.getFoundDoors().size(); i++) {
			
			Door d1 = ia.getFoundDoors().get(i);
			for (int j = 1; j < ia.getFoundDoors().size(); j++) {
							
				Door d2 = ia.getFoundDoors().get(j);

				if (d1.getDoorCoordinates().get(0).getY() == d2.getDoorCoordinates().get(0).getY() && i != j) {

					try {
						// from left to right
						if (d1.getDoorCoordinates().get(d1.getDoorCoordinates().size() - 1).getX() - d2.getDoorCoordinates().get(0).getX() == -32) {
							
							for (int c = 0; c < d2.getDoorCoordinates().size(); c++) {
								d1.addCoordinates(new Coordinates(d2.getDoorCoordinates().get(c).getX(), d2.getDoorCoordinates().get(c).getY()));
							}
							System.out.println("Door built from pieces.");
							ia.deleteDoor(j);
							break;
						}
						
					} catch (Exception ex) {}
					
					try {
						// from right to left
						if (d2.getDoorCoordinates().get(d2.getDoorCoordinates().size() - 1).getX() - d1.getDoorCoordinates().get(0).getX() == -32 && i != j) {
							
							for (int c = 0; c < d1.getDoorCoordinates().size(); c++) {
								d2.addCoordinates(new Coordinates(d1.getDoorCoordinates().get(c).getX(), d1.getDoorCoordinates().get(c).getY()));
							}
							System.out.println("Door built from pieces.");
							ia.deleteDoor(i);
							break;
						}
						
					} catch (Exception ex) {}
				}
			
			}
		}
		
		// delete unnecessary doors, that is doors that are in practice one, must be a single door
		// vertical doors
		for (int i = 0; i < ia.getFoundDoors().size(); i++) {
			
			Door d1 = ia.getFoundDoors().get(i);
			for (int j = 1; j < ia.getFoundDoors().size(); j++) {
							
				Door d2 = ia.getFoundDoors().get(j);

				if (d1.getDoorCoordinates().get(0).getX() == d2.getDoorCoordinates().get(0).getX() && i != j) {
					try {
						if (d1.getDoorCoordinates().get(0).getY() - d2.getDoorCoordinates().get(0).getY() == 32) {
							
							
							for (int c = 0; c < d2.getDoorCoordinates().size(); c++) {
								d1.addCoordinates(new Coordinates(d2.getDoorCoordinates().get(c).getX(), d2.getDoorCoordinates().get(c).getY()));
							}
							System.out.println("Door built from pieces.");
							ia.deleteDoor(j);
							
						}
						
					} catch (Exception ex) {}
				
					try {
						if (d1.getDoorCoordinates().get(d1.getDoorCoordinates().size()-1).getY() - d2.getDoorCoordinates().get(0).getY() == 32 && i != j) {
														
							for (int c = 0; c < d2.getDoorCoordinates().size(); c++) {
								d1.addCoordinates(new Coordinates(d2.getDoorCoordinates().get(c).getX(), d2.getDoorCoordinates().get(c).getY()));
							}
							System.out.println("Door built from pieces.");
							ia.deleteDoor(j);
							
						}
						
					} catch (Exception ex) {}
				}
			
			}
		}

		System.out.println("Found doors: " + ia.getFoundDoors().size());
		for (int d = 0; d < ia.getFoundDoors().size(); d++) {

			//if (ia.getFoundDoors().get(d).getDoorCoordinates().size() > 0) {
				if (ia.getFoundDoors().get(d).getDoorCoordinates().get(0).getX() >= leftWallX - 32
						&& ia.getFoundDoors().get(d).getDoorCoordinates().get(0).getX() <= rightWallX + 32
						&& ia.getFoundDoors().get(d).getDoorCoordinates().get(0).getY() >= upWallY - 32
						&& ia.getFoundDoors().get(d).getDoorCoordinates().get(0).getY() <= downWallY + 32) {

					dr.addDoor(ia.getFoundDoors().get(d));
					
					
					System.out.println("Door added");
				}
			//}

		}
		
		
	
		
		
		dr.setRoomMapped(true);
		dr.setRoomID(roomNr);
		currentRoom = roomNr;
		dr.setArea();


		//System.out.println("This is room number " + roomNr);
		ia.addDetectedRoom(dr);
		roomNr++;

		this.roomsMapped++;

		
		System.out.println("Doors in room: " + dr.getDoors().size());
		System.out.println("Rooms mapped: " + this.roomsMapped);

		this.sweepStartX = leftWallX;
		this.sweepStopX = rightWallX;
		this.sweepStartY = upWallY;
		this.sweepStopY = downWallY;


		ia.setOperation(CLEANROOM);
		System.out.println("Cleaning a room...");
		return;

	}

	private void initFindDoor() {
		
		DetectedRooms dr = ia.getDetectedRoom(currentRoom);


		System.out.println("---");
		System.out.println("Visited doors so far: " + ia.getVisitedDoors().size());
		ArrayList<Door> ds = dr.getDoors();

		System.out.println("Doors in this room: " + ds.size());

		boolean isNonVisitedDoors = false;
		for (int i = 0; i < ds.size(); i++) {
			if (ds.get(i).getVisited() == false) isNonVisitedDoors = true;
		}
		
		if (isNonVisitedDoors == false) {
			System.out.println("Didn't find not-visited doors!");
			ia.setPermission(true);
			System.out.println("Permission to enter visited doors set to true.");
		}
		
		
		int index = -1;

		temp = 0;

		ArrayList<Door> roomsVisitedDoors = new ArrayList<Door>();
		ArrayList<Door> roomsNonVisitedDoors = new ArrayList<Door>();

		
		for (int i = 0; i < ds.size(); i++) {
			if (ds.get(i).getVisited() == true && compareDoors(ds.get(i), previousDoor) == false && isForbiddenDoor(ds.get(i)) == false) roomsVisitedDoors.add(ds.get(i));
			if (ds.get(i).getVisited() == false && compareDoors(ds.get(i), previousDoor) == false && isForbiddenDoor(ds.get(i)) == false) roomsNonVisitedDoors.add(ds.get(i));
		}
		
		
		if (ia.getPermission() == true) {

		   
			//System.out.println("permission == truessa!");
			
			int counter = 0;
			
			if (roomsVisitedDoors.size() > 0) {
				while (index == -1) {
					int r = (int)(Math.random()*100) % roomsVisitedDoors.size();
					if (isForbiddenDoor(roomsVisitedDoors.get(r)) == false && compareDoors(roomsVisitedDoors.get(r), previousDoor) == false) {
						index = r;
						break;
					} else {
						counter++;
						if (counter == ds.size()) {
							for (int i = 0; i < roomsVisitedDoors.size(); i++) {
								if (compareDoors(roomsVisitedDoors.get(i), previousDoor) == true) {
									index = i;
									System.out.println("PANIC: Had to choose a previously visited door.");
								}
							}
							
						}
					}
							
				}
					
				
			} else {
				index = 0;
			}
								
				
			targetDoor.clearDoorCoordinates();
			if (roomsVisitedDoors.size() > 0) {
				for (int t = 0; t < roomsVisitedDoors.get(index).getDoorCoordinates().size(); t++) {
					targetDoor.addCoordinates(new Coordinates(roomsVisitedDoors.get(index).getDoorCoordinates().get(t).getX(), roomsVisitedDoors.get(index).getDoorCoordinates().get(t).getY()));
				}
			} else {
				if (ds.size() == 1) {
					for (int t = 0; t < ds.get(0).getDoorCoordinates().size(); t++) {
						targetDoor.addCoordinates(new Coordinates(ds.get(0).getDoorCoordinates().get(t).getX(), ds.get(0).getDoorCoordinates().get(t).getY()));
					}	
				}
			}
			ia.setTargetX(targetDoor.getDoorCoordinates().get(0).getX());
			ia.setTargetY(targetDoor.getDoorCoordinates().get(0).getY());
			System.out.println("Target coordinates set.");
			System.out.println("Target x: " + ia.getTargetX());
			System.out.println("Target y: " + ia.getTargetY());
			
			ia.setOperation(FINDDOOR);
			System.out.println("Going to a door...");
			return;
			// permission to enter visitedDoors == false
		} else {
			index = -1;
			
			
			if (roomsNonVisitedDoors.size() > 0) {
				
					int r = (int)(Math.random()*100) % roomsNonVisitedDoors.size();
					targetDoor.clearDoorCoordinates();
					for (int t = 0; t < roomsNonVisitedDoors.get(r).getDoorCoordinates().size(); t++) {
						targetDoor.addCoordinates(new Coordinates(roomsNonVisitedDoors.get(r).getDoorCoordinates().get(t).getX(), roomsNonVisitedDoors.get(r).getDoorCoordinates().get(t).getY()));
					}
					
					ia.setTargetX(targetDoor.getDoorCoordinates().get(0).getX());
					ia.setTargetY(targetDoor.getDoorCoordinates().get(0).getY());
					System.out.println("Target coordinates set.");
					System.out.println("Target x: " + ia.getTargetX());
					System.out.println("Target y: " + ia.getTargetY());
					
					ia.setOperation(FINDDOOR);
					System.out.println("Going to a door...");
					return;
			} else {
				int r = 0;
				targetDoor.clearDoorCoordinates();
				for (int t = 0; t < ds.get(r).getDoorCoordinates().size(); t++) {
					targetDoor.addCoordinates(new Coordinates(ds.get(r).getDoorCoordinates().get(t).getX(), ds.get(r).getDoorCoordinates().get(t).getY()));
				}
				
				ia.setTargetX(targetDoor.getDoorCoordinates().get(0).getX());
				ia.setTargetY(targetDoor.getDoorCoordinates().get(0).getY());
				System.out.println("Target coordinates set.");
				System.out.println("Target x: " + ia.getTargetX());
				System.out.println("Target y: " + ia.getTargetY());
				
				ia.setOperation(FINDDOOR);
				System.out.println("Going to a door...");
				return;				
			}
			
		}
		

	}

	private boolean isVisitedDoor(Door door) {

		for (int ddd = 0; ddd < ia.getVisitedDoors().size(); ddd++) {

			if (compareDoors(ia.getVisitedDoors().get(ddd), door) == true)
				return true;

		}
		return false;
	}

	private boolean isForbiddenDoor(Door door) {

		for (int ddd = 0; ddd < this.forbiddenDoor.size(); ddd++) {

			if (compareDoors(this.forbiddenDoor.get(ddd), door) == true)
				return true;

		}
		return false;
	}
	
	private boolean compareDoors(Door door1, Door door2) {

		if (door1 == null || door2 == null)
			return false;
		if (door1.getDoorCoordinates().size() != door2.getDoorCoordinates().size())
			return false;

		int size = door1.getDoorCoordinates().size();
		boolean result = true;

		for (int i = 0; i < door1.getDoorCoordinates().size(); i++) {
			if (!((door1.getDoorCoordinates().get(i).getX() == door2.getDoorCoordinates().get(i).getX()
					&& door1.getDoorCoordinates().get(i).getY() == door2.getDoorCoordinates().get(i).getY())
					|| (door1.getDoorCoordinates().get(size - 1 - i).getX() == door2.getDoorCoordinates()
							.get(size - 1 - i).getX()
							&& door1.getDoorCoordinates().get(i).getY() == door2.getDoorCoordinates().get(i).getY())
					|| (door1.getDoorCoordinates().get(i).getX() == door2.getDoorCoordinates().get(i).getX()
							&& door1.getDoorCoordinates().get(size - 1 - i).getY() == door2.getDoorCoordinates()
									.get(size - 1 - i).getY())))
				result = false;
		}

		return result;
	}

	private DetectedRooms whichRoom() {
		

		for (int i = 0; i < ia.getDetectedRooms().size(); i++) {

			if (ia.getX() >= ia.getDetectedRooms().get(i).getLeftWallX()
					&& ia.getX() <= ia.getDetectedRooms().get(i).getRightWallX()
					&& ia.getY() >= ia.getDetectedRooms().get(i).getUpWallY()
					&& ia.getY() <= ia.getDetectedRooms().get(i).getDownWallY())
				return ia.getDetectedRooms().get(i);
		}

		return null;
		
	}

	private boolean cleanRoom(DetectedRooms dr) {

		if (dr.getRoomCleaned() == true)
			return true;

		if (this.stopHits.get(stopHits.size() - 1).getDirection() == LEFT) {

			if (ia.getPrevDir() == UP) {
				this.cleaningDir = DOWN;
				ia.setDirection(DOWN);
			}
			
			if (ia.getPrevDir() == DOWN) {
				this.cleaningDir = UP;
				ia.setDirection(UP);
			}
			
			if (this.firstRowOrCol == true && this.cleaningDir == -1) {
				if (wallAt(ia.getX() / 32, ia.getY() / 32 + 1) == false
						&& doorAtLocation(ia.getX(), ia.getY() + 32) == false) {
					this.cleaningDir = DOWN;
					ia.setDirection(DOWN);
				} else if ((wallAt(ia.getX() / 32, ia.getY() / 32 - 1) == false)
						&& doorAtLocation(ia.getX(), ia.getY() - 32) == false) {
					this.cleaningDir = UP;
					ia.setDirection(cleaningDir);

				}
				ia.moveStep();
				return false;
			}
			if (this.firstRowOrCol == true && this.cleaningDir != -1) {

				if (this.cleaningDir == DOWN) {
					if (wallAt(ia.getX() / 32, ia.getY() / 32 + 1) == true
							|| doorAtLocation(ia.getX(), ia.getY() + 32) == true) {
						this.cleaningDir = UP;
						ia.setDirection(cleaningDir);
						ia.moveStep();
						return false;

					}
				}
				if (this.cleaningDir == UP) {
					if ((wallAt(ia.getX() / 32, ia.getY() / 32 - 1) == true)
							|| doorAtLocation(ia.getX(), ia.getY() - 32) == true) {
						this.firstRowOrCol = false;
						this.colsCleaned++;
						ia.setDirection(RIGHT);
						ia.moveStep();
						ia.setDirection(DOWN);
						this.cleaningDir = DOWN;

						ia.moveStep();
						return false;
					}

				}
				ia.moveStep();
				return false;
			}

			if (this.firstRowOrCol == false) {

				// the cleaning stops

				if (this.colsCleaned == dr.getWidth() == true) {
					this.cleaningDir = -1;
					this.firstRowOrCol = true;
					this.rowsCleaned = 0;
					this.colsCleaned = 0;
					
					removePossibleRoomDetection(dr);

					if (dr.getDoors().size() == 1) {
						forbiddenDoor.add(dr.getDoors().get(0));
					}
					
					dr.setRoomCleaned(true);

	
					return true;
				}

				if (this.cleaningDir == UP && wallAt(ia.getX() / 32, ia.getY() / 32 - 1) == false
						&& doorAtLocation(ia.getX(), ia.getY() - 32) == false) {
					ia.moveStep();
					return false;
				}

				if (this.cleaningDir == DOWN && wallAt(ia.getX() / 32, ia.getY() / 32 + 1) == false
						&& doorAtLocation(ia.getX(), ia.getY() + 32) == false) {
					ia.moveStep();
					return false;
				}

				if (this.cleaningDir == UP && (wallAt(ia.getX() / 32, ia.getY() / 32 - 1) == true)
						|| doorAtLocation(ia.getX(), ia.getY() - 32) == true) {
					this.colsCleaned++;
					ia.setDirection(RIGHT);
					ia.moveStep();
					this.cleaningDir = DOWN;
					ia.setDirection(cleaningDir);
					this.firstRowOrCol = false;
					ia.moveStep();
					return false;
				}

				if (this.cleaningDir == DOWN && (wallAt(ia.getX() / 32, ia.getY() / 32 + 1) == true)
						|| doorAtLocation(ia.getX(), ia.getY() + 32) == true) {
					this.colsCleaned++;
					ia.setDirection(RIGHT);
					ia.moveStep();
					this.cleaningDir = UP;
					ia.setDirection(cleaningDir);
					ia.moveStep();
					return false;
				}

			}
		}

		if (this.stopHits.get(stopHits.size() - 1).getDirection() == UP) {

			if (ia.getPrevDir() == LEFT) {
				this.cleaningDir = RIGHT;
				ia.setDirection(RIGHT);
			}
			
			if (ia.getPrevDir() == RIGHT) {
				this.cleaningDir = RIGHT;
				ia.setDirection(RIGHT);
			}
			
			if (this.firstRowOrCol == true && this.cleaningDir == -1) {
				if (wallAt(ia.getX() / 32 - 1, ia.getY() / 32) == false
						&& doorAtLocation(ia.getX() - 32, ia.getY()) == false) {
					this.cleaningDir = LEFT;
					ia.setDirection(LEFT);
				} else if ((wallAt(ia.getX() / 32 + 1, ia.getY() / 32) == false)
						&& doorAtLocation(ia.getX() + 32, ia.getY()) == false) {
					this.cleaningDir = RIGHT;
					ia.setDirection(cleaningDir);

				}
				ia.moveStep();
				return false;
			}
			if (this.firstRowOrCol == true && this.cleaningDir != -1) {

				if (this.cleaningDir == LEFT) {
					if (wallAt(ia.getX() / 32 - 1, ia.getY() / 32) == true
							|| doorAtLocation(ia.getX() - 32, ia.getY()) == true) {
						this.cleaningDir = RIGHT;
						ia.setDirection(cleaningDir);
						ia.moveStep();
						return false;

					}
				}

				if (this.cleaningDir == RIGHT) {
					if ((wallAt(ia.getX() / 32 + 1, ia.getY() / 32) == true)
							|| doorAtLocation(ia.getX() + 32, ia.getY()) == true) {
						this.firstRowOrCol = false;
						this.rowsCleaned++;
						ia.setDirection(DOWN);
						ia.moveStep();
						ia.setDirection(RIGHT);
						ia.moveStep();
						ia.setDirection(LEFT);
						this.cleaningDir = LEFT;

						ia.moveStep();
						return false;
					}

				}
				ia.moveStep();
				return false;
			}

			if (this.firstRowOrCol == false) {

				if (this.rowsCleaned == dr.getHeight()) {

					this.cleaningDir = -1;
					this.firstRowOrCol = true;
					this.rowsCleaned = 0;
					this.colsCleaned = 0;

					removePossibleRoomDetection(dr);
					
					if (dr.getDoors().size() == 1) {
						forbiddenDoor.add(dr.getDoors().get(0));
					}
					
					dr.setRoomCleaned(true);
					return true;
				}

				if (this.cleaningDir == RIGHT && wallAt(ia.getX() / 32 + 1, ia.getY() / 32) == false
						&& doorAtLocation(ia.getX() + 32, ia.getY()) == false) {
					ia.moveStep();
					return false;
				}

				if (this.cleaningDir == LEFT && wallAt(ia.getX() / 32 - 1, ia.getY() / 32) == false
						&& doorAtLocation(ia.getX() - 32, ia.getY()) == false) {
					ia.moveStep();
					return false;
				}

				if (this.cleaningDir == RIGHT && (wallAt(ia.getX() / 32 + 1, ia.getY() / 32) == true)
						|| doorAtLocation(ia.getX() + 32, ia.getY()) == true) {
					this.rowsCleaned++;
					ia.setDirection(DOWN);
					ia.moveStep();
					this.cleaningDir = LEFT;
					ia.setDirection(cleaningDir);
					this.firstRowOrCol = false;
					ia.moveStep();
					return false;
				}

				if (this.cleaningDir == LEFT && (wallAt(ia.getX() / 32 - 1, ia.getY() / 32) == true)
						|| doorAtLocation(ia.getX() - 32, ia.getY()) == true) {
					this.rowsCleaned++;
					ia.setDirection(DOWN);
					ia.moveStep();
					this.cleaningDir = RIGHT;
					ia.setDirection(cleaningDir);
					ia.moveStep();
					return false;
				}

			}

		}

		if (this.stopHits.get(stopHits.size() - 1).getDirection() == RIGHT) {

			if (ia.getPrevDir() == UP) {
				this.cleaningDir = DOWN;
				ia.setDirection(DOWN);
			}
			
			if (ia.getPrevDir() == DOWN) {
				this.cleaningDir = UP;
				ia.setDirection(UP);
			}
			
			if (this.firstRowOrCol == true && this.cleaningDir == -1) {
				if (wallAt(ia.getX() / 32, ia.getY() / 32 + 1) == false
						&& doorAtLocation(ia.getX(), ia.getY() + 32) == false) {
					this.cleaningDir = DOWN;
					ia.setDirection(DOWN);
				} else if ((wallAt(ia.getX() / 32, ia.getY() / 32 - 1) == false)
						&& doorAtLocation(ia.getX(), ia.getY() - 32) == false) {
					this.cleaningDir = UP;
					ia.setDirection(cleaningDir);

				}
				ia.moveStep();
				return false;
			}
			if (this.firstRowOrCol == true && this.cleaningDir != -1) {

				if (this.cleaningDir == DOWN) {
					if (wallAt(ia.getX() / 32, ia.getY() / 32 + 1) == true
							|| doorAtLocation(ia.getX(), ia.getY() + 32) == true) {
						this.cleaningDir = UP;
						ia.setDirection(cleaningDir);
						ia.moveStep();
						return false;

					}
				}
				if (this.cleaningDir == UP) {
					if ((wallAt(ia.getX() / 32, ia.getY() / 32 - 1) == true)
							|| doorAtLocation(ia.getX(), ia.getY() - 32) == true) {
						this.firstRowOrCol = false;
						this.colsCleaned++;
						ia.setDirection(LEFT);
						ia.moveStep();
						ia.setDirection(DOWN);
						this.cleaningDir = DOWN;

						ia.moveStep();
						return false;
					}

				}
				ia.moveStep();
				return false;
			}

			if (this.firstRowOrCol == false) {

				if (this.colsCleaned == dr.getWidth()) {
					this.cleaningDir = -1;
					this.firstRowOrCol = true;
					this.colsCleaned = 0;
					this.rowsCleaned = 0;

					removePossibleRoomDetection(dr);

					dr.setRoomCleaned(true);
					
					if (dr.getDoors().size() == 1) {
						forbiddenDoor.add(dr.getDoors().get(0));
					}
					
					return true;
				}

				if (this.cleaningDir == UP && wallAt(ia.getX() / 32, ia.getY() / 32 - 1) == false
						&& doorAtLocation(ia.getX(), ia.getY() - 32) == false) {
					ia.moveStep();
					return false;
				}

				if (this.cleaningDir == DOWN && wallAt(ia.getX() / 32, ia.getY() / 32 + 1) == false
						&& doorAtLocation(ia.getX(), ia.getY() + 32) == false) {
					ia.moveStep();
					return false;
				}

				if (this.cleaningDir == UP && (wallAt(ia.getX() / 32, ia.getY() / 32 - 1) == true)
						|| doorAtLocation(ia.getX(), ia.getY() - 32) == true) {
					this.colsCleaned++;
					ia.setDirection(LEFT);
					ia.moveStep();
					this.cleaningDir = DOWN;
					ia.setDirection(cleaningDir);
					this.firstRowOrCol = false;
					ia.moveStep();
					return false;
				}

				if (this.cleaningDir == DOWN && (wallAt(ia.getX() / 32, ia.getY() / 32 + 1) == true)
						|| doorAtLocation(ia.getX(), ia.getY() + 32) == true) {
					this.colsCleaned++;
					ia.setDirection(LEFT);
					ia.moveStep();
					this.cleaningDir = UP;
					ia.setDirection(cleaningDir);
					ia.moveStep();
					return false;
				}

			}
		}

		if (this.stopHits.get(stopHits.size() - 1).getDirection() == DOWN) {

			
			
			// Ensimm�inen sarake, menn��n yl�s ja alas

			// Aluksi m��ritet��n suunta
			if (this.firstRowOrCol == true && this.cleaningDir == -1) {

				if (ia.getPrevDir() == LEFT) {
					this.cleaningDir = RIGHT;
					ia.setDirection(RIGHT);
				}
				
				if (ia.getPrevDir() == RIGHT) {
					this.cleaningDir = RIGHT;
					ia.setDirection(RIGHT);
				}
				
				
				if (wallAt(ia.getX() / 32 - 1, ia.getY() / 32) == false
						&& doorAtLocation(ia.getX() - 32, ia.getY()) == false) {
					this.cleaningDir = LEFT;
					ia.setDirection(LEFT);
				} else if ((wallAt(ia.getX() / 32 + 1, ia.getY() / 32) == false)
						&& doorAtLocation(ia.getX() + 32, ia.getY()) == false) {
					this.cleaningDir = RIGHT;
					ia.setDirection(cleaningDir);

				}
				
				ia.moveStep();
				return false;
			}
			if (this.firstRowOrCol == true && this.cleaningDir != -1) {

				if (this.cleaningDir == LEFT) {
					if (wallAt(ia.getX() / 32 - 1, ia.getY() / 32) == true
							|| doorAtLocation(ia.getX() - 32, ia.getY()) == true) {
						this.cleaningDir = RIGHT;
						ia.setDirection(cleaningDir);
						ia.moveStep();
						return false;

					}
				}
				if (this.cleaningDir == RIGHT) {
					if ((wallAt(ia.getX() / 32 + 1, ia.getY() / 32) == true)
							|| doorAtLocation(ia.getX() + 32, ia.getY()) == true) {
						this.firstRowOrCol = false;
						ia.setDirection(UP);
						ia.moveStep();
						this.rowsCleaned++;

						ia.setDirection(RIGHT);
						ia.moveStep();
						ia.setDirection(LEFT);
						this.cleaningDir = LEFT;

						ia.moveStep();
						return false;
					}

				}
				ia.moveStep();
				return false;
			}

		}

		if (this.firstRowOrCol == false) {

			if (this.rowsCleaned == ia.getDetectedRoom(currentRoom).getHeight()) {
				this.cleaningDir = -1;
				this.firstRowOrCol = true;
				this.rowsCleaned = 0;
				this.colsCleaned = 0;

				removePossibleRoomDetection(dr);

				if (dr.getDoors().size() == 1) {
					forbiddenDoor.add(dr.getDoors().get(0));
				}
				
				dr.setRoomCleaned(true);
				return true;
			}

			if (this.cleaningDir == RIGHT && wallAt(ia.getX() / 32 + 1, ia.getY() / 32) == false
					&& doorAtLocation(ia.getX() + 32, ia.getY()) == false) {
				ia.moveStep();
				return false;
			}

			if (this.cleaningDir == LEFT && wallAt(ia.getX() / 32 - 1, ia.getY() / 32) == false
					&& doorAtLocation(ia.getX() - 32, ia.getY()) == false) {
				ia.moveStep();
				return false;
			}

			if (this.cleaningDir == RIGHT && (wallAt(ia.getX() / 32 + 1, ia.getY() / 32) == true)
					|| doorAtLocation(ia.getX() + 32, ia.getY()) == true) {
				ia.setDirection(UP);
				this.rowsCleaned++;
				ia.moveStep();
				this.cleaningDir = LEFT;
				ia.setDirection(cleaningDir);
				this.firstRowOrCol = false;
				ia.moveStep();
				return false;
			}

			if (this.cleaningDir == LEFT && (wallAt(ia.getX() / 32 - 1, ia.getY() / 32) == true)
					|| doorAtLocation(ia.getX() - 32, ia.getY()) == true) {
				ia.setDirection(UP);
				this.rowsCleaned++;
				ia.moveStep();
				this.cleaningDir = RIGHT;
				ia.setDirection(cleaningDir);
				ia.moveStep();
				return false;
			}

		}

		return false;
	}

	private void removePossibleRoomDetection(DetectedRooms dr) {
		boolean removed = true;
		boolean removeDone = false;
		
		while (removed == true) {
			for (int i = 0; i < this.possibleNewRoomDetections.size(); i++) {
				if (this.possibleNewRoomDetections.get(i).getX() >= dr.getLeftWallX()
					&& this.possibleNewRoomDetections.get(i).getX() <= dr.getRightWallX()
					&& this.possibleNewRoomDetections.get(i).getY() >= dr.getUpWallY()
					&& this.possibleNewRoomDetections.get(i).getY() <= dr.getDownWallY()) {
					this.possibleNewRoomDetections.remove(i);
					removeDone = true;
				
				}
			}
			
			if (removeDone == true) removed = true; else removed = false;
			removeDone = false;
		}
	}
	private boolean findRoomBorders() {

			DetectedRooms dr = whichRoom();
			if (dr != null) {
				if (dr.getRoomMapped() == true) {

					System.out.println("This room has already been mapped.");
					System.out.println("I'd better find a door...");

					ArrayList<Door> ds = dr.getDoors();
					System.out.println("Doors available: " + ds.size());

					for (int dd = 0; dd < ds.size(); dd++) {

						if (ds.get(dd).getVisited() == false && isForbiddenDoor(ds.get(dd)) == false && compareDoors(ds.get(dd), previousDoor) == false) {

							ia.setTargetX(ds.get(dd).getDoorCoordinates().get(0).getX());
							ia.setTargetY(ds.get(dd).getDoorCoordinates().get(0).getY());
							
							targetDoor.clearDoorCoordinates();
							for (int t = 0; t < ds.get(dd).getDoorCoordinates().size(); t++) {
								targetDoor.addCoordinates(new Coordinates(ds.get(dd).getDoorCoordinates().get(t).getX(), ds.get(dd).getDoorCoordinates().get(t).getY()));
							}
							
							System.out.println(
									"Current target door set. Size: " + targetDoor.getDoorCoordinates().size());
							System.out.println("Target coordinates set.");
							System.out.println("Target x: " + ia.getTargetX());
							System.out.println("Target y: " + ia.getTargetY());
							ia.setOperation(FINDDOOR);
							System.out.println("Going to a door...");
							return false;

						}

					}
					System.out.println("Didn't find not-visited doors!");
					ia.setPermission(true);
					System.out.println("Permission to enter visited doors set to true.");
					initFindDoor();
				}
			}

			this.checkingRoomBorders();			 

			ia.moveStep();
				
				
				if (ia.getX() == rmStart.getX() && ia.getY() == rmStart.getY()) {
			
						this.checkingRoomBorders();
						return true;
					
				}
		

		return false;

	}

	private void checkingRoomBorders() {
		switch (ia.getDirection()) {
		case UP:
			if (ia.getDirection() == UP) {
				if (probeForDoor(ia.getX() / 32, ia.getY() / 32) == true) {
					System.out.println("Door space found.");
					temp++;
				} else if (temp > 0) {
					System.out.println("Door length: " + temp);
					Door door = new Door();
					for (int i = 0; i < tempDoorCoords.size(); i++) {
						door.addCoordinates(
								new Coordinates(tempDoorCoords.get(i).getX(), tempDoorCoords.get(i).getY()));
					}

					ia.addFoundDoor(door);

					tempDoorCoords.clear();
					temp = 0;
				}

				if ((room[ia.getX() / 32][ia.getY() / 32 - 1] > 0
						|| doorAtLocation(ia.getX(), ia.getY() - 32) == true) && ia.getDirection() == UP) {

					if (room[ia.getX() / 32 - 1][ia.getY() / 32] == 0
							&& doorAtLocation(ia.getX() - 32, ia.getY()) == false) {

						ia.setPrevDir(ia.getDirection());
						ia.setDirection(LEFT);
						
						if (temp > 0) {
							System.out.println("Door length: " + temp);
							Door door = new Door();
							for (int i = 0; i < tempDoorCoords.size(); i++) {
								door.addCoordinates(
										new Coordinates(tempDoorCoords.get(i).getX(), tempDoorCoords.get(i).getY()));
							}

							ia.addFoundDoor(door);

							tempDoorCoords.clear();
							temp = 0;
						}
						break;
					}

					if (room[ia.getX() / 32 + 1][ia.getY() / 32] == 0
							&& doorAtLocation(ia.getX() + 32, ia.getY()) == false) {
						ia.setPrevDir(ia.getDirection());
						ia.setDirection(RIGHT);
						
						if (temp > 0) {
							System.out.println("Door length: " + temp);
							Door door = new Door();
							for (int i = 0; i < tempDoorCoords.size(); i++) {
								door.addCoordinates(
										new Coordinates(tempDoorCoords.get(i).getX(), tempDoorCoords.get(i).getY()));
							}

							ia.addFoundDoor(door);

							tempDoorCoords.clear();
							temp = 0;
						}
						break;
					}
				}
			}

		

		case LEFT:
			if (ia.getDirection() == LEFT) {
				if (probeForDoor(ia.getX() / 32, ia.getY() / 32) == true) {
					temp++;
					System.out.println("Door space found.");
				} else if (temp > 0) {
					System.out.println("Door length: " + temp);
					Door door = new Door();
					for (int i = 0; i < tempDoorCoords.size(); i++) {
						door.addCoordinates(
								new Coordinates(tempDoorCoords.get(i).getX(), tempDoorCoords.get(i).getY()));
					}

					ia.addFoundDoor(door);

					tempDoorCoords.clear();
					temp = 0;
				}

				if ((room[ia.getX() / 32 - 1][ia.getY() / 32] > 0
						|| doorAtLocation(ia.getX() - 32, ia.getY()) == true) && ia.getDirection() == LEFT) {

					if (room[ia.getX() / 32][ia.getY() / 32 + 1] == 0
							&& doorAtLocation(ia.getX() + 32, ia.getY()) == false) {

						ia.setPrevDir(ia.getDirection());
						ia.setDirection(DOWN);
						
						if (temp > 0) {
							System.out.println("Door length: " + temp);
							Door door = new Door();
							for (int i = 0; i < tempDoorCoords.size(); i++) {
								door.addCoordinates(
										new Coordinates(tempDoorCoords.get(i).getX(), tempDoorCoords.get(i).getY()));
							}

							ia.addFoundDoor(door);

							tempDoorCoords.clear();
							temp = 0;
						}
						break;
					}

					if (room[ia.getX() / 32][ia.getY() / 32 - 1] == 0
							&& doorAtLocation(ia.getX() - 32, ia.getY()) == false) {
						ia.setPrevDir(ia.getDirection());
						ia.setDirection(UP);
						
						if (temp > 0) {
							System.out.println("Door length: " + temp);
							Door door = new Door();
							for (int i = 0; i < tempDoorCoords.size(); i++) {
								door.addCoordinates(
										new Coordinates(tempDoorCoords.get(i).getX(), tempDoorCoords.get(i).getY()));
							}

							ia.addFoundDoor(door);

							tempDoorCoords.clear();
							temp = 0;
						}
						break;
					}
				}
			}
		

		case RIGHT:

			if (ia.getDirection() == RIGHT) {
				if (probeForDoor(ia.getX() / 32, ia.getY() / 32) == true) {
					temp++;
					System.out.println("Door space found.");
				} else if (temp > 0) {
					System.out.println("Door length: " + temp);
					Door door = new Door();
					for (int i = 0; i < tempDoorCoords.size(); i++) {
						door.addCoordinates(
								new Coordinates(tempDoorCoords.get(i).getX(), tempDoorCoords.get(i).getY()));
					}

					ia.addFoundDoor(door);

					tempDoorCoords.clear();
					temp = 0;
				}

				if ((room[ia.getX() / 32 + 1][ia.getY() / 32] > 0
						|| doorAtLocation(ia.getX() + 32, ia.getY()) == true) && ia.getDirection() == RIGHT) {

					if (room[ia.getX() / 32][ia.getY() / 32 + 1] == 0
							&& doorAtLocation(ia.getX(), ia.getY() + 32) == false) {
						ia.setPrevDir(ia.getDirection());

						ia.setDirection(DOWN);
					
						break;
					}

					if (room[ia.getX() / 32][ia.getY() / 32 - 1] == 0
							&& doorAtLocation(ia.getX(), ia.getY() - 32) == false) {
						ia.setPrevDir(ia.getDirection());
						ia.setDirection(UP);
						
						break;

					}
				}
			}

			

		case DOWN:
			if (ia.getDirection() == DOWN) {
				
				if (probeForDoor(ia.getX() / 32, ia.getY() / 32) == true) {
					System.out.println("Door space found.");
					temp++;
				} else if (temp > 0) {
					System.out.println("Door length: " + temp);
					Door door = new Door();
					for (int i = 0; i < tempDoorCoords.size(); i++) {
						door.addCoordinates(
								new Coordinates(tempDoorCoords.get(i).getX(), tempDoorCoords.get(i).getY()));
					}

					ia.addFoundDoor(door);

					temp = 0;
					tempDoorCoords.clear();
				}

				if ((room[ia.getX() / 32][ia.getY() / 32 + 1] > 0
						|| doorAtLocation(ia.getX(), ia.getY() + 32) == true) && ia.getDirection() == DOWN) {

					if (room[ia.getX() / 32 + 1][ia.getY() / 32] == 0
							&& doorAtLocation(ia.getX() + 32, ia.getY()) == false) {
						ia.setPrevDir(ia.getDirection());
						ia.setDirection(RIGHT);
						
						break;
					}

					if (room[ia.getX() / 32 - 1][ia.getY() / 32] == 0
							&& doorAtLocation(ia.getX() - 32, ia.getY()) == false) {
						ia.setPrevDir(ia.getDirection());
						ia.setDirection(LEFT);
					
						break;
					}
				}

			}
		}
	}
	private boolean doesTheHistoryMakeARectangle() {

		int ex = ia.returnLocationHistory().get(ia.returnLocationHistory().size() - 1).getX();
		int ey = ia.returnLocationHistory().get(ia.returnLocationHistory().size() - 1).getY();

		for (int k = ia.returnLocationHistory().size() - 2; k > -1; k--) {
			if (ex == ia.returnLocationHistory().get(k).getX() && ey == ia.returnLocationHistory().get(k).getY()) {
				// System.out.println("Rectangle!");
				this.roomHistoryStart = k;
				rmStart = new Coordinates(ia.returnLocationHistory().get(k).getX(), ia.returnLocationHistory().get(k).getY());
				System.out.println("Rectangle found!");
				
				this.tempRoomLeft = Math.min(stopHits.get(stopHits.size() - 1).getCoordinates().getX(), stopHits.get(stopHits.size() - 3).getCoordinates().getX());
				this.tempRoomRight = Math.max(stopHits.get(stopHits.size() - 1).getCoordinates().getX(), stopHits.get(stopHits.size() - 3).getCoordinates().getX());
				this.tempRoomUp = Math.min(stopHits.get(stopHits.size() - 1).getCoordinates().getY(), stopHits.get(stopHits.size() - 3).getCoordinates().getY());
				this.tempRoomDown = Math.max(stopHits.get(stopHits.size() - 1).getCoordinates().getY(), stopHits.get(stopHits.size() - 3).getCoordinates().getY());
				return true;
			}
		}

		return false;
	}

	private void moveToDoor() {
	   
	  // important!
	  atDoor2();
	  
	   if (ia.atTargetDoor(this.targetDoor) == true) {
		   System.out.println("At the target door.");
		   ia.moveStep();
			Door vdoor = new Door();
			for (int t = 0; t < targetDoor.getDoorCoordinates().size(); t++) {
				vdoor.addCoordinates(new Coordinates(targetDoor.getDoorCoordinates().get(t).getX(), targetDoor.getDoorCoordinates().get(t).getY()));
			}
			
			boolean inSet = false;
			
			for (int i = 0; i < ia.getVisitedDoors().size(); i++) {
				if (compareDoors(ia.getVisitedDoors().get(i), vdoor) == true) {
					inSet = true;
					break;
				}
			}
			if (inSet == false) ia.addVisitedDoor(vdoor);
			
			
			// set current targetDoor as current previousDoor
			previousDoor.clearDoorCoordinates();
			for (int i = 0; i < targetDoor.getDoorCoordinates().size(); i++) {
				previousDoor.addCoordinates(new Coordinates(targetDoor.getDoorCoordinates().get(i).getX(), targetDoor.getDoorCoordinates().get(i).getY()));
			}
					
		 
		   targetDoor.clearDoorCoordinates();
		   ArrayList<Door> ds = new ArrayList<Door>();
		   
		   DetectedRooms dr = whichRoom();
		   if (dr != null) {
			   ds = dr.getDoors();
		   
		   
			boolean isNonVisitedDoors = false;
			for (int i = 0; i < ds.size(); i++) {
				if (ds.get(i).getVisited() == false) isNonVisitedDoors = true;
			}
			
			if (isNonVisitedDoors == false) {
				System.out.println("Didn't find not-visited doors!");
				ia.setPermission(true);
				System.out.println("Permission to enter visited doors set to true.");
			}
		   
		   
		   
		   	for (int i = 0; i < dr.getDoors().size(); i++) {
			   	if (dr.getDoors().get(i).getVisited() == false) {
				   	if (ia.getPermission() == true) {
			   			ia.setPermission(false); 
				   		System.out.println("Permission to enter visited doors set to false.");
				   		break;
				   	}
				   	
			   	}
		   	}
		   
		   }
		   if (dr != null && ia.getPermission() == false) {
			   	

				
				int index = -1;
				int counter = 0;
				
				
					while (index == -1) {
						int r = (int)(Math.random()*100) % ds.size();
						if (isForbiddenDoor(ds.get(r)) == false && ds.get(r).getVisited() == false && compareDoors(ds.get(r), previousDoor) == false) {
							index = r;
							break;
						} else {
							counter++;
							if (counter == ds.size()) {
								ArrayList<Door> roomsNonVisitedDoors = new ArrayList<Door>();
								
								
								if (roomsNonVisitedDoors.size() > 0) {
									index = (int)(Math.random()*100) % roomsNonVisitedDoors.size();
									break;
								} else {
									
									System.out.println("PANIC! Permission to enter visited doors should be true!");
									
								}
							
								
								
							}
						}
					}
				
				
				targetDoor.clearDoorCoordinates();
				
				for (int t = 0; t < ds.get(index).getDoorCoordinates().size(); t++) {
					targetDoor.addCoordinates(new Coordinates(ds.get(index).getDoorCoordinates().get(t).getX(), ds.get(index).getDoorCoordinates().get(t).getY()));
				}
				

				ia.setTargetX(targetDoor.getDoorCoordinates().get(0).getX());
				ia.setTargetY(targetDoor.getDoorCoordinates().get(0).getY());
				System.out.println("Target coordinates set.");
				System.out.println("Target x: " + ia.getTargetX());
				System.out.println("Target y: " + ia.getTargetY());
				ia.setOperation(FINDDOOR);
				return;
		   }

		   
		   if (dr != null && ia.getPermission() == true) {
			   	

				
				int index = -1;
				int counter = 0;
				
				if (ds.size() > 0) {
					while (index == -1) {
						int r = (int)(Math.random()*100) % ds.size();
						if (isForbiddenDoor(ds.get(r)) == false && compareDoors(ds.get(r), previousDoor) == false) {
							index = r;
							break;
						} else {
							counter++;
							if (counter == ds.size()) {
								ArrayList<Door> roomsVisitedDoors = new ArrayList<Door>();
								ArrayList<Door> roomsNonVisitedDoors = new ArrayList<Door>();
								
								for (int i = 0; i < ds.size(); i++) {
									if (ds.get(i).getVisited() == true && compareDoors(ds.get(i), previousDoor) == false && isForbiddenDoor(ds.get(i)) == false) roomsVisitedDoors.add(ds.get(i));
									if (ds.get(i).getVisited() == false && compareDoors(ds.get(i), previousDoor) == false && isForbiddenDoor(ds.get(i)) == false) roomsNonVisitedDoors.add(ds.get(i));
								}
								if (roomsNonVisitedDoors.size() > 0) {
									index = (int)(Math.random()*100) % roomsNonVisitedDoors.size();
									break;
								} else {
									while (index == -1)
										if (roomsVisitedDoors.size() > 0) {
											index = (int)(Math.random()*100) % roomsVisitedDoors.size();
											if (isForbiddenDoor(ds.get(index))) index = -1;
										} else {
											// must choose previous door, because now the other alternative must be a forbidden door
											for (int i = 0; i < ds.size(); i++) {
												if (compareDoors(ds.get(i), previousDoor) == true) {
													System.out.println("PANIC! Had to choose previously visited door!");
													index = i;
												}
											}
										}
							
								}
								
							}
						}
					}
				}
				
				targetDoor.clearDoorCoordinates();
				
				for (int t = 0; t < ds.get(index).getDoorCoordinates().size(); t++) {
					targetDoor.addCoordinates(new Coordinates(ds.get(index).getDoorCoordinates().get(t).getX(), ds.get(index).getDoorCoordinates().get(t).getY()));
				}
				
				
				ia.setTargetX(targetDoor.getDoorCoordinates().get(0).getX());
				ia.setTargetY(targetDoor.getDoorCoordinates().get(0).getY());
				System.out.println("Target coordinates set.");
				System.out.println("Target x: " + ia.getTargetX());
				System.out.println("Target y: " + ia.getTargetY());
				ia.setOperation(FINDDOOR);
				return;
		   }
		   
				

		   
		   
		   if (dr == null) {
			   if (ia.getPermission() == true) {
			   		ia.setPermission(false);
			   		System.out.println("Permission to enter visited doors set to false.");
			   }
			   	System.out.println("Possible new room detection!");
			   	this.possibleNewRoomDetections.add(new Coordinates(ia.getX(), ia.getY()));
			   
			   if (ia.getDirection() == UP || ia.getDirection() == DOWN) {
				   
				   if (room[ia.getX() / 32 - 1][ia.getY() / 32] == 0) {
					   ia.setDirection(LEFT);
					   ia.setOperation(FINDWALL);
					   return;
				   }
				   
				   if (room[ia.getX() / 32 + 1][ia.getY() / 32] == 0) {
					   ia.setDirection(RIGHT);
					   ia.setOperation(FINDWALL);
					   return;
				   }
			   }
			   
			   if (ia.getDirection() == LEFT || ia.getDirection() == RIGHT) {
				   
				   if (room[ia.getX() / 32][ia.getY() / 32 - 1] == 0) {
					   ia.setDirection(UP);
					   ia.setOperation(FINDWALL);
					   return;
				   }
				   
				   if (room[ia.getX() / 32][ia.getY() / 32 + 1] == 0) {
					   ia.setDirection(DOWN);
					   ia.setOperation(FINDWALL);
					   return;
				   }
			   }
		   }

		   System.out.println("Found doors: "+ia.getFoundDoors().size());
		   System.out.println("Visited doors: "+ia.getVisitedDoors().size());
		   System.out.println("Possible new room detections: "+this.possibleNewRoomDetections.size());
		  
		   System.out.println("Found rooms: "+ia.getRoomsFound());
		   

	   
		   ia.setOperation(FINDDOOR);

		   
		   return;
	   } 
	   
	   if (this.possibleNewRoomDetections.size() == 0 && ia.getVisitedDoors().size() == ia.getFoundDoors().size()) {
		   this.allRoomsCleaned = true;
		   System.out.println("All rooms cleaned!");
		   ia.setOperation(TASKDONE);
		   return;
	   }
	   
	   if (everySecond == 1) {
		   everySecond = -everySecond;
		   
		   if (ia.getTargetX() > ia.getX()) {
			   if (wallAt(ia.getX() / 32 + 1, ia.getY() / 32) == false ) {
				   ia.setPrevDir(ia.getDirection());
				   ia.setDirection(RIGHT);
				   ia.moveStep();			   
				   return;
			   }
		   
		   } 
		   if (ia.getTargetX() < ia.getX()) {
			   if (wallAt(ia.getX() / 32 - 1, ia.getY() / 32) == false ) {
				   ia.setPrevDir(ia.getDirection());
			   		ia.setDirection(LEFT);
			   		ia.moveStep();
			   		return;
			   }
		   }
	   }
	   
	   if (everySecond == -1) {
		   everySecond = -everySecond;
		   
		   if (ia.getTargetY() > ia.getY()) {
		   
		   
			   if (wallAt(ia.getX() / 32, ia.getY() / 32 + 1) == false ) {
				   ia.setPrevDir(ia.getDirection());
				   ia.setDirection(DOWN);
				   ia.moveStep();
			   
				   return;
			   }
		   
		   }
	   
		   if (ia.getTargetY() < ia.getY()) {
		   		   
			   if (wallAt(ia.getX() / 32, ia.getY() / 32 - 1) == false ) {
				   ia.setPrevDir(ia.getDirection());
				   ia.setDirection(UP);
				   ia.moveStep();
			  
				   return;
			   }
		   
		   }
	   }
	   
  }

	private boolean wallAt(int x, int y) {

		if (room[x][y] == 1)
			return true;

		return false;
	}

	private boolean atDoor2() {

		for (int i = 0; i < ia.getFoundDoors().size(); i++) {

			Door door = ia.getFoundDoors().get(i);

			for (int ii = 0; ii < door.getDoorCoordinates().size(); ii++) {

				if (door.getCoordsFromDoorSpace(ii).getX() == ia.getX()
						&& door.getCoordsFromDoorSpace(ii).getY() == ia.getY() && door.getVisited() == false) {
					ia.getFoundDoors().get(i).setVisited();

					boolean inSet = false;

					for (int j = 0; j < ia.getVisitedDoors().size(); j++) {
						if (compareDoors(ia.getVisitedDoors().get(j), door) == true) {
							inSet = true;
							break;
						}
					}
					if (inSet == false)
						ia.addVisitedDoor(door);
						door.setVisited();

					System.out.println("NEW visited door added!");
					return true;
				}

			}
		}
		return false;
	}

	private boolean findWall() {

		switch (ia.getDirection()) {
		case UP:

			if (room[ia.getX() / 32][ia.getY() / 32 - 1] > 0 && ia.getDirection() == UP) {

				
				this.stopHits.add(new StopHit(new Coordinates(ia.getX(), ia.getY()), UP));
				this.lastStopHits++;
				return true;

			} else if (this.doorAtLocation(ia.getX(), ia.getY() - 32) == true && ia.getDirection() == UP) {
				
				this.stopHits.add(new StopHit(new Coordinates(ia.getX(), ia.getY()), UP));
				
				this.lastStopHits++;
				return true;
			}

			

		case LEFT:

			if (room[ia.getX() / 32 - 1][ia.getY() / 32] > 0 && ia.getDirection() == LEFT) {

			
				this.stopHits.add(new StopHit(new Coordinates(ia.getX(), ia.getY()), LEFT));
				this.lastStopHits++;
				return true;

			} else if (this.doorAtLocation(ia.getX() - 32, ia.getY()) == true && ia.getDirection() == LEFT) {
				
				this.stopHits.add(new StopHit(new Coordinates(ia.getX(), ia.getY()), LEFT));
				
				this.lastStopHits++;
				return true;
			}

			

		case RIGHT:

			if (room[ia.getX() / 32 + 1][ia.getY() / 32] > 0 && ia.getDirection() == RIGHT) {

				this.stopHits.add(new StopHit(new Coordinates(ia.getX(), ia.getY()), RIGHT));
				this.lastStopHits++;
				return true;

			} else {
				if (this.doorAtLocation(ia.getX() + 32, ia.getY()) == true && ia.getDirection() == RIGHT) {
					
					this.stopHits.add(new StopHit(new Coordinates(ia.getX(), ia.getY()), RIGHT));
					
					this.lastStopHits++;
					return true;
				}

			}

		case DOWN:

			if (room[ia.getX() / 32][ia.getY() / 32 + 1] > 0 && ia.getDirection() == DOWN) {

			
				this.stopHits.add(new StopHit(new Coordinates(ia.getX(), ia.getY()), DOWN));
				this.lastStopHits++;
				return true;

			} else {
				if (this.doorAtLocation(ia.getX(), ia.getY() + 32) == true && ia.getDirection() == DOWN) {
					
					this.stopHits.add(new StopHit(new Coordinates(ia.getX(), ia.getY()), DOWN));
				
					this.lastStopHits++;
					return true;
				}

			}


		}

		ia.moveStep();

		return false;
	}

	private boolean probeForDoor(int x, int y) {
		
		switch (ia.getDirection()) {
		case DOWN:

			// if there's wall on the right and not wall in the left, there's a door in the
			// left
			if (room[ia.getX() / 32 - 1][ia.getY() / 32] == 0 && ia.getX() == this.tempRoomLeft
					&& ia.getDirection() == DOWN) {
				

					if (alreadySaved(ia.getX() - 32, ia.getY()) == false) {
						ia.addDoorLocation(new Coordinates(ia.getX() - 32, ia.getY()));
						this.tempDoorCoords.add(new Coordinates(ia.getX() - 32, ia.getY()));

						return true;
					}
				}
			if (room[ia.getX() / 32 + 1][ia.getY() / 32] == 0 && ia.getX() == this.tempRoomRight) {

					if (alreadySaved(ia.getX() + 32, ia.getY()) == false) {
						this.tempDoorCoords.add(new Coordinates(ia.getX() + 32, ia.getY()));
						ia.addDoorLocation(new Coordinates(ia.getX() + 32, ia.getY()));

						return true;
					}
				}

			
			break;

		case UP:

			// if there's wall on the right and not wall in the left, there's a door in the
			// left
			if (room[ia.getX() / 32 - 1][ia.getY() / 32] == 0 && ia.getX() == this.tempRoomLeft 
					&& ia.getDirection() == UP) {

					if (alreadySaved(ia.getX() - 32, ia.getY()) == false) {
						ia.addDoorLocation(new Coordinates(ia.getX() - 32, ia.getY()));
						this.tempDoorCoords.add(new Coordinates(ia.getX() - 32, ia.getY()));



						return true;
					}
				} else if (room[ia.getX() / 32 + 1][ia.getY() / 32] == 0 && ia.getX() == this.tempRoomRight ) {

					if (alreadySaved(ia.getX() + 32, ia.getY()) == false) {
						ia.addDoorLocation(new Coordinates(ia.getX() + 32, ia.getY()));
						this.tempDoorCoords.add(new Coordinates(ia.getX() + 32, ia.getY()));

						return true;
					}

					return true;

				}

			
			break;

		case LEFT:

			if (room[ia.getX() / 32][ia.getY() / 32 - 1] == 0 && ia.getY() == this.tempRoomUp
					&& ia.getDirection() == LEFT) {

					if (alreadySaved(ia.getX(), ia.getY() - 32) == false) {
						ia.addDoorLocation(new Coordinates(ia.getX(), ia.getY() - 32));
						this.tempDoorCoords.add(new Coordinates(ia.getX(), ia.getY() - 32));

						return true;
					}
				} else if (room[ia.getX() / 32][ia.getY() / 32 + 1] == 0 && ia.getY() == this.tempRoomDown) {

					if (alreadySaved(ia.getX(), ia.getY() + 32) == false) {
						ia.addDoorLocation(new Coordinates(ia.getX(), ia.getY() + 32));
						this.tempDoorCoords.add(new Coordinates(ia.getX(), ia.getY() + 32));

						return true;
					}
				}
			
			break;

		case RIGHT:

			if (room[ia.getX() / 32][ia.getY() / 32 - 1] == 0 && ia.getY() == this.tempRoomUp
					&& ia.getDirection() == LEFT) {

					if (alreadySaved(ia.getX(), ia.getY() - 32) == false) {
						ia.addDoorLocation(new Coordinates(ia.getX(), ia.getY() - 32));
						this.tempDoorCoords.add(new Coordinates(ia.getX(), ia.getY() - 32));

						return true;
					}
				} else if (room[ia.getX() / 32][ia.getY() / 32 + 1] == 0 && ia.getY() == this.tempRoomDown) {

					if (alreadySaved(ia.getX(), ia.getY() + 32) == false) {
						ia.addDoorLocation(new Coordinates(ia.getX(), ia.getY() + 32));
						this.tempDoorCoords.add(new Coordinates(ia.getX(), ia.getY() + 32));

						return true;
					}
				}
			
			break;

		}
		return false;

	}

	private boolean alreadySaved(int x, int y) {

		for (int i = 0; i < ia.getDoorLocations().size(); i++) {
			if (ia.getDoorCoordsLocations().get(i).getX() == x && ia.getDoorCoordsLocations().get(i).getY() == y)
				return true;
		}

		return false;
	}

	private boolean doorAtLocation(int x, int y) {

		ArrayList<Coordinates> doorLocations = ia.getDoorCoordsLocations();

		for (int i = 0; i < doorLocations.size(); i++) {
			if (doorLocations.get(i).getX() == x && doorLocations.get(i).getY() == y) {
				// System.out.println("Door!");
				return true;
			}
		}
		return false;
	}

}

import acm.graphics.*;
import acm.program.*;
import acm.util.*;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.util.*;
import java.applet.*;
import java.text.*;


public class FinalProject extends GraphicsProgram {

	public static final int TILENUM = 7;
	public static final int APPLICATION_HEIGHT = 800;
	public static final int APPLICATION_WIDTH = 400;
	public static final int TILE_WIDTH = APPLICATION_WIDTH/TILENUM;
	public static final int TILE_HEIGHT = 200;
	
	public Map<String, Double> notes = new HashMap<String, Double>();
	public Map<Integer, String> keys = new HashMap<Integer, String>();
	public Map<String, Integer> tiles = new HashMap<String, Integer>();
	public ArrayList<String> tilelabels = new ArrayList<String>();
	public ArrayList<String> songlist = new ArrayList<String>();
	public ArrayList<String> songlistname = new ArrayList<String>();
	
	private RandomGenerator rgen = new RandomGenerator();
	
	private ArrayList<GObject> blocks = new ArrayList<GObject>();
	ArrayList<String> songNotes;
	
	private boolean isGameLive;

	private double speed;
	
	private String selectedsong_name;
	private String selectedsong_path;
	private int selectedsong;
	
	private Color colorBG;
	private Color colorFont;
	
	private boolean isRandom;
	private int nextIndex;
	
	private double score;
	
	private GRect start_bg = new GRect(340, 80);
	private GLabel start_lb = new GLabel("TITLE");
	private GLabel start_lb_sub = new GLabel("SUBHEADING");
	private GLabel score_lb = new GLabel("SCORE: 0");
	
	public void run() {
		// Prepares initial variable data.
		prepareVariables(true);
		
		// Prepares Map Tables for Song and Key Info.
		prepareTables();
		
		// Selects a Random Song.
		chooseSong(rgen.nextInt(0, songlist.size()-1));
		
		// Generates layout.
		generateLabels();
		generateLines();
		
		// Listeners
		addMouseListeners();
		addKeyListeners();
		
		// Game Starts
		playGame();
	}
	
	private void nextSong()
	{
		selectedsong++;
		if(selectedsong >= songlist.size())
		{
			selectedsong = 0;
		}
		
		chooseSong(selectedsong);
	}
	
	private void chooseSong(int songid)
	{
		selectedsong = songid; //rgen.nextInt(0,songlist.size()-1);
		selectedsong_name = songlistname.get(selectedsong);
		selectedsong_path = songlist.get(selectedsong);
		
		// Get note table of the selected song.
		songNotes = Audio.getNotes(selectedsong_path);
	}
	
	private void prepareVariables(boolean all)
	{
		speed = 2;
		isGameLive = false;
		nextIndex = 0;
		isRandom = false;
		score = 0;
		
		if(all)
		{
			colorBG = rgen.nextColor();
			colorFont = colorBG.brighter();
			colorFont = colorFont.brighter();
			colorFont = colorFont.brighter();
		}
	}
	
	private void createScoring()
	{
		remove(score_lb);
		
		score_lb.setLocation(10,20);
		score_lb.setLabel("SCORE: "+(int)score);
		score_lb.setColor(colorBG);
		
		add(score_lb);
	}
	
	private void prepareTables()
	{
		tiles.put("C", 0);
		tiles.put("D", 1);
		tiles.put("E", 2);
		tiles.put("F", 3);
		tiles.put("G", 4);
		tiles.put("A", 5);
		tiles.put("B", 6);
		
		tiles.put("c", 0);
		tiles.put("d", 1);
		tiles.put("e", 2);
		tiles.put("f", 3);
		tiles.put("g", 4);
		tiles.put("a", 5);
		tiles.put("b", 6);
		
		keys.put(83, "C"); //S
		keys.put(68, "D"); //D
		keys.put(70, "E"); //F
		keys.put(71, "F"); //G
		keys.put(72, "G"); //H
		keys.put(74, "A"); //J
		keys.put(75, "B"); //K
		
		keys.put(83, "c"); //S
		keys.put(68, "d"); //D
		keys.put(70, "e"); //F
		keys.put(71, "f"); //G
		keys.put(72, "g"); //H
		keys.put(74, "a"); //J
		keys.put(75, "b"); //K
		
		tilelabels.add("S");
		tilelabels.add("D");
		tilelabels.add("F");
		tilelabels.add("G");
		tilelabels.add("H");
		tilelabels.add("J");
		tilelabels.add("K");
		
		songlist.add("furelise.txt");
		songlist.add("littlelamb.txt");
		songlist.add("odetojoy.txt");
		songlist.add("twinkletwinkle.txt");
		songlist.add("oldmc.txt");
		songlist.add("got.txt");
		
		songlistname.add("Für Elise");
		songlistname.add("Little Lamb");
		songlistname.add("Ode to Joy");
		songlistname.add("Twinkle Twinkle");
		songlistname.add("Old McDonald Had a Farm");
		songlistname.add("Game of Thrones Theme");
	}
	
	private void generateStart()
	{
		start_bg.setFilled(true);
		start_bg.setColor(colorBG);
		start_bg.setLocation(getWidth()/2 - 170, getHeight()/2 - 30 - 60);
		
		start_lb.setColor(colorFont);
		start_lb.setLabel(""+selectedsong_name);
		start_lb.setFont("SansSerif-22");
		start_lb.setLocation(getWidth()/2 - start_lb.getWidth()/2, getHeight()/2 - start_lb.getHeight() / 2 - 40);
		
		start_lb_sub.setColor(colorFont);
		start_lb_sub.setLabel("Click anywhere on this screen to play or space to switch.");
		start_lb_sub.setLocation(getWidth()/2 - start_lb_sub.getWidth()/2, getHeight()/2 - start_lb_sub.getHeight() / 2 - 20);
		
		start_bg.setVisible(true);
		add(start_bg);
		add(start_lb);
		add(start_lb_sub);
	}
	
	private void clearGameEntities()
	{
		for(int i = blocks.size() - 1; i >= 0; i--)
		{
			GObject rect = blocks.get(i);
			blocks.remove(i);
			remove(rect);
		}
	}
	
	private void removeStart()
	{
		start_bg.setVisible(false);
		remove(start_bg);
		remove(start_lb);
		remove(start_lb_sub);
	}
	
	private void endScreen() {
		GRect rect = new GRect(0,0, getWidth(), getHeight());
		rect.setFilled(true);
		rect.setColor(colorBG);
		
		add(rect);
		
		GLabel label = new GLabel("GAME OVER");
		label.setColor(colorFont);
		
		label.setFont("SansSerif-22");
		label.setLocation(getWidth()/2 - label.getWidth()/2, getHeight()/2 - label.getHeight() / 2 - 40);
		
		add(label);
		
		GLabel label2 = new GLabel("Click anywhere on this screen to play again.");
		label2.setColor(colorFont);
		label2.setLocation(getWidth()/2 - label2.getWidth()/2, getHeight()/2 - label2.getHeight() / 2 - 20);
		
		add(label2);
		
		GLabel label3 = new GLabel("YOUR SCORE: "+(int)score);
		label3.setColor(colorFont);
		
		label3.setFont("SansSerif-18");
		label3.setLocation(getWidth()/2 - label3.getWidth()/2, getHeight() - 20);
		
		add(label3);
		
		waitForClick();
		
		remove(rect);
		remove(label);
		remove(label2);
		remove(label3);
		
		clearGameEntities();
		prepareVariables(false);
		chooseSong(rgen.nextInt(0, songlist.size()-1));
		playGame();
	}
	
	private void moveBlocks() {
		for(int i = blocks.size() -1; i >= 0;  i--)
		{
			GObject obj = blocks.get(i);
			
			obj.move(0,speed);
			
			if(obj.getY() >= getHeight())
			{
				blocks.remove(i);
				remove(obj);
			}
			
			if(obj.getY() + TILE_HEIGHT >= getHeight() && obj.getX() >= 0)
			{
				isGameLive = false;
			}
		}
	}

	private void generateLines() {
		for(int i = 0; i < TILENUM; i++)
		{
			int x = i * TILE_WIDTH;
			
			GLine line = new GLine(x, 0, x, getHeight());
			
			add(line);
		}
	}
	
	private void playGame() {
		generateStart();
		waitForClick();
		isGameLive = true;
		removeStart();
		
		while(isGameLive) {
			if(readyForSpawn())
			{
				generateBlock();
			}
			
			speed = speed + 0.0005;
			moveBlocks();
			createScoring();
			pause(10);
		}
		
		endScreen();
	}
	
	private void generateLabels() {
		GRect rect = new GRect(0, getHeight() - 100, getWidth(), 5);
		rect.setFilled(true);
		rect.setColor(colorBG);
		
		add(rect);
		
		for(int i = 0; i < TILENUM; i++)
		{
			GLabel label = new GLabel("");
			label.setFont("SansSerif-16");
			label.setLabel(tilelabels.get(i));
			
			int x = i * TILE_WIDTH + TILE_WIDTH/2 - (int)label.getWidth()/2;
			int y = getHeight() - 110;
			
			label.setLocation(x,y);
			label.setColor(colorBG);
			add(label);
		}
	}
	
	private boolean readyForSpawn()
	{
		for(int i = 0; i < TILENUM; i++)
		{
			int x = i * TILE_WIDTH + TILE_WIDTH/2;
			int y = 0;
			if(getElementAt(x,y) != null || getElementAt(x,y+2) != null)
			{
				return false;
			}
			
			if(getElementAt(-1000 * TILE_WIDTH,y) != null || getElementAt(-1000 * TILE_WIDTH,y+2) != null)
			{
				return false;
			}
		}
		
		return true;
	}
	
	private void generateBlock() {
		
		int column = -500;
		
		if(isRandom)
		{
			column = rgen.nextInt(0,TILENUM-1);
		}
		else
		{
			if(nextIndex >= songNotes.size())
			{
				nextIndex = 0;
			}
			
			String keyval = songNotes.get(nextIndex);
			nextIndex++;
			
			if(keyval.equals("p") || keyval.equals("P"))
			{
				column = - 1000;
			}
			else
			{
				column = tiles.get(keyval);
			}
		}
		int x = column * TILE_WIDTH;
		
		GRect obj = new GRect(x, -TILE_HEIGHT, TILE_WIDTH, TILE_HEIGHT);
		obj.setFilled(true);
		obj.setColor(Color.BLACK);
		
		blocks.add(obj);
		add(obj);
	}
	
	public void keyPressed(KeyEvent e) {
		
		if(isGameLive)
		{
			int keyVal = e.getKeyCode();
			int x = 0;
			
			String keyName;
			int keyID;
			
			if(keys.containsKey(keyVal))
			{
				keyName = keys.get(keyVal);
				keyID = tiles.get(keyName);
				
				System.out.println(keyName);
				x = keyID * (APPLICATION_WIDTH / TILENUM) + (TILE_WIDTH / 2);
				
				new Thread(new Runnable() {
				  public void run() {
					  AudioClip clip = MediaTools.loadAudioClip("rec/"+keyName.toLowerCase()+".au");
					  clip.play();
				  }
				}).start();
			}
			
			GObject obj = getElementAt(x, getHeight() - 101);
			
			if(obj != null)
			{			
				for(int i = 0; i < blocks.size(); i++)
				{
					GObject objget = blocks.get(i);
					
					if(objget == obj)
					{
						score = score + 101 - ((obj.getY() + obj.getHeight()) - (getHeight() - 101 ));
						remove(obj);
						blocks.remove(i);
					}
				}
			}
			else
			{
				isGameLive = false;
			}
		}
		else if(start_bg.isVisible())
		{
			if(e.getKeyCode() == 32)
			{
				nextSong();
				removeStart();
				generateStart();
			}
		}
	}
}

import java.io.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import javax.imageio.*;

public class Room extends Canvas implements KeyListener, Runnable{
	////////////////////////////
	/////// DIRECTORIES ////////
	////////////////////////////
	/*						*	
	*	Rooms should be located in "rooms"	*
	*	Neural-Nets should be in "neuralnets"	/*	
	
	
	///////////////////////////////////////////////////
	////////** Logic and Graphics Components **////////
	///////////////////////////////////////////////////
	private int[] spawn_point = {0,0};
	private int fps = 100;
	public final static int RIGHT = 0;
	public final static int LEFT = 1;
	public final static int UP = 2;
	public final static int DOWN = 3;
	public final static int SPACE = 4;
	public final static int SHIFT = 5;
	
	/////////////////////
	// WALL COMPONENTS //
	/////////////////////
	private ArrayList<Wall> walls;
	private TreeMap<Integer, ArrayList<Wall>> checkpoint_map;

	/////////////////////////
	///// AI COMPONENTS /////
	/////////////////////////
	private AI_container AIs;
	private int NUMBER_OF_AI = 100;
	private double VARIANCE = 10; /* Maximum possible change in a neuron's weight */
	private double PROBABILITY = 0.4; //40% CHANCE
	private long AI_MAX_LIFESPAN_IN_UPDATES = 1000; //1000 works well its like 15 seconds
	private double MUTATED_POPULATION_PERCENT = 0.9; //percent of new AI that are based off of the most fit
	
	////////////////////////
	// Graphic Components //
	////////////////////////
	private BufferedImage back;
	
	public Room(){
		setFont(new Font("Verdana", Font.ITALIC, 11));
		checkpoint_map = new TreeMap<Integer, ArrayList<Wall>>();
		
		loadMap(new File("../rooms/room3.txt"));
		AIs = new AI_container(NUMBER_OF_AI, loadNeuralNetwork(new File("../neuralnets")));
		
		addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent e) {
		    	spawn_point[0] = e.getX();
				spawn_point[1] = e.getY();
//				System.out.println("<SPAWN POINT SET>: ("+spawn_point[0]+","+spawn_point[1]+")");//these co-ords are relative to the component
			}
		});
		addKeyListener(this);
		new Thread(this).start();
		setVisible(true);
	}
	
	////////////////////////////////////////////////
	/////// LOADING/SAVING UTILITY METHODS /////////
	////////////////////////////////////////////////
	/* LOADS A .SER AS THE MOST FIT AI'S NEURAL NETWORK */
	private NeuralNetwork loadNeuralNetwork(File f){
		try{
			FileInputStream file;
			if(f.isDirectory()){
				File[] net_files = f.listFiles();
				int id = JOptionPane.showOptionDialog(null, "< Exit to start with a fresh Neural-Network >\n\n**WILL NOT WORK IF SERIAL VERSION OF SAVED .SER DOES NOT MATCH**","Neural-Network Loader",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, net_files, net_files[0]);
				file = new FileInputStream(net_files[id]);
			}else{
				file = new FileInputStream(f);
			}
			InputStream buffer = new BufferedInputStream(file);
			ObjectInput input = new ObjectInputStream (buffer);
			NeuralNetwork loaded_net = (NeuralNetwork)input.readObject();
			return loaded_net;
		}
		catch(ClassNotFoundException e){
			System.out.println("Cannot perform input. Class not found: "+ e.getMessage());
		}
		catch(Exception e){
		    System.out.println(e.getMessage());
		}
		return null;
	}
	/* SAVES THE MOST FIT AI NEURAL NETWORK TO A .SER */
	private void saveNeuralNetwork(){
		try (
		  OutputStream file = new FileOutputStream("../neuralnets/"+JOptionPane.showInputDialog(null,"NEURAL-NET FILENAME:")+".ser");
		  OutputStream buffer = new BufferedOutputStream(file);
		  ObjectOutput output = new ObjectOutputStream(buffer);
		){
		  output.writeObject(AIs.getMostFitAI().getNeuralNetwork());
		}  
		catch(IOException e){
		  System.out.println(e.getMessage());
		}
	}
	/* LOADS A NEW TEXT FILE INTO THE ARRAY OF WALLS */
	private void loadMap(File map){
		try{
			Scanner map_Scanner = new Scanner(map);
			for(int i = 0; i<2; i++){
				spawn_point[i] = map_Scanner.nextInt();
			}map_Scanner.nextLine();
			String file = "";
			int rows = 0;
			while(map_Scanner.hasNext()){ 
				file+=map_Scanner.nextLine();
				rows++;
			}
			int cols = file.length()/rows;
			walls = new ArrayList<Wall>();
			for(int i = 0; i < rows; i++){
				for(int j = 0; j < cols; j++){
					char val = file.charAt(i*cols+j);
					if (val != 'y' && val != ' '){
						Wall w = new Wall(j*Window.WIDTH/cols,i*Window.HEIGHT/rows, Window.WIDTH/cols,Window.HEIGHT/rows);
						if (Character.toString(val).matches("[0-9]")){
							int cp_num = Character.getNumericValue(val);
							w.setCheckpoint(cp_num);
							if(!checkpoint_map.containsKey(cp_num)){
								checkpoint_map.put(cp_num, new ArrayList<Wall>());
							}
							checkpoint_map.get(cp_num).add(w);
						}
						walls.add(w);
					}
				}
			}
		}catch(Exception e){
			System.out.println ("Room Reading Error: " +checkpoint_map.toString()+" "+e.getMessage());
			System.exit(0);
		}
	}
	
	@Override
	public void update( Graphics window ){
		///////////////////////
		/////// UPDATES ///////
		///////////////////////
		AIs.update();		
		///////////////////////
		////// COLLISONS //////
		///////////////////////
		for(Wall w: walls){
			AIs.processCollisions(w);					
		}
		////////////////////////
		/// MOVES TO DRAWING ///
		////////////////////////
		paint(window);
	}	
	
	public void paint( Graphics window ){
		/////////////////////
		/// Double Buffer ///
		/////////////////////
		Graphics2D twoDGraph = (Graphics2D)window;
		if(back==null)
		   back = (BufferedImage)(createImage(getWidth(),getHeight()));
		Graphics gtemp = back.createGraphics();
		
		gtemp.setColor(new Color(100,100,100));
		gtemp.fillRect(0,0,Window.WIDTH,Window.HEIGHT);
		
		/*	Marks to point where the AI Bodies spawn	*/
		gtemp.setColor(Color.WHITE);
		gtemp.fillOval(spawn_point[0]-5, spawn_point[1]-5, 10, 10);
		
		//////////////////////////
		/// DRAWS COMPONENTS ///
		//////////////////////////
		AIs.draw(gtemp);
		for(Wall w: walls){
			w.draw(gtemp);
		}
		/* DRAWS GENERATION NUMBER TO THE SCREEN */
		gtemp.setColor(Color.WHITE);
		gtemp.drawString("Generation: "+AIs.getGen() + "  Generations Featuring Improvement: "+ AIs.generations_showing_improvement,10,20);
		twoDGraph.drawImage(back, null, 0, 0); // draws the drawn image to the screen all at once
	}
	
	/**
	 * Updates the keys array based on the key that was pressed
	 * Uses the arrow keys left, right, up and down
	 * @param e the KeyEvent representing the pressed key
	 */
	public void keyPressed(KeyEvent e){
		if(e.getKeyCode() == KeyEvent.VK_W || e.getKeyCode() == KeyEvent.VK_UP){
			AIs.killAll();
		}
		if(e.getKeyCode() == KeyEvent.VK_S || e.getKeyCode() == KeyEvent.VK_DOWN){
			AIs.restartTest();
		}
		if(e.getKeyCode() == KeyEvent.VK_SPACE){
			AIs.observeMostFit();
		}
		/////////////////////////////////////
		////// OPTION MENU KEY - SHIFT //////
		/////////////////////////////////////
		if(e.getKeyCode() == KeyEvent.VK_SHIFT){
			Object[] options = {"CHANGE VARIABLE", "CHANGE MAP", "Save Fittest NeuralNetwork"};
			switch(JOptionPane.showOptionDialog(null, "< DEFAULT CONTROLS >\n'W' :: KILL ALL AI AND ASSIGN MOST FIT\n'S' :: RESTART ENTIRE TEST\n'SPACE' :: WATCH THE CURRENTLY ASSIGNED MOST FIT AI\n\n< COMPONENT MODIFICATIONS >", "Click a button", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0])){
				case 0: 
					Object[] variables = {"CHANGE FPS", "CHANGE VARIANCE", "CHANGE MUTATION RATE", "CHANGE AI LIFESPAN"};
					switch(JOptionPane.showOptionDialog(null, "< VARIABLE MODIFICATIONS >", "Click a button", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, variables, variables[0])){
						case 0: fps = new Integer(JOptionPane.showInputDialog(null,"Current FPS: "+fps+"\n\nEnter New FPS: ?")); break;
						case 1: VARIANCE = new Double(JOptionPane.showInputDialog(null,"Current Variance: "+ VARIANCE+"\n\nEnter New Variance: ?")); break;
						case 2: PROBABILITY = new Double(JOptionPane.showInputDialog(null,"Current Probability: "+PROBABILITY+"\n\n Enter New Probability of Mutation: [ex. 0.2 = 20%]")); break;
						case 3: AI_MAX_LIFESPAN_IN_UPDATES = new Long(JOptionPane.showInputDialog(null,"Current Lifespan: "+AI_MAX_LIFESPAN_IN_UPDATES+" Updates\n\n Enter New Lifespan:")); break;
						default: break;
					}break;	
				case 1: 
					File[] map_files = new File("../rooms").listFiles();
					String[] map_names = new String[map_files.length];
					for(int i = 0; i < map_files.length; i++){		map_names[i] = map_files[i].getName().substring(0,map_files[i].getName().lastIndexOf("."));		}
					loadMap(map_files[JOptionPane.showOptionDialog(null, "< ROOM CHOOSER >","Click a button", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, map_names, map_names[0])]);
					AIs.respawn(); break;
				case 2:	saveNeuralNetwork(); break;
				default: break;
			}
		}
	}
	public void keyReleased(KeyEvent e){}
	public void keyTyped(KeyEvent e){}
	
	////////////////////////////////////////////////////////
	/////////////////// MATH FUNCTIONS /////////////////////
	////////////////////////////////////////////////////////
	public static double dist(double x,double y){
		return Math.sqrt(Math.pow(x,2) + Math.pow(y,2));
	}
	public static double clamp(double value, double min, double max) {
	    double x = value;
	    if (x < min)		{ x = min; } 
	    else if (x > max) 	{ x = max; }
    	return x;
	}
	/* RETURNS THE CLOSEST CHEKPOINT TO THE AI */
	private Wall getClosestCheckpointFromAI(AI ai){
		if (ai.getCurrentCheckpoint() < checkpoint_map.size() ){	/* IF CHECKPOINT EXISTS */
			ArrayList<Wall> current_cps = checkpoint_map.get(ai.getCurrentCheckpoint());
			Wall closest = current_cps.get(0);
			for (Wall cp: current_cps){
				if ( dist((ai.getBody().getX()+ai.getBody().radius) - (cp.x+cp.width/2), (ai.getBody().getY()+ai.getBody().radius) - (cp.y+cp.height/2)) 	<	dist((ai.getBody().getX()+ai.getBody().radius) - (cp.x+cp.width/2), (ai.getBody().getY()+ai.getBody().radius) - (cp.y+cp.height/2)) ){
					closest = cp;
				}
			}
			return closest;
		}
		return null;
	}
	/* SHORTCUT METHOD TO GET THE DISTANCE FROM AN AI TO THIER DESIRED CHECKPOINT */
	private double distanceToCheckpoint(AI ai){
		Wall cp = getClosestCheckpointFromAI(ai);
		return (cp!=null ? dist((ai.getBody().getX()+ai.getBody().radius) - (cp.x+cp.width/2), (ai.getBody().getY()+ai.getBody().radius) - (cp.y+cp.height/2)):Double.POSITIVE_INFINITY); 
	}
	/* SHORTCUT TO GET DIRECTION TO CHECKPOINT FROM THE AI */
	private double directionToCheckpoint(AI ai){
		Wall cp = getClosestCheckpointFromAI(ai);
		return (cp!=null ? (Math.atan2((ai.getBody().getY()+ai.getBody().radius) - (cp.y+cp.height/2),(cp.x+cp.width/2) - (ai.getBody().getX()+ai.getBody().radius))+(Math.PI*2))%((Math.PI*2)):0);
	}
	/* FINDS THE DISTANCE TO CLOSEST WALL IN A DIRECTION */
	public double distToWall(Body p, double dir_, Wall w){
		double dir = (dir_+(Math.PI*2))%(Math.PI*2);
		double min_angle = Double.POSITIVE_INFINITY;
		double max_angle = Double.NEGATIVE_INFINITY;
		/* determines if the *raycast* will collide with the wall based on its direction */
		double current_angle = (Math.atan2( (p.getY()+p.radius) - (w.y), (w.x) - (p.getX()+p.radius) )+(Math.PI*2))%(Math.PI*2);
			min_angle = Math.min(min_angle, current_angle);
			max_angle = Math.max(max_angle, current_angle);
		current_angle = (Math.atan2( (p.getY()+p.radius) - (w.y), (w.x+w.width) - (p.getX()+p.radius) )+(Math.PI*2))%(Math.PI*2);
			min_angle = Math.min(min_angle, current_angle);
			max_angle = Math.max(max_angle, current_angle);
		current_angle = (Math.atan2( (p.getY()+p.radius) - (w.y+w.height), (w.x+w.width) - (p.getX()+p.radius) )+(Math.PI*2))%(Math.PI*2);
			min_angle = Math.min(min_angle, current_angle);
			max_angle = Math.max(max_angle, current_angle);
		current_angle = (Math.atan2( (p.getY()+p.radius) - (w.y+w.height), (w.x) - (p.getX()+p.radius) )+(Math.PI*2))%(Math.PI*2);
			min_angle = Math.min(min_angle, current_angle);
			max_angle = Math.max(max_angle, current_angle);
		if ((max_angle - min_angle > Math.PI ? ((dir < (Math.PI*2) && dir >= max_angle)||(dir >= 0 && dir <= min_angle)):dir <= max_angle && dir >= min_angle)){
		
//			System.out.printf ("BODY DIR: [%.2f°] is between [%.2f° and %.2f°] of wall id: %s with distance ==> ",dir,min_angle,max_angle,w.toString());
			double dx = clamp(p.getX()+p.radius,w.x,w.x+w.width) - (p.getX()+p.radius);
			double dy = -(clamp(p.getY()+p.radius,w.y,w.y+w.height) - (p.getY()+p.radius));
			double dist = 0;
			if (dx == 0){
				dist = Math.abs(dy/Math.sin(dir));
			}else if(dy == 0){
				dist = Math.abs(dx/Math.cos(dir));
			}else if ((dx > 0 && dy > 0) || (dx < 0 && dy < 0)){// 1st and 3rd QUAD 
				if(dir <= (Math.atan2(dy,dx)+(Math.PI*2))%(Math.PI*2)){
					dist = Math.abs(dy/Math.sin(dir));
				}else{
					dist = Math.abs(dx/Math.cos(dir));
				}
			}else if ((dx < 0 && dy > 0) || (dx > 0 && dy < 0)){// 2nd and 4th QUAD
				if(dir >= (Math.atan2(dy,dx)+(Math.PI*2))%(Math.PI*2)){
					dist = Math.abs(dy/Math.sin(dir));
				}else{
					dist = Math.abs(dx/Math.cos(dir));
				}
			}
			return dist;
		}
		return Double.POSITIVE_INFINITY;
		
	}
	////////////////////////////////
	///////// AI CONTAINER /////////
	////////////////////////////////
	class AI_container{
		public int generations_showing_improvement;
		private int generation; /* CURRENT GENERATION CYCLE OF AI */
		private AI[] AI_arr; /* ARRAY OF AI's */
		private AI fittest_AI; /* REFERENCE TO MOST FIT AI [always one of the AI in the array] */
		
		/* CREATES AN AI CONTAINER WITH A GIVEN NUMBER OF AI */
		public AI_container(int s){
			generation = generations_showing_improvement = 0;
			AI_arr = new AI[s];
			for(int i = 0; i < s; i++)	{ 	AI_arr[i] = new AI();	}
			fittest_AI = AI_arr[0];
		}
		/* CREATES AN AI CONTAINER WITH ONE PRE-SET NEURAL-NETWORK AS THE FITTEST, OR CREATES A NORMAL AI CONTAINER IF 'NULL' */
		public AI_container(int s, NeuralNetwork L_net){
			this(s);
			if (L_net != null) 	{	fittest_AI = AI_arr[0] = new AI(L_net);	}
		}
		/* UPDATES THE AI IN THE ARRAY AND HANDLES WHEN TO CREATE NEW GENERATIONS OF AI */
		public void update(){
			/* ASSIGN MOST FIT AI AFTER ALL AI ARE DEAD,
			 * CREATES A NEW GROUP OF AI BASED ON THE MOST FIT AI's NEURAL-NETWORK */
			if(getAliveAI().size() <= 0){
				AI prev_fit = fittest_AI;
				for(AI ai: AI_arr){
					if(	ai.getCurrentCheckpoint() == fittest_AI.getCurrentCheckpoint() &&
						distanceToCheckpoint(ai)  <  distanceToCheckpoint(fittest_AI))			{	fittest_AI = ai;	}
					else if(ai.getCurrentCheckpoint() > fittest_AI.getCurrentCheckpoint())		{	fittest_AI = ai;	}
				}
				if (prev_fit != fittest_AI){
					generations_showing_improvement++;
				}
				/* CREATES THE NEW GENERATION BASESD ON THE MOST FIT AT */
				createNewGenereation(fittest_AI);
			}
			/* UPDATES EACH AI */
			for	(AI ai: AI_arr){	if(ai.isAlive()) {	ai.step();	ai.update(); }	}
		}		
		
		////////////////////////////////////////////
		////////// TESTING CONTROL BUTTONS /////////
		////////////////////////////////////////////
		/* RESTART TEST */
		public void restartTest(){
			restart();
			fittest_AI = AI_arr[0];
		}
		/* NEW GEN [ kills all ] */
		public void killAll(){
			for(AI ai: AI_arr){	ai.setAlive(false); }
		}
		/* WATCH MOST FIT */
		public void observeMostFit(){
			for(AI ai: AI_arr){
				ai.respawn();
				ai.setAlive(false);
			}
			fittest_AI.setAlive(true);
		}
		/* SPAWNS A NEW GROUP OF AI BASED ON THE NEURAL-NETWORK OF A GIVEN AI */
		public void createNewGenereation(AI fit_AI){
			AI_arr[0] = fit_AI;
			NeuralNetwork fit_net = fit_AI.getNeuralNetwork();
			for(int i = 1; i < (int)(MUTATED_POPULATION_PERCENT*AI_arr.length); i++)				{		AI_arr[i] = new AI(fit_net.mutatedNetwork(PROBABILITY, VARIANCE));		}
			for(int i = (int)(MUTATED_POPULATION_PERCENT*AI_arr.length); i < AI_arr.length; i++)	{		AI_arr[i] = new AI();		}
			respawn();
			generation++;
		}
		/* RETURNS THE FITTEST AI */
		public AI getMostFitAI(){
			return fittest_AI;
		}
		
		/* RETURNS THE GENERATION OF AI */
		public int getGen(){
			return generation;
		}
		/* RETURNS THE INDEXES OF THE ALIVE AI */
		public ArrayList<Integer> getAliveAI(){
			ArrayList<Integer> AI_alive = new ArrayList<Integer>();
			for(int i = 0; i < AI_arr.length; i++){
				if(AI_arr[i].isAlive()){
					AI_alive.add(i);
				}
			}
			return AI_alive;
		}
		//////////////////////////////////////////////////
		///// GENERIC REPEATED METHODS FROM AI CLASS /////
		//////////////////////////////////////////////////
		/* PROCESSES THE COLLISIONS OF ALL AI IN THE ARRAY */
		public void processCollisions(Wall w){
			for(AI ai: AI_arr){
				ai.processCollision(w);
			}
		}	
		/* DRAWS THE BODY COMPONENT OF THE AI TO THE SCREEN */
		public void draw(Graphics g){
			for(AI ai: AI_arr){
				if(ai.isAlive()){
					ai.draw(g);
				}
				if(fittest_AI.isAlive()){
					fittest_AI.getBody().draw(g, new Color(128,0,128));
				}
			}
		}			
		/* RESTARTS EACH OF THE AI'S IN THE AI_ARRAY */
		public void restart(){
			generation = generations_showing_improvement = 0;
			for(AI ai: AI_arr){
				ai.restart();
			}
		}
		/* RESPAWN EACH OF THE AI'S IN THE AI_ARRAY */
		public void respawn(){
			for(AI ai: AI_arr){
				ai.respawn();
			}
		}
	}
	//////////////////////
	////// AI CLASS //////
	//////////////////////
	class AI{
		private Body AI_Body; 			/* BODY COMPONENT */
		private NeuralNetwork AI_Net; 	/* NEURAL-NET COMPONENT */
		private boolean alive;
		private long AI_lifespan_in_updates;
		private int current_checkpoint = (int)checkpoint_map.keySet().toArray()[0];
		
		//////////////////////////////////////////
		//// NEURAL-NET CONTRUCTION CONSTANTS ////
		//////////////////////////////////////////
		private final int[] NEURON_LAYER_TEMPLATE = {4};
		private final int NUM_OUTPUTS = 2;
		private final int NUM_INPUTS = 8;
		
		public AI(){
			AI_Body = new Body(spawn_point[0], spawn_point[1]);
			AI_Net = new NeuralNetwork(NUM_INPUTS, NEURON_LAYER_TEMPLATE, NUM_OUTPUTS);
			AI_lifespan_in_updates = 0;
			alive = true;
		}
		public AI(NeuralNetwork net){
			AI_Body = new Body(spawn_point[0], spawn_point[1]);
			AI_Net = net;
			AI_lifespan_in_updates = 0;
			alive = true;
		}
		
		/* UPDATES THE AI's BODY COMPONENT AND KILLS IT IF IT'S LIFESPAN EXCEEDS THE MAX */
		public void update(){
			AI_Body.update();
			AI_lifespan_in_updates++;
			if(AI_lifespan_in_updates > AI_MAX_LIFESPAN_IN_UPDATES)	{	alive = false;	}
		}
		/* RUNS INFORMATION THROUGH THE AI's NEURAL-NET AND OUTPUTS RESULTS TO ITS BODY COMPONENT */
		public void step(){
			if(alive){
				double dist[] = { Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY , Double.POSITIVE_INFINITY };
				/* FINDS THE MINIMUM DISTANCE WALL THAT IS DIRECTLY ON FRONT OF THE BODY */
				for(Wall w: walls){
					if(w.getCheckpoint() == -1){
						dist[0] = Math.min(dist[0],distToWall(AI_Body, AI_Body.getDirection(), w));	
						dist[1] = Math.min(dist[1],distToWall(AI_Body, AI_Body.getDirection() - Math.PI/4, w));		
						dist[2] = Math.min(dist[2],distToWall(AI_Body, AI_Body.getDirection() + Math.PI/4, w));
						dist[3] = Math.min(dist[3],distToWall(AI_Body, AI_Body.getDirection() - Math.PI/3, w));		
						dist[4] = Math.min(dist[4],distToWall(AI_Body, AI_Body.getDirection() + Math.PI/3, w));							
					}
				}
				/* INPUT ARRAY FOR NEURAL NETWORK */
				double[] results = AI_Net.run(new double[]{	dist[0], 	/* STRAIGHT */
															dist[1],	/* RIGHT */	
															dist[2],	/* LEFT */	
															dist[3],
															dist[4],
															distanceToCheckpoint(this),
															directionToCheckpoint(this),
															AI_Body.getDirection()});
				if (results[0] < 0.5)	{	AI_Body.move(new boolean[]{true,false});	/* ACTIVATES THE LEFT INPUT OF THE AI'S BODY COMPONENT */		}
				if (results[1] < 0.5)	{	AI_Body.move(new boolean[]{false,true});	/* ACTIVATES THE RIGHT INPUT OF THE AI'S BODY COMPONENT */	}
			}
		}
		/* CHECK FOR COLLISION WITH A WALL AND PERFORMS THE APPROPRITATE ACTIONS AFTER A COLLISION */
		public void processCollision(Wall w){
			if(alive && AI_Body.collides(w)){
				if(w.getCheckpoint() > -1){							/* IF THE WALL IS A CHECKPOINT */
					if(current_checkpoint == w.getCheckpoint()){	/* IF THE WALL IS THE CHECKPOINT THAT THE BODY IS SUPPOSE TO REACH */
						current_checkpoint++;
						if(current_checkpoint >= checkpoint_map.size()){	/* IF THE AI REACHED THE LAST CHECKPOINT */
							alive = false;
						}
					}
				}else{	/* IF THE WALL IS ONLY USED FOR COLLIDING WITH AND KILLING THE BODY */
					alive = false;
				}
			}
		}
		/* DRAWS THE BODY COMPONENT OF THE AI TO THE SCREEN */
		public void draw(Graphics g){
			AI_Body.draw(g);
		}
		
		/* CHECKPOINT MANIPULATION */
		public int getCurrentCheckpoint()		{		return current_checkpoint;		}
		public void setCurrentCheckpoint(int c)	{		current_checkpoint = c;		}
		
		/* COMPONENTS OF THE AI */
		public Body getBody()					{		return AI_Body;		}
		public NeuralNetwork getNeuralNetwork()	{		return AI_Net;		}
		
		/* STATE VARIABLES OF THE AI */
		public long getUpdateNumber()			{		return AI_lifespan_in_updates;		}
		public boolean isAlive()				{		return alive;		}
		public void setAlive(boolean b)			{		alive = b;		}
		
		/* REFRESHED THE NEURAL-NETWORK, CHECKPOINT, LIFESPAN, AND POSIITON OF THE AI */
		public void restart(){
			AI_Net = new NeuralNetwork(NUM_INPUTS, NEURON_LAYER_TEMPLATE, NUM_OUTPUTS);
			AI_Body = new Body(spawn_point[0], spawn_point[1]);
			AI_lifespan_in_updates = 0;
			current_checkpoint = 0;
			alive = true;
		}
		/* RESTARTS THE AI'S LIFE WITHOUT CHANING THE NEURAL-NETWORK */
		public void respawn(){
			AI_Body = new Body(spawn_point[0], spawn_point[1]);
			AI_lifespan_in_updates = 0;
			current_checkpoint = 0;
			alive = true;
		}
	}
	
	/////////////////////////////
	///////// Main Loop /////////
	/////////////////////////////
	public void run(){
	 	long starttime = 0;
		long frametime = 0;
		try{
			while(true){
				starttime = System.currentTimeMillis();
				repaint();
				frametime = System.currentTimeMillis() - starttime;
				if(1000/fps - frametime > 0){
					Thread.currentThread().sleep(1000/fps - frametime);
				}
			}	
		}catch(Exception e){
			System.out.println (e.getMessage());
		}
	}
}


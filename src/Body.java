import java.awt.*;

public class Body {
	private static final double speeddrag = 0.97;
	private static final double angledrag = 0.95;
	private static final double turnAcceleration = 0.01;
	private static final double force = 0.1;
	private static final double MAX_VEL = 10;
	public static final int radius = 20;
	private double x, y, acceleration, velocityX, velocityY, angularVelocity, direction;
	
    public Body() {
    	x = y = 0;
    	acceleration = velocityX = velocityY = angularVelocity = direction = 0;
    }
    public Body(int x, int y) {
    	this.x = x-radius;
    	this.y = y-radius;
    	acceleration = velocityX = velocityY = angularVelocity = 0;
    	direction = 0;
    }  
   	
	public void draw(Graphics g, Color c){
   		int _x = (int)x;
   		int _y = (int)y;
   		g.setColor(c);
   		g.drawOval(_x,_y,radius*2, radius*2);
   		g.setColor(Color.green);
   		g.drawLine(_x+radius, _y+radius, (int)(x+radius+Math.cos(direction)*radius), (int)(y+radius-Math.sin(direction)*radius));		
	}
   	public void draw(Graphics g){
   		int _x = (int)x;
   		int _y = (int)y;
   		g.setColor(Color.RED);
   		g.drawOval(_x,_y,radius*2, radius*2);
   		g.setColor(Color.green);
   		g.drawLine(_x+radius, _y+radius, (int)(x+radius+Math.cos(direction)*radius), (int)(y+radius-Math.sin(direction)*radius));
   	}
   	
   	public void update(){
   		x += velocityX;
   		y += velocityY;
   		velocityX = (velocityX + acceleration*Math.cos(direction))*speeddrag;
   		velocityY = (velocityY - acceleration*Math.sin(direction))*speeddrag;
   		direction = (direction+angularVelocity+Math.PI*2)%(Math.PI*2);
   		angularVelocity *= angledrag;
   		if(Math.abs(velocityX) > MAX_VEL){
   			velocityX *= MAX_VEL/Math.abs(velocityX);
   		}
   		if(Math.abs(velocityY) > MAX_VEL){
   			velocityY *= MAX_VEL/Math.abs(velocityY);
   		}
   	}
   	public boolean collides(Wall w) {
   		/*												(x_dist)^2		   +		(y_dist)^2			< 		radius^2								 */
	    return Room.dist(x+radius - Room.clamp(x+radius, w.x, w.x + w.width), y+radius -  Room.clamp(y+radius, w.y, w.y + w.height)) < radius;
	}   	
   	public void move(boolean[] keys){
   		if(keys[Room.RIGHT])	{ turn(-turnAcceleration); }
   		if(keys[Room.LEFT]) 	{ turn(turnAcceleration); }
   		if(keys[Room.RIGHT] && keys[Room.LEFT])	{ acceleration = 0; }
   		else 									{ acceleration = force; }
   	}
   	public void turn(double val){   	angularVelocity+=val;   	}
	
   	public double getVelocity()	{		return Math.sqrt(velocityX*velocityX + velocityY*velocityY);		}
   	public double getDirection(){		return direction;		}
   	public double getX()		{		return x;		}
   	public double getY()		{		return y;		}
}
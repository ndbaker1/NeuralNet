import java.awt.*;

public class Wall{
	public static final char GOAL_CHAR = '.';
	
	public int x,y,height,width; // center coordinates
	private int checkpoint;
	
    public Wall(int x, int y, int w, int h) {
    	this.x = x;
    	this.y = y;
    	width = w;
    	height = h;
    	checkpoint = -1;
    }
    
    public void draw(Graphics g){
    	if (checkpoint > -1){
    		g.setColor(Color.BLUE);
    	}else{
    		g.setColor(Color.black);
    	}
    	g.fillRect(x, y, width, height);
		g.setColor(Color.black); g.drawRect(x,y,width,height);
    }

    public void setCheckpoint(int num){
    	checkpoint = num;
    }
    public int getCheckpoint(){
    	return checkpoint;
    }
}
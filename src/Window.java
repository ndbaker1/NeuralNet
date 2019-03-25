import java.awt.*;
import javax.swing.*;

public class Window extends JFrame{
	public static final int WIDTH = 1200;
	public static final int HEIGHT = 800; 
	
	public Window(){
		super("AI Bodies - SHIFT KEY FOR OPTIONS");
		setSize(WIDTH, HEIGHT+50);
		setAlwaysOnTop(true);
		setAlwaysOnTop(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Room r = new Room();
		r.setFocusable(true);
		getContentPane().add(r);
		setVisible(true);
	}
	
	public static void main(String[] args){
		Window AI_TEST = new Window();
	}
}


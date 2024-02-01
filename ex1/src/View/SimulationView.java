package main.java.ass03_parte1.View;


import main.java.ass03_parte1.Controller.Controller;
import main.java.ass03_parte1.Model.Body;
import main.java.ass03_parte1.Model.Boundary;
import main.java.ass03_parte1.Model.P2d;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public class SimulationView {
        
	private final VisualiserFrame frame;

    public SimulationView(int w, int h) {
    	this.frame = new VisualiserFrame(w,h);
    }

	/*
	Repaint the components in the figure
	 */
    public void display(ArrayList<Body> bodies, double vt, long iter, Boundary bounds){
		this.frame.display(bodies, vt, iter, bounds);
    }

	public void setController(Controller controller) {
		this.frame.setController(controller);
	}

	public static class VisualiserFrame extends JFrame {

        private final VisualiserPanel panelWithBalls;
		private Controller controller;
		private final JButton startButton;
		private final JButton stopButton;

        public VisualiserFrame(int w, int h) {
            setTitle("Bodies Simulation");
            setSize(w,h);
            setResizable(false);
			this.panelWithBalls = new VisualiserPanel(w,h);
			JPanel panelWithButtons = new JPanel();
			panelWithButtons.setSize(300,300);
			JPanel panelMain = new JPanel(new BorderLayout());

			this.startButton = new JButton("start");
			this.stopButton = new JButton("stop");
			this.stopButton.setEnabled(false);

			// ActionListener for the startButton button.
			// On click the GUI controller startButton the simulation (method notifyStarted()).
			this.startButton.addActionListener(startButton -> {
					System.out.println("The execution is starting...");
					this.controller.notifyStarted();
					this.startButton.setEnabled(false);
					this.stopButton.setEnabled(true);
			});

			// ActionListener for the stopButton button.
			// On click the GUI controller stopButton the simulation (method notifyStopped()).
			stopButton.addActionListener((stopButton) -> {
					System.out.println("The execution has been interrupted ...");
					this.controller.notifyStopped();
					this.stopButton.setEnabled(false);
					this.startButton.setEnabled(true);
			});

			panelWithButtons.add(this.startButton);
			panelWithButtons.add(this.stopButton);

			panelMain.add(this.panelWithBalls, BorderLayout.CENTER);
			panelMain.add(panelWithButtons, BorderLayout.SOUTH);

			getContentPane().add(panelMain);

            addWindowListener(new WindowAdapter(){
    			public void windowClosing(WindowEvent ev){
    				System.exit(-1);
    			}
    			public void windowClosed(WindowEvent ev){
    				System.exit(-1);
    			}
    		});
    		this.setVisible(true);
        }
        
        public void display(ArrayList<Body> bodies, double vt, long iter, Boundary bounds){

			try {
	        	SwingUtilities.invokeAndWait(() -> {
					this.panelWithBalls.display(bodies, vt, iter, bounds);
	            	repaint();
	        	});
        	} catch (Exception ex) {
				ex.printStackTrace();
			}
        }


		public void setController(Controller controller) {
			this.controller = controller;
		}
	}

    public static class VisualiserPanel extends JPanel implements KeyListener {
        
    	private ArrayList<Body> bodies;
    	private Boundary bounds;
    	
    	private long nIter;
    	private double vt;
    	private double scale = 1;
    	
        private final long dx;
        private final long dy;
        
        public VisualiserPanel(int w, int h){
            setSize(w,h);
            dx = w/2 - 20;
            dy = h/2 - 20;
			this.addKeyListener(this);
			setFocusable(true);
			setFocusTraversalKeysEnabled(false);
			requestFocusInWindow(); 
        }

        public void paint(Graphics g){    		    		
    		if (bodies != null) {
        		Graphics2D g2 = (Graphics2D) g;
        		
        		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        		          RenderingHints.VALUE_ANTIALIAS_ON);
        		g2.setRenderingHint(RenderingHints.KEY_RENDERING,
        		          RenderingHints.VALUE_RENDER_QUALITY);
        		g2.clearRect(0,0,this.getWidth(),this.getHeight());

        		
        		int x0 = getXcoord(bounds.getX0());
        		int y0 = getYcoord(bounds.getY0());
        		
        		int wd = getXcoord(bounds.getX1()) - x0;
        		int ht = y0 - getYcoord(bounds.getY1());
        		
    			g2.drawRect(x0, y0 - ht, wd, ht);
    			
	    		bodies.forEach( b -> {
	    			P2d p = b.getPos();
			        int radius = (int) (10*scale);
			        if (radius < 1) {
			        	radius = 1;
			        }
			        g2.drawOval(getXcoord(p.getX()),getYcoord(p.getY()), radius, radius); 
			    });		    
	    		String time = String.format("%.2f", vt);
	    		g2.drawString("Bodies: " + bodies.size() + " - vt: " + time + " - nIter: " + nIter + " (UP for zoom in, DOWN for zoom out)", 2, 20);
    		}
        }
        
        private int getXcoord(double x) {
        	return (int)(dx + x*dx*scale);
        }

        private int getYcoord(double y) {
        	return (int)(dy - y*dy*scale);
        }
        
		public void display(ArrayList<Body> bodies, double vt, long iter, Boundary bounds){
            this.bodies = bodies;
            this.bounds = bounds;
            this.vt = vt;
            this.nIter = iter;
        }

		@Override
		public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == 38){  		/* KEY UP */
					scale *= 1.1;
				} else if (e.getKeyCode() == 40){  	/* KEY DOWN */
					scale *= 0.9;  
				} 
		}

		public void keyReleased(KeyEvent e) {}
		public void keyTyped(KeyEvent e) {}
    }
}

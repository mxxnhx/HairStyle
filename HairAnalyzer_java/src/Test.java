import java.io.File;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;


public class Test {
	public static void main(String[] args){
		File f = new File("res/hurban-1.png");
		HairAnalyzer h = new HairAnalyzer(f.getPath());
		h.detectFace();
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//Mat img = h.meanshift();
		Mat img = h.getImage();
		Mat segmented = h.meanshift();
		Mat hair = h.getHairArea(78,37);//hurban-1.png
//		Mat hair = h.getHairArea(452,262); // suit.jpg
		
		
//		int[][] neighbor = h.getneighbor(segmented);
//		for(int i=0;i<neighbor.length;i++){
//			for(int j=0;j<neighbor[0].length;j++)
//				System.out.print(neighbor[i][j] + " ");
//			System.out.println();
//		}
		
		DrawPanel panel = new DrawPanel(img,segmented);
		frame.setSize(img.cols(),img.rows());
//		DrawPanel panel = new DrawPanel(img,segmented,2);
//		frame.setSize(img.cols()/2,img.rows()/2);
		frame.add(panel);
		
		
		frame.setVisible(true);
		
	}
}


class DrawPanel extends JPanel {
	private Mat img;
	private Mat segmented;
	private int scale;
	public DrawPanel(Mat img,Mat segmented,int scale){
		this.img = img;
		this.segmented = segmented;
		this.scale = scale;
	}
	public DrawPanel(Mat img,Mat segmented){
		this.img = img;
		this.segmented = segmented;
		scale = 1;
	}
	
    private void doDrawing(Graphics g) {

        Graphics2D g2d = (Graphics2D) g;
        double[] color;
        for(int i=0;i<img.rows()/scale;i++) {
        	for(int j=0;j<img.cols()/scale;j++){
        		color = img.get(scale*i, j*scale);
                g2d.setColor(new Color((int)color[2],(int)color[1],(int)color[0]));
                g2d.drawLine(j, i, j, i);
        	}
        }
        if(segmented != null){
        	int a = (int)Core.minMaxLoc(segmented).maxVal;
        	System.out.println("maxval = "+a);
        	Color[] colors = new Color[a];
        	colors[0] = new Color(240,100,100);
        	for(int i=1;i<colors.length;i++)
        		colors[i] = new Color((int)(Math.random()*255),(int)(Math.random()*255),(int)(Math.random()*255));
        	
        	for(int i=0;i<img.rows()/scale;i++) {
            	for(int j=0;j<img.cols()/scale;j++){
            		if(segmented.get(i*scale,j*scale)[0] == -1)
            			g2d.setColor(Color.red);
            		else if((int)segmented.get(i*scale,j*scale)[0] != 0){
            			g2d.setColor(colors[(int)segmented.get(i*scale,j*scale)[0]-1]);
            			g2d.drawLine(j, i, j, i);
            		}
            		
            	}
        	}
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        
        super.paintComponent(g);
        doDrawing(g);
    }
}
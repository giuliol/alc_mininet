package dsp.unige.ALC.utils;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class ActualVisualizer implements Visualizer{

	JFrame frame;
	JPanel panel;
	JLabel label;
	
	public ActualVisualizer(JLabel labelRef){
		label = labelRef;		
	}
	
	public ActualVisualizer(){
	}
	
	public void init(String name){
		frame = new JFrame(name);
		panel = new JPanel(new BorderLayout());
		label = new JLabel();
		panel.add(label, BorderLayout.CENTER);
		panel.setVisible(true);
		frame.add(panel);
//		frame.setSize(Constants.WIDTH*2, Constants.HEIGHT*2);
		frame.setVisible(true);
//		frame.pack();
	}
	
	@Override
	public void display(byte[] img, int contentId) {
		
		ImageIcon image = new ImageIcon(img);
		label.setIcon(image);
		frame.pack();
	}

}

package dsp.unige.alc.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.HeadlessException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class WindowVisualizer implements Visualizer{

	JFrame frame;
	JPanel panel;
	JLabel label;
	JLabel idLabel; 
	boolean headless = false;

	public WindowVisualizer(JLabel labelRef){
		label = labelRef;		
	}

	public WindowVisualizer(){
	}

	public void init(String name){
		try {
			frame = new JFrame(name);
			panel = new JPanel(new BorderLayout());
			label = new JLabel();
			idLabel = new JLabel();
			idLabel.setOpaque(true);
			panel.add(label, BorderLayout.CENTER);
			panel.add(idLabel,BorderLayout.SOUTH);
			panel.setVisible(true);
			frame.add(panel);
			frame.setVisible(true);
			frame.setSize(Constants.WIDTH*2, Constants.HEIGHT*2);
		} catch (HeadlessException e) {
			headless = true;
		} 

	}

	int lastId = 0;

	@Override
	public void display(byte[] img, int contentId) {
		if(!headless){
			ImageIcon image = new ImageIcon(img);
			label.setIcon(image);
			idLabel.setText("F: "+contentId);
			
			if(contentId != lastId+1)
				idLabel.setBackground(Color.RED);
			else
				idLabel.setBackground(Color.GREEN);
			
			lastId = contentId;

		}
		//		frame.pack();
	}
	
	@Override
	public void display(byte[] img, int contentId, String misc) {
		
		if(!headless){
			ImageIcon image = new ImageIcon(img);
			label.setIcon(image);
			idLabel.setText("F. "+contentId+misc);

			if(contentId != lastId+1)
				idLabel.setBackground(Color.RED);
			else
				idLabel.setBackground(Color.GREEN);
			
			lastId = contentId;

		}

	}

	public void close() {
		if(!headless){
			frame.removeAll();
			frame.dispose();
		}
	}

	public void setOnTheRight() {
		frame.setLocation(Constants.WIDTH*2, frame.getLocation().y);
	}



}

package qrcode;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class ImageDrawer {

	public static JFrame frame;
	BufferedImage img;
	public static int WIDTH = 800;
	public static int HEIGHT = 600;

	public ImageDrawer() {
	}

	public static void main(String[] a) {

		ImageDrawer t = new ImageDrawer();

		frame = new JFrame("WINDOW");
		frame.setVisible(true);

		t.start();
		frame.add(new JLabel(new ImageIcon(t.getImage())));

		frame.pack();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	public Image getImage() {
		return img;
	}

	public void start() {

		try {
			img = QRCodeGenerator.generateQRCodeBufferedImage("http://nabeelbukhari.github.io", 300, 300, "./MyQRCode.png");
		} catch (Exception e) {
			e.printStackTrace();
		}

		boolean running = true;
		while (running) {
			BufferStrategy bs = frame.getBufferStrategy();
			if (bs == null) {
				frame.createBufferStrategy(4);
				return;
			}

			Graphics g = bs.getDrawGraphics();
			g.drawImage(img, 0, 0, WIDTH, HEIGHT, null);
			g.dispose();
			bs.show();
		}
	}
}
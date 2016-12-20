package gfx.gen;

import java.io.*;
import java.text.MessageFormat;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import gfx.tick1.Vector3;

public class Generator {
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("USAGE: gfx.gen.Generator <filename1> <filename2> ...");
			System.exit(-1);
		}
		
		for (int i = 0; i < args.length; ++i) {
			String filename = args[i];
			
			BufferedImage img = null;
			FileWriter writer = null;
			
			String format = "<sphere x=\"{0}\" y=\"{1}\" z=\"{2}\" radius=\"{3}\" colour=\"{4}\"/>";
			
			try {
				img = ImageIO.read(new File(filename));
				
				File file = new File(filename + ".xml");
				file.createNewFile();
				
				writer = new FileWriter(file);
				
				writer.write("<scene>\n");
				writer.write("<ambient-light colour=\"#ffffff\"/>\n");
				writer.write("<point-light x=\"0\" y=\"0\" z=\"0\" colour=\"#ffffff\" intensity=\"1000\"/>\n");
				
				// Perspective stuffs
				double width = img.getWidth();
				double height = img.getHeight();
				double fov = 45;
				double aspectRatio = width / height;
				double width_m = 2 * Math.tan(Math.toRadians(fov) / 3);
				double height_m = width_m / aspectRatio;
				double x_step_m = width_m / width;
				double y_step_m = height_m / height;
				
				// Loop through image
				for (int x = 0; x < width; ++x) {
					for (int y = 0; y < height; ++y) {
						int rgb = img.getRGB(x, y);
						
						String colour = "000000" + Integer.toHexString(rgb);
						colour = "#" + colour.substring(colour.length() - 6);
						
						double x_pos = (x_step_m - width_m) / 2 + x * x_step_m;
						double y_pos = (y_step_m + height_m) / 2 - y * y_step_m;
						
						Vector3 d = new Vector3(x_pos, y_pos, 1).normalised();
						d = d.scale(Math.random() * 200 + 100);
						
						writer.write(MessageFormat.format(format, d.x, d.y, d.z, 0.5, colour) + "\n");
					}
				}
				
				writer.write("</scene>");
				writer.close();
			}
			catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
	}
}
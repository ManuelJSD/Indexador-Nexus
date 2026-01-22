
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class IconResizer {
    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Usage: java IconResizer <input_file> <output_file> <width> <height>");
            return;
        }

        String inputPath = args[0];
        String outputPath = args[1];
        int targetWidth = Integer.parseInt(args[2]);
        int targetHeight = Integer.parseInt(args[3]);

        try {
            File inputFile = new File(inputPath);
            BufferedImage originalImage = ImageIO.read(inputFile);

            System.out.println("Processing " + inputPath);
            System.out.println("Original Dimensions: " + originalImage.getWidth() + "x" + originalImage.getHeight());

            // Resize using getScaledInstance for better quality (SCALE_SMOOTH)
            Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);

            // Convert back to BufferedImage
            BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = outputImage.createGraphics();
            g2d.drawImage(resultingImage, 0, 0, null);
            g2d.dispose();

            File outputFile = new File(outputPath);
            // Ensure directory exists
            outputFile.getParentFile().mkdirs();

            ImageIO.write(outputImage, "png", outputFile);
            System.out.println("Resized to " + targetWidth + "x" + targetHeight + " at " + outputPath);

        } catch (IOException e) {
            System.err.println("Error resizing " + inputPath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}

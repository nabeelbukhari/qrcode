package qrcode;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;

public class QRCodeGenerator {
	public static void main(String[] args) {
		int dimension = 300;
		int width = dimension, height = dimension;
		try {
			generateQRCodeImage("https://www.google.com", width, height, "./MyQRCode.png");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void generateQRCodeImage(String text, int width, int height, String filePath)
			throws WriterException, IOException {
		BufferedImage image = generateQRCodeBufferedImage(text, width, height, filePath);

		File outputfile = new File(filePath);
		ImageIO.write(image, "png", outputfile);
	}

	public static BufferedImage generateQRCodeBufferedImage(String text, int width, int height, String filePath)
			throws WriterException, IOException {
		final Map<EncodeHintType, Object> encodingHints = new HashMap<>();
		encodingHints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		QRCode code = Encoder.encode(text, ErrorCorrectionLevel.H, encodingHints);
		BufferedImage image = renderQRImage(code, width, height, 4);
		return image;
	}

	private static BufferedImage renderQRImage(QRCode code, int width, int height, int quietZone) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();

		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setBackground(Color.white);
		graphics.clearRect(0, 0, width, height);
		graphics.setColor(Color.black);

		ByteMatrix input = code.getMatrix();
		if (input == null) {
			throw new IllegalStateException();
		}
		int inputWidth = input.getWidth();
		int inputHeight = input.getHeight();
		int qrWidth = inputWidth + (quietZone * 2);
		int qrHeight = inputHeight + (quietZone * 2);
		int outputWidth = Math.max(width, qrWidth);
		int outputHeight = Math.max(height, qrHeight);

		int multiple = Math.min(outputWidth / qrWidth, outputHeight / qrHeight);
		int leftPadding = (outputWidth - (inputWidth * multiple)) / 2;
		int topPadding = (outputHeight - (inputHeight * multiple)) / 2;
		final int FINDER_PATTERN_SIZE = 7;
		final float CIRCLE_SCALE_DOWN_FACTOR = 21f / 30f;
		int circleSize = (int) (multiple * CIRCLE_SCALE_DOWN_FACTOR);

		for (int inputY = 0, outputY = topPadding; inputY < inputHeight; inputY++, outputY += multiple) {
			for (int inputX = 0, outputX = leftPadding; inputX < inputWidth; inputX++, outputX += multiple) {
				if (input.get(inputX, inputY) == 1) {
					if (!(inputX <= FINDER_PATTERN_SIZE && inputY <= FINDER_PATTERN_SIZE
							|| inputX >= inputWidth - FINDER_PATTERN_SIZE && inputY <= FINDER_PATTERN_SIZE
							|| inputX <= FINDER_PATTERN_SIZE && inputY >= inputHeight - FINDER_PATTERN_SIZE)) {
						graphics.fillOval(outputX, outputY, circleSize, circleSize);
					}
				}
			}
		}

		drawFinderPatternStyle(graphics, leftPadding, topPadding, inputWidth, inputHeight, multiple,
				FINDER_PATTERN_SIZE);

		return image;
	}

	private static void drawFinderPatternStyle(Graphics2D graphics, int leftPadding, int topPadding, int inputWidth,
			int inputHeight, int multiple, int FINDER_PATTERN_SIZE) {

		int circleDiameter = multiple * FINDER_PATTERN_SIZE;
//		drawFinderPatternCircleStyle(graphics, leftPadding, topPadding, circleDiameter);
//		drawFinderPatternCircleStyle(graphics, leftPadding + (inputWidth - FINDER_PATTERN_SIZE) * multiple, topPadding,
//				circleDiameter);
//		drawFinderPatternCircleStyle(graphics, leftPadding, topPadding + (inputHeight - FINDER_PATTERN_SIZE) * multiple,
//				circleDiameter);

		drawFinderPatternCustomStyle(graphics, leftPadding, topPadding, multiple, circleDiameter, 270);
		drawFinderPatternCustomStyle(graphics, leftPadding + (inputWidth - FINDER_PATTERN_SIZE) * multiple, topPadding,
				multiple, circleDiameter, 0);
		drawFinderPatternCustomStyle(graphics, leftPadding, topPadding + (inputHeight - FINDER_PATTERN_SIZE) * multiple,
				multiple, circleDiameter, 180);
	}

	private static void drawFinderPatternCustomStyle(Graphics2D graphics, int x, int y, int multiple, int circleDiameter,
			double rotateAngle) {

		File spritesheet = new File("./spritesheet.png");

		BufferedImage bufferedSprite = null;
		try {
			bufferedSprite = ImageIO.read(spritesheet);
		} catch (IOException exception) {
			throw new IllegalStateException();
		}

		int patternX = 425, patternY = 305;
		BufferedImage patternImage = bufferedSprite.getSubimage(patternX, patternY, 50, 50);

		int subPatternX = 185, subPatternY = 5;
		BufferedImage subPatternImage = bufferedSprite.getSubimage(subPatternX, subPatternY, 50, 50);

		/*
		 * Rotate sub image with give radians
		 */
		double rotationRequired = Math.toRadians(rotateAngle);

		double locationX = patternImage.getWidth() / 2;
		double locationY = patternImage.getHeight() / 2;
		AffineTransform tx = AffineTransform.getRotateInstance(rotationRequired, locationX, locationY);
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
		AffineTransformOp subOp = new AffineTransformOp(AffineTransform.getScaleInstance(1, 1),
				AffineTransformOp.TYPE_BILINEAR);

		double scale = circleDiameter / patternImage.getWidth();
		int offset = circleDiameter / 4;
		try {
			/* Draw the image, applying the filter */
			graphics.drawImage(transformImage(patternImage, scale, (int) rotateAngle), subOp, x, y);
			graphics.drawImage(transformImage(subPatternImage, scale / 2, (int) rotateAngle), subOp, x + offset, y + offset);
		} catch (Exception exception) {
			throw new IllegalStateException();
		}
	}

	private static void drawFinderPatternCircleStyle(Graphics2D graphics, int x, int y, int circleDiameter) {
		final int WHITE_CIRCLE_DIAMETER = circleDiameter * 5 / 7;
		final int WHITE_CIRCLE_OFFSET = circleDiameter / 7;
		final int MIDDLE_DOT_DIAMETER = circleDiameter * 3 / 7;
		final int MIDDLE_DOT_OFFSET = circleDiameter * 2 / 7;

		graphics.setColor(Color.black);
		graphics.fillOval(x, y, circleDiameter, circleDiameter);
		graphics.setColor(Color.white);
		graphics.fillOval(x + WHITE_CIRCLE_OFFSET, y + WHITE_CIRCLE_OFFSET, WHITE_CIRCLE_DIAMETER,
				WHITE_CIRCLE_DIAMETER);
		graphics.setColor(Color.black);
		graphics.fillOval(x + MIDDLE_DOT_OFFSET, y + MIDDLE_DOT_OFFSET, MIDDLE_DOT_DIAMETER, MIDDLE_DOT_DIAMETER);
	}

	/**
	 * Transforms the image efficiently without losing image quality. Scales the
	 * image to a width of (600 * scale) pixels, rotates the image, and translates
	 * (moves) the image to recenter it if rotated 90 or 270 degrees.
	 */
	protected static BufferedImage transformImage(BufferedImage image, double scale, int rotation) {
		int scaledWidth = (int) (scale * image.getWidth());
		int scaledHeight = (int) (scale * image.getHeight());

		// Methods AffineTransform.rotate(), AffineTransform.scale() and
		// AffineTransform.translate()
		// transform AffineTransform's transformation matrix to multiply with the
		// buffered image.
		// Therefore those methods are called in a counterintuitive sequence.
		AffineTransform transform;
		if (rotation % 180 == 0) {
			// First scale and second rotate image
			transform = AffineTransform.getRotateInstance(Math.toRadians(rotation), scaledWidth / 2, scaledHeight / 2);
			transform.scale(scale, scale);
		} else {
			// First scale, second rotate, and third translate image
			transform = AffineTransform.getTranslateInstance((scaledHeight - scaledWidth) / 2,
					(scaledWidth - scaledHeight) / 2);
			transform.rotate(Math.toRadians(rotation), scaledWidth / 2, scaledHeight / 2);
			transform.scale(scale, scale);
		}
		AffineTransformOp operation = new AffineTransformOp(transform, AffineTransformOp.TYPE_BICUBIC);
		BufferedImage transformedImage = operation.createCompatibleDestImage(image, image.getColorModel());
		return operation.filter(image, transformedImage);
	}
}

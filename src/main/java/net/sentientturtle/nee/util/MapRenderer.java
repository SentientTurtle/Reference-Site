package net.sentientturtle.nee.util;

import net.sentientturtle.nee.data.SDEData;
import net.sentientturtle.nee.data.datatypes.Mappable;
import net.sentientturtle.nee.data.datatypes.Region;
import net.sentientturtle.util.ExceptionUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.OptionalDouble;

/**
 * Renderer {@link Mappable} maps/charts, which are returned as byte arrays, containing the image in PNG format
 * <br>
 * To be replaced by dynamic map
 */
@Deprecated
public class MapRenderer {
    private static final int TARGET_WIDTH = 512;
    private static final int TARGET_HEIGHT = 512;
    private static final double MARGIN = 0.05;
    private static final int STAR_SIZE = 32;

    @SuppressWarnings("WeakerAccess")
    public static byte[] render(Mappable mappable, SDEData SDEData) {
        HashMap<Integer, Mappable> points = new HashMap<>();
        mappable.getMapPoints(SDEData).forEach(point -> points.put(point.getID(), point));

        double mini = Double.MAX_VALUE;
        double maxiX = Double.MAX_VALUE * -1;
        double miniZ = Double.MAX_VALUE;
        double maxiZ = Double.MAX_VALUE * -1;

        for (Mappable point : points.values()) {
            mini = Math.min(point.x(), mini);
            miniZ = Math.min(point.z(), miniZ);
            maxiX = Math.max(point.x(), maxiX);
            maxiZ = Math.max(point.z(), maxiZ);
        }

        // Final variables for use in lambda later
        final double minX = mini;
        final double maxX = maxiX;
        final double minZ = miniZ;
        final double maxZ = maxiZ;

        double sizeWidth = maxX - minX;
        double sizeHeight = maxZ - minZ;

        final int renderSize;
        if (sizeWidth > sizeHeight) {
            renderSize = (int) Math.max(sizeWidth / Math.pow(10, 14), 512.0);
        } else {
            renderSize = (int) Math.max(sizeHeight / Math.pow(10, 14), 512.0);
        }

        double scaleX = renderSize / (maxX - minX);
        double scaleZ = renderSize / (maxZ - minZ);

        BufferedImage image = new BufferedImage((int) (renderSize * (2 * MARGIN + 1)), (int) (renderSize * (2 * MARGIN + 1)), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();

        graphics.setStroke(new BasicStroke(5));
        graphics.setColor(new Color(155, 155, 155));

        mappable.getMapLines(SDEData).forEach(jump -> {
            Mappable from = points.get(jump.fromSolarSystemID());
            Mappable to = points.get(jump.toSolarSystemID());
            graphics.drawLine(
                    (int) ((MARGIN * renderSize) + (from.x() - minX) * scaleX),
                    (int) ((MARGIN * renderSize) + (from.z() - minZ) * scaleZ),
                    (int) ((MARGIN * renderSize) + (to.x() - minX) * scaleX),
                    (int) ((MARGIN * renderSize) + (to.z() - minZ) * scaleZ)
            );
        });

        for (Mappable point : points.values()) {
            OptionalDouble security = point.getSecurity(SDEData);
            if (security.isPresent()) {
                graphics.setColor(calcColor(security.getAsDouble()));
            } else {
                graphics.setColor(new Color(255, 255, 255));
            }
            graphics.fillOval(
                    (int) ((MARGIN * renderSize) + ((point.x() - minX) * scaleX) - (STAR_SIZE / 2.0)),
                    (int) ((MARGIN * renderSize) + ((point.z() - minZ) * scaleZ) - (STAR_SIZE / 2.0)),
                    STAR_SIZE,
                    STAR_SIZE
            );
        }

        if (mappable instanceof Region mapRegion) {
            graphics.setColor(Color.WHITE);

            double bbXMin = (MARGIN * renderSize) + (mapRegion.xMin - minX) * scaleX;
            double bbXMax = (MARGIN * renderSize) + (mapRegion.xMax - minX) * scaleX;
            double bbZMin = (MARGIN * renderSize) + (mapRegion.zMin - minZ) * scaleZ;
            double bbZMax = (MARGIN * renderSize) + (mapRegion.zMax - minZ) * scaleZ;

            graphics.fillRect(
                (int) ((MARGIN * renderSize) + ((mapRegion.x() - minX) * scaleX) - (STAR_SIZE / 2.0)),
                (int) ((MARGIN * renderSize) + ((mapRegion.z() - minZ) * scaleZ) - (STAR_SIZE / 2.0)),
                STAR_SIZE,
                STAR_SIZE
            );

            graphics.drawRect((int) bbXMin, (int) bbZMin, (int) (bbXMax - bbXMin), (int) (bbZMax - bbZMin));
        }

        graphics.dispose();

        BufferedImage scaledImage = new BufferedImage(TARGET_WIDTH, TARGET_HEIGHT, image.getType());
        Graphics2D scaledImageGraphics = scaledImage.createGraphics();
        scaledImageGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        scaledImageGraphics.drawImage(image, 0, TARGET_HEIGHT, TARGET_WIDTH, 0, 0, 0, image.getWidth(), image.getHeight(), null);   // Scale and flip image
        scaledImageGraphics.dispose();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(scaledImage, "PNG", byteArrayOutputStream);
        } catch (IOException e) {
            System.err.println("ByteArrayOutputStream shouldn't throw IO Errors!");
            ExceptionUtil.sneakyThrow(e);
        }
        return byteArrayOutputStream.toByteArray();
    }


    public static final String[] SECURITY_COLORS = {
            "#F00000",
            "#D73000",
            "#F04800",
            "#F06000",
            "#D77700",
            "#EFEF00",
            "#8FEF2F",
            "#00F000",
            "#00EF47",
            "#48F0C0",
            "#2FEFEF"
    };

    private static Color calcColor(Double temperature) {
        double security;
        if (temperature == null) {
            security = 0;
        } else {
            security = Math.max(temperature, 0);
        }
        security = Math.round(security*10);
        return Color.decode(SECURITY_COLORS[(int) security]);
    }
}

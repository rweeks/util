package com.newbrightidea.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

public class VisualizeRTree {
    static final int BOX_ALPHA = 180;
    static final Color[] BOX_COLOURS = new Color[] {
            new Color(230, 25, 75, BOX_ALPHA),
            new Color(60, 180, 75, BOX_ALPHA),
            new Color(255, 225, 25, BOX_ALPHA),
            new Color(0, 130, 200, BOX_ALPHA),
            new Color(245, 130, 48, BOX_ALPHA),
            new Color(145, 30, 180, BOX_ALPHA),
            new Color(70, 240, 240, BOX_ALPHA),
            new Color(240, 50, 230, BOX_ALPHA)
    };

    static final Stroke BOX_STROKE = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    static final Color[] DOT_COLOURS = new Color[] {
            new Color(230, 25, 75),
            new Color(60, 180, 75),
            new Color(255, 225, 25),
            new Color(0, 130, 200),
            new Color(245, 130, 48),
            new Color(145, 30, 180),
            new Color(70, 240, 240),
            new Color(240, 50, 230)
    };

    static final Stroke DOT_STROKE = new BasicStroke();

    private void drawLegend(Graphics2D g2d, int numPoints) {
        final int offsX = 50;
        final int offsY = 50;
        final int boxW = 40;
        final int boxH = 40;
        final int padding = 5;
        final int maxDepth = 4;
        for (int i = 0; i <= maxDepth; i++) {
            final int boxOffsX = offsX;
            final int boxOffsY = offsY + (boxH + padding) * i;
            final Rectangle2D.Double boxRect = new Rectangle2D.Double(boxOffsX, boxOffsY, boxW, boxH);
            g2d.setColor(DOT_COLOURS[i]);
            g2d.fill(boxRect);
            g2d.setStroke(BOX_STROKE);
            g2d.drawString("Depth " + i, boxOffsX + boxW + 15, boxOffsY + 28);
            g2d.setColor(Color.WHITE);
            g2d.draw(boxRect);
        }
        g2d.drawString("Total Tweets: " + numPoints, offsX, offsY + (boxH + padding) * (maxDepth + 2));
    }

    /**
     * Produces a set of png images demonstrating the growth/rebalancing of the RTree
     * @param pointFile a text file, lines not of the form<br/>
     *                  LONLAT\t&lt;lon in floating-point form&gt;&lt;lat in floating-point form%gt;\n<br/>
     *                  will be ignored.
     * @param maxPoints the maximum number of entries to consider from the above file
     * @param stepPoints the number of entries to add to the RTree between frames
     * @throws Exception
     */
    private void renderVisualization(String outputDir, String backgroundImage,
                                     float bgSwLon, float bgSwLat, float bgNeLon, float bgNeLat,
                                     String pointFile, int maxPoints, int stepPoints) throws Exception {
        int frameNumber = 0;
        for (int target = 0; target < 4500; target += 10) {
            final BufferedImage background = ImageIO.read(new File("/Users/rweeks/Desktop/uk_map_1080.png"));
            final Graphics2D g2d = (Graphics2D) background.getGraphics();
            g2d.setFont(g2d.getFont().deriveFont(20.0f).deriveFont(Font.BOLD));

            final int width = background.getWidth();
            final int height = background.getHeight();

            final BiFunction<Float, Float, Point2D.Float> coordConvert = (lon, lat) -> new Point2D.Float(
                    width * ((lon - bgSwLon) / (bgNeLon - bgSwLon)),
                    height - (height * ((lat - bgSwLat) / (bgNeLat - bgSwLat)))
            );

            int numInserted = 0;

            final RTree<String> tweets = new RTree<>(100, 2, 2, RTree.SeedPicker.LINEAR);
            try (final BufferedReader br = new BufferedReader(new FileReader(pointFile))) {
                String tweetline = null;
                float[] coordBuf = new float[2];
                while ((tweetline = br.readLine()) != null && numInserted < target) {
                    if (tweetline.startsWith("LONLAT")) {
                        final String[] coords = tweetline.split("\t");
                        coordBuf[0] = Float.parseFloat(coords[1]);
                        coordBuf[1] = Float.parseFloat(coords[2]);
                        if (coordBuf[0] < bgSwLon || coordBuf[0] > bgNeLon || coordBuf[1] < bgSwLat || coordBuf[1] > bgNeLat) {
                            System.out.println(String.format("Discarding bad value %3.3f %3.3f", coordBuf[0], coordBuf[1]));
                            continue;
                        }
                        tweets.insert(coordBuf, "T");
                        numInserted++;
                    }
                }
                System.out.println("Inserted " + numInserted + " entries");
            }
            drawLegend(g2d, numInserted);
            final AtomicInteger maxDepth = new AtomicInteger(0);
            tweets.visit(new RTree.NodeVisitor<String>() {
                @Override
                public void visit(int depth, float[] coords, float[] dimensions, String value) {
                    if (maxDepth.get() < depth) {
                        maxDepth.set(depth);
                    }
                    if (value != null) {
                        g2d.setStroke(DOT_STROKE);
                        g2d.setColor(DOT_COLOURS[depth]);
                        final Point2D renderedPoint = coordConvert.apply(coords[0], coords[1]);
                        g2d.fill(new Ellipse2D.Double(renderedPoint.getX(), renderedPoint.getY(), 4, 4));
                    } else {
                        final Point2D swPoint = coordConvert.apply(coords[0], coords[1]);
                        final Point2D nePoint = coordConvert.apply(coords[0] + dimensions[0], coords[1] + dimensions[1]);
                        g2d.setStroke(BOX_STROKE);
                        g2d.setColor(BOX_COLOURS[depth]);
                        g2d.draw(new Rectangle2D.Double(swPoint.getX(), nePoint.getY(),
                                nePoint.getX() - swPoint.getX(), swPoint.getY() - nePoint.getY()));
                    }
                }
            });
            ImageIO.write(background, "png",
                    new File(String.format("%s/frame-%03d.png", outputDir, frameNumber))
            );
            System.out.println("Saved frame " + frameNumber);
            frameNumber++;
        }
    }

    /**
     * Works for me!
     * java -cp build/production:build/test com.newbrightidea.util.VisualizeRTree /Users/rweeks/rtree-output /Users/rweeks/uk_map_1080.png -21.25 48.5 7.65 58.78 /Users/rweeks/output.txt 4500 10
     */
    public static void main(String[] args) throws Exception {
        final String outputDir = args[0];
        final String backgroundImage = args[1];
        final float bgSwLon = Float.parseFloat(args[2]);
        final float bgSwLat = Float.parseFloat(args[3]);
        final float bgNeLon = Float.parseFloat(args[4]);
        final float bgNeLat = Float.parseFloat(args[5]);
        final String pointFile = args[6];
        final int maxPoints = Integer.parseInt(args[7]);
        final int stepPoints = Integer.parseInt(args[8]);

        new VisualizeRTree().renderVisualization(outputDir, backgroundImage, bgSwLon, bgSwLat, bgNeLon, bgNeLat, pointFile, maxPoints, stepPoints);
    }
}

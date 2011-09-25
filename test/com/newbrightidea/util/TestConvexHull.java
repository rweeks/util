package com.newbrightidea.util;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Random;

public class TestConvexHull {

  private static final int IMG_WIDTH = 1024;
  private static final int IMG_HEIGHT = 1024;
  private static final int IMG_TYPE = BufferedImage.TYPE_INT_ARGB;
  
  private float[][] generatePoints(int numPoints)
  {
    Random r = new Random();
    float[][] points = new float[numPoints][2];
    for ( int i = 0; i < numPoints; i++ )
    {
      float angle = (float)(r.nextDouble() * 2.0d * Math.PI);
      float hyp = (float)r.nextGaussian() / 6.0f;
      points[i][0] = hyp*(float)Math.cos(angle);
      points[i][1] = hyp*(float)Math.sin(angle);
    }
    return points;
  }

  private float[][] generatePoints()
  {
//    return new float[][]
//            {
//                    { 0.0f, 0.0f },
//                    { 0.0f, 0.5f },
//                    { 0.5f, 0.5f },
//                    { 0.5f, 0.0f }
//            };
//    return new float[][]
//            {
//                    { 0.0f, 0.0f },
//                    { 0.0f, 0.5f },
//                    { 0.5f, 0.5f },
//                    { 0.5f, 0.0f },
//                    { 0.26f, 0.25f }
//            };
    return new float[][]
            {
                    { 0.251f, 0.251f },
                    { 0.752f, 0.752f },
                    { 0.0f, 0.0f },
                    { 0.0f, 0.5f },
                    { 0.53f, 0.53f },
                    { 0.5f, 0.0f }
            };
  }
  
  private BufferedImage renderPoints(float[][] allPoints, float[][] hullPoints)
          throws IOException
  {
    BufferedImage img = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, IMG_TYPE);
    Graphics2D g2d = (Graphics2D)img.getGraphics();
    g2d.setBackground(Color.WHITE);
    g2d.setColor(Color.WHITE);
    g2d.fillRect(0, 0, IMG_WIDTH, IMG_HEIGHT);
    g2d.setComposite(AlphaComposite.SrcOver.derive(0.5f));
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setColor(Color.GREEN);
    AffineTransform transform = AffineTransform.getTranslateInstance(IMG_WIDTH / 2, IMG_HEIGHT / 2);
    transform.scale(IMG_WIDTH / 2, IMG_HEIGHT / 2);
    float[] origin = new float[] { 0.0f, 0.0f };
    transform.transform(origin, 0, origin, 0, 1);
    g2d.fillArc((int)origin[0]-8, (int)origin[1]-8, 16, 16, 0, 360);
    g2d.setColor(Color.BLUE);
    Path2D.Float p = new Path2D.Float();
    for ( int i = 0; i < hullPoints.length; i++ )
    {
      transform.transform(hullPoints[i], 0, hullPoints[i], 0, 1);
      g2d.fillArc((int)hullPoints[i][0]-2, (int)hullPoints[i][1]-2, 4, 4, 0, 360);
      if ( i == 0 )
      {
        p.moveTo(hullPoints[i][0], hullPoints[i][1]);
      }
      else
      {
        p.lineTo(hullPoints[i][0], hullPoints[i][1]);
      }
    }
    p.lineTo(hullPoints[0][0], hullPoints[0][1]);
    g2d.draw(p);
    g2d.setColor(Color.BLACK);
    for ( int i = 0; i < allPoints.length; i++ )
    {
      transform.transform(allPoints[i], 0, allPoints[i], 0, 1);
      g2d.fillArc((int)allPoints[i][0]-2, (int)allPoints[i][1]-2, 4, 4, 0, 360);
    }



    return img;
  }

  private void writeImage(BufferedImage img, String outName)
          throws IOException
  {
    ImageWriter out = ImageIO.getImageWritersByFormatName("png").next();
    out.setOutput(new FileImageOutputStream(new File(outName)));
    out.write(img);
  }

  public static void main(String[] args)
          throws IOException
  {
    PrintStream p = System.out;
    TestConvexHull tst = new TestConvexHull();
    float[][] points = tst.generatePoints(100);
    p.println("Got " + points.length + " points");
    float[][] hull = ConvexHull.findConvexHull(points);
    p.println("Hull == " + hull.length + " points");
    BufferedImage img = tst.renderPoints(points, hull);
    tst.writeImage(img, "/tmp/hull.png");
  }
}

package com.newbrightidea.util;

import java.util.*;

public class ConvexHull
{

  private static float ccw( float[] p1, float[] p2, float[] p3 )
  {
    return (p2[0] - p1[0])*(p3[1] - p1[1]) - (p2[1] - p1[1])*(p3[0] - p1[0]);
  }
  
  public static float[][] findConvexHull(float[][] pts)
  {
    if ( pts.length < 3 )
    {
      return pts;
    }
    float[] minY = { Float.MAX_VALUE, Float.MAX_VALUE };
    int ixMinY = -1;
    for ( int i = 0; i < pts.length; i++ )
    {
      float[] pt = pts[i];
      if ( pt[1] < minY[1] )
      {
        minY = pt;
        ixMinY = i;
      }
      else if ( pt[1] == minY[1] && pt[0] < minY[0] )
      {
        minY = pt;
        ixMinY = i;
      }
    }

    pts[ixMinY] = pts[0];
    pts[0] = minY;
    
    Arrays.sort(pts, 1, pts.length, new AngleComparator(minY));
    Deque<float[]> ps = new LinkedList<float[]>();
    ps.push(pts[0]);
    ps.push(pts[1]);
    for ( int i = 2; i < pts.length; i++ )
    {
      float[] p0 = ps.pop();
      float[] p1 = ps.pop();
      float ccw = ccw(p0, p1, pts[i]);
      ps.push(p1);
      ps.push(p0);
      if ( ccw == 0 )
      {
        ps.pop();
        ps.push(pts[i]);
      }
      else if ( ccw < 0 )
      {
        ps.push(pts[i]);
      }
      else
      {
        while ( (ccw >= 0) && ps.size() > 2 )
        {
          ps.pop();
          p0 = ps.pop();
          p1 = ps.pop();
          ccw = ccw(p0, p1, pts[i]);
          ps.push(p1);
          ps.push(p0);
        }
        ps.push(pts[i]);
      }
    }
    return ps.toArray(new float[ps.size()][]);
  }

  private static class AngleComparator implements Comparator<float[]>
  {
    private final float[] pt;

    public AngleComparator(float[] pt)
    {
      this.pt = pt;
    }

    public int compare(float[] m, float[] n)
    {
      // cos t = a/h
      // a = m.x - pt.x
      // h = sqrt((m.x-pt.x)^2 + (m.y - pt.y)^2)
      float cm = (m[0] - pt[0])/
              (float)Math.sqrt((m[0] - pt[0])*(m[0] - pt[0]) + (m[1] - pt[1])*(m[1] - pt[1]));
      cm = 1 - cm;
      float cn = (n[0] - pt[0])/
              (float)Math.sqrt((n[0] - pt[0])*(n[0] - pt[0]) + (n[1] - pt[1])*(n[1] - pt[1]));
      cn = 1 - cn;
      if ( cm < cn )
      {
          return -1;
      }
      else if ( cm > cn )
      {
          return 1;
      }
      else
      {
          return 0;
      }
    }
  }
}

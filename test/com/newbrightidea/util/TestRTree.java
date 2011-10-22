package com.newbrightidea.util;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import org.junit.Test;

public class TestRTree
{
  private static final float[] ZEROES = { 0.0f, 0.0f };
  private static final float[] ONES = {1.0f, 1.0f};
  private static final float[] NEG_ONES = {-1.0f, -1.0f};
  private static final float[] POINT_FIVES = {0.5f, 0.5f};
  private static final float[] NEG_POINT_FIVES = {-0.5f, -0.5f};

  @Test
  public void testAssertionsEnabled()
  {
    try
    {
      assert(false) : "Assert failed";
      throw new IllegalStateException("Enable assertions!");
    }
    catch ( AssertionError ae )
    {
      // do nothing
    }
  }
  
  @Test
  public void testCreation()
  {
    RTree<Long> rt = new RTree<Long>();
  }
  
  @Test
  public void testInsertWithPoint()
  {
    RTree<Object> rt = new RTree<Object>();
    Object o = new Object();
    rt.insert(ZEROES, ZEROES, o);
    List<Object> results =
      rt.search(new float[] {-1.0f, -1.0f},
                new float[] { 2.0f,  2.0f});
    assert(results.get(0) == o);
  }
  
  @Test
  public void testInsertWithRect()
  {
    RTree<Object> rt = new RTree<Object>();
    Object o = new Object();
    rt.insert(ZEROES, ONES, o);
    List<Object> results =
      rt.search( new float[] {-1.0f, -1.0f},
                 new float[] {3.0f, 3.0f} );
    assert(results.get(0) == o);
  }
  
  @Test
  public void testInsertWithNegativeCoords()
  {
    RTree<Object> rt = new RTree<Object>();
    Object n = new Object();
    Object p = new Object();
    rt.insert(NEG_ONES, POINT_FIVES, n);
    rt.insert(POINT_FIVES, ONES, p);
    List<Object> results =
      rt.search( NEG_POINT_FIVES, ZEROES );
    assert(results.size() == 1) : "Invalid returns for neg, expected 1 got " + results.size();
    assert(results.get(0) == n);
    results =
      rt.search( POINT_FIVES, ZEROES );
    assert(results.size() == 1) : "Invalid returns for pos, expected 1 got " + results.size();
    assert(results.get(0) == p);
  }
  
  @Test
  public void testEmptyResults()
  {
    RTree<Object> rt = new RTree<Object>();
    Object o = new Object();
    rt.insert(ZEROES, ZEROES, o);
    List<Object> results =
      rt.search(new float[] {-1.0f, -1.0f},
                new float[] { 0.5f,  0.5f});
    assert(results.isEmpty());
  }
  
  @Test
  public void testSplitNodesSmall()
  {
    RTree<Object> rt = new RTree(2,1,2);
    float[][] coords = new float[][] { {0.0f, 0.0f}, {1.0f, 1.0f}, {2.0f, 2.0f}, {3.0f, 3.0f} };
    float[] dims = new float[]{0.5f, 0.5f};
    Object[] entries = new Object[] { new Object(), new Object(), new Object(), new Object() };
    for (int i = 0; i < entries.length; i++ )
    {
      rt.insert(coords[i], dims, entries[i]);
    }
    List<Object> results = rt.search(new float[] {2.0f, 2.0f},
                                     new float[] {0.5f, 0.5f});
    assert (results.size() == 1);
    assert (results.get(0).equals(entries[2]));
  }
  
  @Test
  public void testSplitNodesBig()
  {
    RTree<Object> rt = new RTree<Object>(50,2,2);
    int numEntries = rt.getMaxEntries() * 4;
    float[] coords = new float[] { 0.0f, 0.0f };
    float[] dims = new float[] { 0.5f, 0.5f };
    Object[] entries = new Object[numEntries];
    
    for ( int i = 0; i < numEntries; i++ )
    {
      coords[0] = i;
      entries[i] = new Object();
      rt.insert(coords, dims, entries[i]);
    }
    
    for ( int i = 0; i < numEntries; i++ )
    {
      coords[0] = i;
      List<Object> results = rt.search(coords, dims);
      assert(results.size() == 1);
      assert(results.get(0) == entries[i]);
    }
  }
  
  @Test
  public void testVisualize()
    throws Exception
  {
    RTree<Object> rt = new RTree<Object>(50,2,2);
    int numEntries = rt.getMaxEntries() * 4;
    float[] coords = new float[] { 0.0f, 0.0f };
    float[] dims = new float[] { 0.5f, 0.5f };
    Object[] entries = new Object[numEntries];
    
    for ( int i = 0; i < numEntries; i++ )
    {
      coords[0] = i;
      entries[i] = new Object();
      rt.insert(coords, dims, entries[i]);
    }
    String html = rt.visualize();
    System.err.println("Writing to " + System.getProperty("java.io.tmpdir"));
    OutputStream os = new FileOutputStream(System.getProperty("java.io.tmpdir") + "/rtree.html");
    os.write(html.getBytes());
    os.flush();
    os.close();
  }
  
  @Test
  public void testDelete()
  {
    RTree<Object> rt = new RTree<Object>();
    Object entry = new Object();
    List<Object> results;
    rt.insert(ZEROES, ONES, entry);
    results = rt.search(ZEROES, ONES);
    assert(results.size() == 1);
    assert(results.get(0) == entry);
    rt.delete(ZEROES, ONES, entry);
    results = rt.search(ZEROES, ONES);
    assert(results.size() == 0);
  }
}

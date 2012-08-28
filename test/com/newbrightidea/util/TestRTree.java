package com.newbrightidea.util;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
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
    RTree<Object> rt = new RTree(2,1,2, RTree.SeedPicker.QUADRATIC);
    float[][] coords = new float[][] { {0.0f, 0.0f}, {1.0f, 1.0f}, {2.0f, 2.0f}, {3.0f, 3.0f} };
    float[] dims = new float[]{0.5f, 0.5f};
    Object[] entries = new Object[] { 0, 1, 2, 3 };
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
  public void testRemoveAll()
  {
    // setup is like testSplitNodesSmall
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

    float[] sCoords = new float[] { -0.5f * Float.MAX_VALUE, -0.5f * Float.MAX_VALUE };
    float[] sDims = new float[] { Float.MAX_VALUE, Float.MAX_VALUE };
    results = rt.search(sCoords, sDims);
    assert(results.size() == rt.size());
    for ( Object result: results )
    {
      boolean deleted = rt.delete(sCoords, sDims, result);
      assert(deleted);
    }
    assert(rt.size() == 0);
    float [] newCoords = new float[] { 0.0f, 0.0f };
    float [] newDims = new float[] { 0.0f, 0.0f };
    Object entry = new Object();
    rt.insert(newCoords, newDims, entry);
    assert( rt.search(newCoords, newDims).get(0) == entry );
  }

  @Test
  public void testRemoveAlmostAll()
  {
    // setup is like testSplitNodesSmall
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

    float[] sCoords = new float[] { -0.5f * Float.MAX_VALUE, -0.5f * Float.MAX_VALUE };
    float[] sDims = new float[] { Float.MAX_VALUE, Float.MAX_VALUE };
    results = rt.search(sCoords, sDims);
    assert(results.size() == rt.size());
    Iterator resultIter = results.iterator();
    while ( resultIter.hasNext() )
    {
      Object toRemove = resultIter.next();
      if ( !resultIter.hasNext() )
      {
        break;
      }
      boolean deleted = rt.delete(sCoords, sDims, toRemove);
      assert(deleted);
    }

    assert(rt.size() == 1);
    float [] newCoords = new float[] { 5.0f, 5.0f };
    float [] newDims = new float[] { 0.0f, 0.0f };
    Object entry = new Object();
    rt.insert(newCoords, newDims, entry);
    results = rt.search(newCoords, newDims);
    assert (results.size() == 1);
    assert (results.get(0) == entry);
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

  @Test
  public void testInsertDelete() {
    class DataObject {
      final float[] val;
      final float[] dim;
      final Integer id;

      DataObject(float[] val, float[] dim, int id) {
        this.val = val;
        this.dim = dim;
        this.id = id;
      }
    }

    for ( int j = 0; j < 500; j++ )
    {
      RTree<Integer> tree = new RTree<Integer>(10, 2, 3, RTree.SeedPicker.LINEAR);
      List<DataObject> rects = new ArrayList<DataObject>();

      for (int i = 0; i < 150; i++) {
        rects.add(new DataObject(
            new float[]{i, i * 2, i * 3},
            new float[]{0, 0, 0},
            i));
        DataObject dataObject = rects.get(i);
        tree.insert(dataObject.val, dataObject.dim, dataObject.id);
      }

      for (int i = 0; i < 150; i++) {
        DataObject dataObject = rects.get(i);
        boolean deleted = tree.delete(dataObject.val, dataObject.dim, dataObject.id);
        assert deleted;
      }
    }
  }

  @Test
  public void testInsertNegCoords() {
    class DataObject {
      final float[] val;
      final float[] dim;
      final Integer id;

      DataObject(float[] val, float[] dim, int id) {
        this.val = val;
        this.dim = dim;
        this.id = id;
      }
    }

    for ( int j = 0; j < 500; j++ )
    {
      RTree<Integer> tree = new RTree<Integer>(10, 2, 3);
      List<DataObject> rects = new ArrayList<DataObject>();

      for (int i = -70; i < 80; i++) {
        rects.add(new DataObject(
            new float[]{i, i * 2, i * 3},
            new float[]{0, 0, 0},
            i));
        DataObject dataObject = rects.get(70+i);
        tree.insert(dataObject.val, dataObject.dim, dataObject.id);
      }

      for (int i = 0; i < rects.size(); i++) {
        DataObject dataObject = rects.get(i);
        boolean deleted = tree.delete(dataObject.val, dataObject.dim, dataObject.id);
        assert deleted;
      }
      assert tree.size() == 0;
    }
  }

  @Test
  public void testCircleInsert() {
    RTree<Integer> tree = new RTree<Integer>(10, 2, 3);
    tree.insert(new float[] {-133.35106f, -27.210342f, -20.107727f}, 0);
    tree.insert(new float[] {-123.99338f, -48.480087f, 4.125839f}, 1);
    tree.insert(new float[] {-105.43765f, -103.077614f, 43.90358f}, 2);
    tree.insert(new float[] {-63.769794f, -96.73669f, 55.480133f}, 3);
    tree.insert(new float[] {18.339613f, -96.38262f, 62.539703f}, 4);
    tree.insert(new float[] {86.32401f, -75.77544f, 38.990417f}, 5);
    tree.insert(new float[] {114.32304f, -54.91469f, 14.197311f}, 6);
    tree.insert(new float[] {129.68332f, -6.8892365f, -39.09485f}, 7);
    tree.insert(new float[] {90.21576f, 41.889008f, -84.45425f}, 8);
    tree.insert(new float[] {49.884323f, 57.874786f, -95.06121f}, 9);
    tree.insert(new float[] {-28.798616f, 63.59677f, -101.57546f}, 10);
    tree.insert(new float[] {-75.07794f, 55.577896f, -104.90407f}, 11);
    tree.insert(new float[] {-111.65004f, 32.36528f, -83.63572f}, 12);
    tree.insert(new float[] {-127.87806f, 2.7788544f, -49.90689f}, 13);
    tree.delete(new float[] {-133.35106f, -27.210342f, -20.107727f}, 0);
    tree.delete(new float[] {-127.87806f, 2.7788544f, -49.90689f}, 13);
    tree.delete(new float[] {-111.65004f, 32.36528f, -83.63572f}, 12);
    tree.delete(new float[] {-75.07794f, 55.577896f, -104.90407f}, 11);
    tree.delete(new float[] {-28.798616f, 63.59677f, -101.57546f}, 10);
    tree.delete(new float[] {49.884323f, 57.874786f, -95.06121f}, 9);
    tree.delete(new float[] {90.21576f, 41.889008f, -84.45425f}, 8);
  }
}

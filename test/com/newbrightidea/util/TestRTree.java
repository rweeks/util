package com.newbrightidea.util;

import java.util.List;

import org.junit.Test;

public class TestRTree
{
  private static final float[] ZEROES = { 0.0f, 0.0f };
  private static final float[] ONES = {1.0f, 1.0f};

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
  public void testSplitNodes()
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
      assert(results.size() == 0);
      assert(results.get(0) == entries[i]);
    }
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
    rt.delete(ZEROES, ONES, entry);
    results = rt.search(ZEROES, ONES);
    assert(results.size() == 0);
  }
}

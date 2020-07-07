package main.java.bgu.spl.mics;

import static org.junit.Assert.*;

import bgu.spl.mics.Future;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/*import java.security.InvalidAlgorithmParameterException;*/

public class FutureTest {

    Future<Integer> future;
    @Before
    public void setUp() throws Exception {
        future=new Future<Integer>();
    }

    @After
    public void tearDown() throws Exception {
        future=null;
    }

    @Test
    public void testFuture() {
        assertTrue(future!=null);
        assertTrue(future.isDone()==false);
    }

    @Test
    public void testGet() {
        assertTrue(future.get()==null);
        future.resolve(10);
        assertTrue(future.get()==10);
    }

    @Test
    public void testResolve() {
        future.resolve(10);
        assertTrue(future.get()==10);
    }

    @Test
    public void testIsDone()
    {
        assertTrue(future.isDone()==false);
      future.resolve(5);
      assertTrue(future.isDone());

    }
    @Test
    public void testGetLongTimeUnit()
    {
 //assertNull(future.get(3000,unit));
    }

}

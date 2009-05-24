package com.xuggle.xuggler;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xuggle.xuggler.io.IURLProtocolHandler;

import static org.junit.Assert.*;

/**
 * Tests to make sure we can interrupt long-running Xuggler calls.
 * 
 * @author aclarke
 *
 */
public class InterruptingExhaustiveTest
{
  private final Logger log = LoggerFactory.getLogger(this.getClass());
 
  // needs a long time out because if running in an exhaustive
  // test on a slow machine (oh like our build server) it can
  // sometime fail.
  @Test(timeout=120*1000)
  public void testXugglerSupportInterruptions() throws InterruptedException
  {
    final AtomicBoolean testStarted = new AtomicBoolean(false);
    final AtomicBoolean testPassed = new AtomicBoolean(false);
    final Object lock = new Object();
    Thread thread = new Thread(
        new Runnable(){
          public void run()
          {
            IContainer container = IContainer.make();

            synchronized(lock) {
              testStarted.set(true);
              lock.notifyAll();
            }
            log.info("About to call blocking ffmpeg method");
            int retval = container.open(
//                "fixtures/testfile.flv",
               "udp://127.0.0.1:12345/notvalid.flv?localport=28302",
                IContainer.Type.READ,
                null, true, false);
            log.info("ffmpeg method returned");
            // we should return from this method in an
            // interrupted state.
            if (Thread.interrupted()) {
              log.info("thread was interrupted");
              // clear the interrupt
              if (retval < 0)
                testPassed.set(true);
              else
                log.info("but test did not pass");
            }
          }},
        "xuggler thread");
    
    synchronized(lock) {
      thread.start();
      while(!testStarted.get()) {
        log.info("waiting for start");
        lock.wait();
      }
    }
    // give the thread about a half a second to get into ffmpeg land
    Thread.sleep(500);
    log.info("about to interrupt ffmpeg-calling thread");
    thread.interrupt();
    log.info("interrupted ffmpeg-calling thread");
    thread.join();
    assertTrue("test did not pass", testPassed.get());
  }

  @Test
  public void testInteruptStatusPreservedAccrossJNICalls()
  {
    IContainer container = IContainer.make();
    
    IURLProtocolHandler handler = new IURLProtocolHandler(){
      public int close()
      {
        return 0;
      }

      public boolean isStreamed(String url, int flags)
      {
        return true;
      }

      public int open(String url, int flags)
      {
        return 0;
      }

      public int read(byte[] buf, int size)
      {
        // interrupt the thread
        Thread.currentThread().interrupt();
        throw new RuntimeException("doh");
      }

      public long seek(long offset, int whence)
      {
        return -1;
      }

      public int write(byte[] buf, int size)
      {
        return size;
      }
    };
    
    assertTrue(!Thread.currentThread().isInterrupted());
    try {
      container.open(handler,
          IContainer.Type.READ, null, true, false);
      fail("should not get here");
    } catch (RuntimeException e) {
      // ignore and make sure we're interrupted below.
    }
    assertTrue("thread interrupt not preserved from open",
        Thread.currentThread().isInterrupted());
  }
}

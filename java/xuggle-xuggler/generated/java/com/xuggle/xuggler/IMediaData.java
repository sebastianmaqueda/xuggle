/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 1.3.37
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.xuggle.xuggler;
import com.xuggle.ferry.*;
/**
 * The parent class of all media objects than can be gotten from an 
 * {@link IStream}.  
 */
public class IMediaData extends RefCounted {
  // JNIHelper.swg: Start generated code
  // >>>>>>>>>>>>>>>>>>>>>>>>>>>
  /**
   * This method is only here to use some references and remove
   * a Eclipse compiler warning.
   */
  @SuppressWarnings("unused")
  private void noop()
  {
    IBuffer.make(null, 1);
  }
   
  private volatile long swigCPtr;

  protected IMediaData(long cPtr, boolean cMemoryOwn) {
    super(XugglerJNI.SWIGIMediaDataUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }
  
  /**
   * Internal Only.  Not part of public API.
   *
   * Get the raw value of the native object that obj is proxying for.
   *   
   * @param obj The java proxy object for a native object.
   * @return The raw pointer obj is proxying for.
   */
  public static long getCPtr(IMediaData obj) {
    if (obj == null) return 0;
    return obj.getMyCPtr();
  }

  /**
   * Internal Only.  Not part of public API.
   *
   * Get the raw value of the native object that we're proxying for.
   *   
   * @return The raw pointer we're proxying for.
   */  
  public long getMyCPtr() {
    if (swigCPtr == 0) throw new IllegalStateException("underlying native object already deleted");
    return swigCPtr;
  }
  
  /**
   * Create a new IMediaData object that is actually referring to the
   * exact same underlying native object.
   *
   * This method increases the ref count of the underlying Native object.
   *
   * @return the new Java object.
   */
  public IMediaData copyReference() {
    if (swigCPtr == 0)
      return null;
    else
    {
      // acquire before making copy to avoid memory allocator being
      // overridden
      IMediaData retval = null;
      this.acquire();
      try {
         retval = new IMediaData(swigCPtr, false);
      } catch (Throwable t) {
        this.release();
        throw new RuntimeException(t);
      }
      return retval;
    }
  }

  /**
   * Compares two values, returning true if the underlying objects in native code are the same object.
   *
   * That means you can have two different Java objects, but when you do a comparison, you'll find out
   * they are the EXACT same object.
   *
   * @return True if the underlying native object is the same.  False otherwise.
   */
  public boolean equals(Object obj) {
    boolean equal = false;
    if (obj instanceof IMediaData)
      equal = (((IMediaData)obj).swigCPtr == this.swigCPtr);
    return equal;
  }
  
  /**
   * Get a hashable value for this object.
   *
   * @return the hashable value.
   */
  public int hashCode() {
     return (int)swigCPtr;
  }
  
  // <<<<<<<<<<<<<<<<<<<<<<<<<<<
  // JNIHelper.swg: End generated code
  /**
   * Releases any underlying native memory and marks this object
   * as invalid.
   * <p>
   * Normally Ferry manages when to release native memory.
   * </p>
   * <p>
   * In the unlikely event you want to control EXACTLY when a native 
   * object is released, each Xuggler object has a {@link #delete()}
   * method that you can use. Once you call {@link #delete()},
   * you must ENSURE your object is never referenced again from
   * that Java object -- Ferry tries to help you avoid crashes if you
   * accidentally use an object after deletion but on this but we
   * cannot offer 100% protection (specifically if another thread
   *  is accessing that object EXACTLY when you {@link #delete()} it). 
   * </p>
   */
  

  public synchronized void delete() {
    if(swigCPtr != 0 && swigCMemOwn) {
      swigCMemOwn = false;
      throw new UnsupportedOperationException("C++ destructor does not have public access");
    }
    swigCPtr = 0;
    super.delete();
  }


  // used to correct timezone offsets for timestamp format 

  private static final long TIME_OFFSET = -java.util.Calendar.getInstance()
    .getTimeZone().getRawOffset();

  /** The default time stamp format. */
  
  public static final String DEFALUT_TIME_STAMP_FORMAT =
    "%1$tH:%1$tM:%1$tS.%1$tL";

  /**
   * Gets the underlying {@link java.nio.ByteBuffer} for this {@link
   * IMediaData} object.  Users may modify the contents of the
   * ByteBuffer and their changes will be reflected in the underlying
   * memory.  See {@link com.xuggle.ferry.IBuffer} for the warnings
   * associated with the use of raw memory.  The buffer position and
   * mark are initialized to zero, and the limit is initialized to the
   * number of elements in the buffer.
   *
   * <p>
   *
   * This is a convenience method to allow you to avoid getting the
   * IBuffer object that has the actual data.
   *
   * </p>
   *
   * @return The underlying ByteBuffer
   */

  public java.nio.ByteBuffer getByteBuffer() {
    com.xuggle.ferry.IBuffer buf = this.getData();
    if (buf == null)
      return null;
    java.nio.ByteBuffer byteBuffer = buf.getByteBuffer(0, this.getSize());
    if (byteBuffer != null)
    {
      byteBuffer.position(0);
      byteBuffer.mark();
      byteBuffer.limit(this.getSize());
    }
    return byteBuffer;
  }

  /**
   * Get a string representation of the time stamp for this {@link
   * IMediaData}.  The time is formatted as: <b>HH:MM:SS.ms</b>
   *
   * @return the printable string form of the time stamp of this media
   *
   * @see #getFormattedTimeStamp(String)
   * @see #DEFALUT_TIME_STAMP_FORMAT
   */

  public String getFormattedTimeStamp()
  {
    return getFormattedTimeStamp(DEFALUT_TIME_STAMP_FORMAT);
  }

  /**
   * Get a string representation of the time stamp for this {@link
   * IMediaData}.  The format of the resulting string is specified by
   * the format parameter.  See {@link java.util.Formatter} for 
   * details on how to specify formats, however a good place to start
   * is with the following format: <b>%1$tH:%1$tM:%1$tS.%1$tL</b>
   *
   * @param format the format for the time stamp string
   *
   * @return the printable string form of the timestamp
   * 
   * @see #getFormattedTimeStamp()
   * @see #DEFALUT_TIME_STAMP_FORMAT
   * @see java.util.Formatter
   */

  public String getFormattedTimeStamp(String format)
  {
    java.util.Formatter formatter = new java.util.Formatter();
    return formatter.format(format,
      (long)(getTimeStamp() * getTimeBase().getDouble() * 1000) +
      TIME_OFFSET).toString();
  }

/**
 * Get the time stamp of this object in getTimeBase() units.  
 * @return	the time stamp  
 */
  public long getTimeStamp() {
    return XugglerJNI.IMediaData_getTimeStamp(swigCPtr, this);
  }

/**
 * Set the time stamp for this object in getTimeBase() units.  
 * @param	aTimeStamp The time stamp  
 */
  public void setTimeStamp(long aTimeStamp) {
    XugglerJNI.IMediaData_setTimeStamp(swigCPtr, this, aTimeStamp);
  }

/**
 * Get the time base that time stamps of this object are represented 
 * in.  
 * Caller must release the returned value.  
 * @return	the time base.  
 */
  public IRational getTimeBase() {
    long cPtr = XugglerJNI.IMediaData_getTimeBase(swigCPtr, this);
    return (cPtr == 0) ? null : new IRational(cPtr, false);
  }

/**
 * Set the time base that time stamps of this object are represented 
 * in.  
 *  
 */
  public void setTimeBase(IRational aBase) {
    XugglerJNI.IMediaData_setTimeBase(swigCPtr, this, IRational.getCPtr(aBase), aBase);
  }

/**
 * Get any underlying raw data available for this object.  
 * @return	The raw data, or null if not accessible.  
 */
  public IBuffer getData() {
    long cPtr = XugglerJNI.IMediaData_getData(swigCPtr, this);
    return (cPtr == 0) ? null : new IBuffer(cPtr, false);
  }

/**
 * Get the size in bytes of the raw data available for this object. 
 *  
 * @return	the size in bytes, or -1 if it cannot be computed.  
 */
  public int getSize() {
    return XugglerJNI.IMediaData_getSize(swigCPtr, this);
  }

/**
 * Is this object a key object? i.e. it can be interpreted without needing 
 * any other media objects  
 * @return	true if it's a key, false if not  
 */
  public boolean isKey() {
    return XugglerJNI.IMediaData_isKey(swigCPtr, this);
  }

}

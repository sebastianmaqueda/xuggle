/*
 * Copyright (c) 2008-2009 by Xuggle Inc. All rights reserved.
 *
 * It is REQUESTED BUT NOT REQUIRED if you use this library, that you let 
 * us know by sending e-mail to info@xuggle.com telling us briefly how you're
 * using the library and what you like or don't like about it.
 *
 * This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later
 * version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
#ifndef ICONTAINER_H_
#define ICONTAINER_H_

#include <com/xuggle/ferry/RefCounted.h>
#include <com/xuggle/xuggler/Xuggler.h>
#include <com/xuggle/xuggler/IContainerFormat.h>
#include <com/xuggle/xuggler/IStream.h>
#include <com/xuggle/xuggler/IPacket.h>

namespace com { namespace xuggle { namespace xuggler
{
  /**
   * A file (or network data source) that contains one or more {@link IStream}s of
   * audio and video data.
   */
  class VS_API_XUGGLER IContainer : public com::xuggle::ferry::RefCounted
  {
  public:
    /**
     * The different types of Containers we support.  A container
     * may only be opened in a uni-directional mode.
     */
    typedef enum {
      READ,
      WRITE,
    } Type;
    
    /**
     * Set the buffer length we'll suggest to FFMPEG for reading inputs.
     * 
     * If called when a IContainer is open, the call is ignored and -1 is returned.
     * 
     * @param size The suggested buffer size.
     * @return size on success; <0 on error.
     */
    virtual int32_t setInputBufferLength(uint32_t size)=0;
    
    /**
     * Return the input buffer length.
     * 
     * @return The input buffer length we've told FFMPEG to assume.  0 means FFMPEG should choose it's own
     *   size (and it'll probably be 32768).
     */
    virtual uint32_t getInputBufferLength()=0;

    /**
     * Is this container opened?
     * @return true if opened; false if not.
     */
    virtual bool isOpened()=0;
    
    /**
     * Has a header been successfully written?
     * @return true if yes, false if no.
     */
    virtual bool isHeaderWritten()=0;
    
    /**
     * Open this container and make it ready for reading or writing.
     * 
     * The caller must call close() when done, but if not, we'll close
     * it for them later but warn to the logging system.
     * 
     * This just forwards to {@link #open(String, Type, IContainerFormat, boolean, boolean)}
     * passing false for aStreamsCanBeAddedDynamically, and true for aLookForAllStreams.
     * 
     * @param url The resource to open; The format of this string is any
     *   url that FFMPEG supports (including additional protocols if added
     *   through the xuggler.io library).
     * @param type The type of this container.
     * @param pContainerFormat A pointer to a ContainerFormat object specifying
     *   the format of this container, or 0 (NULL) if you want us to guess.
     * 
     * @return >= 0 on success; < 0 on error.
     */
    virtual int32_t open(const char *url, Type type,
        IContainerFormat* pContainerFormat)=0;

    /**
     * Open this container and make it ready for reading or writing, optionally
     * reading as far into the container as necessary to find all streams.
     * 
     * The caller must call close() when done, but if not, we'll close
     * it for them later but warn to the logging system.
     * 
     * @param url The resource to open; The format of this string is any
     *   url that FFMPEG supports (including additional protocols if added
     *   through the xuggler.io library).
     * @param type The type of this container.
     * @param pContainerFormat A pointer to a ContainerFormat object specifying
     *   the format of this container, or 0 (NULL) if you want us to guess.
     * @param aStreamsCanBeAddedDynamically If true, open() will expect that new
     *   streams can be added at any time, even after the format header has been read.
     * @param aLookForAllStreams If true, open() will block until it has ready
     *   enough data to find all streams in a container.  If false, it will only
     *   block to read a header for this container format.
     * 
     * @return >= 0 on success; < 0 on error.
     */
    virtual int32_t open(const char *url, Type type,
        IContainerFormat* pContainerFormat,
        bool aStreamsCanBeAddedDynamically,
        bool aLookForAllStreams)=0;
    
        /**
     * Returns the IContainerFormat object being used for this IContainer,
     * or null if we don't yet know.
     * 
     * @return the IContainerFormat object, or null.
     */
    virtual IContainerFormat *getContainerFormat()=0;
    
    /**
     * Close the container.  open() must have been called first, or
     * else an error is returned.
     * 
     * @return >= 0 on success; < 0 on error.
     */
    virtual int32_t close()=0;
    
    /**
     * Find out the type of this container.
     * 
     * @return The Type of this container.  READ if not yet opened.
     */
    virtual Type getType()=0;
    
    /**
     * The number of streams in this container.
     * 
     * If opened in READ mode we'll query the stream and find out 
     * how many streams are in it.
     * 
     * If opened in WRITE mode, this will return the number of streams
     * the caller has added to date.
     * 
     * @return The number of streams in this container.
     */
    virtual int32_t getNumStreams()=0;
    
    /**
     * Get the stream at the given position.
     * 
     * @return The stream at that position in the container, or null if none there.
     */
    virtual IStream* getStream(uint32_t position)=0;

    /**
     * Creates a new stream in this container and returns it.
     * 
     * @param id A format-dependent id for this stream.
     * 
     * @return A new stream.
     */
    virtual IStream* addNewStream(int32_t id)=0;

    /**
     * Adds a header, if needed, for this container.
     * 
     * Call this AFTER you've added all streams you want to add
     * and before you write the first frame.
     * 
     * @return 0 if successful.  < 0 if not.  Always -1 if this is
     *           a READ container.
     */
    virtual int32_t writeHeader()=0;

    /**
     * Adds a trailer, if needed, for this container.
     * 
     * Call this AFTER you've written all data you're going to write
     * to this container.  You must call #writeHeader() before you call
     * this (and if you don't, we'll warn loudly and not actually write the trailer).
     * 
     * @return 0 if successful.  < 0 if not.  Always -1 if this is
     *           a READ container.
     */
    virtual int32_t writeTrailer()=0;

    /**
     * Reads the next packet into the IPacket.  This method will
     * release any buffers currently help by this packet and allocate
     * new ones (sorry; such is the way FFMPEG works).
     * 
     * @param  packet [In/Out] The packet we read into.
     * 
     * @return # of bytes read if successful, or -1 if not.
     */
    virtual int32_t readNextPacket(IPacket *packet)=0;
    
    /**
     * Writes the contents of the packet to the container.
     * 
     * @param packet [In] The packet to write out.
     * @param forceInterleave [In] If true, then we will make sure all packets
     *   are interleaved by DTS (even across streams in a container).  If false, we won't,
     *   and it's up to the caller to interleave if necessary.
     * 
     * @return # of bytes read if successful, or -1 if not.
     */
    virtual int32_t writePacket(IPacket *packet, bool forceInterleave)=0;

    /**
     * Writes the contents of the packet to the container, but make sure the packets are
     * interleaved.
     * 
     * This means we may have to queue up packets from one stream while waiting for
     * packets from another.
     * 
     * @param packet [In] The packet to write out.
     * 
     * @return # of bytes read if successful, or -1 if not.
     */
    virtual int32_t writePacket(IPacket *packet)=0;
    /**
     * Create a new container object.
     * 
     * @return a new container, or null on error.
     */
    static IContainer* make();
    protected:
      virtual ~IContainer()=0;

  };
}}}
#endif /*ICONTAINER_H_*/
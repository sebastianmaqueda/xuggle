/*
 * Copyright (c) 2008, 2009 by Xuggle Incorporated. All rights reserved.
 * 
 * This file is part of Xuggler.
 * 
 * You can redistribute Xuggler and/or modify it under the terms of the GNU
 * Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * Xuggler is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with Xuggler. If not, see <http://www.gnu.org/licenses/>.
 */

package com.xuggle.mediatool;

import java.awt.image.BufferedImage;

import com.xuggle.mediatool.event.AddStreamEvent;
import com.xuggle.mediatool.event.AudioSamplesEvent;
import com.xuggle.mediatool.event.CloseCoderEvent;
import com.xuggle.mediatool.event.CloseEvent;
import com.xuggle.mediatool.event.FlushEvent;
import com.xuggle.mediatool.event.OpenCoderEvent;
import com.xuggle.mediatool.event.OpenEvent;
import com.xuggle.mediatool.event.ReadPacketEvent;
import com.xuggle.mediatool.event.VideoPictureEvent;
import com.xuggle.mediatool.event.WriteHeaderEvent;
import com.xuggle.mediatool.event.WritePacketEvent;
import com.xuggle.mediatool.event.WriteTrailerEvent;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.IVideoPicture;

/**
 * Defines the events that be generated by an {@link IMediaGenerator}.
 * 
 * <p>
 * 
 * You can use objects that implement this interface to listen to events
 * generated by an {@link IMediaGenerator} object. To do that, create your
 * {@link IMediaListener} object and attach it to an {@link IMediaGenerator} by
 * calling {@link IMediaGenerator#addListener(IMediaListener)}.
 * 
 * </p>
 * <p>
 * 
 * These methods block the calling {@link IMediaGenerator} while they process so try
 * to return quickly. If you have long running actions to perform, use a
 * separate thread.
 * 
 * </p>
 */

public interface IMediaListener
{
  
  /**
   * Called after a video picture has been decoded by a {@link IMediaReader} or
   * encoded by a {@link IMediaWriter}.
   * 
   * @param event An event containing either an {@link IVideoPicture},
   *   a {@link BufferedImage}, or both.
   */

  public void onVideoPicture(VideoPictureEvent event);

  /**
   * Called after audio samples have been decoded or encoded by an
   * {@link IMediaGenerator}
   * @param event An event containing the {@link IAudioSamples}
   *   for this event.
   */

  public void onAudioSamples(AudioSamplesEvent event);

  /**
   * Called after an {@link IMediaGenerator} is opened.
   * @param event A {@link OpenEvent}
   */

  public void onOpen(OpenEvent event);

  /**
   * Called after an {@link IMediaGenerator} is closed.
   * @param event A {@link CloseEvent}
   */

  public void onClose(CloseEvent event);

  /**
   * Called after an stream is added to an {@link IMediaGenerator}. This occurs when
   * a new stream is added (if writing) or encountered by the pipe (if reading).
   * If the stream is not already been opened, then
   * {@link #onOpenCoder(OpenCoderEvent)} will be called at some later
   * point.
   * @param event A {@link AddStreamEvent} event
   */

  public void onAddStream(AddStreamEvent event);

  /**
   * Called after a decoder or encoder is opened by a {@link IMediaGenerator}
   * @param event A {@link OpenCoderEvent}
   */

  public void onOpenCoder(OpenCoderEvent event);

  /**
   * Called after an decoder or encoder is closed by the {@link IMediaGenerator}.
   * @param event A {@link CloseCoderEvent}
   */

  public void onCloseCoder(CloseCoderEvent event);

  /**
   * Called after a {@link com.xuggle.xuggler.IPacket} has been read by a
   * {@link IMediaReader}.
   * @param event A {@link ReadPacketEvent}.  This {@link IPacket}
   *        in this event is only valid for
   *        the duration of this call. If you need to use the data after this
   *        call has returned, you must either copy the data in this call, or
   *        use {@link IPacket#copyReference()} to create a new object
   *        with a reference you can own.
   */

  public void onReadPacket(ReadPacketEvent event);

  /**
   * Called after a {@link com.xuggle.xuggler.IPacket} has been written by a
   * {@link IMediaWriter}.
   * @param event A {@link WritePacketEvent}.  The {@link IPacket}
   * in this event is only valid
   *        for the duration of this call. If you need to use the data after
   *        this call has returned, you must either copy the data in this call,
   *        or use {@link IPacket#copyReference()} to create a new object
   *        with a reference you can own.
   */

  public void onWritePacket(WritePacketEvent event);

  /**
   * Called after a {@link IMediaWriter} writes the header.
   * @param event A {@link WriteHeaderEvent}
   */

  public void onWriteHeader(WriteHeaderEvent event);

  /**
   * Called after a {@link IMediaWriter} has flushed its buffers.
   * @param event A {@link FlushEvent}
   */

  public void onFlush(FlushEvent event);

  /**
   * Called after a {@link IMediaWriter} writes the trailer.
   * @param event A {@link WriteTrailerEvent}
   */

  public void onWriteTrailer(WriteTrailerEvent event);
}
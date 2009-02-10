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
package com.xuggle.xuggler;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xuggle.xuggler.IAudioResampler;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IContainerFormat;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IRational;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoResampler;

/**
 * An example class that shows how to use the Xuggler library to open, decode, re-sample, encode and write media files.
 * <p>
 * This method is called by the {@link Xuggler} class to do all the heavy lifting.
 * </p><p>
 * Read <a href="{@docRoot}/src-html/com/xuggle/xuggler/Converter.html">the Converter.java source</a>
 * for a good example of a program exercising this library.
 * </p>
 * 
 * @author aclarke
 *
 */
public class Converter
{
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  /**
   * A container we'll use to read data from.
   */
  private IContainer mIContainer = null;
  /**
   * A container we'll use to write data from.
   */
  private IContainer mOContainer = null;
  
  /**
   * A set of {@link IStream} values for each stream in the input {@link IContainer}.
   */
  private IStream[] mIStreams = null;
  /**
   * A set of {@link IStreamCoder} objects we'll use to decode audio and video.
   */
  private IStreamCoder[] mICoders = null;

  /**
   * A set of {@link IStream} objects for each stream we'll output to the output {@link IContainer}.
   */
  private IStream[] mOStreams = null;
  /**
   * A set of {@link IStreamCoder} objects we'll use to encode audio and video.
   */
  private IStreamCoder[] mOCoders = null;

  /**
   * A set of {@link IVideoPicture} objects that we'll use to hold decoded video data.
   */
  private IVideoPicture[] mIVideoPictures = null;
  /**
   * A set of {@link IVideoPicture} objects we'll use to hold potentially-resampled video data before we encode it.
   */
  private IVideoPicture[] mOVideoPictures = null;
  
  /**
   * A set of {@link IAudioSamples} objects we'll use to hold decoded audio data.
   */
  private IAudioSamples[] mISamples = null;
  /**
   * A set of {@link IAudioSamples} objects we'll use to hold potentially-resampled audio data before we encode it.
   */
  private IAudioSamples[] mOSamples = null;
  
  /**
   * A set of {@link IAudioResampler} objects (one for each stream) we'll use to resample audio if needed.
   */
  private IAudioResampler[] mASamplers = null;
  /**
   * A set of {@link IVideoResampler} objects (one for each stream) we'll use to resample video if needed.
   */
  private IVideoResampler[] mVSamplers = null;
  

  /**
   * Define all the command line options this program can take.
   * @return The set of accepted options.
   */
  public Options defineOptions()
  {
    Options options = new Options();

    Option help = new Option( "help", "print this message");

    /*
     * Output container options.
     */
    OptionBuilder.withArgName("container-format");
    OptionBuilder.hasArg(true);
    OptionBuilder.withDescription("output container format to use (e.g. \"mov\")");
    Option containerFormat = OptionBuilder.create("containerformat");
    
    /*
     * Audio options
     */
    OptionBuilder.withArgName("codec");
    OptionBuilder.hasArg(true);
    OptionBuilder.withDescription("audio codec to encode with (e.g. \"libmp3lame\")");
    Option acodec = OptionBuilder.create("acodec");

    OptionBuilder.withArgName("sample-rate");
    OptionBuilder.hasArg(true);
    OptionBuilder.withDescription("audio sample rate to (up/down) encode with (in hz) (e.g. \"22050\")");
    Option asamplerate = OptionBuilder.create("asamplerate");

    OptionBuilder.withArgName("channels");
    OptionBuilder.hasArg(true);
    OptionBuilder.withDescription("number of audio channels (1 or 2) to encode with (e.g. \"2\")");
    Option achannels = OptionBuilder.create("achannels");

    OptionBuilder.withArgName("abit-rate");
    OptionBuilder.hasArg(true);
    OptionBuilder.withDescription("bit rate to encode audio with (in bps) (e.g. \"60000\")");
    Option abitrate= OptionBuilder.create("abitrate");

    OptionBuilder.withArgName("quality");
    OptionBuilder.hasArg(true);
    OptionBuilder.withDescription("quality setting to use for audio.  0 means same as source; higher numbers are (perversely) lower quality.  Defaults to 0.");
    Option aquality= OptionBuilder.create("aquality");
    
    /*
     * Video options
     */
    OptionBuilder.withArgName("codec");
    OptionBuilder.hasArg(true);
    OptionBuilder.withDescription("video codec to encode with (e.g. \"mpeg4\")");
    Option vcodec = OptionBuilder.create("vcodec");

    OptionBuilder.withArgName("factor");
    OptionBuilder.hasArg(true);
    OptionBuilder.withDescription("scaling factor to scale output video by (e.g. \"0.75\")");
    Option vscaleFactor = OptionBuilder.create("vscalefactor");
    
    OptionBuilder.withArgName("vbitrate");
    OptionBuilder.hasArg(true);
    OptionBuilder.withDescription("bit rate to encode video with (in bps) (e.g. \"60000\")");
    Option vbitrate= OptionBuilder.create("vbitrate");

    OptionBuilder.withArgName("vbitratetolerance");
    OptionBuilder.hasArg(true);
    OptionBuilder.withDescription("bit rate tolerance the bitstream is allowed to diverge from the reference (in bits) (e.g. \"1200000\")");
    Option vbitratetolerance= OptionBuilder.create("vbitratetolerance");


    OptionBuilder.withArgName("quality");
    OptionBuilder.hasArg(true);
    OptionBuilder.withDescription("quality setting to use for video.  0 means same as source; higher numbers are (perversely) lower quality.  Defaults to 0.");
    Option vquality= OptionBuilder.create("vquality");
    
    options.addOption(help);
    options.addOption(containerFormat);
    options.addOption(acodec);
    options.addOption(asamplerate);
    options.addOption(achannels);
    options.addOption(abitrate);
    options.addOption(aquality);
    options.addOption(vcodec);
    options.addOption(vscaleFactor);
    options.addOption(vbitrate);
    options.addOption(vbitratetolerance);
    options.addOption(vquality);

    return options;
  }

  /**
   * Given a set of arguments passed into this object, return back a parsed command line.
   * @param opt A set of options as defined by {@link #defineOptions()}.
   * @param args A set of command line arguments passed into this class.
   * @return A parsed command line.
   * @throws ParseException If there is an error in the command line.
   */
  public CommandLine parseOptions(Options opt, String[] args)
    throws ParseException
  {
    CommandLine cmdLine = null;
    
    CommandLineParser parser = new GnuParser();
    
    cmdLine = parser.parse(opt,args);

    if (cmdLine.hasOption("help"))
    {
      HelpFormatter help = new HelpFormatter();
      help.printHelp("Xuggler [options] input_url output_url", opt);
      System.exit(1);
    }
    // Make sure we have only two left over args
    if (cmdLine.getArgs().length != 2)
      throw new ParseException("missing input or output url");

    return cmdLine;
  }

  /**
   * Get an integer value from a command line argument.
   * @param cmdLine A parsed command line (as returned from {@link #parseOptions(Options, String[])}
   * @param key The key for an option you want.
   * @param defaultVal The default value you want set if the key is not present in cmdLine.
   * @return The value for the key in the cmdLine, or defaultVal if it's not there.
   */
  private int getIntOptionValue(CommandLine cmdLine, String key,
      int defaultVal)
  {
    int retval = defaultVal;
    String optValue = cmdLine.getOptionValue(key);
   
    if (optValue != null)
    {
      try {
        retval = Integer.parseInt(optValue);
      } catch (Exception ex)
      {
        log.warn(
            "Option \"{}\" value \"{}\" cannot be converted to integer; using {} instead",
            new Object[] { key, optValue, defaultVal }); 
      }
    }
    return retval;
  }

  /**
   * Get a double value from a command line argument.
   * @param cmdLine A parsed command line (as returned from {@link #parseOptions(Options, String[])}
   * @param key The key for an option you want.
   * @param defaultVal The default value you want set if the key is not present in cmdLine.
   * @return The value for the key in the cmdLine, or defaultVal if it's not there.
   */
  private double getDoubleOptionValue(CommandLine cmdLine, String key,
      double defaultVal)
  {
    double retval = defaultVal;
    String optValue = cmdLine.getOptionValue(key);
   
    if (optValue != null)
    {
      try {
        retval = Double.parseDouble(optValue);
      } catch (Exception ex)
      {
        log.warn(
            "Option \"{}\" value \"{}\" cannot be converted to double; using {} instead",
            new Object[] { key, optValue, defaultVal }); 
      }
    }
    return retval;
  }

  /**
   * Open an initialize all Xuggler objects needed to encode and decode a video file.
   * @param cmdLine A command line (as returned from {@link #parseOptions(Options, String[])}) that 
   *   specifies what files we want to process and how to process them.
   * @return Number of streams in the input file, or <= 0 on error.
   */

  int setupStreams(CommandLine cmdLine)
  {
    String inputURL = cmdLine.getArgs()[0];
    String outputURL = cmdLine.getArgs()[1];
    
    String acodec = cmdLine.getOptionValue("acodec");
    String vcodec = cmdLine.getOptionValue("vcodec");
    String containerFormat = cmdLine.getOptionValue("containerformat");
    int aquality = getIntOptionValue(cmdLine, "aquality", 0);
    
    int sampleRate = getIntOptionValue(cmdLine, "asamplerate", 0);
    int channels = getIntOptionValue(cmdLine, "achannels", 0);
    int abitrate = getIntOptionValue(cmdLine, "abitrate", 0);
    int vbitrate = getIntOptionValue(cmdLine, "vbitrate", 0);
    int vbitratetolerance = getIntOptionValue(cmdLine, "vbitratetolerance", 0);
    int vquality = getIntOptionValue(cmdLine, "vquality", 0);
    double vscaleFactor = getDoubleOptionValue(cmdLine, "vscalefactor", 1.0);
    
    // Should have everything now!
    int retval = 0;
    
    /**
     * Create one container for input, and one for output.
     */
    mIContainer = IContainer.make();
    mOContainer = IContainer.make();
    IContainerFormat oFmt = null;
    
    /**
     * Open the input container for Reading.
     */
    retval = mIContainer.open(inputURL, IContainer.Type.READ, null);
    if (retval < 0)
      throw new RuntimeException("could not open url: " + inputURL);
    
    /**
     * If the user EXPLICITLY asked for a output container format, we'll try to
     * honor their request here.
     */
    if (containerFormat != null)
    {
      oFmt = IContainerFormat.make();
      /**
       * Try to find an output format based on what the user specified, or failing
       * that, based on the outputURL (e.g. if it ends in .flv, we'll guess FLV).
       */
      retval = oFmt.setOutputFormat(containerFormat, outputURL, null);
      if (retval < 0)
        throw new RuntimeException("could not find output container format: " + containerFormat);
    }
    /**
     * Open the output container for writing.  If oFmt is null, we are telling
     * Xuggler to guess the output container format based on the outputURL.
     */
    retval = mOContainer.open(outputURL, IContainer.Type.WRITE, oFmt);
    if (retval < 0)
      throw new RuntimeException("could not open output url: " + outputURL);
    
    /**
     * Find out how many streams are there in the input container?  For example,
     * most FLV files will have 2 -- 1 audio stream and 1 video stream.
     */
    int numStreams = mIContainer.getNumStreams();
    if (numStreams <= 0)
      throw new RuntimeException("not streams in input url: " + inputURL);
    
    /**
     * Here we create IStream, IStreamCoders and other objects for each
     * input stream.
     * 
     * We make parallel objects for each output stream as well.
     */
    mIStreams = new IStream[numStreams];
    mICoders = new IStreamCoder[numStreams];
    mOStreams = new IStream[numStreams];
    mOCoders = new IStreamCoder[numStreams];
    mASamplers = new IAudioResampler[numStreams];
    mVSamplers = new IVideoResampler[numStreams];
    mIVideoPictures = new IVideoPicture[numStreams];
    mOVideoPictures = new IVideoPicture[numStreams];
    mISamples = new IAudioSamples[numStreams];
    mOSamples = new IAudioSamples[numStreams];

    /**
     * Now let's go through the input streams one by one and
     * explicitly set up our contexts.
     */
    for (int i = 0; i < numStreams; i++)
    {
      /**
       * Get the IStream for this input stream.
       */
      IStream is = mIContainer.getStream(i);
      /**
       * And get the input stream coder.  Xuggler will set up all sorts
       * of defaults on this StreamCoder for you (such as the audio sample rate)
       * when you open it.  
       * 
       * You can create IStreamCoders yourself using IStreamCoder#make(IStreamCoder.Direction),
       * but then you have to set all parameters yourself.
       */
      IStreamCoder ic = is.getStreamCoder();
      
      /**
       * Find out what Codec Xuggler guessed the input stream was encoded with.
       */
      ICodec.Type cType = ic.getCodecType();
      
      mIStreams[i] = is;
      mICoders[i] = ic;
      mOStreams[i] = null;
      mOCoders[i] = null;
      mASamplers[i] = null;
      mVSamplers[i] = null;
      mIVideoPictures[i] = null;
      mOVideoPictures[i] = null;
      mISamples[i] = null;
      mOSamples[i] = null;
      
      if (cType == ICodec.Type.CODEC_TYPE_AUDIO)
      {
        /**
         * So it looks like this stream as an audio stream.  Now we add
         * an audio stream to the output container that we will use
         * to encode our resampled audio.
         */
        IStream os=mOContainer.addNewStream(i);
        
        /**
         * And we ask the IStream for an appropriately configured IStreamCoder for output.
         * 
         * Unfortunately you still need to specify a lot of things for outputting
         * (because we can't really guess what you want to encode as).
         */
        IStreamCoder oc = os.getStreamCoder();
        
        mOStreams[i] = os;
        mOCoders[i] = oc;
        
        /**
         * First, did the user specify an audio codec?
         */
        if (acodec != null)
        {
          ICodec codec = null;
          /**
           * Looks like they did specify one; let's look it up by name.
           */
          codec = ICodec.findEncodingCodecByName(acodec);
          if (codec == null || codec.getType() != cType)
            throw new RuntimeException("could not find encoder: " + acodec);
          /**
           * Now, tell the output stream coder that it's to use that codec.
           */
          oc.setCodec(codec);
        } else
        {
          /**
           * Looks like the user didn't specify an output coder for audio.
           * 
           * So we ask Xuggler to guess an appropriate output coded based
           * on the URL, container format, and that it's audio.
           */
          ICodec codec = ICodec.guessEncodingCodec(oFmt, null,
              outputURL, null, cType);
          if (codec == null)
            throw new RuntimeException("could not guess "+cType+
                " encoder for: " + outputURL);
          /**
           * Now let's use that.
           */
          oc.setCodec(codec);
        }
        
        /**
         * In general a IStreamCoder encoding audio needs to know:
         *   1) A ICodec to use.
         *   2) The sample rate and number of channels of the audio.
         * Most everything else can be defaulted.
         */

        /**
         * If the user didn't specify a sample rate to encode as,
         * then just use the same sample rate as the input.
         */
        if (sampleRate == 0)
          sampleRate = ic.getSampleRate();
        oc.setSampleRate(sampleRate);
        /**
         * If the user didn't specify a bit rate to encode as,
         * then just use the same bit as the input.
         */
        if (abitrate == 0)
         abitrate = ic.getBitRate();
        oc.setBitRate(abitrate);
        /**
         * If the user didn't specify the number of channels to encode
         * audio as, just assume we're keeping the same number of channels.
         */
        if (channels == 0)
          channels = ic.getChannels();
        oc.setChannels(channels);
        
        /**
         * And set the quality (which defaults to 0, or highest, if the user doesn't tell us one).
         */
        oc.setGlobalQuality(aquality);
        
        /**
         * Now check if our output channels or sample rate differ
         * from our input channels or sample rate.
         * 
         * If they do, we're going to need to resample the input audio
         * to be in the right format to output.
         */
        if (oc.getChannels() != ic.getChannels() ||
            oc.getSampleRate() != ic.getSampleRate())
        {
          /**
           * Create an audio resampler to do that job.
           */
          mASamplers[i] = IAudioResampler.make(
              oc.getChannels(), ic.getChannels(),
              oc.getSampleRate(), ic.getSampleRate());
          if (mASamplers[i] == null)
          {
            throw new RuntimeException("could not open audio resampler for stream: " + i);
          }
        } else {
          mASamplers[i] = null;
        }
        /**
         * Finally, create some buffers for the input and output audio themselves.
         * 
         * We'll use these repeated during the #run(CommandLine) method.
         */
        mISamples[i] = IAudioSamples.make(1024, ic.getChannels());
        mOSamples[i] = IAudioSamples.make(1024, oc.getChannels());
      }
      else if (cType == ICodec.Type.CODEC_TYPE_VIDEO)
      {
        /**
         * If you're reading these commends, this does the same thing as the above
         * branch, only for video.  I'm going to assume you read those commends and
         * will only document something substantially different here.
         */
        IStream os=mOContainer.addNewStream(i);
        IStreamCoder oc = os.getStreamCoder();
        
        mOStreams[i] = os;
        mOCoders[i] = oc;
        
        if (vcodec != null)
        {
          ICodec codec = null;
          codec = ICodec.findEncodingCodecByName(vcodec);
          if (codec == null || codec.getType() != cType)
            throw new RuntimeException("could not find encoder: " + vcodec);
          oc.setCodec(codec);
          oc.setGlobalQuality(0);
        } else
        {
          ICodec codec = ICodec.guessEncodingCodec(oFmt, null,
              outputURL, null, cType);
          if (codec == null)
            throw new RuntimeException("could not guess "+cType+
                " encoder for: " + outputURL);
          
          oc.setCodec(codec);
        }
        
        /**
         * In general a IStreamCoder encoding video needs to know:
         *   1) A ICodec to use.
         *   2) The Width and Height of the Video
         *   3) The pixel format (e.g. IPixelFormat.Type#YUV420P) of the video data.
         * Most everything else can be defaulted.
         */
        if (vbitrate == 0)
          vbitrate = ic.getBitRate();
         oc.setBitRate(vbitrate);
         if (vbitratetolerance > 0)
           oc.setBitRateTolerance(vbitratetolerance);
         
         int oWidth = ic.getWidth();
         int oHeight = ic.getHeight();
         
         if (oHeight <= 0 || oWidth <= 0)
           throw new RuntimeException("could not find width or height in url: " + inputURL);

         /**
          * For this program we don't allow the user to specify the pixel format type;
          * we force the output to be the same as the input.
          */
         oc.setPixelType(ic.getPixelType());

         if (vscaleFactor != 1.0)
         {
           /**
            * In this case, it looks like the output video requires rescaling,
            * so we create a IVideoResampler to do that dirty work. 
            */
           oWidth = (int)(oWidth * vscaleFactor);
           oHeight = (int)(oHeight * vscaleFactor);
           
           mVSamplers[i] = IVideoResampler.make(
               oWidth, oHeight, oc.getPixelType(),
               ic.getWidth(), ic.getHeight(), ic.getPixelType());
           if (mVSamplers[i] == null)
           {
             throw new RuntimeException("This version of Xuggler does not support video resampling " + i);
           }
         } else {
           mVSamplers[i] = null;
         }
         oc.setHeight(oHeight);
         oc.setWidth(oWidth);

         oc.setFlag(IStreamCoder.Flags.FLAG_QSCALE, true);
         oc.setGlobalQuality(vquality);
         
         /**
          * TimeBases are important, especially for Video.  In general Audio encoders
          * will assume that any new audio happens IMMEDIATELY after any prior
          * audio finishes playing.  But for video, we need to make sure it's
          * being output at the right rate.
          * 
          * In this case we make sure we set the same time base as the input,
          * and then we don't change the time stamps of any IVideoPictures.
          * 
          * But take my word that time stamps are tricky, and this only touches
          * the envelope.  The good news is, it's easier in Xuggler than some
          * other systems.
          */
         IRational num = null;
         num = ic.getFrameRate();
         oc.setFrameRate(num);
         oc.setTimeBase(IRational.make(num.getDenominator(), num.getNumerator()));
         num = null;
         
         /**
          * And allocate buffers for us to store decoded and resample video pictures.
          */
         mIVideoPictures[i] = IVideoPicture.make(ic.getPixelType(), ic.getWidth(),
             ic.getHeight());
         mOVideoPictures[i] = IVideoPicture.make(oc.getPixelType(), oc.getWidth(),
             oc.getHeight());
      }
      else
      {
        log.warn("Ignoring input stream {} of type {}", i, cType);
      }
      
      /**
       * Now, once you've set up all the parameters on the StreamCoder, you must
       * open() them so they can do work.
       * 
       * They will return an error if not configured correctly, so we check for
       * that here.
       */
      if (mOCoders[i] != null)
      {
        retval = mOCoders[i].open();
        if (retval < 0)
          throw new RuntimeException("could not open output encoder for stream: " + i);
        retval = mICoders[i].open();
        if (retval < 0)
          throw new RuntimeException("could not open input decoder for stream: " + i);
      }
    }
    
    /**
     * Pretty much every output container format has a header they need written, so
     * we do that here.
     * 
     * You must configure your output IStreams correctly before writing a header,
     * and few formats deal nicely with key parameters changing (e.g. video width)
     * after a header is written.
     */
    retval = mOContainer.writeHeader();
    if (retval < 0)
      throw new RuntimeException("Could not write header for: " + outputURL);
    
    /**
     * That's it with setup; we're good to begin!
     */
    return numStreams;
  }
  
  /**
   * Close and release all resources we used to run this program.
   */
  void closeStreams()
  {
    int numStreams = 0;
    int i = 0;
    
    /**
     * We do a nice clean-up here to show you how you should do it.
     * 
     * That said, Xuggler goes to great pains to clean up after you if you
     * forget to release things.  But still, you should be a good boy or giral and
     * clean up yourself.
     */
    numStreams = mIContainer.getNumStreams();
    for (i = 0; i < numStreams; i++)
    {
      /**
       * Some video coders (e.g. MP3) will often "read-ahead" in a stream and keep
       * extra data around to get efficient compression.  But they need some way to know
       * they're never going to get more data.  The convention for that case is to pass
       * null for the IMediaData (e.g. IAudioSamples or IVideoPicture) in encodeAudio(...)
       * or encodeVideo(...) once before closing the coder.
       * 
       * In that case, the IStreamCoder will flush all data.
       */
      if (mOCoders[i] != null) {
        IPacket oPacket = IPacket.make();
        if (mOCoders[i].getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO)
          mOCoders[i].encodeAudio(oPacket, null, 0);
        else
          mOCoders[i].encodeVideo(oPacket, null, 0);
        if (oPacket.isComplete())
          mOContainer.writePacket(oPacket);

        /**
         * And close the input coder to tell Xuggler it can release all native memory.
         */
        mOCoders[i].close();
      }
      mOCoders[i] = null;
      if (mICoders[i] != null)
        /**
         * Close the input coder to tell Xuggler it can release all native memory.
         */
        mICoders[i].close();
      mICoders[i] = null;
    }
    /**
     * Some container formats require a trailer to be written to avoid a corrupt files.
     * 
     * Others, such as the FLV container muxer, will take a writeTrailer() call to tell
     * it to seek() back to the start of the output file and write the (now known) duration
     * into the Meta Data.
     * 
     *  So trailers are required.  In general if a format is a streaming format, then the
     *  writeTrailer() will never seek backwards.
     */
    mOContainer.writeTrailer();
    
    /**
     * Tell Xuggler it can close the output file, write all data, and free all relevant memory.
     */
    mOContainer.close();
    /**
     * And do the same with the input file.
     */
    mIContainer.close();

    /**
     * Technically setting everything to null here doesn't do anything but tell
     * Java it can collect the memory it used.
     * 
     * The interesting thing to note here is that if you forget to close()
     * a Xuggler object, but also loose all references to it from Java, you
     * won't leak the native memory.  Instead, we'll clean up after you,
     * but we'll complain LOUDLY in your logs, so you really don't want to do that.
     */
    mOContainer = null;
    mIContainer = null;
    mISamples = null;
    mOSamples = null;
    mIVideoPictures = null;
    mOVideoPictures = null;
    mOCoders = null;
    mICoders = null;
    mASamplers = null;
    mVSamplers = null;
    

  }
  
  /** Allow child class to override this method to alter the audio frame
   * before it is rencoded and written.  In this implementation the
   * audio frame is passed through unmodified.
   *
   * @param audio the source audio frame to be modified
   *
   * @return the modified audio frame
   */

  protected IAudioSamples alterAudioFrame(IAudioSamples audioFrame)
  {
    return audioFrame;
  }

  /** Allow child class to override this method to alter the video frame
   * before it is rencoded and written.  In this implementation the
   * video frame is passed through unmodified.
   *
   * @param videoFrame the source video frame to be modified
   *
   * @return the modified video frame
   */

  protected IVideoPicture alterVideoFrame(IVideoPicture videoFrame)
  {
    return videoFrame;
  }

  /**
   * Takes a given command line and decodes the input file, and encodes with
   * new parameters to the output file. 
   * @param cmdLine A command line returned from {@link #parseOptions(Options, String[])}.
   */
  public void run(CommandLine cmdLine)
  {
    /**
     * Setup all our input and outputs
     */
    setupStreams(cmdLine);

    /**
     * Create packet buffers for reading data from and writing data to the conatiners.
     */
    IPacket iPacket = IPacket.make();
    IPacket oPacket = IPacket.make();

    /**
     * Keep some "pointers' we'll use for the audio we're working with.
     */
    IAudioSamples inSamples = null;
    IAudioSamples outSamples = null;
    IAudioSamples reSamples = null;

    int retval = 0;
    
    /**
     * And keep some convenience pointers for the specific stream we're working on
     * for a packet.
     */
    IStreamCoder ic = null;
    IStreamCoder oc = null;
    IAudioResampler as = null;
    IVideoResampler vs = null;
    IVideoPicture inFrame = null;
    IVideoPicture reFrame = null;
    
    /**
     * Now, we've already opened the files in #setupStreams(CommandLine).  We
     * just keep reading packets from it until the IContainer returns <0
     */
    while (mIContainer.readNextPacket(iPacket) == 0)
    {
      /**
       * Find out which stream this packet belongs to.
       */
      int i = iPacket.getStreamIndex();
      int offset = 0;
      
      /**
       * And look up the appropriate objects that are working on that stream.
       */
      ic = mICoders[i];
      oc = mOCoders[i];
      as = mASamplers[i];
      vs = mVSamplers[i];
      inFrame = mIVideoPictures[i];
      reFrame = mOVideoPictures[i];
      inSamples = mISamples[i];
      reSamples = mOSamples[i];
      
      /**
       * Find out if the stream is audio or video.
       */
      ICodec.Type cType = ic.getCodecType();
      
      if (cType == ICodec.Type.CODEC_TYPE_AUDIO)
      {
        /**
         * Decoding audio works by taking the data in the packet, and eating
         * chunks from it to create decoded raw data.
         * 
         * However, there may be more data in a packet than is needed
         * to get one set of samples (or less), so you need to iterate
         * through the byts to get that data.
         * 
         * The following loop is the standard way of doing that.
         */
        while (offset < iPacket.getSize())
        {
          retval = ic.decodeAudio(inSamples, iPacket, offset);
          if (retval <= 0)
            throw new RuntimeException("could not decode audio");
          
          /**
           * If not an error, the decodeAudio returns the number of bytes
           * it consumed.  We use that so the next time around the loop we
           * get new data.
           */
          offset += retval;
          int numSamplesConsumed = 0;
          /**
           * If as is not null then we know a resample was needed, so we do that resample now.
           */
          if (as != null && inSamples.getNumSamples() >0)
          {
            retval = as.resample(reSamples, inSamples,
                inSamples.getNumSamples());

            outSamples = reSamples;
          }
          else
          {
            outSamples = inSamples;
          }

          /**
           * Include call a hook to derivied classes to allow them to
           * alter the audio frame.
           */

          outSamples = alterAudioFrame(outSamples);
          
          /**
           * Now that we've resampled, it's time to encode the audio.
           * 
           * This workflow is similar to decoding; you may have more, less or just enough
           * audio samples available to encode a packet.  But you must iterate through.
           * 
           * Unfortunately (don't ask why) there is a slight difference between
           * encodeAudio and decodeAudio; encodeAudio returns the number of samples consumed,
           * NOT the number of bytes.  This can be confusing, and we encourage you to read
           * the IAudioSamples documentation to find out what the difference is.
           * 
           * But in any case, the following loop encodes the samples we have into packets.
           */
          while (numSamplesConsumed < outSamples.getNumSamples()) {
            retval = oc.encodeAudio(oPacket, outSamples, numSamplesConsumed);
            if (retval <= 0)
              throw new RuntimeException("Could not encode any audio: " + retval);
            /**
             * Increment the number of samples consumed, so that the next time through this
             * loop we encode new audio 
             */
            numSamplesConsumed += retval;
            if (oPacket.isComplete())
            {
              /**
               * If we got a complete packet out of the encoder, then go ahead and write it
               * to the container.
               */
              retval = mOContainer.writePacket(oPacket);
              if (retval < 0)
                throw new RuntimeException("could not write output packet");
            }
          }
        }
        
      }
      else if (cType == ICodec.Type.CODEC_TYPE_VIDEO)
      {
        /**
         * This encoding workflow is pretty much the same as the for the audio above.
         * 
         * The only major delta is that encodeVideo() will always consume one frame (whereas
         * encodeAudio() might only consume some samples in an IAudioSamples buffer); it might
         * not be able to output a packet yet, but you can assume that you it consumes the entire frame.
         */
        IVideoPicture outFrame = null;
        while (offset < iPacket.getSize()) {
          retval = ic.decodeVideo(inFrame, iPacket, offset);
          if (retval <= 0)
            throw new RuntimeException("could not decode any video");
          offset += retval;
          if (inFrame.isComplete())
          {
            if (vs != null)
            {
              retval = vs.resample(reFrame, inFrame);
              if (retval < 0)
                throw new RuntimeException("could not resample video");
              outFrame = reFrame;
            } 
            else 
            {
              outFrame = inFrame;
            }
            
            /**
             * Include call a hook to derivied classes to allow them to
             * alter the audio frame.
             */

            outFrame = alterVideoFrame(outFrame);

            outFrame.setQuality(0);
            retval = oc.encodeVideo(oPacket, outFrame, 0);
            if (retval < 0)
              throw new RuntimeException("could not encode video");
            if (oPacket.isComplete())
            {
              retval = mOContainer.writePacket(oPacket);
              if (retval < 0)
                throw new RuntimeException("could not write video packet");
            }
          }
        }
      }
      else
      {
        /**
         * Just to be complete; there are other types of data that can show up
         * in streams (e.g. SUB TITLE).
         * 
         * Right now we don't support decoding and encoding that data, but youc
         * could still decide to write out the packets if you wanted.
         */
        log.debug("ignoring packet of type: {}", cType);
      }

    }
    
    // and cleanup.
    closeStreams();
  }
}
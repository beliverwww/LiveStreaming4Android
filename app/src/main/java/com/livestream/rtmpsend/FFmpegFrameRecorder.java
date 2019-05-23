package com.livestream.rtmpsend;

import android.os.Environment;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.javacpp.ShortPointer;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder;

import java.io.File;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Map.Entry;

import static org.bytedeco.javacpp.avcodec.AVCodec;
import static org.bytedeco.javacpp.avcodec.AVCodecContext;
import static org.bytedeco.javacpp.avcodec.AVPacket;
import static org.bytedeco.javacpp.avcodec.AVPicture;
import static org.bytedeco.javacpp.avcodec.AV_CODEC_ID_AAC;
import static org.bytedeco.javacpp.avcodec.AV_CODEC_ID_FFV1;
import static org.bytedeco.javacpp.avcodec.AV_CODEC_ID_FLV1;
import static org.bytedeco.javacpp.avcodec.AV_CODEC_ID_H263;
import static org.bytedeco.javacpp.avcodec.AV_CODEC_ID_H264;
import static org.bytedeco.javacpp.avcodec.AV_CODEC_ID_HUFFYUV;
import static org.bytedeco.javacpp.avcodec.AV_CODEC_ID_MPEG4;
import static org.bytedeco.javacpp.avcodec.AV_CODEC_ID_NONE;
import static org.bytedeco.javacpp.avcodec.AV_CODEC_ID_PCM_S16BE;
import static org.bytedeco.javacpp.avcodec.AV_CODEC_ID_PCM_S16LE;
import static org.bytedeco.javacpp.avcodec.AV_CODEC_ID_PCM_U16BE;
import static org.bytedeco.javacpp.avcodec.AV_CODEC_ID_PCM_U16LE;
import static org.bytedeco.javacpp.avcodec.AV_CODEC_ID_PNG;
import static org.bytedeco.javacpp.avcodec.AV_CODEC_ID_RAWVIDEO;
import static org.bytedeco.javacpp.avcodec.AV_PKT_FLAG_KEY;
import static org.bytedeco.javacpp.avcodec.AV_CODEC_CAP_EXPERIMENTAL;
import static org.bytedeco.javacpp.avcodec.AV_CODEC_FLAG_GLOBAL_HEADER;
import static org.bytedeco.javacpp.avcodec.AV_CODEC_FLAG_QSCALE;
//import static org.bytedeco.javacpp.avcodec.CODEC_CAP_EXPERIMENTAL;
//import static org.bytedeco.javacpp.avcodec.CODEC_FLAG_GLOBAL_HEADER;
//import static org.bytedeco.javacpp.avcodec.CODEC_FLAG_QSCALE;
//import static org.bytedeco.javacpp.avcodec.FF_MIN_BUFFER_SIZE;
import static org.bytedeco.javacpp.avcodec.av_init_packet;
import static org.bytedeco.javacpp.avcodec.avcodec_close;
import static org.bytedeco.javacpp.avcodec.avcodec_encode_audio2;
import static org.bytedeco.javacpp.avcodec.avcodec_encode_video2;
import static org.bytedeco.javacpp.avcodec.avcodec_fill_audio_frame;
import static org.bytedeco.javacpp.avcodec.avcodec_find_encoder;
import static org.bytedeco.javacpp.avcodec.avcodec_find_encoder_by_name;
import static org.bytedeco.javacpp.avcodec.avcodec_open2;
import static org.bytedeco.javacpp.avcodec.avpicture_fill;
import static org.bytedeco.javacpp.avcodec.avpicture_get_size;
import static org.bytedeco.javacpp.avformat.AVFMT_GLOBALHEADER;
import static org.bytedeco.javacpp.avformat.AVFMT_NOFILE;
//import static org.bytedeco.javacpp.avformat.AVFMT_RAWPICTURE;
import static org.bytedeco.javacpp.avformat.AVFormatContext;
import static org.bytedeco.javacpp.avformat.AVIOContext;
import static org.bytedeco.javacpp.avformat.AVIO_FLAG_READ_WRITE;
import static org.bytedeco.javacpp.avformat.AVOutputFormat;
import static org.bytedeco.javacpp.avformat.AVStream;
import static org.bytedeco.javacpp.avformat.av_dump_format;
import static org.bytedeco.javacpp.avformat.av_interleaved_write_frame;
import static org.bytedeco.javacpp.avformat.av_register_all;
import static org.bytedeco.javacpp.avformat.av_write_frame;
import static org.bytedeco.javacpp.avformat.av_write_trailer;
import static org.bytedeco.javacpp.avformat.avformat_alloc_output_context2;
import static org.bytedeco.javacpp.avformat.avformat_network_init;
import static org.bytedeco.javacpp.avformat.avformat_new_stream;
import static org.bytedeco.javacpp.avformat.avformat_write_header;
import static org.bytedeco.javacpp.avformat.avio_close;
import static org.bytedeco.javacpp.avformat.avio_open;
import static org.bytedeco.javacpp.avutil.AVDictionary;
import static org.bytedeco.javacpp.avutil.AVFrame;
import static org.bytedeco.javacpp.avutil.AVMEDIA_TYPE_AUDIO;
import static org.bytedeco.javacpp.avutil.AVMEDIA_TYPE_VIDEO;
import static org.bytedeco.javacpp.avutil.AVRational;
import static org.bytedeco.javacpp.avutil.AV_NOPTS_VALUE;
import static org.bytedeco.javacpp.avutil.AV_PIX_FMT_BGR24;
import static org.bytedeco.javacpp.avutil.AV_PIX_FMT_GRAY16BE;
import static org.bytedeco.javacpp.avutil.AV_PIX_FMT_GRAY16LE;
import static org.bytedeco.javacpp.avutil.AV_PIX_FMT_GRAY8;
import static org.bytedeco.javacpp.avutil.AV_PIX_FMT_NONE;
import static org.bytedeco.javacpp.avutil.AV_PIX_FMT_NV21;
import static org.bytedeco.javacpp.avutil.AV_PIX_FMT_RGB32;
import static org.bytedeco.javacpp.avutil.AV_PIX_FMT_RGBA;
import static org.bytedeco.javacpp.avutil.AV_PIX_FMT_YUV420P;
import static org.bytedeco.javacpp.avutil.AV_SAMPLE_FMT_DBL;
import static org.bytedeco.javacpp.avutil.AV_SAMPLE_FMT_DBLP;
import static org.bytedeco.javacpp.avutil.AV_SAMPLE_FMT_FLT;
import static org.bytedeco.javacpp.avutil.AV_SAMPLE_FMT_FLTP;
import static org.bytedeco.javacpp.avutil.AV_SAMPLE_FMT_NONE;
import static org.bytedeco.javacpp.avutil.AV_SAMPLE_FMT_S16;
import static org.bytedeco.javacpp.avutil.AV_SAMPLE_FMT_S16P;
import static org.bytedeco.javacpp.avutil.AV_SAMPLE_FMT_S32;
import static org.bytedeco.javacpp.avutil.AV_SAMPLE_FMT_S32P;
import static org.bytedeco.javacpp.avutil.AV_SAMPLE_FMT_U8;
import static org.bytedeco.javacpp.avutil.AV_SAMPLE_FMT_U8P;
import static org.bytedeco.javacpp.avutil.FF_QP2LAMBDA;
import static org.bytedeco.javacpp.avutil.av_d2q;
import static org.bytedeco.javacpp.avutil.av_dict_free;
import static org.bytedeco.javacpp.avutil.av_dict_set;
import static org.bytedeco.javacpp.avutil.av_dict_set_int;
import static org.bytedeco.javacpp.avutil.av_find_nearest_q_idx;
import static org.bytedeco.javacpp.avutil.av_frame_alloc;
import static org.bytedeco.javacpp.avutil.av_frame_free;
import static org.bytedeco.javacpp.avutil.av_free;
import static org.bytedeco.javacpp.avutil.av_get_bytes_per_sample;
import static org.bytedeco.javacpp.avutil.av_get_default_channel_layout;
import static org.bytedeco.javacpp.avutil.av_inv_q;
import static org.bytedeco.javacpp.avutil.av_malloc;
import static org.bytedeco.javacpp.avutil.av_rescale_q;
import static org.bytedeco.javacpp.avutil.av_sample_fmt_is_planar;
import static org.bytedeco.javacpp.avutil.av_samples_get_buffer_size;
import static org.bytedeco.javacpp.swresample.SwrContext;
import static org.bytedeco.javacpp.swresample.swr_alloc_set_opts;
import static org.bytedeco.javacpp.swresample.swr_convert;
import static org.bytedeco.javacpp.swresample.swr_free;
import static org.bytedeco.javacpp.swresample.swr_init;
import static org.bytedeco.javacpp.swscale.SWS_BILINEAR;
import static org.bytedeco.javacpp.swscale.SwsContext;
import static org.bytedeco.javacpp.swscale.sws_freeContext;
import static org.bytedeco.javacpp.swscale.sws_getCachedContext;
import static org.bytedeco.javacpp.swscale.sws_scale;

public class FFmpegFrameRecorder extends FrameRecorder {

	private static Exception loadingException = null;

	public static void tryLoad() throws Exception {
		if (loadingException != null) {
			throw loadingException;
		}
		else {
			try {
				Loader.load(org.bytedeco.javacpp.avutil.class);
				Loader.load(org.bytedeco.javacpp.swresample.class);
				Loader.load(org.bytedeco.javacpp.avcodec.class);
				Loader.load(org.bytedeco.javacpp.avformat.class);
				Loader.load(org.bytedeco.javacpp.swscale.class);

				/* initialize libavcodec, and register all codecs and formats */
				av_register_all();
				avformat_network_init();
			}
			catch (Throwable t) {
				if (t instanceof Exception) {
					throw loadingException = (Exception)t;
				}
				else {
					throw loadingException = new Exception("Failed to load " + FFmpegFrameRecorder.class, t);
				}
			}
		}
	}

	static {
		try {
			tryLoad();
		}
		catch (Exception ex) {
		}
	}

	public static String filename;
	private AVFrame			picture;
	private AVFrame			tmp_picture;
	private AVFrame			frame;
	private BytePointer		picture_buf;
	private BytePointer		video_outbuf;
	private int				video_outbuf_size;
	private Pointer[]		samples_in;
	private BytePointer[]	samples_out;
	private PointerPointer	samples_in_ptr;
	private PointerPointer	samples_out_ptr;
	private BytePointer		audio_outbuf;
	private int				audio_outbuf_size;
	private int				audio_input_frame_size;
	private AVOutputFormat	oformat;
	private AVFormatContext formatCtx;
	private AVCodec			codecVideo;
	private AVCodec			codecAudio;
	private AVCodecContext	codecCtxVideo;
	private AVCodecContext	codecCtxAudio;
	private AVStream		streamVideo;
	private AVStream		streamAudio;
	private SwsContext img_convert_ctx;
	private SwrContext samples_convert_ctx;
	private int samples_channels, samples_format, samples_rate;
	private AVPacket video_pkt, audio_pkt;
	private int[] got_video_packet, got_audio_packet;

	public int lastFrameIndex = 0;


	public FFmpegFrameRecorder(String filename, int imageWidth, int imageHeight, int audioChannels) {
//		this.filename = filename;
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		this.audioChannels = audioChannels;

		this.pixelFormat = AV_PIX_FMT_NONE;
		this.videoCodec = AV_CODEC_ID_NONE;
		this.videoBitrate = 800000;
		this.frameRate = 30;

		this.sampleFormat = AV_SAMPLE_FMT_NONE;
		this.audioCodec = AV_CODEC_ID_NONE;
		this.audioBitrate = 64000;
		this.sampleRate = 44100;

		this.interleaved = true;

		this.video_pkt = new AVPacket();
		this.audio_pkt = new AVPacket();
	}

	public void release() throws Exception {
		synchronized (org.bytedeco.javacpp.avcodec.class) {
			releaseUnsafe();
		}
	}

	public void releaseUnsafe() throws Exception {
		/* close each codec */
		if (codecCtxVideo != null) {
			avcodec_close(codecCtxVideo);
			codecCtxVideo = null;
		}
		if (codecCtxAudio != null) {
			avcodec_close(codecCtxAudio);
			codecCtxAudio = null;
		}
		if (picture_buf != null) {
			av_free(picture_buf);
			picture_buf = null;
		}
		if (picture != null) {
			av_frame_free(picture);
			picture = null;
		}
		if (tmp_picture != null) {
			av_frame_free(tmp_picture);
			tmp_picture = null;
		}
		if (video_outbuf != null) {
			av_free(video_outbuf);
			video_outbuf = null;
		}
		if (frame != null) {
			av_frame_free(frame);
			frame = null;
		}
		if (samples_out != null) {
			for (int i = 0; i < samples_out.length; i++) {
				av_free(samples_out[i].position(0));
			}
			samples_out = null;
		}
		if (audio_outbuf != null) {
			av_free(audio_outbuf);
			audio_outbuf = null;
		}
		if (streamVideo != null && streamVideo.metadata() != null) {
			av_dict_free(streamVideo.metadata());
			streamVideo.metadata(null);
		}
		if (streamAudio != null && streamAudio.metadata() != null) {
			av_dict_free(streamAudio.metadata());
			streamAudio.metadata(null);
		}
		streamVideo = null;
		streamAudio = null;

		if (formatCtx != null && !formatCtx.isNull()) {
			if ((oformat.flags() & AVFMT_NOFILE) == 0) {
				/* close the output file */
				avio_close(formatCtx.pb());
			}

			/* free the streams */
			int nb_streams = formatCtx.nb_streams();
			for (int i = 0; i < nb_streams; i++) {
				av_free(formatCtx.streams(i).codec());
				av_free(formatCtx.streams(i));
			}

			/* free metadata */
			if (formatCtx.metadata() != null) {
				av_dict_free(formatCtx.metadata());
				formatCtx.metadata(null);
			}

			/* free the stream */
			av_free(formatCtx);
			formatCtx = null;
		}

		if (img_convert_ctx != null) {
			sws_freeContext(img_convert_ctx);
			img_convert_ctx = null;
		}

		if (samples_convert_ctx != null) {
			swr_free(samples_convert_ctx);
			samples_convert_ctx = null;
		}
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		release();
	}

	@Override
	public int getFrameNumber() {
		return picture == null ? super.getFrameNumber() : (int)picture.pts();
	}

	@Override
	public void setFrameNumber(int frameNumber) {
		if (picture == null) {
			super.setFrameNumber(frameNumber);
		}
		else {
			picture.pts(frameNumber);
		}
	}

	// best guess for timestamp in microseconds
	@Override
	public long getTimestamp() {
		long timeStamp = Math.round(getFrameNumber() * 1000000L / getFrameRate());
		return timeStamp;
	}

	@Override
	public void setTimestamp(long timestamp) {
		int frameNumber = (int)Math.round(timestamp * getFrameRate() / 1000000L);
		if (frameNumber <= lastFrameIndex) {
			frameNumber = lastFrameIndex + 1;
		}
		lastFrameIndex = frameNumber;
		setFrameNumber(frameNumber);
	}

	public void start() throws Exception {
		synchronized (org.bytedeco.javacpp.avcodec.class) {
			startUnsafe();
		}
	}

	private static final String MAIN_DIR="FFmpegSample1";

	public static File getMainDir(){
		File file=new File(Environment.getExternalStorageDirectory(),MAIN_DIR);
		if(!file.exists()){
			file.mkdirs();
		}
		return file;
	}

	public void startUnsafe() throws Exception {
		int ret;
		picture = null;
		tmp_picture = null;
		picture_buf = null;
		frame = null;
		video_outbuf = null;
		audio_outbuf = null;
		formatCtx = null;
		codecCtxVideo = null;
		codecCtxAudio = null;
		streamVideo = null;
		streamAudio = null;
		got_video_packet = new int[1];
		got_audio_packet = new int[1];

		/* auto detect the output format from the name. */
		String format_name = format == null || format.length() == 0 ? null : format;
//		filename = getMainDir() + "/ffmpeg.flv";//"rtmp://192.168.1.101/live/test";

//		if ((oformat = av_guess_format(format_name, filename, null)) == null) {
//			int proto = filename.indexOf("://");
//			if (proto > 0) {
//				format_name = filename.substring(0, proto);
//			}
//			if ((oformat = av_guess_format(format_name, filename, null)) == null) {
//				throw new Exception("av_guess_format() error: Could not guess output format for \"" + filename + "\" and " + format + " format.");
//			}
//		}
//		format_name = "flv";//oformat.name().getString();
//
//		/* allocate the output media context */
//		if ((formatCtx = avformat_alloc_context()) == null) {
//			throw new Exception("avformat_alloc_context() error: Could not allocate format context");
//		}
//
//		formatCtx.oformat(oformat);
//		formatCtx.filename().putString(filename);
		formatCtx = new AVFormatContext(null);
		avformat_alloc_output_context2(formatCtx, null, "mpegts", null);
		oformat = formatCtx.oformat();
		/* add the audio and video streams using the format codecs
		   and initialize the codecs */

		if (imageWidth > 0 && imageHeight > 0) {
			if (videoCodec != AV_CODEC_ID_NONE) {
				oformat.video_codec(videoCodec);
			}
			else if ("flv".equals(format_name)) {
				oformat.video_codec(AV_CODEC_ID_FLV1);
			}
			else if ("mp4".equals(format_name)) {
				oformat.video_codec(AV_CODEC_ID_MPEG4);
			}
			else if ("3gp".equals(format_name)) {
				oformat.video_codec(AV_CODEC_ID_H263);
			}
			else if ("avi".equals(format_name)) {
				oformat.video_codec(AV_CODEC_ID_HUFFYUV);
			}

			/* find the video encoder */
			if ((codecVideo = avcodec_find_encoder_by_name(videoCodecName)) == null &&
				(codecVideo = avcodec_find_encoder(oformat.video_codec())) == null) {
				release();
				throw new Exception("avcodec_find_encoder() error: Video codec not found.");
			}
			oformat.video_codec(codecVideo.id());

			AVRational frame_rate = av_d2q(frameRate, 1001000);
			AVRational supported_framerates = codecVideo.supported_framerates();
			if (supported_framerates != null) {
				int idx = av_find_nearest_q_idx(frame_rate, supported_framerates);
				frame_rate = supported_framerates.position(idx);
			}

			/* add a video output stream */
			if ((streamVideo = avformat_new_stream(formatCtx, codecVideo)) == null) {
				release();
				throw new Exception("avformat_new_stream() error: Could not allocate video stream.");
			}
			codecCtxVideo = streamVideo.codec();
			codecCtxVideo.codec_id(oformat.video_codec());
			codecCtxVideo.codec_type(AVMEDIA_TYPE_VIDEO);

			/* put sample parameters */
			codecCtxVideo.bit_rate(videoBitrate);
			/* resolution must be a multiple of two, but round up to 16 as often required */
			codecCtxVideo.width(640);
			codecCtxVideo.height(480);
//			codecCtxVideo.width((imageWidth + 15) / 16 * 16);
//			codecCtxVideo.height(imageHeight);
			if (aspectRatio > 0) {
				AVRational r = av_d2q(aspectRatio, 255);
				codecCtxVideo.sample_aspect_ratio(r);
				streamVideo.sample_aspect_ratio(r);
			}
			/*
			 time base: this is the fundamental unit of time (in seconds) in terms
			   of which frame timestamps are represented. for fixed-fps content,
			   timebase should be 1/framerate and timestamp increments should be
			   identically 1. */
			codecCtxVideo.time_base(av_inv_q(frame_rate));
			streamVideo.time_base(av_inv_q(frame_rate));
			codecCtxVideo.gop_size(10);
			codecCtxVideo.qcompress(0.6F);
			codecCtxVideo.qmin(10);
			codecCtxVideo.qmax(51);
			codecCtxVideo.max_b_frames(0);
			/*if (gopSize >= 0) {
				codecCtxVideo.gop_size(gopSize);
			}*/
			if (videoQuality >= 0) {
				codecCtxVideo.flags(codecCtxVideo.flags() | AV_CODEC_FLAG_QSCALE);
				codecCtxVideo.global_quality((int)Math.round(FF_QP2LAMBDA * videoQuality));
			}

			if (pixelFormat != AV_PIX_FMT_NONE) {
				codecCtxVideo.pix_fmt(pixelFormat);
			}
			else if (codecCtxVideo.codec_id() == AV_CODEC_ID_RAWVIDEO || codecCtxVideo.codec_id() == AV_CODEC_ID_PNG ||
				codecCtxVideo.codec_id() == AV_CODEC_ID_HUFFYUV || codecCtxVideo.codec_id() == AV_CODEC_ID_FFV1) {
				codecCtxVideo.pix_fmt(AV_PIX_FMT_RGB32);   // appropriate for common lossless formats
			}
			else {
				codecCtxVideo.pix_fmt(AV_PIX_FMT_YUV420P); // lossy, but works with about everything
			}

			if (codecCtxVideo.codec_id() == AV_CODEC_ID_H264) {
				// default to constrained baseline to produce content that plays back on anything,
				// without any significant tradeoffs for most use cases
				codecCtxVideo.profile(AVCodecContext.FF_PROFILE_H264_MAIN);
			}

			// some formats want stream headers to be separate
			if ((oformat.flags() & AVFMT_GLOBALHEADER) != 0) {
				codecCtxVideo.flags(codecCtxVideo.flags() | AV_CODEC_FLAG_GLOBAL_HEADER);
			}

			if ((codecVideo.capabilities() & AV_CODEC_CAP_EXPERIMENTAL) != 0) {
				codecCtxVideo.strict_std_compliance(AVCodecContext.FF_COMPLIANCE_EXPERIMENTAL);
			}
		}

		/*
		 * add an audio output stream
		 */
		if (audioChannels > 0 && audioBitrate > 0 && sampleRate > 0) {
			if (audioCodec != AV_CODEC_ID_NONE) {
				oformat.audio_codec(audioCodec);
			}
			else if ("flv".equals(format_name) || "mp4".equals(format_name) || "3gp".equals(format_name)) {
				oformat.audio_codec(AV_CODEC_ID_AAC);
			}
			else if ("avi".equals(format_name)) {
				oformat.audio_codec(AV_CODEC_ID_PCM_S16LE);
			}

			/* find the audio encoder */
			if ((codecAudio = avcodec_find_encoder_by_name(audioCodecName)) == null &&
				(codecAudio = avcodec_find_encoder(oformat.audio_codec())) == null) {
				release();
				throw new Exception("avcodec_find_encoder() error: Audio codec not found.");
			}
			oformat.audio_codec(codecAudio.id());

			if ((streamAudio = avformat_new_stream(formatCtx, codecAudio)) == null) {
				release();
				throw new Exception("avformat_new_stream() error: Could not allocate audio stream.");
			}
			codecCtxAudio = streamAudio.codec();
			codecCtxAudio.codec_id(oformat.audio_codec());
			codecCtxAudio.codec_type(AVMEDIA_TYPE_AUDIO);

			/* put sample parameters */
			codecCtxAudio.bit_rate(audioBitrate);
			codecCtxAudio.sample_rate(sampleRate);
			codecCtxAudio.channels(audioChannels);
			codecCtxAudio.channel_layout(av_get_default_channel_layout(audioChannels));
			if (sampleFormat != AV_SAMPLE_FMT_NONE) {
				codecCtxAudio.sample_fmt(sampleFormat);
			}
			else {
				// use AV_SAMPLE_FMT_S16 by default, if available
				codecCtxAudio.sample_fmt(AV_SAMPLE_FMT_FLTP);
				IntPointer formats = codecCtxAudio.codec().sample_fmts();
				for (int i = 0; formats.get(i) != -1; i++) {
					if (formats.get(i) == AV_SAMPLE_FMT_S16) {
						codecCtxAudio.sample_fmt(AV_SAMPLE_FMT_S16);
						break;
					}
				}
			}
			codecCtxAudio.time_base().num(1).den(sampleRate);
			streamAudio.time_base().num(1).den(sampleRate);
			switch (codecCtxAudio.sample_fmt()) {
				case AV_SAMPLE_FMT_U8:
				case AV_SAMPLE_FMT_U8P:
					codecCtxAudio.bits_per_raw_sample(8);
					break;
				case AV_SAMPLE_FMT_S16:
				case AV_SAMPLE_FMT_S16P:
					codecCtxAudio.bits_per_raw_sample(16);
					break;
				case AV_SAMPLE_FMT_S32:
				case AV_SAMPLE_FMT_S32P:
					codecCtxAudio.bits_per_raw_sample(32);
					break;
				case AV_SAMPLE_FMT_FLT:
				case AV_SAMPLE_FMT_FLTP:
					codecCtxAudio.bits_per_raw_sample(32);
					break;
				case AV_SAMPLE_FMT_DBL:
				case AV_SAMPLE_FMT_DBLP:
					codecCtxAudio.bits_per_raw_sample(64);
					break;
				default:
					assert false;
			}
			if (audioQuality >= 0) {
				codecCtxAudio.flags(codecCtxAudio.flags() | AV_CODEC_FLAG_QSCALE);
				codecCtxAudio.global_quality((int)Math.round(FF_QP2LAMBDA * audioQuality));
			}

			// some formats want stream headers to be separate
			if ((oformat.flags() & AVFMT_GLOBALHEADER) != 0) {
				codecCtxAudio.flags(codecCtxAudio.flags() | AV_CODEC_FLAG_GLOBAL_HEADER);
			}

			if ((codecAudio.capabilities() & AV_CODEC_CAP_EXPERIMENTAL) != 0) {
				codecCtxAudio.strict_std_compliance(AVCodecContext.FF_COMPLIANCE_EXPERIMENTAL);
			}
		}

		av_dump_format(formatCtx, 0, filename, 1);

		/* now that all the parameters are set, we can open the audio and
		   video codecs and allocate the necessary encode buffers */
		if (streamVideo != null) {
			AVDictionary options = new AVDictionary(null);
			if (videoQuality >= 0) {
				av_dict_set(options, "crf", "" + videoQuality, 0);
			}
			for (Entry<String, String> e : videoOptions.entrySet()) {
				av_dict_set(options, e.getKey(), e.getValue(), 0);
			}
			av_dict_set(options, "preset", "superfast",   0);
			av_dict_set(options, "tune",   "zerolatency", 0);
			av_dict_set_int(options, "maxrate", (long)(videoBitrate * 2.5), 0);
			av_dict_set_int(options, "bufsize", (long)(videoBitrate * 1.5), 0);

			/* open the codec */
			if ((ret = avcodec_open2(codecCtxVideo, codecVideo, options)) < 0) {
				release();
				throw new Exception("avcodec_open2() error " + ret + ": Could not open video codec.");
			}
			av_dict_free(options);

			video_outbuf = null;
			if ((oformat.flags() & 0x0020) == 0) { //AVFMT_RAWPICTURE) == 0) {
				/* allocate output buffer */
				/* XXX: API change will be done */
				/* buffers passed into lav* can be allocated any way you prefer,
				   as long as they're aligned enough for the architecture, and
				   they're freed appropriately (such as using av_free for buffers
				   allocated with av_malloc) */
				video_outbuf_size = Math.max(256 * 1024, 8 * codecCtxVideo.width() * codecCtxVideo.height()); // a la ffmpeg.c
				video_outbuf = new BytePointer(av_malloc(video_outbuf_size));
			}

			/* allocate the encoded raw picture */
			if ((picture = av_frame_alloc()) == null) {
				release();
				throw new Exception("av_frame_alloc() error: Could not allocate picture.");
			}
			picture.pts(0); // magic required by libx264

			int size = avpicture_get_size(codecCtxVideo.pix_fmt(), codecCtxVideo.width(), codecCtxVideo.height());
			if ((picture_buf = new BytePointer(av_malloc(size))).isNull()) {
				release();
				throw new Exception("av_malloc() error: Could not allocate picture buffer.");
			}

			/* if the output format is not equal to the image format, then a temporary
			   picture is needed too. It is then converted to the required output format */
			if ((tmp_picture = av_frame_alloc()) == null) {
				release();
				throw new Exception("av_frame_alloc() error: Could not allocate temporary picture.");
			}

			AVDictionary metadata = new AVDictionary(null);
			for (Entry<String, String> e : videoMetadata.entrySet()) {
				av_dict_set(metadata, e.getKey(), e.getValue(), 0);
			}
			streamVideo.metadata(metadata);
		}

		if (streamAudio != null) {
			AVDictionary options = new AVDictionary(null);
			if (audioQuality >= 0) {
				av_dict_set(options, "crf", "" + audioQuality, 0);
			}
			for (Entry<String, String> e : audioOptions.entrySet()) {
				av_dict_set(options, e.getKey(), e.getValue(), 0);
			}
			/* open the codec */
			if ((ret = avcodec_open2(codecCtxAudio, codecAudio, options)) < 0) {
				release();
				throw new Exception("avcodec_open2() error " + ret + ": Could not open audio codec.");
			}
			av_dict_free(options);

			audio_outbuf_size = 256 * 1024;
			audio_outbuf = new BytePointer(av_malloc(audio_outbuf_size));

			/* ugly hack for PCM codecs (will be removed ASAP with new PCM
			   support to compute the input frame size in samples */
			if (codecCtxAudio.frame_size() <= 1) {
				audio_outbuf_size = 16384; //FF_MIN_BUFFER_SIZE;
				audio_input_frame_size = audio_outbuf_size / codecCtxAudio.channels();
				switch (codecCtxAudio.codec_id()) {
					case AV_CODEC_ID_PCM_S16LE:
					case AV_CODEC_ID_PCM_S16BE:
					case AV_CODEC_ID_PCM_U16LE:
					case AV_CODEC_ID_PCM_U16BE:
						audio_input_frame_size >>= 1;
						break;
					default:
						break;
				}
			}
			else {
				audio_input_frame_size = codecCtxAudio.frame_size();
			}
			//int bufferSize = audio_input_frame_size * codecCtxAudio.bits_per_raw_sample()/8 * codecCtxAudio.channels();
			int planes = av_sample_fmt_is_planar(codecCtxAudio.sample_fmt()) != 0 ? (int)codecCtxAudio.channels() : 1;
			int data_size = av_samples_get_buffer_size((IntPointer)null, codecCtxAudio.channels(),
				audio_input_frame_size, codecCtxAudio.sample_fmt(), 1) / planes;
			samples_out = new BytePointer[planes];
			for (int i = 0; i < samples_out.length; i++) {
				samples_out[i] = new BytePointer(av_malloc(data_size)).capacity(data_size);
			}
			samples_in = new Pointer[AVFrame.AV_NUM_DATA_POINTERS];
			samples_in_ptr = new PointerPointer(AVFrame.AV_NUM_DATA_POINTERS);
			samples_out_ptr = new PointerPointer(AVFrame.AV_NUM_DATA_POINTERS);

			/* allocate the audio frame */
			if ((frame = av_frame_alloc()) == null) {
				release();
				throw new Exception("av_frame_alloc() error: Could not allocate audio frame.");
			}
			frame.pts(0); // magic required by libvorbis and webm

			AVDictionary metadata = new AVDictionary(null);
			for (Entry<String, String> e : audioMetadata.entrySet()) {
				av_dict_set(metadata, e.getKey(), e.getValue(), 0);
			}
			streamAudio.metadata(metadata);
		}

		/* open the output file, if needed */
		if ((oformat.flags() & AVFMT_NOFILE) == 0) {
			AVIOContext pb = new AVIOContext(null);
			if ((ret = avio_open(pb, filename, AVIO_FLAG_READ_WRITE)) < 0) {
				release();
				throw new Exception("avio_open error() error " + ret + ": Could not open '" + filename + "'");
			}
			formatCtx.pb(pb);
		}

		AVDictionary options = new AVDictionary(null);
		for (Entry<String, String> e : this.options.entrySet()) {
			av_dict_set(options, e.getKey(), e.getValue(), 0);
		}
		AVDictionary metadata = new AVDictionary(null);
		for (Entry<String, String> e : this.metadata.entrySet()) {
			av_dict_set(metadata, e.getKey(), e.getValue(), 0);
		}
		/* write the stream header, if any */
		avformat_write_header(formatCtx.metadata(metadata), options);
		av_dict_free(options);
	}

	public void stop() throws Exception {
		if (formatCtx != null) {
			try {
				/* flush all the buffers */
				while (streamVideo != null && recordImage(0, 0, 0, 0, 0, AV_PIX_FMT_NONE, (Buffer[])null))
					;
				while (streamAudio != null && recordSamples(0, 0, (Buffer[])null)) ;

				if (interleaved && streamVideo != null && streamAudio != null) {
					av_interleaved_write_frame(formatCtx, null);
				}
				else {
					av_write_frame(formatCtx, null);
				}

				/* write the trailer, if any */
				av_write_trailer(formatCtx);
			}
			finally {
				release();
			}
		}
	}

	@Override
	public void record(Frame frame) throws Exception {
		record(frame, AV_PIX_FMT_NONE);
	}

	public void record(Frame frame, int pixelFormat) throws Exception {
		if (frame == null || (frame.image == null && frame.samples == null)) {
			recordImage(0, 0, 0, 0, 0, pixelFormat, (Buffer[])null);
		}
		else {
			if (frame.image != null) {
				frame.keyFrame = recordImage(frame.imageWidth, frame.imageHeight, frame.imageDepth,
					frame.imageChannels, frame.imageStride, pixelFormat, frame.image);
			}
			if (frame.samples != null) {
				frame.keyFrame = recordSamples(frame.sampleRate, frame.audioChannels, frame.samples);
			}
		}
	}

	public boolean record(AVFrame frame) throws Exception {
		int ret;

		av_init_packet(audio_pkt);
		audio_pkt.data(audio_outbuf);
		audio_pkt.size(audio_outbuf_size);
		if ((ret = avcodec_encode_audio2(codecCtxAudio, audio_pkt, frame, got_audio_packet)) < 0) {
			throw new Exception("avcodec_encode_audio2() error " + ret + ": Could not encode audio packet.");
		}
		if (frame != null) {
			frame.pts(frame.pts() + frame.nb_samples()); // magic required by libvorbis and webm
		}
		if (got_audio_packet[0] != 0) {
			if (audio_pkt.pts() != AV_NOPTS_VALUE) {
				audio_pkt.pts(av_rescale_q(audio_pkt.pts(), codecCtxAudio.time_base(), streamAudio.time_base()));
			}
			if (audio_pkt.dts() != AV_NOPTS_VALUE) {
				audio_pkt.dts(av_rescale_q(audio_pkt.dts(), codecCtxAudio.time_base(), streamAudio.time_base()));
			}
			audio_pkt.flags(audio_pkt.flags() | AV_PKT_FLAG_KEY);
			audio_pkt.stream_index(streamAudio.index());
		}
		else {
			return false;
		}

		/* write the compressed frame in the media file */
		synchronized (formatCtx) {
			if (interleaved && streamVideo != null) {
				if ((ret = av_interleaved_write_frame(formatCtx, audio_pkt)) < 0) {
					throw new Exception("av_interleaved_write_frame() error " + ret + " while writing interleaved audio frame.");
				}
			}
			else {
				if ((ret = av_write_frame(formatCtx, audio_pkt)) < 0) {
					throw new Exception("av_write_frame() error " + ret + " while writing audio frame.");
				}
			}
		}
		return true;
	}

	public boolean recordImage(int width, int height, int depth, int channels, int stride, int pixelFormat, Buffer... image) throws Exception {
		if (streamVideo == null) {
			throw new Exception("No video output stream (Is imageWidth > 0 && imageHeight > 0 and has start() been called?)");
		}
		int ret;

		if (image == null || image.length == 0) {
			/*
			no more frame to compress. The codec has a latency of a few
			frames if using B frames, so we get the last frames by
			passing the same picture again
			*/
		}
		else {
			int step = stride * Math.abs(depth) / 8;
			BytePointer data = image[0] instanceof ByteBuffer
				? new BytePointer((ByteBuffer)image[0].position(0))
				: new BytePointer(new Pointer(image[0].position(0)));

			if (pixelFormat == AV_PIX_FMT_NONE) {
				if ((depth == Frame.DEPTH_UBYTE || depth == Frame.DEPTH_BYTE) && channels == 3) {
					pixelFormat = AV_PIX_FMT_BGR24;
				}
				else if ((depth == Frame.DEPTH_UBYTE || depth == Frame.DEPTH_BYTE) && channels == 1) {
					pixelFormat = AV_PIX_FMT_GRAY8;
				}
				else if ((depth == Frame.DEPTH_USHORT || depth == Frame.DEPTH_SHORT) && channels == 1) {
					pixelFormat = ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN) ?
						AV_PIX_FMT_GRAY16BE : AV_PIX_FMT_GRAY16LE;
				}
				else if ((depth == Frame.DEPTH_UBYTE || depth == Frame.DEPTH_BYTE) && channels == 4) {
					pixelFormat = AV_PIX_FMT_RGBA;
				}
				else if ((depth == Frame.DEPTH_UBYTE || depth == Frame.DEPTH_BYTE) && channels == 2) {
					pixelFormat = AV_PIX_FMT_NV21; // Android's camera capture format
					step = width;
				}
				else {
					throw new Exception("Could not guess pixel format of image: depth=" + depth + ", channels=" + channels);
				}
			}

			if (codecCtxVideo.pix_fmt() != pixelFormat || codecCtxVideo.width() != width || codecCtxVideo.height() != height) {
				/* convert to the codec pixel format if needed */
				img_convert_ctx = sws_getCachedContext(img_convert_ctx, width, height, pixelFormat,
					codecCtxVideo.width(), codecCtxVideo.height(), codecCtxVideo.pix_fmt(), SWS_BILINEAR,
					null, null, (DoublePointer)null);
				if (img_convert_ctx == null) {
					throw new Exception("sws_getCachedContext() error: Cannot initialize the conversion context.");
				}
				avpicture_fill(new AVPicture(tmp_picture), data, pixelFormat, width, height);
				avpicture_fill(new AVPicture(picture), picture_buf, codecCtxVideo.pix_fmt(), codecCtxVideo.width(), codecCtxVideo.height());
				tmp_picture.linesize(0, step);
				tmp_picture.format(pixelFormat);
				tmp_picture.width(width);
				tmp_picture.height(height);
				picture.format(codecCtxVideo.pix_fmt());
				picture.width(codecCtxVideo.width());
				picture.height(codecCtxVideo.height());
				sws_scale(img_convert_ctx, new PointerPointer(tmp_picture), tmp_picture.linesize(),
					0, height, new PointerPointer(picture), picture.linesize());
			}
			else {
				avpicture_fill(new AVPicture(picture), data, pixelFormat, width, height);
				picture.linesize(0, step);
				picture.format(pixelFormat);
				picture.width(width);
				picture.height(height);
			}
		}

		if ((oformat.flags() & 0x0020) != 0) { //AVFMT_RAWPICTURE) != 0) {
			if (image == null || image.length == 0) {
				return false;
			}
			/* raw video case. The API may change slightly in the future for that? */
			av_init_packet(video_pkt);
			video_pkt.flags(video_pkt.flags() | AV_PKT_FLAG_KEY);
			video_pkt.stream_index(streamVideo.index());
			video_pkt.data(new BytePointer(picture));
			video_pkt.size(Loader.sizeof(AVPicture.class));
		}
		else {
			/* encode the image */
			av_init_packet(video_pkt);
			video_pkt.data(video_outbuf);
			video_pkt.size(video_outbuf_size);
			picture.quality(codecCtxVideo.global_quality());

			if ((ret = avcodec_encode_video2(codecCtxVideo, video_pkt, image == null || image.length == 0 ? null : picture, got_video_packet)) < 0) {
				throw new Exception("avcodec_encode_video2() error " + ret + ": Could not encode video packet.");
			}
			picture.pts(picture.pts() + 1); // magic required by libx264

			/* if zero size, it means the image was buffered */
			if (got_video_packet[0] != 0) {

				if (video_pkt.pts() != AV_NOPTS_VALUE) {
					video_pkt.pts(av_rescale_q(video_pkt.pts(), codecCtxVideo.time_base(), streamVideo.time_base()));
				}
				if (video_pkt.dts() != AV_NOPTS_VALUE) {
					video_pkt.dts(av_rescale_q(video_pkt.dts(), codecCtxVideo.time_base(), streamVideo.time_base()));
				}

				video_pkt.stream_index(streamVideo.index());
			}
			else {
				return false;
			}
		}

		synchronized (formatCtx) {
			/* write the compressed frame in the media file */
			if (interleaved && streamAudio != null) {
				if ((ret = av_interleaved_write_frame(formatCtx, video_pkt)) < 0) {
					throw new Exception("av_interleaved_write_frame() error " + ret + " while writing interleaved video frame.");
				}
			}
			else {
				if ((ret = av_write_frame(formatCtx, video_pkt)) < 0) {
					throw new Exception("av_write_frame() error " + ret + " while writing video frame.");
				}
			}
		}
		return image != null ? (video_pkt.flags() & AV_PKT_FLAG_KEY) != 0 : got_video_packet[0] != 0;
	}

	public boolean recordSamples(Buffer... samples) throws Exception {
		return recordSamples(0, 0, samples);
	}

	public boolean recordSamples(int sampleRate, int audioChannels, Buffer... samples) throws Exception {
		if (streamAudio == null) {
			throw new Exception("No audio output stream (Is audioChannels > 0 and has start() been called?)");
		}
		int ret;
		if (codecCtxAudio == null)
			return false;


		if (sampleRate <= 0 && codecCtxAudio.sample_rate() > 0) {
			sampleRate = codecCtxAudio.sample_rate();
		}
		else {
			sampleRate = 44100;
		}
		if (audioChannels <= 0 && codecCtxAudio.channels() > 0) {
			audioChannels = codecCtxAudio.channels();
		}
		else {
			audioChannels = 1;
		}
		int inputSize = samples != null ? samples[0].limit() - samples[0].position() : 0;
		int inputFormat = AV_SAMPLE_FMT_NONE;
		int inputChannels = samples != null && samples.length > 1 ? 1 : audioChannels;
		int inputDepth = 0;
		int outputFormat = codecCtxAudio.sample_fmt();
		int outputChannels = samples_out.length > 1 ? 1 : codecCtxAudio.channels();
		int outputDepth = av_get_bytes_per_sample(outputFormat);
		if (samples != null && samples[0] instanceof ByteBuffer) {
			inputFormat = samples.length > 1 ? AV_SAMPLE_FMT_U8P : AV_SAMPLE_FMT_U8;
			inputDepth = 1;
			for (int i = 0; i < samples.length; i++) {
				ByteBuffer b = (ByteBuffer)samples[i];
				if (samples_in[i] instanceof BytePointer && samples_in[i].capacity() >= inputSize && b.hasArray()) {
					((BytePointer)samples_in[i]).position(0).put(b.array(), b.position(), inputSize);
				}
				else {
					samples_in[i] = new BytePointer(b);
				}
			}
		}
		else if (samples != null && samples[0] instanceof ShortBuffer) {
			inputFormat = samples.length > 1 ? AV_SAMPLE_FMT_S16P : AV_SAMPLE_FMT_S16;
			inputDepth = 2;
			for (int i = 0; i < samples.length; i++) {
				ShortBuffer b = (ShortBuffer)samples[i];
				if (samples_in[i] instanceof ShortPointer && samples_in[i].capacity() >= inputSize && b.hasArray()) {
					((ShortPointer)samples_in[i]).position(0).put(b.array(), samples[i].position(), inputSize);
				}
				else {
					samples_in[i] = new ShortPointer(b);
				}
			}
		}
		else if (samples != null && samples[0] instanceof IntBuffer) {
			inputFormat = samples.length > 1 ? AV_SAMPLE_FMT_S32P : AV_SAMPLE_FMT_S32;
			inputDepth = 4;
			for (int i = 0; i < samples.length; i++) {
				IntBuffer b = (IntBuffer)samples[i];
				if (samples_in[i] instanceof IntPointer && samples_in[i].capacity() >= inputSize && b.hasArray()) {
					((IntPointer)samples_in[i]).position(0).put(b.array(), samples[i].position(), inputSize);
				}
				else {
					samples_in[i] = new IntPointer(b);
				}
			}
		}
		else if (samples != null && samples[0] instanceof FloatBuffer) {
			inputFormat = samples.length > 1 ? AV_SAMPLE_FMT_FLTP : AV_SAMPLE_FMT_FLT;
			inputDepth = 4;
			for (int i = 0; i < samples.length; i++) {
				FloatBuffer b = (FloatBuffer)samples[i];
				if (samples_in[i] instanceof FloatPointer && samples_in[i].capacity() >= inputSize && b.hasArray()) {
					((FloatPointer)samples_in[i]).position(0).put(b.array(), b.position(), inputSize);
				}
				else {
					samples_in[i] = new FloatPointer(b);
				}
			}
		}
		else if (samples != null && samples[0] instanceof DoubleBuffer) {
			inputFormat = samples.length > 1 ? AV_SAMPLE_FMT_DBLP : AV_SAMPLE_FMT_DBL;
			inputDepth = 8;
			for (int i = 0; i < samples.length; i++) {
				DoubleBuffer b = (DoubleBuffer)samples[i];
				if (samples_in[i] instanceof DoublePointer && samples_in[i].capacity() >= inputSize && b.hasArray()) {
					((DoublePointer)samples_in[i]).position(0).put(b.array(), b.position(), inputSize);
				}
				else {
					samples_in[i] = new DoublePointer(b);
				}
			}
		}
		else if (samples != null) {
			throw new Exception("Audio samples Buffer has unsupported type: " + samples);
		}

		if (samples_convert_ctx == null || samples_channels != audioChannels || samples_format != inputFormat || samples_rate != sampleRate) {
			samples_convert_ctx = swr_alloc_set_opts(samples_convert_ctx, codecCtxAudio.channel_layout(), outputFormat, codecCtxAudio.sample_rate(),
				av_get_default_channel_layout(audioChannels), inputFormat, sampleRate, 0, null);
			if (samples_convert_ctx == null) {
				throw new Exception("swr_alloc_set_opts() error: Cannot allocate the conversion context.");
			}
			else if ((ret = swr_init(samples_convert_ctx)) < 0) {
				throw new Exception("swr_init() error " + ret + ": Cannot initialize the conversion context.");
			}
			samples_channels = audioChannels;
			samples_format = inputFormat;
			samples_rate = sampleRate;
		}

		for (int i = 0; samples != null && i < samples.length; i++) {
			samples_in[i].position(samples_in[i].position() * inputDepth).
				limit((samples_in[i].position() + inputSize) * inputDepth);
		}
		while (true) {
			int inputCount = (int)Math.min(samples != null ? (samples_in[0].limit() - samples_in[0].position()) / (inputChannels * inputDepth) : 0, Integer.MAX_VALUE);
			int outputCount = (int)Math.min((samples_out[0].limit() - samples_out[0].position()) / (outputChannels * outputDepth), Integer.MAX_VALUE);
			inputCount = Math.min(inputCount, (outputCount * sampleRate + codecCtxAudio.sample_rate() - 1) / codecCtxAudio.sample_rate());
			for (int i = 0; samples != null && i < samples.length; i++) {
				samples_in_ptr.put(i, samples_in[i]);
			}
			for (int i = 0; i < samples_out.length; i++) {
				samples_out_ptr.put(i, samples_out[i]);
			}
			if ((ret = swr_convert(samples_convert_ctx, samples_out_ptr, outputCount, samples_in_ptr, inputCount)) < 0) {
				throw new Exception("swr_convert() error " + ret + ": Cannot convert audio samples.");
			}
			else if (ret == 0) {
				break;
			}
			for (int i = 0; samples != null && i < samples.length; i++) {
				samples_in[i].position(samples_in[i].position() + inputCount * inputChannels * inputDepth);
			}
			for (int i = 0; i < samples_out.length; i++) {
				samples_out[i].position(samples_out[i].position() + ret * outputChannels * outputDepth);
			}

			if (samples == null || samples_out[0].position() >= samples_out[0].limit()) {
				frame.nb_samples(audio_input_frame_size);
				avcodec_fill_audio_frame(frame, codecCtxAudio.channels(), outputFormat, samples_out[0], (int)Math.min(samples_out[0].limit(), Integer.MAX_VALUE), 0);
				for (int i = 0; i < samples_out.length; i++) {
					frame.data(i, samples_out[i].position(0));
					frame.linesize(i, (int)Math.min(samples_out[i].limit(), Integer.MAX_VALUE));
				}
				frame.quality(codecCtxAudio.global_quality());
				record(frame);
			}
		}
		return samples != null ? frame.key_frame() != 0 : record((AVFrame)null);
	}

}

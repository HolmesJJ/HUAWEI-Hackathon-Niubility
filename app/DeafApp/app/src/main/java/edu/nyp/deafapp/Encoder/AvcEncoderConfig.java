package edu.nyp.deafapp.Encoder;

import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

public class AvcEncoderConfig extends EncoderConfig {
    public static final String MIME_TYPE = "video/avc";
    private static final int IFRAME_INTERVAL = 0;          // 10 seconds between I-frames

    //Defaults from the BigFlake Sample
    private static final int DEFAULT_WIDTH = 320;
    private static final int DEFAULT_HEIGHT = 240;
    private static final String DEFAULT_PATH = new File(Environment.getExternalStorageDirectory(),
            "test." + MIME_TYPE.split("/")[1] + '.' + DEFAULT_WIDTH + "x" + DEFAULT_HEIGHT + ".mp4").getAbsolutePath();

    public AvcEncoderConfig() {
        this(DEFAULT_PATH ,DEFAULT_WIDTH, DEFAULT_HEIGHT,15,2000000);
    }

    public AvcEncoderConfig(final String path, final float framesPerSecond, final int bitRate) {
        super(path, DEFAULT_WIDTH, DEFAULT_HEIGHT, framesPerSecond, bitRate);
    }

    public AvcEncoderConfig(final String path, final int width, final int height, final float framesPerSecond, final int bitRate) {
        super(path, width, height, framesPerSecond, bitRate);
    }


    @Override
    public MediaFormat getVideoMediaFormat() {
        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, getWidth(), getHeight());

        // Set some properties.  Failing to specify some of these can cause the MediaCodec
        // configure() call to throw an unhelpful exception.
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, getBitRate());
        format.setFloat(MediaFormat.KEY_FRAME_RATE, getFramePerSecond());
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);

        return format;
    }

    @Override
    public FrameMuxer getFrameMuxer() throws IOException {
        return new Mp4FrameMuxer(getPath(), getFramePerSecond());
    }
}

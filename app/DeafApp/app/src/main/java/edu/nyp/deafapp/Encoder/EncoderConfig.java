package edu.nyp.deafapp.Encoder;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;

import java.io.IOException;

public abstract class EncoderConfig {

    private final String mPath;
    private final int mWidth;
    private final int mHeight;
    private final float mFramesPerSecond;
    private final int mBitRate;

    abstract FrameMuxer getFrameMuxer() throws IOException;
    abstract MediaFormat getVideoMediaFormat();

    public static boolean isSupported(final String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);

            if (!codecInfo.isEncoder()) {
                continue;
            }

            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    return true;
                }
            }
        }
        return false;
    }

    public EncoderConfig(final String path, final int width, final int height, final float framesPerSecond, final int bitRate) {
        mPath = path;
        mWidth = width;
        mHeight = height;
        mFramesPerSecond = framesPerSecond;
        mBitRate = bitRate;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public int getBitRate() {
        return mBitRate;
    }

    public float getFramePerSecond() {
        return mFramesPerSecond;
    }

    public String getPath() {
        return mPath;
    }

}

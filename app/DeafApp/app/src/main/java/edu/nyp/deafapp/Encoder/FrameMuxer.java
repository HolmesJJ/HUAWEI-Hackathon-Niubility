package edu.nyp.deafapp.Encoder;

import android.media.MediaCodec;

import java.nio.ByteBuffer;

public interface FrameMuxer {
    boolean isStarted();
    void start(final FrameEncoder frameEncoder);
    void muxVideoFrame(final ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo);
    void release();
}

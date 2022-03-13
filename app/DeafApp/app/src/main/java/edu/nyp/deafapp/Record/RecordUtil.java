package edu.nyp.deafapp.Record;

import android.media.MediaRecorder;

public class RecordUtil {

	private MediaRecorder mRecorder;

	public void startRecord() {
		if (mRecorder != null) {
			return;
		}
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

		try {
			mRecorder.prepare();
			mRecorder.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 停止录音
	public void stopRecord() {
		if (mRecorder != null) {
			//设置异常时不崩溃
			mRecorder.setOnErrorListener(null);
			mRecorder.stop();
			mRecorder.release();
			mRecorder = null;
		}
	}
}

package org.innovationsforlearning.StudentPortal;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


public class Audio {

    private static final String LOG_TAG = "SP_Audio";
    private static final String FILE_NAME = "/recording.aac";
    private static final String AUDIO_HEADER = "data:audio/aac;base64,";

    private static String mFileName = Environment.getExternalStorageDirectory().getAbsolutePath()+FILE_NAME;


    private MediaRecorder mRecorder = null;
    private MediaPlayer   mPlayer = null;

    public void startPlaying(MediaPlayer.OnCompletionListener onCompletion) {
        mPlayer = new MediaPlayer();
        mPlayer.setVolume((float)1.0,(float)1.0);
        mPlayer.setOnCompletionListener(onCompletion);
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed"+e);
        }
    }

    public void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    public void startRecording() {
        File file = new File(mFileName);

        if(file.exists()) {
            file.delete();
        }

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        Log.e(LOG_TAG, "startRecording:");

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed"+e);
        }

        mRecorder.start();
    }

    public void stopRecording() {
        Log.e(LOG_TAG, "stopRecording:");
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    private byte[] loadFile(){
        File file = new File(mFileName);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bytes;
    }

    public String getBase64() throws IOException {
        byte[] bytes = loadFile();

        String encoded = Base64.encodeToString(bytes, Base64.DEFAULT);
        return AUDIO_HEADER+encoded;
    }

}

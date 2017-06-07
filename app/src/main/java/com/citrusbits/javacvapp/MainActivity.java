package com.citrusbits.javacvapp;

import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.bytedeco.javacpp.avcodec.AV_CODEC_ID_H264;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private Bitmap frameBitmap;
    private TextView textView;
    public StringBuilder stringBuilder;
    private SavingThread savingThread;
    private int mProgress = 0;
    private boolean isException;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView)findViewById(R.id.imageView);
        textView = (TextView)findViewById(R.id.textView);

        savingThread = new SavingThread();
        savingThread.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        savingThread.kill();
    }

    class SavingThread  extends Thread{

        private boolean isRunning = true;

        @Override
        public void run() {

            stringBuilder = new StringBuilder();
            URL url;
            HttpURLConnection urlConnection = null;

            try {

                File inputFile = new File(Environment.getExternalStorageDirectory(),"input-video.mp4");
//                    File inputFile = new File(Environment.getExternalStorageDirectory(),"GOPR0175.mp4");

                File outputFile = new File(Environment.getExternalStorageDirectory(),"test-clip.mp4");
                outputFile.delete();
                outputFile.createNewFile();

//              url = new URL("http://dev.exiv2.org/attachments/372/3D_L0064.MP4");
//
//              url = new URL(multiMedia.getUrl());
//              urlConnection = (HttpURLConnection) url.openConnection();
//              InputStream inputStream = urlConnection.getInputStream();
//              InputStream inputStream = new BridgeInputStream(new BridgeFile(multiMedia.getPath() + "/" + multiMedia.getName()));
                InputStream inputStream = new FileInputStream(inputFile);

//                    FFmpegFrameGrabber grabber= new FFmpegFrameGrabber(inputFile);
////                    FFmpegFrameGrabber grabber= new FFmpegFrameGrabber(inputStream);
//                    grabber.setFormat("mp4");
//                    grabber.setFrameNumber(1000);
//                    grabber.start();
//                    Frame frame = grabber.grab();
//
//                    frameBitmap = new AndroidFrameConverter().convert(frame);
//
//                    grabber.stop();

                //-------- trimming clip -----------

//                InputStream inputStream = new FileInputStream(inputFile);
//                FFmpegFrameGrabber grabber= new FFmpegFrameGrabber(inputStream);
                FFmpegFrameGrabber grabber= new FFmpegFrameGrabber(inputFile);
                grabber.setFormat("mp4");
                grabber.start();

                final int totalFrames = grabber.getLengthInFrames();

//                OutputStream outputStream = new FileOutputStream(outputFile);

                FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile,grabber.getImageWidth(),grabber.getImageHeight(),grabber.getAudioChannels());
                recorder.setVideoCodec(AV_CODEC_ID_H264);
                recorder.setFormat("mp4");
                recorder.setFrameRate(grabber.getFrameRate());
                recorder.setVideoBitrate(grabber.getVideoBitrate());
                Frame grabbedImage = null;
                recorder.start();
                long start = System.currentTimeMillis();
                int numberOfFramesSaved = 0;
                showToast("Saving started!");
                while ( isRunning && (grabbedImage = grabber.grab()) != null) {
                    recorder.record(grabbedImage);
                    numberOfFramesSaved += 1;
                    int progress = (int)(numberOfFramesSaved / (float)totalFrames * 100);
                    if(mProgress != progress && System.currentTimeMillis() - start > 100) {
                        mProgress = progress;
                        frameBitmap = new AndroidFrameConverter().convert(grabbedImage);
                        if(frameBitmap != null) updateUi();
                        updateProgress();
                    }
                }
                showToast("Saving finish!");
                recorder.stop();
                grabber.stop();
            }catch (Exception e){
                isException = true;
                stringBuilder.append("Exception:\n");
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                stringBuilder.append(sw.toString());
                e.printStackTrace();
            } finally {
                if(urlConnection != null){
                    urlConnection.disconnect();
                }
            }

            updateUi();
        }

        public void kill() {
            isRunning = false;
        }
    }

    private void updateProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText("Progress: " + mProgress);
            }
        });
    }

    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this,message,Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUi() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(isException ) textView.setText(stringBuilder.toString()+ "\nframeBitmap" + frameBitmap);
                imageView.setImageBitmap(frameBitmap);
            }
        });
    }
}

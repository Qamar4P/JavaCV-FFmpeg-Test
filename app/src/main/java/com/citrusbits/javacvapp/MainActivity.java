package com.citrusbits.javacvapp;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private Bitmap frameBitmap;
    private TextView textView;
    public StringBuilder stringBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView)findViewById(R.id.imageView);
        textView = (TextView)findViewById(R.id.textView);

    }

    @Override
    protected void onStart() {
        super.onStart();

        new Thread(new Runnable() {

            @Override
            public void run() {
                URL url;
                HttpURLConnection urlConnection = null;

                FFmpegFrameGrabber grabber = null;
                try {

                    url = new URL("http://dev.exiv2.org/attachments/372/3D_L0064.MP4");
                    urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    grabber = new FFmpegFrameGrabber(inputStream);
                    grabber.start();
                    int grabStep = 1000000;
                    grabber.setFrameNumber(grabStep);

                    Frame frame = grabber.grab();

                    frameBitmap = new AndroidFrameConverter().convert(frame);

                    updateUi();

                } catch (Exception e) {
                    e.printStackTrace();
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("Exception:\n");
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    stringBuilder.append(sw.toString());
                    showToast(e.getMessage());
                }finally {
                    try {
                        if (grabber != null) {
                            grabber.stop();
                        }
                    } catch (FrameGrabber.Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();
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
                textView.setText(stringBuilder.toString());
                imageView.setImageBitmap(frameBitmap);
            }
        });
    }
}

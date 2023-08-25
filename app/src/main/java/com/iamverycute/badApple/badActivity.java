package com.iamverycute.badApple;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.videoio.*;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.arges.sepan.argmusicplayer.Models.ArgAudio;
import com.arges.sepan.argmusicplayer.PlayerViews.ArgPlayerSmallView;
import com.google.common.io.ByteStreams;
import com.neberox.library.asciicreator.ASCIIConverter;

import org.opencv.android.OpenCVLoader;
import org.opencv.videoio.VideoCapture;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class badActivity extends AppCompatActivity {
    static final String TAG = "badApple_Demo";
    ImageView videoView;
    ArgPlayerSmallView audioView;
    File badPath;
    String fileName = "badApple.mp4";
    boolean isAscii = true;

    static {
        if (!OpenCVLoader.initDebug())
            Log.d(TAG, "Unable to load OpenCV");
        else
            Log.d(TAG, "OpenCV loaded");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        File f = new File(Objects.requireNonNull(getExternalFilesDir(null)).getAbsolutePath());
        if (!f.exists()) {
            if (f.mkdirs()) {
                Log.d(TAG, "Make Files Dir");
            }
        }
        audioView = new ArgPlayerSmallView(this);
        videoView = findViewById(R.id.myContainer);
        videoView.setOnClickListener(v -> isAscii = !isAscii);
        badPath = new File(f.getAbsolutePath(), fileName);
        if (!badPath.exists()) {
            try (InputStream badStream = this.getAssets().open(fileName)) {
                try (FileOutputStream fos = new FileOutputStream(badPath)) {
                    ByteStreams.copy(badStream, fos);
                }
            } catch (IOException ignored) {
            }
        }
        play();
    }
    /**
     * @noinspection BusyWait
     */
    void play() {
        VideoCapture vc = new VideoCapture(badPath.getAbsolutePath(), Videoio.CAP_ANY);
        if (vc.isOpened()) {
            ASCIIConverter converter = new ASCIIConverter(this);
            // using https://github.com/zelin/ASCII-Art-Generator
            converter.setFontSize(6);
            converter.setReversedLuminance(false);
            converter.setGrayScale(true);
            new Thread(() -> {
                Mat mat = new Mat();
                while (true) {
                    if (vc.read(mat)) {
                        Bitmap rawBmp = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.RGB_565);
                        Utils.matToBitmap(mat, rawBmp);
                        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                            rawBmp.compress(Bitmap.CompressFormat.JPEG, 1, out);
                            Bitmap minBmp = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
                            runOnUiThread(() -> {
                                try {
                                    Bitmap asciiBmp;
                                    if (isAscii) {
                                        asciiBmp = converter.createASCIIImage(minBmp);
                                        videoView.setImageBitmap(asciiBmp);
                                    } else {
                                        videoView.setImageBitmap(minBmp);
                                    }
                                } catch (ExecutionException | InterruptedException ignored) {
                                }
                            });
                        } catch (IOException ignored) {
                        }
                        try {
                            Thread.sleep(26);
                        } catch (InterruptedException ignored) {
                        }
                        continue;
                    }
                    break;
                }
                vc.release();
                play();
            }).start();
            audioView.play(ArgAudio.createFromFilePath("", "", badPath.getAbsolutePath()));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
        System.exit(0);
    }
}
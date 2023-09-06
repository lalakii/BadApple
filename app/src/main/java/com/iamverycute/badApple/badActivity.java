package com.iamverycute.badApple;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.videoio.*;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.jean.jcplayer.model.JcAudio;
import com.example.jean.jcplayer.view.JcPlayerView;
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

public class badActivity extends AppCompatActivity implements Runnable {
    static final String TAG = "badApple_Demo";
    ASCIIConverter converter;
    VideoCapture videoCapture;
    JcPlayerView mAudioView;
    ImageView mImageView;
    TextView mTextView;
    File videoPath;
    String fileName = "badApple.mp4";
    boolean isAscii = true;
    Mat mat = new Mat();

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
        mAudioView = new JcPlayerView(this);
        mTextView = findViewById(R.id.text_show);
        mImageView = findViewById(R.id.bitmap_show);
        mImageView.setOnClickListener(v -> isAscii = !isAscii);
        videoPath = new File(f.getAbsolutePath(), fileName);
        if (!videoPath.exists()) {
            try (InputStream badStream = this.getAssets().open(fileName)) {
                try (FileOutputStream fos = new FileOutputStream(videoPath)) {
                    ByteStreams.copy(badStream, fos);
                }
            } catch (IOException ignored) {
            }
        }
        converter = new ASCIIConverter(this);
        // using https://github.com/zelin/ASCII-Art-Generator
        converter.setFontSize(6);
        converter.setReversedLuminance(false);
        converter.setGrayScale(true);
        play();
    }

    /**
     * @noinspection BusyWait
     */
    @Override
    public void run() {
        runOnUiThread(() -> mAudioView.playAudio(JcAudio.createFromAssets(fileName)));
        while (videoCapture.read(mat)) {
            Bitmap rawBmp = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.RGB_565);
            Utils.matToBitmap(mat, rawBmp);
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                rawBmp.compress(Bitmap.CompressFormat.JPEG, 1, out);
                try (ByteArrayInputStream bmpArray = new ByteArrayInputStream(out.toByteArray())) {
                    Bitmap minBmp = BitmapFactory.decodeStream(bmpArray);
                    runOnUiThread(() -> {
                        try {
                            Bitmap asciiBmp;
                            if (isAscii) {
                                asciiBmp = converter.createASCIIImage(minBmp);
                                mImageView.setImageBitmap(asciiBmp);
                            } else {
                                mImageView.setImageBitmap(minBmp);
                            }
                            mTextView.setText(converter.createASCIIString(minBmp));
                        } catch (ExecutionException | InterruptedException ignored) {
                        }
                    });
                }
            } catch (IOException ignored) {
            }
            try {
                Thread.sleep(26);
            } catch (InterruptedException ignored) {
            }
        }
        videoCapture.release();
        play();
    }

    void play() {
        videoCapture = new VideoCapture(videoPath.getAbsolutePath(), Videoio.CAP_ANY);
        if (videoCapture.isOpened()) {
            new Thread(this).start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
        System.exit(0);
    }
}
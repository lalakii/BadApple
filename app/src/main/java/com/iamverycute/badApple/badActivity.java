package com.iamverycute.badApple;
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
import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
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
    FrameGrabber videoCapture;
    JcPlayerView mAudioView;
    ImageView mImageView;
    TextView mTextView;
    File videoPath;
    String fileName = "badApple.mp4";
    boolean isAscii = true;
    AndroidFrameConverter frameConverter = new AndroidFrameConverter();
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
        //import https://github.com/zelin/ASCII-Art-Generator
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
        try {
            videoCapture.restart();
        } catch (FrameGrabber.Exception ignored) {
        }
        Frame image = null;
        while (true) {
            try {
                if ((image = videoCapture.grabFrame()) == null) break;
            } catch (FrameGrabber.Exception ignored) {
            }
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                Bitmap raw = frameConverter.convert(image);
                raw.compress(Bitmap.CompressFormat.JPEG, 1, out);
                try (ByteArrayInputStream bmpArray = new ByteArrayInputStream(out.toByteArray())) {
                    Bitmap minBmp = BitmapFactory.decodeStream(bmpArray);
                    runOnUiThread(() -> {
                        try {
                            if (isAscii) {
                                mImageView.setImageBitmap(converter.createASCIIImage(minBmp));
                            } else {
                                mImageView.setImageBitmap(raw);
                            }
                            mTextView.setText(converter.createASCIIString(minBmp));
                        } catch (ExecutionException | InterruptedException ignored) {
                        }
                    });
                }
            } catch (IOException ignored) {
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {
            }
        }
        play();
    }

    void play() {
        try {
            videoCapture = FrameGrabber.createDefault(videoPath.getAbsolutePath());
        } catch (FrameGrabber.Exception ignored) {
        }
        new Thread(this).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (videoCapture != null) {
                videoCapture.stop();
                videoCapture.close();
            }
        } catch (FrameGrabber.Exception ignored) {
        }
        finish();
        System.exit(0);
    }
}
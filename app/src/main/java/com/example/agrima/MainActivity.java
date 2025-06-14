package com.example.agrima;

import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.widget.TextView;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.content.pm.PackageManager;
import android.Manifest;

import com.example.agrima.databinding.ActivityMainBinding;

import java.nio.ByteBuffer;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("agrima");
    }

    private GLSurfaceView glSurfaceView;
    private Render render;

    private ActivityMainBinding binding;
    private TextureView textureView;
    private Button toggleButton;

    private TextView fpsText;

    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSessions;
    private CaptureRequest.Builder captureRequestBuilder;
    private Handler backgroundHandler;
    private HandlerThread backgroundThread;
    private long lastFrameTime = 0;

    private int mode = 0;

    public native byte[] nativeProcessAndRender(byte[] data, int width, int height, int mode);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        glSurfaceView = findViewById(R.id.glSurfaceView);
        glSurfaceView.setEGLContextClientVersion(2);
        render = new Render(this);
        glSurfaceView.setRenderer(render);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        textureView = findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(textureListener);


        toggleButton = findViewById(R.id.toggleButton);
        toggleButton.setText("Mode: Raw");
        toggleButton.setBackgroundColor(Color.parseColor("#6200EE"));
        toggleButton.setTextColor(Color.WHITE);
        toggleButton.setPadding(16, 8, 16, 8);
        toggleButton.setAllCaps(false);

        toggleButton.setOnClickListener(v -> {
            mode = (mode + 1) % 6;  // Assuming 0-5 modes

            String modeName;
            switch (mode) {
                case 0: modeName = "Raw"; render.setMode(0); break;
                case 1: modeName = "Grayscale"; render.setMode(0); break;
                case 2: modeName = "Canny Edge"; render.setMode(0); break;
                case 3: modeName = "Invert"; render.setMode(0); break;
                case 4: modeName = "Blur"; render.setMode(0); break;
                case 5: modeName = "Threshold"; render.setMode(0); break;
                default: modeName = "Unknown"; render.setMode(0);
            }

            toggleButton.setText(modeName);
            Log.d("MAIN", "Switched mode to: " + mode + " (" + modeName + ")");
        });
        fpsText = findViewById(R.id.fpsText);

    }

    private final TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) { }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

//        @Override
//        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//            Bitmap bitmap = textureView.getBitmap(640, 480);
//            if (bitmap != null) {
//                backgroundHandler.post(() -> {
//                    int frameWidth = bitmap.getWidth();
//                    int frameHeight = bitmap.getHeight();
//
//                    ByteBuffer buffer = ByteBuffer.allocate(bitmap.getByteCount());
//                    bitmap.copyPixelsToBuffer(buffer);
//
//                    byte[] processed = nativeProcessAndRender(buffer.array(), frameWidth, frameHeight, mode);
//
//                    if (processed != null) {
//                        Bitmap processedBitmap = Bitmap.createBitmap(frameWidth, frameHeight, Bitmap.Config.ARGB_8888);
//                        processedBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(processed));
//
//
//                        runOnUiThread(() -> {
//                            if (processed != null) {
//                                render.updateFrame(processedBitmap);
//                                glSurfaceView.requestRender();
//                            }
//
//                            long now = System.currentTimeMillis();
//                            if (lastFrameTime != 0) {
//                                long delta = now - lastFrameTime;
//                                float fps = 1000f / delta;
//                                fpsText.setText("FPS: " + String.format("%.1f", fps));
//                                Log.d("FPS", "Frame time: " + delta + " ms (" + fps + " FPS)");
//                            }
//                            lastFrameTime = now;
//                        });
//
//                    } else {
//                        Log.e("MAIN", "Native processing failed or returned null");
//                    }
//
//                    bitmap.recycle();
//                });
//            }
//        }
@Override
public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    Bitmap bitmap = textureView.getBitmap(640, 480);
    if (bitmap != null) {
        backgroundHandler.post(() -> {
            int frameWidth = bitmap.getWidth();
            int frameHeight = bitmap.getHeight();

            ByteBuffer buffer = ByteBuffer.allocate(bitmap.getByteCount());
            bitmap.copyPixelsToBuffer(buffer);

            byte[] processed = nativeProcessAndRender(buffer.array(), frameWidth, frameHeight, mode);

//            Bitmap processedBitmap = null;
//            if (processed != null) {
//                processedBitmap = Bitmap.createBitmap(frameWidth, frameHeight, Bitmap.Config.ARGB_8888);
//                processedBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(processed));
//            }
//
//            runOnUiThread(() -> {
//                if (processedBitmap != null) {
//                    render.updateFrame(processedBitmap);
//                    glSurfaceView.requestRender();
//                }
//
//                long now = System.currentTimeMillis();
//                if (lastFrameTime != 0) {
//                    long delta = now - lastFrameTime;
//                    float fps = 1000f / delta;
//                    fpsText.setText("FPS: " + String.format("%.1f", fps));
//                    Log.d("FPS", "Frame time: " + delta + " ms (" + fps + " FPS)");
//                }
//                lastFrameTime = now;
//            });
            if (processed != null) {
                Bitmap processedBitmap = Bitmap.createBitmap(frameWidth, frameHeight, Bitmap.Config.ARGB_8888);
                processedBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(processed));

                runOnUiThread(() -> {
                    render.updateFrame(processedBitmap);
                    glSurfaceView.requestRender();

                    long now = System.currentTimeMillis();
                    if (lastFrameTime != 0) {
                        long delta = now - lastFrameTime;
                        float fps = 1000f / delta;
                        fpsText.setText("FPS: " + String.format("%.1f", fps));
                        Log.d("FPS", "Frame time: " + delta + " ms (" + fps + " FPS)");
                    }
                    lastFrameTime = now;
                });
            }


            bitmap.recycle();
        });
    }
}

    };

    private void openCamera() {
        Log.d("CAMERA_DEBUG", "openCamera called");
        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            String cameraId = manager.getCameraIdList()[0];

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 200);
                return;
            }

            manager.openCamera(cameraId, stateCallback, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close();
            cameraDevice = null;
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            camera.close();
            cameraDevice = null;
        }
    };

    private void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;

            texture.setDefaultBufferSize(640, 480);
            Surface surface = new Surface(texture);

            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(Collections.singletonList(surface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            if (cameraDevice == null) return;
                            cameraCaptureSessions = session;
                            updatePreview();
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {
                            Log.e("CAMERA_DEBUG", "Camera configuration failed");
                        }
                    }, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        if (cameraDevice == null) return;
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }

    @Override
    protected void onPause() {
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        stopBackgroundThread();
        super.onPause();
    }

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Log.e("CAMERA_DEBUG", "Camera permission denied");
            }
        }
    }


}


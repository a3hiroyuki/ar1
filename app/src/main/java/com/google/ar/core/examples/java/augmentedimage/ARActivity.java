package com.abe.ar.augmentedimage;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.abe.ar.R;
import com.abe.ar.augmentedimage.factory.TreasureChestFactory;
import com.abe.ar.augmentedimage.sceneform.AugmentedImageNode;
import com.abe.ar.common.helpers.CameraPermissionHelper;
import com.abe.ar.common.helpers.FullScreenHelper;
import com.abe.ar.common.helpers.SnackbarHelper;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArActivity extends AppCompatActivity implements AugmentedImageNode.Callback {
    private static final String TAG = ArActivity.class.getSimpleName();
    private static final int SOUND_NUM = 10;
    public static int gKeyScore = 0;
    public static List<Integer> gKeyScoreList = new ArrayList<Integer>();
    private final SnackbarHelper messageSnackbarHelper = new SnackbarHelper();
    ImageView[] mKeyViewArr = new ImageView[3];
    // Rendering. The Renderers are created here, and initialized when the GL surface is created.
    private ArSceneView arSceneView;
    private boolean installRequested;
    private Session session;
    private boolean shouldConfigureSession = false;
    private TreasureChestFactory mTcFactory;
    private ImageView mFitToScanView;
    private RequestManager mGlideRequestManager;
    private SoundPool mSp;
    private Map<String, Integer> mSoundMap = new HashMap<String, Integer>();
    private Map<Integer, String> keyUriMap = new HashMap<Integer, String>() {
        {
            put(0, "file:///android_asset/");
        }

        {
            put(1, "file:///android_asset/bronze_key.png");
        }

        {
            put(2, "file:///android_asset/silver_key.png");
        }

        {
            put(3, "file:///android_asset/gold_key.png");
        }
    };
    private Map<Integer, String> itemUriMap = new HashMap<Integer, String>() {
        {
            put(1, "file:///android_asset/kin.png");
        }

        {
            put(2, "file:///android_asset/kin.png");
        }

        {
            put(3, "file:///android_asset/kin.png");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ar_main);

        Intent intent = getIntent();
        gKeyScore = intent.getIntExtra("key_rank", 0);

        gKeyScoreList.clear();
        for (int i = 1; i <= gKeyScore; i++) {
            gKeyScoreList.add(i);
        }

        //サウンド関連処理
        AudioAttributes attr = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

        mSp = new SoundPool.Builder()
                .setAudioAttributes(attr)
                .setMaxStreams(SOUND_NUM)
                .build();
        try {
            AssetFileDescriptor fd = getAssets().openFd("hazure.mp3");
            int id = mSp.load(fd, 1);
            mSoundMap.put("hazure", id);
            fd = getAssets().openFd("seikai.mp3");
            id = mSp.load(fd, 1);
            mSoundMap.put("seikai", id);
            fd = getAssets().openFd("get.mp3");
            id = mSp.load(fd, 1);
            mSoundMap.put("get", id);
        } catch (IOException e) {
            e.printStackTrace();
        }

        arSceneView = findViewById(R.id.surfaceview);

        installRequested = false;

        mTcFactory = new TreasureChestFactory(this);
        mTcFactory.createAll();

        mFitToScanView = findViewById(R.id.image_view_fit_to_scan);
        mKeyViewArr[0] = findViewById(R.id.image_key1);
        mKeyViewArr[1] = findViewById(R.id.image_key2);
        mKeyViewArr[2] = findViewById(R.id.image_key3);
        mGlideRequestManager = Glide.with(this);
        mGlideRequestManager
                .load(Uri.parse("file:///android_asset/fit_to_scan2.png"))
                .into(mFitToScanView);

        for (int id : gKeyScoreList) {
            mGlideRequestManager
                    .load(Uri.parse(keyUriMap.get(id)))
                    .into(mKeyViewArr[id - 1]);
        }

        initializeSceneView();
    }

    @Override
    protected void onResume() {
        super.onResume();


        if (session == null) {
            Exception exception = null;
            String message = null;
            try {
                switch (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
                    case INSTALL_REQUESTED:
                        installRequested = true;
                        return;
                    case INSTALLED:
                        break;
                }

                // ARCore requires camera permissions to operate. If we did not yet obtain runtime
                // permission on Android M and above, now is a good time to ask the user for it.
                if (!CameraPermissionHelper.hasCameraPermission(this)) {
                    CameraPermissionHelper.requestCameraPermission(this);
                    return;
                }

                session = new Session(/* context = */ this);
            } catch (UnavailableArcoreNotInstalledException
                    | UnavailableUserDeclinedInstallationException e) {
                message = "Please install ARCore";
                exception = e;
            } catch (UnavailableApkTooOldException e) {
                message = "Please update ARCore";
                exception = e;
            } catch (UnavailableSdkTooOldException e) {
                message = "Please update this app";
                exception = e;
            } catch (Exception e) {
                message = "This device does not support AR";
                exception = e;
            }

            if (message != null) {
                messageSnackbarHelper.showError(this, message);
                Log.e(TAG, "Exception creating session", exception);
                return;
            }

            shouldConfigureSession = true;
        }

        if (shouldConfigureSession) {
            configureSession();
            shouldConfigureSession = false;
            arSceneView.setupSession(session);
        }

        // Note that order matters - see the note in onPause(), the reverse applies here.
        try {
            session.resume();
            arSceneView.resume();
        } catch (CameraNotAvailableException e) {
            // In some cases (such as another camera app launching) the camera may be given to
            // a different app instead. Handle this properly by showing a message and recreate the
            // session at the next iteration.
            messageSnackbarHelper.showError(this, "Camera not available. Please restart the app.");
            session = null;
            return;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (session != null) {
            // Note that the order matters - GLSurfaceView is paused first so that it does not try
            // to query the session. If Session is paused before GLSurfaceView, GLSurfaceView may
            // still call session.update() and get a SessionPausedException.
            arSceneView.pause();
            session.pause();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(
                    this, "Camera permissions are needed to run this application", Toast.LENGTH_LONG)
                    .show();
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(this);
            }
            finish();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus);
    }

    private void initializeSceneView() {
        arSceneView.getScene().setOnUpdateListener((this::onUpdateFrame));
    }

    private void onUpdateFrame(FrameTime frameTime) {
        Frame frame = arSceneView.getArFrame();
        Collection<AugmentedImage> updatedAugmentedImages = frame.getUpdatedTrackables(AugmentedImage.class);

        for (AugmentedImage augmentedImage : updatedAugmentedImages) {
            if (augmentedImage.getTrackingState() == TrackingState.TRACKING) {
                AugmentedImageNode node = mTcFactory.getAugmentedImageNode(this, augmentedImage);
                if (node != null) {
                    arSceneView.getScene().addChild(node);
                }
                // Check camera image matches our reference image
//                if (augmentedImage.getName().equals("delorean")) {
//                    AugmentedImageNode node = new AugmentedImageNode(this, "model.sfb");
//                    node.setImage(augmentedImage);
//                    arSceneView.getScene().addChild(node);
//                }else if(augmentedImage.getName().equals("glove")){
//                    AugmentedImageNode node = new AugmentedImageNode(this, "model.sfb");
//                    node.setImage(augmentedImage);
//                    arSceneView.getScene().addChild(node);
//                }

            }
        }
    }

    private void configureSession() {
        Config config = new Config(session);
        if (!setupAugmentedImageDb(config)) {
            messageSnackbarHelper.showError(this, "Could not setup augmented image database");
        }
        config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
        session.configure(config);
    }

    private boolean setupAugmentedImageDb(Config config) {
        AugmentedImageDatabase augmentedImageDatabase;
        augmentedImageDatabase = new AugmentedImageDatabase(session);

        Map<String, String> idMap = mTcFactory.getMap();

        for (String id : idMap.keySet()) {
            String fileName = idMap.get(id);
            Bitmap augmentedImageBitmap = loadAugmentedImage(fileName);
            if (augmentedImageBitmap == null) {
                return false;
            }
            augmentedImageDatabase.addImage(id, augmentedImageBitmap);
        }

//        Bitmap augmentedImageBitmap = loadAugmentedImage("delorean.jpg");
//        if (augmentedImageBitmap == null) {
//            return false;
//        }
//        augmentedImageDatabase.addImage("delorean", augmentedImageBitmap);
//
//        augmentedImageBitmap = loadAugmentedImage("glove.jpg");
//        if (augmentedImageBitmap == null) {
//            return false;
//        }
//        augmentedImageDatabase.addImage("glove", augmentedImageBitmap);

        config.setAugmentedImageDatabase(augmentedImageDatabase);
        return true;
    }

    private Bitmap loadAugmentedImage(String fileName) {
        try (InputStream is = getAssets().open(fileName)) {
            return BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            Log.e(TAG, "IO exception loading augmented image bitmap.", e);
        }
        return null;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSp.unload(mSoundMap.get("hazure"));
        mSp.unload(mSoundMap.get("seikai"));
        mSp.unload(mSoundMap.get("get"));
        mSp.release();
    }

    @Override
    public void openTreasureChest(int rank) {
        playSound("seikai");
        mGlideRequestManager
                .load(Uri.parse(keyUriMap.get(0)))
                .into(mKeyViewArr[rank - 1]);
    }

    @Override
    public void getItem(int itemGrade) {
        playSound("get");
        mGlideRequestManager
                .load(Uri.parse(itemUriMap.get(itemGrade)))
                .into(mKeyViewArr[itemGrade - 1]);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
                intent.putExtra("aaa", itemGrade);
                getApplicationContext().startActivity(intent);
                //finish();
            }
        }).start();
    }

    @Override
    public void playSound(String name) {
        int id = mSoundMap.get(name);
        mSp.play(id, 1, 1, 0, 0, 1);
    }
}

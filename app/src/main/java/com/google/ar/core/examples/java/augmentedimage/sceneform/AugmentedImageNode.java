
package com.google.ar.core.examples.java.augmentedimage.sceneform;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Pose;
import com.google.ar.core.examples.java.augmentedimage.ARActivity;
import com.google.ar.core.examples.java.augmentedimage.R;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;

import java.util.concurrent.CompletableFuture;

public class AugmentedImageNode extends AnchorNode {

    private static final String TAG = "AugmentedImageNode";

    private AugmentedImage image;
    private CompletableFuture<ModelRenderable> modelFuture, modelFuture2;
    private Node mInfoCard;
    private Callback mCallback;
    private Node mNode1, mNode2;
    private int mKeyScore;
    private boolean mIsNodeDisp = true;

    public interface Callback{
        public abstract void openTreasureChest(int keyScore);
        public abstract void playSound(String name);
    }

    public AugmentedImageNode(Context context, String filename, int keyScore) {
        // Upon construction, start loading the modelFuture
        mKeyScore = keyScore;
        modelFuture = ModelRenderable.builder().setRegistryId("modelFuture")
                .setSource(context, Uri.parse(filename))
                .build();
        modelFuture2 = ModelRenderable.builder().setRegistryId("modelFuture2")
                .setSource(context, Uri.parse("Saturn.sfb"))
                .build();
        mCallback = (ARActivity)context;
        if(ARActivity.gKeyScore >= keyScore){
            mInfoCard = createSelectCard(context);
        }else{
            mInfoCard = createInfoCard(context);
        }
    }

    /**
     * Called when the AugmentedImage is detected and should be rendered. A Sceneform node tree is
     * created based on an Anchor created from the image.
     *
     * @param image captured by your camera
     */
    public void setImage(AugmentedImage image, int keyScore) {

        this.image = image;
        if(mIsNodeDisp){
            if (!modelFuture.isDone()) {
                CompletableFuture.allOf(modelFuture).thenAccept((Void aVoid) -> {
                    setImage(image, keyScore);
                }).exceptionally(throwable -> {
                    Log.e(TAG, "Exception loading", throwable);
                    return null;
                });
            }
        }else{
            if (!modelFuture2.isDone()) {
                CompletableFuture.allOf(modelFuture2).thenAccept((Void aVoid) -> {
                    setImage(image, keyScore);
                }).exceptionally(throwable -> {
                    Log.e(TAG, "Exception loading", throwable);
                    return null;
                });
            }
        }

        setAnchor(image.createAnchor(image.getCenterPose()));

        if(mIsNodeDisp){
            if(mNode1 == null){
                mNode1 = new Node();
                mNode1.setParent(this);
            }
            mNode1.setOnTapListener(new OnTapListener() {
                @Override
                public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
                    mInfoCard.setEnabled(true);
                    mCallback.playSound("hazure");
                }
            });

            Pose pose = Pose.makeTranslation(0.0f, 0.0f, -0.15f);

            mNode1.setLocalPosition(new Vector3(pose.tx(), pose.ty(), pose.tz()));
            mNode1.setLocalRotation(new Quaternion(pose.qx(), pose.qy(), pose.qz(), pose.qw()));
            mNode1.setRenderable(modelFuture.getNow(null));
            mInfoCard.setParent(mNode1);
            mInfoCard.setLocalPosition(new Vector3(0.0f, 0.0f, -0.2f));
        }else{
            if(mNode2 == null){
                mNode2 = new Node();
                mNode2.setParent(this);
            }
            mNode2.setOnTapListener(new OnTapListener() {
                @Override
                public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
                    mCallback.openTreasureChest(mKeyScore);
                }
            });

            Pose pose = Pose.makeTranslation(0.0f, 0.0f, -0.15f);

            mNode2.setLocalPosition(new Vector3(pose.tx(), pose.ty(), pose.tz()));
            mNode2.setLocalRotation(new Quaternion(pose.qx(), pose.qy(), pose.qz(), pose.qw()));
            mNode2.setRenderable(modelFuture2.getNow(null));
        }
    }

    public AugmentedImage getImage() {
        return image;
    }


    private Node createInfoCard(Context con) {
        Node infoCard = new Node();
        ViewRenderable.builder()
                .setView(con, R.layout.card_view)
                .build()
                .thenAccept(
                        (renderable) -> {
                            infoCard.setRenderable(renderable);
                            TextView textView = (TextView) renderable.getView();
                        })
                .exceptionally(
                        (throwable) -> {
                            throw new AssertionError("Could not load plane card view.", throwable);
                        });
        infoCard.setEnabled(false);
        return infoCard;
    }

    private Node createSelectCard(Context con){
        Node infoCard = new Node();
        ViewRenderable.builder()
                .setView(con, R.layout.select_view)
                .build()
                .thenAccept(
                        (renderable) -> {
                            infoCard.setRenderable(renderable);
                            View selectView = renderable.getView();
                            Button btn = selectView.findViewById(R.id.btn1);
                            btn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mIsNodeDisp = false;
                                    removeChild(mNode1);
                                    mNode1.setRenderable(null);
                                    mNode1.setEnabled(false);
                                    setImage(image, mKeyScore);
                                }
                            });
                        })
                .exceptionally(
                        (throwable) -> {
                            throw new AssertionError("Could not load plane card view.", throwable);
                        });
        infoCard.setEnabled(false);
        return infoCard;
    }

    @Override
    public void onUpdate(FrameTime frameTime) {
        if (mInfoCard == null) {
            return;
        }
        if (getScene() == null) {
            return;
        }
        Vector3 cameraPosition = getScene().getCamera().getWorldPosition();
        Vector3 cardPosition = mInfoCard.getWorldPosition();
        Vector3 direction = Vector3.subtract(cameraPosition, cardPosition);
        Quaternion lookRotation = Quaternion.lookRotation(direction, Vector3.up());
        mInfoCard.setWorldRotation(lookRotation);
    }
}


package com.google.ar.core.examples.java.augmentedimage.sceneform;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Pose;
import com.google.ar.core.examples.java.augmentedimage.ArActivity;
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
    private int mTreasureChestRank;
    private boolean mIsNodeDisp = true;
    private Context mContext;
    String mess1 = "この鍵で宝箱を開けられます\neverタップしてください";
    String mess2 = "この鍵で宝箱は開けられません";

    public interface Callback{
        public abstract void openTreasureChest();
        public abstract void getItem(int itemGrade);
        public abstract void playSound(String name);
    }

    public AugmentedImageNode(Context context, String filename, int rank) {
        // Upon construction, start loading the modelFuture
        mTreasureChestRank = rank;
        mContext = context;
        modelFuture = ModelRenderable.builder().setRegistryId("modelFuture")
                .setSource(context, Uri.parse(filename))
                .build();
        modelFuture2 = ModelRenderable.builder().setRegistryId("modelFuture2")
                .setSource(context, Uri.parse("Saturn.sfb"))
                .build();
        mCallback = (ArActivity)context;
        mInfoCard = createInfoCard(context, mess2);
        mCallback.playSound("seikai");
    }

    /**
     * Called when the AugmentedImage is detected and should be rendered. A Sceneform node tree is
     * created based on an Anchor created from the image.
     *
     * @param image captured by your camera
     */
    public void setImage(AugmentedImage image) {

        this.image = image;
        if(mIsNodeDisp){
            if (!modelFuture.isDone()) {
                CompletableFuture.allOf(modelFuture).thenAccept((Void aVoid) -> {
                    setImage(image);
                }).exceptionally(throwable -> {
                    Log.e(TAG, "Exception loading", throwable);
                    return null;
                });
            }
        }else{
            if (!modelFuture2.isDone()) {
                CompletableFuture.allOf(modelFuture2).thenAccept((Void aVoid) -> {
                    setImage(image);
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
                mNode1.setOnTapListener(new OnTapListener() {
                    @Override
                    public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
                        if(ArActivity.gKeyScore >= mTreasureChestRank){
                            mIsNodeDisp = false;
                            Pose pose = Pose.makeTranslation(100.0f, 100.0f, -100.0f);
                            mNode1.setLocalPosition(new Vector3(pose.tx(), pose.ty(), pose.tz()));
                            mNode1.setParent(AugmentedImageNode.this);
                            setImage(image);
                            //mCallback.playSound("seikai");
                            mCallback.openTreasureChest();
                        }else{
                            mCallback.playSound("hazure");
                            mInfoCard.setEnabled(false);
                        }
                    }
                });
            }
            Pose pose = Pose.makeTranslation(0.0f, 0.0f, -0.15f);

            mNode1.setLocalPosition(new Vector3(pose.tx(), pose.ty(), pose.tz()));
            //mNode1.setLocalRotation(new Quaternion(pose.qx(), pose.qy(), pose.qz(), pose.qw()));
            mNode1.setRenderable(modelFuture.getNow(null));
            mInfoCard.setParent(mNode1);
            mInfoCard.setLocalPosition(new Vector3(0.0f, 0.0f, -0.2f));
        }else{
            if(mNode2 == null){
                mNode2 = new Node();
                mNode2.setParent(this);
                mNode2.setOnTapListener(new OnTapListener() {
                    @Override
                    public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
                        mCallback.getItem(mTreasureChestRank);
                        Pose pose = Pose.makeTranslation(100.0f, 100.0f, -100.0f);
                        mNode2.setLocalPosition(new Vector3(pose.tx(), pose.ty(), pose.tz()));
                        mNode2.setParent(AugmentedImageNode.this);
                    }
                });
            }
            Pose pose = Pose.makeTranslation(0.0f, 0.0f, -0.15f);

            mNode2.setLocalPosition(new Vector3(pose.tx(), pose.ty(), pose.tz()));
            //mNode2.setLocalRotation(new Quaternion(pose.qx(), pose.qy(), pose.qz(), pose.qw()));
            mNode2.setRenderable(modelFuture2.getNow(null));
        }
    }

    public AugmentedImage getImage() {
        return image;
    }


    private Node createInfoCard(Context con, String mess) {
        Node infoCard = new Node();
        ViewRenderable.builder()
                .setView(con, R.layout.card_view)
                .build()
                .thenAccept(
                        (renderable) -> {
                            infoCard.setRenderable(renderable);
                            View view = renderable.getView();
                            TextView tv = (TextView) view.findViewById(R.id.infoCard);
                            tv.setText(mess);
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
                                    mNode1.setEnabled(false);
                                    Pose pose = Pose.makeTranslation(100.0f, 100.0f, -100.0f);
                                    mNode1.setLocalPosition(new Vector3(pose.tx(), pose.ty(), pose.tz()));
                                    mNode1.setParent(AugmentedImageNode.this);
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

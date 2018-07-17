
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
    private CompletableFuture<ModelRenderable> modelFuture1, modelFuture2;
    private Node mInfoCard;
    private Callback mCallback;
    private Node mNode1, mNode2;
    private int mTreasureChestRank;
    private boolean mIsNodeDisp = true;
    private Context mContext;
    String mess1 = "この鍵で宝箱を開けられます\neverタップしてください";
    String mess2 = "この鍵で\nこの宝箱は\n開けられません";
    private Vector3 mInitPosition = new Vector3();
    private Quaternion mInitRotation = new Quaternion();

    public interface Callback{
        public abstract void openTreasureChest();
        public abstract void getItem(int itemGrade);
        public abstract void playSound(String name);
    }

    public AugmentedImageNode(Context context, String objFilename, String rewardFileName, int rank) {
        // Upon construction, start loading the modelFuture
        mTreasureChestRank = rank;
        mContext = context;
        modelFuture1 = ModelRenderable.builder().setRegistryId("modelFuture")
                .setSource(context, Uri.parse(objFilename))
                .build();
        modelFuture2 = ModelRenderable.builder().setRegistryId("modelFuture2")
                .setSource(context, Uri.parse(rewardFileName))
                .build();
        mCallback = (ArActivity)context;
        mCallback.playSound("seikai");
    }

    public void setRotation(Quaternion q){
        mInitRotation = q;
    }
    public void setPosition(Vector3 p){
        mInitPosition = p;
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
            if (!modelFuture1.isDone()) {
                CompletableFuture.allOf(modelFuture1).thenAccept((Void aVoid) -> {
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

        if(mIsNodeDisp) {
            if(mNode1 == null){
                mNode1 = createNode(mInitPosition, mInitRotation, new OnTapListener() {
                    @Override
                    public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
                        if (ArActivity.gKeyScore >= mTreasureChestRank) {
                            mIsNodeDisp = false;
                            Pose pose = Pose.makeTranslation(100.0f, 100.0f, -100.0f);
                            mNode1.setLocalPosition(new Vector3(pose.tx(), pose.ty(), pose.tz()));
                            mNode1.setParent(AugmentedImageNode.this);
                            setImage(image);
                            mCallback.openTreasureChest();
                        } else {
                            mCallback.playSound("hazure");
                            mInfoCard.setEnabled(true);
                        }
                    }
                });
            }
            mNode1.setRenderable(modelFuture1.getNow(null));
            if(mInfoCard == null){
                mInfoCard = createInfoCard(mContext, mess2);
            }
        }else{
            if(mNode2 == null){
                Vector3 posi = new Vector3(0.0f, 0.0f, -0.1f);
                mNode2 = createNode(posi, new Quaternion(), new OnTapListener() {
                    @Override
                    public void onTap(HitTestResult hitTestResult, MotionEvent motionEvent) {
                        mCallback.getItem(mTreasureChestRank);
                        Pose pose = Pose.makeTranslation(100.0f, 100.0f, -100.0f);
                        mNode2.setLocalPosition(new Vector3(pose.tx(), pose.ty(), pose.tz()));
                        mNode2.setParent(AugmentedImageNode.this);
                    }
                });
            }
            mNode2.setRenderable(modelFuture2.getNow(null));
        }
    }

    private Node createNode(Vector3 initPosition, Quaternion initQuaternion, OnTapListener listener){
        Node node = new Node();
        node.setParent(this);
        node.setOnTapListener(listener);
        node.setLocalPosition(initPosition);
        node.setLocalRotation(initQuaternion);
        return node;
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
        infoCard.setParent(mNode1);
        Vector3 vec1 = new Vector3(0.0f, 0.5f, 0.0f);
        Vector3 vec2 = Vector3.add(mInitPosition, vec1);
        infoCard.setLocalPosition(vec2);
        return infoCard;
    }

//    private Node createSelectCard(Context con){
//        Node infoCard = new Node();
//        ViewRenderable.builder()
//                .setView(con, R.layout.select_view)
//                .build()
//                .thenAccept(
//                        (renderable) -> {
//                            infoCard.setRenderable(renderable);
//                            View selectView = renderable.getView();
//                            Button btn = selectView.findViewById(R.id.btn1);
//                            btn.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    mIsNodeDisp = false;
//                                    removeChild(mNode1);
//                                    mNode1.setEnabled(false);
//                                    Pose pose = Pose.makeTranslation(100.0f, 100.0f, -100.0f);
//                                    mNode1.setLocalPosition(new Vector3(pose.tx(), pose.ty(), pose.tz()));
//                                    mNode1.setParent(AugmentedImageNode.this);
//                                }
//                            });
//                        })
//                .exceptionally(
//                        (throwable) -> {
//                            throw new AssertionError("Could not load plane card view.", throwable);
//                        });
//        infoCard.setEnabled(false);
//        return infoCard;
//    }

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
        Vector3 objPosition = mNode1.getWorldPosition();
        Vector3 direction1 = Vector3.subtract(cameraPosition, cardPosition);
        Vector3 direction2 = Vector3.subtract(cameraPosition, objPosition);
        Quaternion lookRotation1 = Quaternion.lookRotation(direction1, Vector3.up());
        Quaternion lookRotation2 = Quaternion.lookRotation(direction2, Vector3.up());
        mInfoCard.setWorldRotation(lookRotation1);
        //mNode1.setWorldRotation(lookRotation2);
    }
}

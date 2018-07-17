package com.google.ar.core.examples.java.augmentedimage.treasurechest;

import android.content.Context;

import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Pose;
import com.google.ar.core.examples.java.augmentedimage.sceneform.AugmentedImageNode;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;

public class GoldenTreasureChest extends TreasureChestBase {

    private static String OBJ_NAME = "chest3.sfb";
    private static String REWARD_NAME = "Saturn.sfb";
    private static int RANK = 3;

    public GoldenTreasureChest(Context con, String id, String fileName) {
        super(con, id, fileName);
    }

    @Override
    public AugmentedImageNode createAugmentedImageNode(Context con, AugmentedImage image) {
        if(mAugmentedImageNode == null){
            mAugmentedImageNode = new AugmentedImageNode(con, OBJ_NAME, REWARD_NAME, RANK);
            Quaternion q1 = Quaternion.axisAngle(new Vector3(1, 0, 0), 90);
            Quaternion q2 = Quaternion.axisAngle(new Vector3(0, 0, 1), 180);
            Quaternion q = Quaternion.multiply(q1, q2);
            mAugmentedImageNode.setRotation(q);
            Pose pose = Pose.makeTranslation(0.25f, -0.3f, -0.1f);
            mAugmentedImageNode.setPosition(new Vector3(pose.tx(), pose.ty(), pose.tz()));
        }
        mAugmentedImageNode.setImage(image);
        return mAugmentedImageNode;
    }
}

package com.google.ar.core.examples.java.augmentedimage.treasurechest;

import android.content.Context;

import com.google.ar.core.AugmentedImage;
import com.google.ar.core.examples.java.augmentedimage.sceneform.AugmentedImageNode;

public class SilverTreasureChest extends TreasureChestBase {

    private static String OBJ_NAME = "andy2.sfb";
    private static int RANK = 2;

    public SilverTreasureChest(Context con, String id, String fileName) {
        super(con, id, fileName);
    }

    @Override
    public AugmentedImageNode createAugmentedImageNode(Context con, AugmentedImage image) {
        if(mAugmentedImageNode == null){
            mAugmentedImageNode = new AugmentedImageNode(con, OBJ_NAME, RANK);
        }
        mAugmentedImageNode.setImage(image);
        return mAugmentedImageNode;
    }
}

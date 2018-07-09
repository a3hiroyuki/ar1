package com.google.ar.core.examples.java.augmentedimage.treasurechest;

import android.content.Context;

import com.google.ar.core.AugmentedImage;
import com.google.ar.core.examples.java.augmentedimage.sceneform.AugmentedImageNode;

public class GoldenTreasureChest extends TreasureChestBase {

    private static String OBJ_NAME = "andy1.sfb";
    private static int KEY_SCORE = 3;


    public GoldenTreasureChest(Context con, String id, String fileName) {
        super(con, id, fileName);
    }

    @Override
    public AugmentedImageNode createAugmentedImageNode(Context con, AugmentedImage image) {
        if(mAugmentedImageNode == null){
            mAugmentedImageNode = new AugmentedImageNode(con, OBJ_NAME, KEY_SCORE);
        }
        mAugmentedImageNode.setImage(image, KEY_SCORE);
        return mAugmentedImageNode;
    }
}

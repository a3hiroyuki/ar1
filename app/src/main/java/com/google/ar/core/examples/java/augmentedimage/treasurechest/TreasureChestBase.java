package com.google.ar.core.examples.java.augmentedimage.treasurechest;

import android.content.Context;

import com.google.ar.core.AugmentedImage;
import com.google.ar.core.examples.java.augmentedimage.ARActivity;
import com.google.ar.core.examples.java.augmentedimage.sceneform.AugmentedImageNode;

public abstract class TreasureChestBase {

    private String mId;
    private String mFileName;
    private Context mCon;
    protected AugmentedImageNode mAugmentedImageNode = null;

    public TreasureChestBase(Context con, String id, String fileName){
        mCon = con;
        mId = id;
        mFileName = fileName;
    }

    public String getImageFileName() {
        return mFileName;
    }
    public String getId() {
        return mId;
    }

    public abstract AugmentedImageNode createAugmentedImageNode(Context con, AugmentedImage image);
}

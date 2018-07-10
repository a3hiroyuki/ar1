package com.google.ar.core.examples.java.augmentedimage.factory;

import android.content.Context;

import com.google.ar.core.AugmentedImage;
import com.google.ar.core.examples.java.augmentedimage.treasurechest.GoldenTreasureChest;
import com.google.ar.core.examples.java.augmentedimage.treasurechest.SilverTreasureChest;
import com.google.ar.core.examples.java.augmentedimage.treasurechest.TreasureChestBase;
import com.google.ar.core.examples.java.augmentedimage.sceneform.AugmentedImageNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class
TreasureChestFactory extends Factory {


    protected enum ChestColor  { Gold, Silver, Bronse};
    private List<TreasureChestBase> mTcList = new ArrayList<TreasureChestBase>();
    private Context mCon;

    public TreasureChestFactory(Context con){
        mCon = con;
    }

    @Override
    protected TreasureChestBase createTreasureChest(ChestColor color, String id, String imageFile) {
        if(color == ChestColor.Gold){
            return new GoldenTreasureChest(mCon, id, imageFile);
        }else if(color == ChestColor.Silver){
            return new SilverTreasureChest(mCon, id, imageFile);
        }else{
            return null;
        }
    }

    @Override
    protected void registerTreasureChest(TreasureChestBase tc) {
        mTcList.add(tc);
    }

    public void createAll(){
        create(ChestColor.Gold, "delorean", "delorean.jpg");
        create(ChestColor.Silver, "docomodake", "docomodake.jpg");
    }

    public void checkAll(String id){
        for(TreasureChestBase tcb : mTcList){
            if(tcb.getId().equals(id)){
                break;
            }
        }
    }

    public Map getMap(){
        Map<String, String> map = new HashMap<String, String>();
        for(TreasureChestBase tcb : mTcList){
            map.put(tcb.getId(), tcb.getImageFileName());
        }
        return map;
    }

    public AugmentedImageNode getAugmentedImageNode(Context con, AugmentedImage image){
        for(TreasureChestBase tcb : mTcList){
            if(image.getName().equals(tcb.getId())){
                return tcb.createAugmentedImageNode(con, image);
            }
        }
        return null;
    }

}

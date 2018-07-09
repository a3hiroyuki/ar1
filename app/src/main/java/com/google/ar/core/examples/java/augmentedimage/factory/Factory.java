package com.google.ar.core.examples.java.augmentedimage.factory;

import com.google.ar.core.examples.java.augmentedimage.treasurechest.TreasureChestBase;

public abstract class Factory {
    public final TreasureChestBase create(TreasureChestFactory.ChestColor color, String id, String imgFile) {
        TreasureChestBase t = createTreasureChest(color, id, imgFile);
        registerTreasureChest(t);
        return t;
    }
    protected abstract TreasureChestBase createTreasureChest(TreasureChestFactory.ChestColor color, String id, String imageFile);
    protected abstract void registerTreasureChest(TreasureChestBase tc);
}

package com.github.qompute.boxify.data;

import java.io.File;

public class Constants {

    public static String MAIN_PATH;
    public static String LOCALE_DATA;
    public static String ITEM_DATA;
    public static String IMAGE_PATH;
    public static String LOCALE_IMAGES;
    public static String ITEM_IMAGES;

    public static LocaleList globalLocaleList;

    public static void generateConstants(String internalStoragePath) {
        MAIN_PATH = internalStoragePath + "/Boxify";
        LOCALE_DATA = MAIN_PATH + "/locales";
        ITEM_DATA = MAIN_PATH + "/items";
        IMAGE_PATH = MAIN_PATH + "/images";
        LOCALE_IMAGES = IMAGE_PATH + "/locales";
        ITEM_IMAGES = IMAGE_PATH + "/items";
        createDirectories();
    }

    public static void createDirectories() {
        File mainPath = new File(MAIN_PATH);
        if (!mainPath.exists()) {
            mainPath.mkdirs();
        }
        File localeData = new File(LOCALE_DATA);
        if (!localeData.exists()) {
            localeData.mkdirs();
        }
        File itemData = new File(ITEM_DATA);
        if (!itemData.exists()) {
            itemData.mkdirs();
        }
        File imageData = new File(IMAGE_PATH);
        if (!imageData.exists()) {
            imageData.mkdirs();
        }
        File localeImages = new File(LOCALE_IMAGES);
        if (!localeImages.exists()) {
            localeImages.mkdirs();
        }
        File itemImages = new File(ITEM_IMAGES);
        if (!itemImages.exists()) {
            itemImages.mkdirs();
        }
    }
}

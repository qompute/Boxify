package com.github.qompute.boxify.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

public class ItemData {
    private String name;
    private Bitmap picture;
    private long id;
    private long localeID;

    public ItemData(String name, Bitmap picture, long localeID, long id) {
        this.name = name;
        this.picture = picture;
        this.id = id;
        this.localeID = localeID;
    }

    public ItemData(String name, long localeID, long id) {
        this(name, getImageByID(id), localeID, id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getID() {
        return id;
    }

    public long getLocaleID() {
        return localeID;
    }

    public void setLocaleID(long localeID) {
        this.localeID = localeID;
    }

    public Bitmap getPicture() {
        if (picture != null) {
            return picture;
        } else {
            return null;
        }
    }

    public void setPicture(Bitmap picture) {
        this.picture = picture;
    }

    public void saveItemData() {
        //Hashmap to store locale data into file system
        HashMap<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("id", id);
        data.put("locale", localeID);
        File localeData = new File(Constants.ITEM_DATA + "/" + id);
        FileOutputStream fout;
        ObjectOutputStream oout;
        try {
            fout = new FileOutputStream(localeData);
            oout = new ObjectOutputStream(fout);
            oout.writeObject(data);
            fout.close();
            oout.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Store image into file system
        File imageData = new File(Constants.ITEM_IMAGES + "/" + id);
        try {
            fout =  new FileOutputStream(imageData);
            picture.compress(Bitmap.CompressFormat.PNG, 100, fout); // bmp is your Bitmap instance
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteItemData() {
        File itemData = new File(Constants.ITEM_DATA + "/" + id);
        if (itemData.exists()) {
            itemData.delete();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o.getClass() != this.getClass()) {
            return false;
        }
        return ((ItemData)o).getID() == this.getID();
    }

    @Override
    public int hashCode() {
        return (int)getID();
    }

    public static ItemData getItemDataByIDFromSystem(long id) {
        File localeFile = new File(Constants.ITEM_DATA + "/" + id);
        return getItemDataFromSystem(localeFile);
    }

    public static ItemData getItemDataFromSystem(File itemFile) {
        if (itemFile.exists()) {
            try {
                FileInputStream fis = new FileInputStream(itemFile);
                ObjectInputStream ois = new ObjectInputStream(fis);

                HashMap<String, Object> data = (HashMap) ois.readObject();
                String name = (String)data.get("name");
                long idNum = ((Long)data.get("id")).longValue();
                long localeID = ((Long)data.get("locale")).longValue();
                Bitmap picture = getImageByID(idNum);
                return new ItemData(name, picture, localeID, idNum);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static Bitmap getImageByID(long id) {
        File imageData = new File(Constants.ITEM_IMAGES + "/" + id);
        if(imageData.exists()) {
            return BitmapFactory.decodeFile(imageData.getAbsolutePath());
        }
        return null;
    }
}

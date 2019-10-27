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

/**
 * Data tag to store locales. Each tag includes:
 * - Name
 * - Reference picture
 * - ID
 */
public class LocaleData {
    private String name;
    private Bitmap picture;
    private long id;

    public LocaleData(String name, Bitmap picture, long id) {
        this.name = name;
        this.picture = picture;
        this.id = id;
    }

    public LocaleData(String name, long id) {
        this.name = name;
        this.id = id;
        picture = getImageByID(id);
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

    public void saveLocaleData() {
        //Hashmap to store locale data into file system
        HashMap<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("id", id);
        File localeData = new File(Constants.LOCALE_DATA + "/" + id);
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
        File imageData = new File(Constants.LOCALE_IMAGES + "/" + id);
        try {
            fout =  new FileOutputStream(imageData);
            picture.compress(Bitmap.CompressFormat.PNG, 100, fout); // bmp is your Bitmap instance
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteLocaleData() {
        File localeData = new File(Constants.LOCALE_DATA + "/" + id);
        if (localeData.exists()) {
            localeData.delete();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o.getClass() != this.getClass()) {
            return false;
        }
        return ((LocaleData)o).getID() == this.getID();
    }

    @Override
    public int hashCode() {
        return (int)getID();
    }

    public static LocaleData getLocaleDataByIDFromSystem(long id) {
        File localeFile = new File(Constants.LOCALE_DATA + "/" + id);
        return getLocaleDataFromSystem(localeFile);
    }

    public static LocaleData getLocaleDataFromSystem(File localeFile) {
        if (localeFile.exists()) {
            try {
                FileInputStream fis = new FileInputStream(localeFile);
                ObjectInputStream ois = new ObjectInputStream(fis);

                HashMap<String, Object> data = (HashMap) ois.readObject();
                String name = (String)data.get("name");
                long idNum = ((Long)data.get("id")).longValue();
                Bitmap picture = getImageByID(idNum);
                return new LocaleData(name, picture, idNum);

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

    public static Bitmap getImageByID(long id) {
        File imageData = new File(Constants.LOCALE_IMAGES + "/" + id);
        if(imageData.exists()) {
            return BitmapFactory.decodeFile(imageData.getAbsolutePath());
        }
        return null;
    }
}

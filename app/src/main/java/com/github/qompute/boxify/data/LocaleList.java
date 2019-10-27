package com.github.qompute.boxify.data;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocaleList {
    private Map<Long, LocaleData> idMap;
    private Map<Long, ItemList> itemMaps;
    private List<Long> idList;
    private ItemList fullItemList;

    public LocaleList() {
        idMap =  new HashMap<>();
        itemMaps = new HashMap<>();
        idList = new ArrayList<>();
        fullItemList = new ItemList();
    }

    public void addLocale(LocaleData locale) {
        if (!idMap.containsKey(locale.getID())) {
            idList.add(locale.getID());
        }
        idMap.put(locale.getID(), locale);
        itemMaps.put(locale.getID(), new ItemList());
    }

    public void addItem(long localeID, ItemData item) {
        if (fullItemList.getItem(item.getID()) != null) {
            itemMaps.get(item.getLocaleID()).removeItem(item);
            fullItemList.removeItem(item);
        }
        if (itemMaps.containsKey(localeID)) {
            item.setLocaleID(localeID);
            itemMaps.get(localeID).addItem(item);
            fullItemList.addItem(item);
        }
    }

    public void removeLocale(long localeID) {
        if (idMap.containsKey(localeID)) {
            idMap.get(localeID).deleteLocaleData();
            idMap.remove(localeID);
            idList.remove(localeID);
            ItemList itemList = itemMaps.get(localeID);
            for (long itemID : itemList.getIDList()) {
                fullItemList.removeItem(itemID);
            }
            itemList.removeAllItems();
            itemMaps.remove(localeID);
        }
    }

    public void removeItem(ItemData itemData) {
        itemMaps.get(itemData.getLocaleID()).removeItem(itemData.getID());
        fullItemList.removeItem(itemData.getID());
    }

    public List<Long> getIDList() {
        return idList;
    }

    public LocaleData getLocaleData(long id) {
        return idMap.get(id);
    }

    public ItemList getItemListFromLocale(long id) {
        return itemMaps.get(id);
    }

    public void addItem(ItemData item) {
        addItem(item.getLocaleID(), item);
    }

    public ItemList getFullItemList() {
        return fullItemList;
    }

    public boolean containsID(long id) {
        return idMap.containsKey(id);
    }

    public static LocaleList makeLocaleListDataFromSystem() {
        LocaleList localeList = new LocaleList();
        //Populate locales first
        File localePath = new File(Constants.LOCALE_DATA);
        if (localePath.exists() && localePath.isDirectory()) {
            for (File file : localePath.listFiles()) {
                if (file.isFile()) {
                    LocaleData data = LocaleData.getLocaleDataFromSystem(file);
                    if (data != null) {
                        localeList.addLocale(data);
                    }
                }
            }
        }
        //Populate items second
        File itemPath = new File(Constants.ITEM_DATA);
        if (itemPath.exists() && itemPath.isDirectory()) {
            for (File file : itemPath.listFiles()) {
                if (file.isFile()) {
                    ItemData data = ItemData.getItemDataFromSystem(file);
                    if (data != null) {
                        long localeID = data.getLocaleID();
                        localeList.addItem(localeID, data);
                    }
                }
            }
        }
        return localeList;
    }
}

package com.github.qompute.boxify.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ItemList {

    private HashMap<Long, ItemData> itemMap;
    private List<Long> idList;

    public ItemList() {
        itemMap = new HashMap<>();
        idList = new ArrayList<>();
    }

    public void addItem(ItemData item) {
        itemMap.put(item.getID(), item);
        idList.add(item.getID());
    }

    public void insertItem(int pos, ItemData item) {
        itemMap.put(item.getID(), item);
        idList.add(pos, item.getID());
    }

    public void removeItem(ItemData item) {
        removeItem(item.getID());
    }

    public void removeItem(long id) {
        itemMap.remove(id);
        idList.remove(id);
    }

    public void removeAllItems() {
        for (long id : idList) {
            itemMap.remove(id);
        }
        idList.clear();
    }

    public boolean containsID(long id) {
        return itemMap.containsKey(id);
    }

    public List<Long> getIDList() {
        return idList;
    }

    public ItemData getItem(long id) {
        return itemMap.get(id);
    }
}

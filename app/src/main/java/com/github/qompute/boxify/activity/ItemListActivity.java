package com.github.qompute.boxify.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.qompute.boxify.R;
import com.github.qompute.boxify.data.BitmapLabelPair;
import com.github.qompute.boxify.data.Constants;
import com.github.qompute.boxify.data.ItemData;
import com.github.qompute.boxify.data.ItemList;
import com.github.qompute.boxify.data.LocaleData;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class ItemListActivity extends AppCompatActivity {

    public class ItemDataAdapter extends RecyclerView.Adapter<ItemDataAdapter.ItemDataViewHolder>{

        public class ItemDataViewHolder extends RecyclerView.ViewHolder {

            public TextView textName;
            public ImageView image;
            public ItemData data;
            public int position;
            public ItemDataViewHolder(View v) {
                super(v);
                textName = v.findViewById(R.id.textName);
                image = v.findViewById(R.id.image);
                image.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        Intent intent = new Intent(ItemListActivity.this, AddItemActivity.class);
                        intent.putExtra("position", position);
                        intent.putExtra("itemID", data.getID());
                        intent.putExtra("localeID", localeID);
                        startActivityForResult(intent, ITEM_CHANGE_CODE);
                    }
                });
            }

            public void setData(ItemData data, int position) {
                this.data = data;
                this.position = position;
                textName.setText(data.getName());
                image.setImageBitmap(data.getPicture());
            }
        }

        private ItemList items;

        public ItemDataAdapter(ItemList items) {
            this.items = items;
        }

        @Override
        public ItemDataAdapter.ItemDataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View contactView = inflater.inflate(R.layout.row_layout, parent, false);
            return new ItemDataAdapter.ItemDataViewHolder(contactView);
        }

        /** Displays data based on the position */
        @Override
        public void onBindViewHolder(ItemDataAdapter.ItemDataViewHolder viewHolder, int position) {
            /** Get data from position */
            long id = items.getIDList().get(position);
            ItemData data = items.getItem(id);

            /** Set view */
            viewHolder.setData(data, position);
        }

        // Returns the total count of items in the list
        @Override
        public int getItemCount() {
            return items.getIDList().size();
        }
    }

    public class SearchDataAdapter extends RecyclerView.Adapter<SearchDataAdapter.SearchDataViewHolder>{

        public class SearchDataViewHolder extends RecyclerView.ViewHolder {

            public TextView textName;
            public ImageView image;
            public ItemData data;
            public int position;
            public SearchDataViewHolder(View v) {
                super(v);
                textName = v.findViewById(R.id.textName);
                image = v.findViewById(R.id.image);
            }

            public void setData(ItemData data, int position) {
                this.data = data;
                this.position = position;
                textName.setText(data.getName());
                image.setImageBitmap(data.getPicture());
            }
        }

        private ItemList searchHits;
        private ItemList allItems;

        public SearchDataAdapter(ItemList allItems) {
            this.allItems = allItems;
            searchHits = new ItemList();
        }

        @Override
        public SearchDataAdapter.SearchDataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View contactView = inflater.inflate(R.layout.search_row_layout, parent, false);
            return new SearchDataAdapter.SearchDataViewHolder(contactView);
        }

        /** Displays data based on the position */
        @Override
        public void onBindViewHolder(SearchDataAdapter.SearchDataViewHolder viewHolder, int position) {
            /** Get data from position */
            long id = searchHits.getIDList().get(position);
            ItemData data = searchHits.getItem(id);

            /** Set view */
            viewHolder.setData(data, position);
        }

        // Returns the total count of items in the list
        @Override
        public int getItemCount() {
            return searchHits.getIDList().size();
        }

        public void updateSearchList(String query) {
            int insertC = 0;
            query = query.toLowerCase();
            for (long itemID : allItems.getIDList()) {
                String checkName = allItems.getItem(itemID).getName();
                if (checkName.toLowerCase().contains(query)) {
                    if (insertC >= getItemCount() || searchHits.getIDList().get(insertC) != itemID) {
                        searchHits.insertItem(insertC, allItems.getItem(itemID));
                        notifyItemInserted(insertC);
                        insertC += 1;
                    } else {
                        insertC += 1;
                    }
                } else {
                    if (insertC < getItemCount() && searchHits.getIDList().get(insertC) == itemID) {
                        searchHits.removeItem(allItems.getItem(itemID));
                        notifyItemRemoved(insertC);
                    }
                }
            }
        }
    }

    private long localeID;
    private ItemList itemList;
    private ItemDataAdapter recyclerAdapter;
    private SearchDataAdapter searchDataAdapter;
    private RecyclerView recyclerView;

    private boolean modifiedLocale;
    private boolean deletedLocale;

    private static final int ITEM_CHANGE_CODE = 130;
    private static final int MULTI_TAKE = 134;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Constants.generateConstants(getFilesDir().getAbsolutePath());
        setContentView(R.layout.activity_item_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /** Get necessary data */
        localeID = getIntent().getLongExtra("localeID", -1);
        itemList = Constants.globalLocaleList.getItemListFromLocale(localeID);

        /** Change title */
        getSupportActionBar().setTitle(Constants.globalLocaleList.getLocaleData(localeID).getName());

        /** Code to work on the floating action button, used to add new items into the data */
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent data = new Intent(ItemListActivity.this, AddItemActivity.class);
                data.putExtra("localeID", localeID);
                data.putExtra("itemID", -1);

                startActivityForResult(data, ITEM_CHANGE_CODE);
            }
        });

        /** Setup recycler view */
        recyclerView = (RecyclerView) findViewById(R.id.body).findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerAdapter = new ItemDataAdapter(itemList);
        searchDataAdapter = new SearchDataAdapter(itemList);
        recyclerView.setAdapter(recyclerAdapter);

        /** Setup search bar */
        SearchView searchBar = findViewById(R.id.searchBar);
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (query.length() > 0) {
                    recyclerView.setAdapter(searchDataAdapter);
                    searchDataAdapter.updateSearchList(query);
                    findViewById(R.id.fab).setVisibility(View.INVISIBLE);
                } else {
                    recyclerView.setAdapter(recyclerAdapter);
                    findViewById(R.id.fab).setVisibility(View.VISIBLE);
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_item_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_edit_locale) {
            Intent intent = new Intent(ItemListActivity.this, AddLocaleActivity.class);
            intent.putExtra("position", getIntent().getIntExtra("position", -1));
            intent.putExtra("localeID", localeID);
            startActivityForResult(intent, MainActivity.LOCALE_CHANGE_CODE);
        } else if (id == R.id.action_delete_locale) {
            while (Constants.globalLocaleList.getItemListFromLocale(localeID).getIDList().size() > 0) {
                long itemID = Constants.globalLocaleList.getItemListFromLocale(localeID).getIDList().get(0);
                ItemData itemData = Constants.globalLocaleList.getItemListFromLocale(localeID).getItem(itemID);
                itemData.deleteItemData();
                Constants.globalLocaleList.removeItem(itemData);
            }
            LocaleData localeData = Constants.globalLocaleList.getLocaleData(localeID);
            localeData.deleteLocaleData();
            Constants.globalLocaleList.removeLocale(localeData.getID());
            deletedLocale = true;
            finish();
        } else if (id == R.id.action_multi_pic) {
            Intent data = new Intent(ItemListActivity.this, ObjectIdentifyCameraActivity.class);
            data.putExtra("number", -1);
            startActivityForResult(data, MULTI_TAKE);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ITEM_CHANGE_CODE && resultCode == Activity.RESULT_OK) {
            boolean inserted = data.getBooleanExtra("inserted", false);
            boolean deleted = data.getBooleanExtra("deleted", false);
            int position = data.getIntExtra("position", -1);
            if (deleted) {
                recyclerAdapter.notifyItemRemoved(position);
                Snackbar.make(findViewById(R.id.fab), "Deleted item successfully", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } else if (inserted) {
                recyclerAdapter.notifyItemInserted(position);
                Snackbar.make(findViewById(R.id.fab), "Added new item successfully", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } else {
                recyclerAdapter.notifyItemChanged(position);
                Snackbar.make(findViewById(R.id.fab), "Modified item successfully", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
        if (requestCode == MainActivity.LOCALE_CHANGE_CODE && resultCode == Activity.RESULT_OK) {
            modifiedLocale = true;
            getSupportActionBar().setTitle(Constants.globalLocaleList.getLocaleData(localeID).getName());
            Snackbar.make(findViewById(R.id.fab), "Modified locale successfully", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        if (requestCode == MULTI_TAKE && resultCode == Activity.RESULT_OK) {
            List<BitmapLabelPair> bitmapLabelPairList = ObjectIdentifyCameraActivity.dataList;
            for (BitmapLabelPair bitmapLabelPair : bitmapLabelPairList) {
                long itemID = AddItemActivity.generateNewRandomItemID();
                ItemData itemData = new ItemData(bitmapLabelPair.getLabel(), bitmapLabelPair.getBitmap(), localeID, itemID);
                itemData.saveItemData();
                Constants.globalLocaleList.addItem(itemData);
                recyclerAdapter.notifyItemInserted(recyclerAdapter.getItemCount() - 1);
            }
            if (bitmapLabelPairList.size() > 0) {
                Snackbar.make(findViewById(R.id.fab), "Added all items successfully", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                bitmapLabelPairList.clear();
            }
        }
    }

    @Override
    public void finish() {
        Intent data = new Intent(this, MainActivity.class);
        data.putExtra("changed", modifiedLocale);
        data.putExtra("deleted", deletedLocale);
        data.putExtra("position", getIntent().getIntExtra("position", -1));
        setResult(Activity.RESULT_OK, data);
        super.finish();
    }
}


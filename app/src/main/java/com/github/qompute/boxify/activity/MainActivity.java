package com.github.qompute.boxify.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.qompute.boxify.R;
import com.github.qompute.boxify.data.Constants;
import com.github.qompute.boxify.data.ItemData;
import com.github.qompute.boxify.data.ItemList;
import com.github.qompute.boxify.data.LocaleData;
import com.github.qompute.boxify.data.LocaleList;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

/**
 * The first screen that opens, this will be where the locations are stored, as well as the functionality
 * to search for items
 */
public class MainActivity extends AppCompatActivity {

    public class LocaleDataAdapter extends RecyclerView.Adapter<LocaleDataAdapter.LocaleDataViewHolder>{

        public class LocaleDataViewHolder extends RecyclerView.ViewHolder {

            public TextView textName;
            public ImageView image;
            public LocaleData data;
            public int position;
            public LocaleDataViewHolder(View v) {
                super(v);
                textName = v.findViewById(R.id.textName);
                image = v.findViewById(R.id.image);
                image.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        Intent intent = new Intent(MainActivity.this, ItemListActivity.class);
                        intent.putExtra("position", position);
                        intent.putExtra("localeID", data.getID());
                        startActivityForResult(intent, MainActivity.ITEM_LIST_ENTER_CODE);
                    }
                });
            }

            public void setData(LocaleData data, int position) {
                this.data = data;
                this.position = position;
                textName.setText(data.getName());
                image.setImageBitmap(data.getPicture());
            }
        }

        private LocaleList locales;

        public LocaleDataAdapter(LocaleList locales) {
            this.locales = locales;
        }

        @Override
        public LocaleDataAdapter.LocaleDataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View contactView = inflater.inflate(R.layout.row_layout, parent, false);
            return new LocaleDataAdapter.LocaleDataViewHolder(contactView);
        }

        /** Displays data based on the position */
        @Override
        public void onBindViewHolder(LocaleDataAdapter.LocaleDataViewHolder viewHolder, int position) {
            /** Get data from position */
            long id = locales.getIDList().get(position);
            LocaleData data = locales.getLocaleData(id);

            /** Set view */
            viewHolder.setData(data, position);
        }

        // Returns the total count of items in the list
        @Override
        public int getItemCount() {
            return locales.getIDList().size();
        }
    }

    public class SearchDataAdapter extends RecyclerView.Adapter<SearchDataAdapter.SearchDataViewHolder>{

        public class SearchDataViewHolder extends RecyclerView.ViewHolder {

            public TextView textName;
            public ImageView image;
            public TextView localeName;
            public ItemData data;
            public int position;
            public SearchDataViewHolder(View v) {
                super(v);
                textName = v.findViewById(R.id.textName);
                image = v.findViewById(R.id.image);
                localeName = v.findViewById(R.id.localeName);
//                image.setOnClickListener(new View.OnClickListener() {
//                    public void onClick(View view) {
//                        Intent intent = new Intent(MainActivity.this, ItemListActivity.class);
//                        intent.putExtra("position", position);
//                        intent.putExtra("localeID", data.getID());
//                        startActivityForResult(intent, MainActivity.ITEM_LIST_ENTER_CODE);
//                    }
//                });
            }

            public void setData(ItemData data, int position) {
                this.data = data;
                this.position = position;
                textName.setText(data.getName());
                image.setImageBitmap(data.getPicture());
                localeName.setText(Constants.globalLocaleList.getLocaleData(data.getLocaleID()).getName());
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

    private LocaleList localeList;
    private LocaleDataAdapter recyclerAdapter;
    private SearchDataAdapter searchDataAdapter;

    public static final int LOCALE_CHANGE_CODE = 100;
    public static final int ITEM_LIST_ENTER_CODE = 104;
    public static final int PERM_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /** Check for permissions **/
        checkForPermissions();

        Log.e("info", getFilesDir().getAbsolutePath());
        Constants.generateConstants(getFilesDir().getAbsolutePath());
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /** Code to work on the floating action button, used to add new locations into the data */
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent data = new Intent(MainActivity.this, AddLocaleActivity.class);
                data.putExtra("localeID", -1);

                startActivityForResult(data, LOCALE_CHANGE_CODE);
            }
        });

        /** Setup recycler view */
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.body).findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        localeList = LocaleList.makeLocaleListDataFromSystem();
        Constants.globalLocaleList = localeList;
        recyclerAdapter = new LocaleDataAdapter(localeList);
        searchDataAdapter = new SearchDataAdapter(localeList.getFullItemList());
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

    public void checkForPermissions() {
        ArrayList<String> permissionsNeeded = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED) {
            permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED) {
            permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            permissionsNeeded.add(Manifest.permission.CAMERA);
        }

        if (permissionsNeeded.size() > 0) {
            String[] perms = new String[permissionsNeeded.size()];
            int i = 0;
            for (String perm : permissionsNeeded) {
                perms[i] = perm;
                i++;
            }

            ActivityCompat.requestPermissions(MainActivity.this, perms, PERM_CODE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        checkForPermissions();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCALE_CHANGE_CODE && resultCode == Activity.RESULT_OK) {
            boolean inserted = data.getBooleanExtra("inserted", false);
            int position = data.getIntExtra("position", -1);
            if (inserted) {
                recyclerAdapter.notifyItemInserted(position);
                Snackbar.make(findViewById(R.id.fab), "Added new locale successfully", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } else {
                recyclerAdapter.notifyItemChanged(position);
                Snackbar.make(findViewById(R.id.fab), "Modified locale successfully", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
        if (requestCode == ITEM_LIST_ENTER_CODE && resultCode == Activity.RESULT_OK) {
            boolean deleted = data.getBooleanExtra("deleted", false);
            boolean changed = data.getBooleanExtra("changed", false);
            int position = data.getIntExtra("position", -1);
            if (deleted) {
                recyclerAdapter.notifyItemRemoved(position);
                Snackbar.make(findViewById(R.id.fab), "Deleted locale successfully", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } else if (changed) {
                recyclerAdapter.notifyItemChanged(position);
            }
        }
        if (requestCode == PERM_CODE && resultCode == Activity.RESULT_OK) {
            checkForPermissions();
        }
    }
}

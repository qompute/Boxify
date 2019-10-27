package com.github.qompute.boxify.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.qompute.boxify.R;
import com.github.qompute.boxify.data.BitmapLabelPair;
import com.github.qompute.boxify.data.Constants;
import com.github.qompute.boxify.data.ItemData;
import com.github.qompute.boxify.utils.Utils;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.List;

public class AddItemActivity extends AppCompatActivity {

    private final static int GET_IMAGE = 120;

    private long localeID;
    private long itemID;
    private boolean finishAndSave;
    private boolean deleted;

    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_add_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        finishAndSave = false;
        localeID = getIntent().getLongExtra("localeID", -1);
        itemID = getIntent().getLongExtra("itemID", -1);

        /** Sets up the click listener for getting an image */
        ImageView imageView = findViewById(R.id.body).findViewById(R.id.image);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddItemActivity.this, ObjectIdentifyCameraActivity.class);
                intent.putExtra("number", 1);
                startActivityForResult(intent, GET_IMAGE);
            }
        });

        EditText editText = findViewById(R.id.nameText);
        Button button = findViewById(R.id.doneButton);

        if (itemID != -1) {
            bitmap = Constants.globalLocaleList.getItemListFromLocale(localeID).getItem(itemID).getPicture();
            imageView.setImageBitmap(Constants.globalLocaleList.getItemListFromLocale(localeID).getItem(itemID).getPicture());
            editText.setText(Constants.globalLocaleList.getItemListFromLocale(localeID).getItem(itemID).getName());
            getSupportActionBar().setTitle("Edit Item");
            button.setText("Edit Item");
        } else {
            bitmap = Utils.drawableToBitmap(imageView.getDrawable());
            editText.setText("");
        }

        button.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                if (editText.getText().toString().length() > 0) {
                    finishAndSave = true;
                    finish();
                } else {
                    Snackbar.make(v, "Item name cannot be empty", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (getIntent().getLongExtra("itemID", -1) != -1) {
            getMenuInflater().inflate(R.menu.menu_item, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_delete_item) {
            deleted = true;
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_IMAGE && resultCode == Activity.RESULT_OK) {
            List<BitmapLabelPair> bitmapLabelPairList = ObjectIdentifyCameraActivity.dataList;
//            Uri selectedImage = data.getData();
//            String[] filePath = { MediaStore.Images.Media.DATA };
//            Cursor c = getContentResolver().query(selectedImage,filePath, null, null, null);
//            c.moveToFirst();
//            int columnIndex = c.getColumnIndex(filePath[0]);
//            String imagePath = c.getString(columnIndex);
//            c.close();
//
//            File file = new File(imagePath);
            bitmap = bitmapLabelPairList.get(0).getBitmap();
            ImageView imageView = findViewById(R.id.image);
            imageView.setImageBitmap(bitmap);
            EditText text = findViewById(R.id.nameText);
            text.setText(bitmapLabelPairList.get(0).getLabel());
            bitmapLabelPairList.clear();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void finish() {
        if (finishAndSave) {
            Intent data = new Intent(this, MainActivity.class);
            EditText nameText = findViewById(R.id.nameText);
            String name = nameText.getText().toString();
            if (itemID == -1) {
                itemID = generateNewRandomItemID();
                data.putExtra("position", Constants.globalLocaleList.getItemListFromLocale(localeID).getIDList().size());
                data.putExtra("inserted", true);
            } else {
                data.putExtra("position", getIntent().getIntExtra("position", -1));
            }
            data.putExtra("localeID", localeID);
            data.putExtra("itemID", itemID);
            ItemData itemData = new ItemData(name, bitmap, localeID, itemID);
            itemData.saveItemData();
            Constants.globalLocaleList.addItem(itemData);
            setResult(Activity.RESULT_OK, data);
        } else if (deleted) {
            Intent data = new Intent(AddItemActivity.this, ItemListActivity.class);
            data.putExtra("position", getIntent().getIntExtra("position", -1));
            data.putExtra("itemID", itemID);
            data.putExtra("localeID", localeID);
            data.putExtra("deleted", true);
            Constants.globalLocaleList.getItemListFromLocale(localeID).getItem(itemID).deleteItemData();
            Constants.globalLocaleList.removeItem(Constants.globalLocaleList.getItemListFromLocale(localeID).getItem(itemID));
            setResult(Activity.RESULT_OK, data);
        } else {
            setResult(Activity.RESULT_CANCELED);
        }
        super.finish();
    }

    public static long generateNewRandomItemID() {
        long id = System.nanoTime();
        while (Constants.globalLocaleList.getFullItemList().containsID(id)) {
            id = System.nanoTime();
        }
        return id;
    }

}

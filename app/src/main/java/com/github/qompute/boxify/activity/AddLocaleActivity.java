package com.github.qompute.boxify.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.qompute.boxify.R;
import com.github.qompute.boxify.data.BitmapLabelPair;
import com.github.qompute.boxify.data.Constants;
import com.github.qompute.boxify.data.LocaleData;
import com.github.qompute.boxify.utils.Utils;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.List;

public class AddLocaleActivity extends AppCompatActivity {

    private final static int GET_IMAGE = 104;
    private final static int PERM_CODE = 108;

    private long localeID;
    private boolean finishAndSave;

    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.locale_add_layout);
        finishAndSave = false;
        localeID = getIntent().getLongExtra("localeID", -1);

        /** Sets up the click listener for getting an image */
        ImageView imageView = findViewById(R.id.body).findViewById(R.id.image);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent intent = new Intent(AddLocaleActivity.this, ObjectIdentifyCameraActivity.class);
                    intent.putExtra("number", 1);
                    startActivityForResult(intent, GET_IMAGE);
            }
        });

        final EditText editText = findViewById(R.id.nameText);
        Button button = findViewById(R.id.doneButton);

        if (localeID != -1) {
            bitmap = Constants.globalLocaleList.getLocaleData(localeID).getPicture();
            imageView.setImageBitmap(Constants.globalLocaleList.getLocaleData(localeID).getPicture());
            editText.setText(Constants.globalLocaleList.getLocaleData(localeID).getName());
            getSupportActionBar().setTitle("Edit Locale");
            button.setText("Edit Locale");
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
                    Snackbar.make(v, "Locale name cannot be empty", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
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
        if (requestCode == PERM_CODE && resultCode == Activity.RESULT_OK) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, GET_IMAGE);
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
            if (localeID == -1) {
                localeID = generateNewRandomLocaleID();
                data.putExtra("position", Constants.globalLocaleList.getIDList().size());
                data.putExtra("inserted", true);
            } else {
                data.putExtra("position", Constants.globalLocaleList.getIDList().indexOf(localeID));
            }
            data.putExtra("localeID", localeID);
            LocaleData localeData = new LocaleData(name, bitmap, localeID);
            localeData.saveLocaleData();
            Constants.globalLocaleList.addLocale(localeData);
            setResult(Activity.RESULT_OK, data);
        } else {
            setResult(Activity.RESULT_CANCELED);
        }
        super.finish();
    }

    public static long generateNewRandomLocaleID() {
        long id = System.nanoTime();
        while (Constants.globalLocaleList.containsID(id)) {
            id = System.nanoTime();
        }
        return id;
    }

}

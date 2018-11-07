package com.ajengkelin.polimoji;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_STORAGE = 1;
    private static final String FILE_PROVIDER_AUTHORITY = "joss.polinema.fileprovider";

    @BindView(R.id.imageView)
    ImageView mImageView;
    @BindView(R.id.btnSave)
    Button btnSave;
    @BindView(R.id.btnClear)
    Button btnClear;
    @BindView(R.id.btnShare)
    Button btnShare;
    @BindView(R.id.btnTake)
    Button btnTakePicture;
    @BindView(R.id.textView)
    TextView titleText;

    private String tempPath;
    private Bitmap resultBmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btnTake)
    public void emojify() {
        // Check for the external storage permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // If you do not have permission, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CAMERA);
        } else {
            // Launch the camera if the permission exists
//            Toast.makeText(this, "Belum ada permission", Toast.LENGTH_SHORT).show();
            launchCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // Called when you request permission to read and write to external storage
        switch (requestCode) {
            case REQUEST_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // If you get permission, launch the camera
                    launchCamera();
                } else {
                    // If you do not get permission, show a Toast
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    private void launchCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the temporary File where the photo should go
            File photoFile = null;
            try {
                photoFile = BitmapUtils.createTempImageFile(this);
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {

                // Get the path of the temporary file
                tempPath = photoFile.getAbsolutePath();

                // Get the content URI for the image file
                Uri photoURI = FileProvider.getUriForFile(this,
                        FILE_PROVIDER_AUTHORITY,
                        photoFile);

                // Add the URI so the camera can store the image
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                // Launch the camera activity
                startActivityForResult(takePictureIntent, REQUEST_CAMERA);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If the image capture activity was called and was successful
        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            // Process the image and set it to the TextView
            processAndSetImage();
        } else {

            // Otherwise, delete the temporary image file
            BitmapUtils.deleteImageFile(this, tempPath);
        }
    }

    private void processAndSetImage() {
        // Resample the saved image to fit the ImageView
        resultBmp= BitmapUtils.resamplePic(this, tempPath);

        // Set the new bitmap to the ImageView
        mImageView.setImageBitmap(resultBmp);
    }

    @OnClick(R.id.btnSave)
    public void saveImage(){
        // Save the image
        BitmapUtils.saveImage(this, resultBmp);
    }

    @OnClick(R.id.btnClear)
    public void clearImage(){
        // Delete the temporary image file
        BitmapUtils.deleteImageFile(this, tempPath);
    }

    @OnClick(R.id.btnShare)
    public void shareImage(){
        // Delete the temporary image file

        // Save the image
        BitmapUtils.saveImage(this, resultBmp);

        BitmapUtils.shareImage(this,tempPath);
    }

}

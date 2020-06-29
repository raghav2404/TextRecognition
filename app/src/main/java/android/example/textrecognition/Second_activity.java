package android.example.textrecognition;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Environment;
import android.text.ClipboardManager;
import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Second_activity extends AppCompatActivity {
    private static final int CAMERA_REQUEST_CODE=1;
    private static final int STORAGE_REQUEST_CODE=2;
    private static final int IMAGE_PICK_GALLERY_CODE=3;
    private static final int IMAGE_PICK_CAMERA_CODE=4;
    String[] cameraPermission;
    String[] storagePermission;
    EditText mResult;
    ImageView mPreview;
    private Button savePdf;
    Uri image_uri;
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity2);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mAdView = findViewById(R.id.adView);
        savePdf=findViewById(R.id.savePdf);
        AdRequest adRequest = new AdRequest.Builder()

                .build();
        mAdView.loadAd(adRequest);
           mAdView.isShown();
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());


        mResult=findViewById(R.id.resultEt);
        mPreview=findViewById(R.id.imageIv);
        //CAMERA PERMISSION
        cameraPermission=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ActionBar actionBar= getSupportActionBar();
        //actionBar.setSubtitle("Click Image Button To Insert Image");
        savePdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveAsPdf();
            }
        });




    }
    private void saveAsPdf() {
        //Save Text as pdf

        Document mDoc =new Document();
        String fileName= new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis());
        String filePath= Environment.getExternalStorageDirectory()+"/"+ fileName +".pdf";
        try {
            PdfWriter.getInstance(mDoc,new FileOutputStream(filePath));
            mDoc.open();
            String text=mResult.getText().toString().trim();
            mDoc.add(new Paragraph(text));
            Toast.makeText(getApplicationContext(),fileName+".pdf\n SAVED! to"+filePath,Toast.LENGTH_SHORT).show();


            mDoc.close();

        }
        catch(Exception e)
        {
            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }




    //actionbarmenu


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_items,menu);
        return true;
    }

    //handle clicklistener for actionbar menu options

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.AddImage) {
            showImageImportDialog();
        }
        if(id==R.id.CopyText)
             copyToClipboard();
        if(id==R.id.Share)
            share();



        return super.onOptionsItemSelected(item);
    }

    private void share() {
        //implicit intent
        Intent sharingIntent= new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_TEXT,mResult.getText().toString().trim());
        startActivity(Intent.createChooser(sharingIntent,"Share Using"));
    }

    private void copyToClipboard()
    {
        ClipboardManager cm = (ClipboardManager)this.getSystemService(Context.CLIPBOARD_SERVICE);
        assert cm != null;
        cm.setText(mResult.getText());
        Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
    }


    private void showImageImportDialog()
    {

        String items[]={"Camera","Gallery"};
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Select Image");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i==0) {

                    if(!checkCameraPermission())
                        requestCameraPermission();
                    else {


                        //take picture
                        pickCamera();
                    }

                }
                //camera options clicked

                if(i==1)//gallery options clicked
                {
                    if(!checkStoragePermission())
                        requestStoragePermission();
                    else
                        //take picture
                        pickGallery();
                }


            }
        });
        builder.create().show();



    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQUEST_CODE);
    }

    private void pickGallery() {

        //Intent to pick image from gallery
        Intent intent=new Intent(Intent.ACTION_PICK);
        //SET INTENT TYPE TO IMAGE
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);

    }

    private boolean checkStoragePermission() {
        return  ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);

    }

    private void pickCamera() {
        ContentValues contentValues= new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"New Pic");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Image To Text");
        image_uri=getContentResolver().insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);

            startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }



    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,cameraPermission,CAMERA_REQUEST_CODE);


    }

    private boolean checkCameraPermission() {
        boolean result= ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        boolean result1=ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return (result&&result1);
    }
    //HANDLE PERMISSIONS RESULT

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted)
                        pickCamera();

                } else
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                break;
            case STORAGE_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if ( storageAccepted)
                        pickGallery();

                } else
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                break;


        }

    }
    //handle image result

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {


        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)//GOT IMAGE
        {//GOT IMAGE FROM GALLERY NOW CROP IT
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                assert data != null;
                CropImage.activity(data.getData()).setGuidelines(CropImageView.Guidelines.ON).start(this);
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE)//got image from CAMERA so now crop it
            {
                CropImage.activity(image_uri).setGuidelines(CropImageView.Guidelines.ON).start(this);
            }

        }
        //get cropped image
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                //GET IMAGE URI
                Uri result_uri = result.getUri();
                //set image to text view
                mPreview.setImageURI(result_uri);
                //get drawable bitman for text recognition

                BitmapDrawable bitmapDrawable = (BitmapDrawable) mPreview.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();
                TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
                if (!textRecognizer.isOperational())
                    Toast.makeText(this, "Error", Toast.LENGTH_LONG).show();
                else {
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock> items = textRecognizer.detect(frame);//array which maps integer to objects ,but indices can contain gaps
                    StringBuilder sb = new StringBuilder();
                    //get text from string builder until there is no text
                    for (int i = 0; i < items.size(); i++) {
                        TextBlock myItem = items.valueAt(i);
                        sb.append(myItem.getValue());
                        sb.append("\n");
                    }
                    //set text from string builder object to textviw
                    mResult.setText(sb);
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception exception = result.getError();
                Toast.makeText(this, "" + exception, Toast.LENGTH_SHORT).show();


            }
        }

    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder=new AlertDialog.Builder(Second_activity.this);
        builder.setMessage("Do you want to Quit?");
      //  builder.setCancelable(true);
        //negative button
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        //Positive Button
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        AlertDialog alertDialog=builder.create();
        alertDialog.show();
       // super.onBackPressed();
    }
}


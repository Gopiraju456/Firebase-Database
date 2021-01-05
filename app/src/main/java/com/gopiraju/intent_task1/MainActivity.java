package com.gopiraju.intent_task1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.Manifest.permission.CAMERA;

public class MainActivity extends AppCompatActivity {

    TextView tv;
    ImageView iv;
    Intent it;
    //  SharedPreferences sharedPreferences;
    StorageReference storageReference;
    DatabaseReference databaseReference;
    String current;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv=findViewById(R.id.title);
        iv=findViewById(R.id.iv);

        storageReference= FirebaseStorage.getInstance().getReference("Images");
        databaseReference= FirebaseDatabase.getInstance().getReference("Images");

      /*  sharedPreferences=(SharedPreferences)getSharedPreferences("Gopiraju",MODE_PRIVATE);
        if (sharedPreferences.contains("Gold"))
        {
            tv.setText(sharedPreferences.getString("Gold"," "));
          }
*/

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                it=new Intent(MainActivity.this,MainActivity2.class);
                startActivityForResult(it,4);
            }
        });
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "hi", Toast.LENGTH_SHORT).show();
                PopupMenu popupMenu=new PopupMenu(MainActivity.this,iv);
                popupMenu.getMenuInflater().inflate(R.menu.menu,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.cam:
                               permission();
                                return true;
                            case R.id.gal:

                                ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},123);

                                 it =new Intent(Intent.ACTION_PICK);
                                it.setData((MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
                                startActivityForResult(it,2);
                                return  true;


                        }
                        return false;
                    }
                });
                popupMenu.show();



            }
        });
      }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 4) {
            if (resultCode == RESULT_OK) {
                tv = findViewById(R.id.title);
                Bundle b = data.getExtras();
                String sts = b.getString("gopi");
                tv.setText(sts);
/*                if (tv.length() != 0) {
                    String value1 = tv.getText().toString().trim();
                    SharedPreferences.Editor sre = sharedPreferences.edit();
                    sre.putString("Gold", value1).commit();

                }*/
            }
        }


        if (requestCode == 2) {

            if (resultCode == RESULT_OK) {
                Uri u = data.getData();
                iv.setImageURI(u);
                uploadimage("name",u);

            }

        }


        if (requestCode == 6) {
            if (resultCode == RESULT_OK) {

                File f=new File(current);
                iv.setImageURI(Uri.fromFile(f));

                it=new  Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri conne=Uri.fromFile(f);
                it.setData(conne);
                this.sendBroadcast(it);

                uploadimage(f.getName(),conne);


        }
        }
    }

    public void permission(){
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{CAMERA},123);
        }else
        {
            dispatchTakePictureIntent();
        }

    }



    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile( imageFileName,  ".jpg", storageDir);
        current = image.getAbsolutePath();
        return image;
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {

            }            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.gopiraju.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, 6);
            }
        }
    }
    public void uploadimage(String name,Uri uri){
        final StorageReference image=storageReference.child("image/"+name);
        image.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                image.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                    }
                });
                String title = tv.getText().toString().trim();
                @SuppressWarnings("VisibleForTests")
                uploadinfo imageUploadInfo = new uploadinfo(title, taskSnapshot.toString());
                String ImageUploadId = databaseReference.push().getKey();
                databaseReference.child("Gopiraju").setValue(imageUploadInfo);
            }
        });




          }
}
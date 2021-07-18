package com.example.dhvani;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationMenu;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationV=findViewById(R.id.bottomNavigationView);
        bottomNavigationV.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId())
                {
                    R.id.home:
                    Toast.makeText(this,"Home",Toast.LENGTH_SHORT).show();
                    break;
                    R.id.record:
                    Toast.makeText(this,"Record",Toast.LENGTH_SHORT).show();
                    break;
                    R.id.find:
                    Toast.makeText(this,"Find",Toast.LENGTH_SHORT).show();
                    break;


                  //  break;
                  //  default:
                     //  throw new IllegalStateException("Unexpected value: " + item.getItemId());
                }
                return true;
            }
        });
      /*  buttton=findViewById(R.id.button);
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},1);
        }*/

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==1)
        {
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT);
            }
            else
            {
                Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT);
            }
        }
    }
}
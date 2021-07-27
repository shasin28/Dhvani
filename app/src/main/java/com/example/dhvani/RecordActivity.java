package com.example.dhvani;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class RecordActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
      //  setContentView(R.layout.activity_find);
       // setContentView(R.layout.activity_main);
        bottomNavigationV=findViewById(R.id.bottomNavigationView);
       // bottomNavigationV.setSelectedItemId(R.id.home);
        bottomNavigationV.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId())
                {
                   /* case R.id.home:
                        Toast.makeText(MainActivity.this, "Home", Toast.LENGTH_SHORT).show();
                        break;*/
                    case R.id.home:
                        // Toast.makeText(MainActivity.this, "Record", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        overridePendingTransition(R.anim.leftanimation,R.anim.rightanimation);
                        break;
                    case R.id.record:
                        // Toast.makeText(MainActivity.this, "Record", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(),RecordActivity.class));
                        overridePendingTransition(R.anim.leftanimation,R.anim.rightanimation);
                        break;
                    case R.id.find:
                        //Toast.makeText(MainActivity.this, "Find", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(),FindActivity.class));
                        overridePendingTransition(R.anim.leftanimation,R.anim.rightanimation);
                        break;

                }
                return true;
            }
        });
    }
}
package com.parkland.stockopname;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;


public class splash_activity_pcard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_activity_pcard);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Button btn_offline_pcard = (Button) findViewById(R.id.btn_offline_pcard);
//        Button btn_sampling = (Button) findViewById(R.id.btn_sampling);
//        Button btn_online = (Button) findViewById(R.id.btn_online);

        btn_offline_pcard.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(splash_activity_pcard.this, PCARD_MainActivity_Offline.class);
                startActivity(intent);
                finish();
            }
        });
/*        btn_sampling.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(splash_activity_pcard.this,FG_MainActivity_Sampling.class);
                startActivity(intent);
                finish();
            }
        });
        btn_online.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(splash_activity_pcard.this,FG_MainActivity_Online.class);
                startActivity(intent);
                finish();
            }
        });*/

    }
}
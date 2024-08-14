package com.parkland.stockopname;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;


public class splash_activity_fg extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_activity_fg);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Button btn_offline = (Button) findViewById(R.id.btn_offline);
        Button btn_sampling = (Button) findViewById(R.id.btn_sampling);
        Button btn_online = (Button) findViewById(R.id.btn_online);

        btn_offline.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(splash_activity_fg.this, FG_MainActivity_Offline.class);
                startActivity(intent);
                finish();
            }
        });
        btn_sampling.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(splash_activity_fg.this,FG_MainActivity_Sampling.class);
                startActivity(intent);
                finish();
            }
        });
        btn_online.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(splash_activity_fg.this,FG_MainActivity_Online.class);
                startActivity(intent);
                finish();
            }
        });

    }
}
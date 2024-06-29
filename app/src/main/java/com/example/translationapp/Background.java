package com.example.translationapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class Background extends AppCompatActivity {
    Button btnApp,btnVoiceToVoice;
    ImageView translationImage,touristImage;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background);
        btnApp=findViewById(R.id.button);
        btnVoiceToVoice=findViewById(R.id.button2);
        translationImage=findViewById(R.id.translationImg);
        touristImage=findViewById(R.id.touristImg);
        getWindow().setStatusBarColor(0xFF000000);

        /*int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if(nightModeFlags == Configuration.UI_MODE_NIGHT_YES){
            getWindow().setStatusBarColor(0x00000000);

        }
        else getWindow().setStatusBarColor(0xFFFFFFFF);*/



        btnApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Background.this, MainActivity.class);
                startActivity(intent);
            }
        });
        btnVoiceToVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Background.this, TouristMode.class);
                startActivity(intent);
            }
        });
        touristImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Background.this, TouristMode.class);
                startActivity(intent);
            }
        });
        translationImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Background.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
package com.psru.ginraidee.controller;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.psru.ginraidee.R;
import com.psru.ginraidee.util.BottomNavHelper;

import com.psru.ginraidee.view.AboutActivity;
import com.psru.ginraidee.view.HowToActivity;
import com.psru.ginraidee.view.SettingsActivity;

public class MoreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.layoutHowTo).setOnClickListener(v ->
                startActivity(new Intent(this, HowToActivity.class)));

        findViewById(R.id.layoutAbout).setOnClickListener(v ->
                startActivity(new Intent(this, AboutActivity.class)));

        BottomNavHelper.setup(this, R.id.navMore);
    }
}

package com.psru.ginraidee.view;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.psru.ginraidee.R;
import com.psru.ginraidee.util.BottomNavHelper;

public class HowToActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_to);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        BottomNavHelper.setup(this, R.id.navMore);
    }
}

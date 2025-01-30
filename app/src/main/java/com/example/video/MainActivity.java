package com.example.video;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }



    public void setText(String s){
        var t = (TextView) findViewById(R.id.textView);
        t.setText(s);
    }

    public void information(View v){
        Intent i = new Intent(this,InformationActivity.class);
        i.putExtra("Key","Value");
        startActivity(i);
    }

    public void accelerometerView(View v){
        Intent i = new Intent(this,AccelerometerViewActivity.class);
        startActivity(i);
    }

    public void accelerometerApp(View v){
        Intent i = new Intent(this,AccelerometerAppActivity.class);
        startActivity(i);
    }


}
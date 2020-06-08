package android.example.textrecognition;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.content.Intent;
import android.example.textrecognition.R;
import android.example.textrecognition.Second_activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    private static int Splash_time_out=3000;
ImageView i,ii,iii;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        i=findViewById(R.id.imm1);
        ii=findViewById(R.id.imm2);
        iii=findViewById(R.id.imm3);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent homeIntent=new Intent(MainActivity.this,Second_activity.class);
                startActivity(homeIntent);
                finish();

            }
        },Splash_time_out);
    }
}
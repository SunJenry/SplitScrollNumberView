package com.sun.splitverticalscrollnumberview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.sun.splitverticalscrollnumberview.view.SplitScrollNumberView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private SplitScrollNumberView mSv;
    private int mNumber = 50;
    private Random mRandom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSv = findViewById(R.id.sv_number);
        mRandom = new Random(47);
    }

    public void add(View view) {
        mNumber += 1;
        mSv.setNumber(mNumber);
    }

    public void minus(View view) {
        mNumber -= 1;
        mSv.setNumber(mNumber);
    }

    public void random(View view) {
        int i = mRandom.nextInt(800);
        mNumber = i;
        mSv.setNumber(mNumber);
    }
}

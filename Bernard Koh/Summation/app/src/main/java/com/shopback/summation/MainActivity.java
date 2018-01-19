package com.shopback.summation;

import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    TextView sumView, timeTakenView;
    Button sumButton;
    EditText userInputField;
    static long timeTaken, start, end;
    static long total;
    final static int ARRAY_LENGTH = 10000000;
    static Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sumView = findViewById(R.id.sum_view);
        timeTakenView = findViewById(R.id.time_taken_view);
        sumButton = findViewById(R.id.sum_button);
        userInputField = findViewById(R.id.user_input);

        mainHandler = new Handler();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    public void clearFields(MenuItem menuItem) {

        userInputField.setText("");
        sumView.setText("");
        timeTakenView.setText("");
    }

    public void calculateSum(View view) {
        total = 0;
        doConcurrentSummation(Long.parseLong(userInputField.getText().toString()));
    }

    private void doConcurrentSummation(long size) {
        start = System.currentTimeMillis();

        int numArrays = (int) Math.floor(size / ARRAY_LENGTH);
        final int remainingSize = (int) size - (ARRAY_LENGTH * numArrays);

        for (long i = 0; i < numArrays; i++) {
            CalculationThreadPool.post(new Runnable() {
                @Override
                public void run() {
                    int[] data = populateArray(ARRAY_LENGTH);
                    long threadSum = doThreadSum(data);
                    addToOverallSum(threadSum);
                    mainHandler.post(updateUI());
                }
            });
        }

        if (remainingSize > 0) {
            CalculationThreadPool.post(new Runnable() {
                @Override
                public void run() {
                    int[] data = populateArray(remainingSize);
                    long threadSum = doThreadSum(data);
                    addToOverallSum(threadSum);
                    mainHandler.post(updateUI());
                }
            });
        }
    }

    private Runnable updateUI() {
        return new  Runnable() {
            public void run() {
                end = System.currentTimeMillis();
                timeTaken = end - start;

                sumView.setText(((Long) total).toString());
                timeTakenView.setText("Time taken: "+((Long) timeTaken).toString()+ "ms");
            }
        };
    }


    private long doThreadSum(int[] data) {
        long sum = 0;
        for (int i = 0; i < data.length; i++) {
            sum += data[i];
        }
        Log.d("doing thread sum", "thread sum: " + ((Long) sum).toString());
        return sum;
    }

    private int[] populateArray(int size) {
        int[] array = new int[size];
        Random rgn = new Random();
        for (int i = 0; i < array.length; i++) {
            array[i] = rgn.nextInt(100) + 1;
        }
        return array;
    }

    private void addToOverallSum(long partialSum) {
        total += partialSum;
        Log.d("Total Sum", ((Long) total).toString());
    }


}

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

    private void doConcurrentSummation(final long size) {
        start = System.currentTimeMillis();
        final int numCores = Runtime.getRuntime().availableProcessors();

        for (int i = 0; i< numCores; i++) {
            final long chunkSize;

            if (i == numCores-1) {
                chunkSize = size/numCores + size%numCores;
            } else {
                chunkSize = size/numCores;
            }

            CalculationThreadPool.post(new Runnable() {
                @Override
                public void run() {
                    long threadSum = doThreadSum(chunkSize);
                    mainHandler.post(updateUI(threadSum));
                }
            });
        }
    }

    private Runnable updateUI(final long threadSum) {
        return new  Runnable() {
            public void run() {
                end = System.currentTimeMillis();
                timeTaken = end - start;

                addToOverallSum(threadSum);

                sumView.setText(((Long) total).toString());
                timeTakenView.setText("Time taken: "+((Long) timeTaken).toString()+ "ms");
            }
        };
    }


    private long doThreadSum(long size) {
        Random rgn = new Random();
        long sum = 0;
        for (long i = 0; i < size; i++) {
            sum += rgn.nextInt(100);
        }
        return sum;
    }

    private void addToOverallSum(long partialSum) {
        total += partialSum;
        Log.d("Total Sum", ((Long) total).toString());
    }


}

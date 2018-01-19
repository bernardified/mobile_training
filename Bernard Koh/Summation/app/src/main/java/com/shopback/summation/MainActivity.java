package com.shopback.summation;

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
    static long timeTaken;
    final static int ARRAY_LENGTH  = 1000000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sumView = findViewById(R.id.sum_view);
        timeTakenView = findViewById(R.id.time_taken_view);
        sumButton = findViewById(R.id.sum_button);
        userInputField = findViewById(R.id.user_input);
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
        Long sum = doConcurrentSummation(Long.parseLong(userInputField.getText().toString()), Runtime.getRuntime().availableProcessors());
        sumView.setText(sum.toString());
        timeTakenView.setText("Time Taken = " + ((Long) timeTaken).toString() + "ms");
    }

    private long doConcurrentSummation(long size, int numThread) {
        long total = 0;

        long timeStart = System.currentTimeMillis();

        int numArrays = (int)Math.floor(size/ARRAY_LENGTH);
        int remainingSize = (int)size - (ARRAY_LENGTH*numArrays);


        for(long i=0; i<numArrays; i+=numThread) {
            SummationThread[] threadArray = new SummationThread[numThread];
            for(int j=0; j<numThread; j++) {
                threadArray[j] = new SummationThread(ARRAY_LENGTH);
                threadArray[j].start();
            }
            try {
                for (SummationThread thread: threadArray) {
                    thread.join();
                }
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

            for(SummationThread thread: threadArray) {
                total += thread.getSum();
            }
            Log.d("concurrent summation", "partial total: " + ((Long)total).toString());
        }

        int arraysRemaining = numArrays % numThread;
        SummationThread[] threadArray = new SummationThread[arraysRemaining];
        for(int i=0; i<arraysRemaining; i++) {
            threadArray[i] = new SummationThread(ARRAY_LENGTH);
        }
        try {
            for (SummationThread thread: threadArray) {
                thread.join();
            }
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }

        for(SummationThread thread: threadArray) {
            total += thread.getSum();
        }
        Log.d("concurrent summation", "partial total: " + ((Long)total).toString());

        if (remainingSize > 0) {
            Log.d("remaining size", ((Integer)remainingSize).toString());
            SummationThread thread = new SummationThread((int)remainingSize);
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            total += thread.getSum();
            Log.d("concurrent summation", "partial total: " + ((Long)total).toString());
        }

        long timeEnd = System.currentTimeMillis();
        timeTaken = timeEnd - timeStart;

        Log.d("concurrent summation", "total: " + ((Long)total).toString());
        return total;
    }
}

class SummationThread extends Thread {
    private int[] data;
    private long threadSum;
    private int size;

    SummationThread(int size) {
        this.size = size;
    }

    @Override
    public void run() {

        this.data = populateArray();
        this.threadSum = doThreadSum();
    }

    private long doThreadSum() {
        long sum = 0;
        for(int i = 0; i <data.length; i++) {
            sum += data[i];
        }
        Log.d("doing thread sum", "thread sum: " + ((Long)sum).toString());
        return sum;
    }

    long getSum() {
        return threadSum;
    }

    private int[] populateArray() {
        int[] array = new int[size];
        Random rgn = new Random();
        for (int i=0; i<array.length; i++) {
            array[i] = rgn.nextInt(100) + 1;
        }
        Log.d("Populating array", "size = " + ((Integer)size).toString());
        return array;
    }
}

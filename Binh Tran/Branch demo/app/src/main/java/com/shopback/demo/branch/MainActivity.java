package com.shopback.demo.branch;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.branch.referral.Branch;
import io.branch.referral.BranchError;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private String deepLink;
    private String message;
    private TextView messageView;
    private TextView deepLinkView;
    private Button btnSend;

    private DateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm aaa");

        messageView = findViewById(R.id.message);
        deepLinkView = findViewById(R.id.deeplink);
        btnSend = findViewById(R.id.send);
        btnSend.setOnClickListener(this);

        validateUI();
    }

    @Override
    public void onStart() {
        super.onStart();
        Branch branch = Branch.getInstance();

        branch.initSession(new Branch.BranchReferralInitListener() {
            @Override
            public void onInitFinished(JSONObject referringParams, BranchError error) {
                if (error == null) {
                    Log.e("Binh", "onInitFinished " + referringParams.toString());
                    deepLink = referringParams.optString("$x_deeplink");
                    message = referringParams.optString("$x_message");
                    validateUI();
                } else {
                    Log.i("MyApp", error.getMessage());
                }
            }

        }, this.getIntent().getData(), this);
    }

    private void validateUI() {
        btnSend.setVisibility(!TextUtils.isEmpty(deepLink) && !TextUtils.isEmpty(message) ?
                View.VISIBLE : View.GONE);
        messageView.setText(message);
        deepLinkView.setText(deepLink);
    }

    @Override
    public void onNewIntent(Intent intent) {
        this.setIntent(intent);
    }

    @Override
    public void onClick(View v) {
        Branch branch = Branch.getInstance();
        JSONObject meta = new JSONObject();
        try {
            meta.put("message", message);
            meta.put("deepLink", deepLink);
            meta.put("click_time", dateFormat.format(new Date()));

            branch.userCompletedAction("test_action", meta);

        } catch (JSONException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }
}

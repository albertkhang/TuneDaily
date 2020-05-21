package com.albertkhang.tunedaily.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.utils.FirebaseManager;
import com.albertkhang.tunedaily.utils.SettingManager;
import com.albertkhang.tunedaily.utils.SoftKeyboardManager;

public class InSearchActivity extends AppCompatActivity {
    private SettingManager settingManager;

    private EditText txtSearchText;
    private ScrollView scroll_view;
    private ImageView imgCollapse;
    private RelativeLayout root_view;
    private ConstraintLayout top_frame;
    private ImageView imgClear;
    private TextView txtArtist;
    private TextView txtSongs;
    private TextView txtResultStatus;

    private static final long DELAY_GETTING_TEXT_DURATION = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_search);

        addControl();
        addEvent();
    }

    private void addControl() {
        settingManager = SettingManager.getInstance(this);

        txtSearchText = findViewById(R.id.txtSearchText);
        txtSearchText.requestFocus();
        scroll_view = findViewById(R.id.scroll_view);
        imgCollapse = findViewById(R.id.imgCollapse);
        root_view = findViewById(R.id.root_view);
        top_frame = findViewById(R.id.top_frame);
        imgClear = findViewById(R.id.imgClear);
        txtArtist = findViewById(R.id.txtArtist);
        txtSongs = findViewById(R.id.txtSongs);
        txtResultStatus = findViewById(R.id.txtResultStatus);

        updateTheme();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addEvent() {
        scroll_view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                SoftKeyboardManager.hideSoftKeyboard(InSearchActivity.this, txtSearchText);
                txtSearchText.clearFocus();
                scroll_view.requestFocus();
                return false;
            }
        });

        imgCollapse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        txtSearchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
//                Log.d("txtSearchText", "beforeTextChanged: " + "start: " + start + ", count:" + count + ", after: " + after);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
//                Log.d("txtSearchText", "onTextChanged: " + "start: " + start + ", before: " + before + ", count: " + count);
                if (count != 0) {
                    imgClear.setVisibility(View.VISIBLE);
                } else {
                    imgClear.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(final Editable editable) {
                Log.d("txtSearchText", "afterTextChanged: " + "editable: " + editable);

                final String temp = editable.toString();

                Runnable r = new Runnable() {
                    public void run() {
                        Log.d("afterTextChanged", "editable: " + editable + ", temp: " + temp);
                        if (temp.equals(editable.toString())) {
                            Log.d("afterTextChanged", "run");

                            FirebaseManager.getInstance().searchTrackByTitle(temp);
                            FirebaseManager.getInstance().searchAlbumByTitle(temp);
                        }
                    }
                };

                Handler handler = new Handler();
                handler.postDelayed(r, DELAY_GETTING_TEXT_DURATION);
            }
        });

        imgClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txtSearchText.setText("");
            }
        });
    }

    private void updateTheme() {
        if (settingManager.isDarkTheme()) {
            root_view.setBackgroundColor(getResources().getColor(R.color.colorDark1));

            top_frame.setBackgroundColor(getResources().getColor(R.color.colorDark2));
            imgCollapse.setColorFilter(getResources().getColor(R.color.colorLight5));
            txtSearchText.setTextColor(getResources().getColor(R.color.colorLight1));
            imgClear.setColorFilter(getResources().getColor(R.color.colorLight5));

            txtArtist.setTextColor(getResources().getColor(R.color.colorLight1));
            txtSongs.setTextColor(getResources().getColor(R.color.colorLight1));
        } else {
            root_view.setBackgroundColor(getResources().getColor(R.color.colorLight1));

            top_frame.setBackgroundColor(getResources().getColor(R.color.colorLight2));
            imgCollapse.setColorFilter(getResources().getColor(R.color.colorDark5));
            txtSearchText.setTextColor(getResources().getColor(R.color.colorDark1));
            imgClear.setColorFilter(getResources().getColor(R.color.colorDark5));

            txtArtist.setTextColor(getResources().getColor(R.color.colorDark1));
            txtSongs.setTextColor(getResources().getColor(R.color.colorDark1));
        }
    }
}

package com.albertkhang.tunedaily.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.events.UpdateLanguageEvent;
import com.albertkhang.tunedaily.utils.PlaylistManager;
import com.albertkhang.tunedaily.utils.SettingManager;
import com.albertkhang.tunedaily.views.RoundImageView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.greenrobot.eventbus.EventBus;

public class SettingsActivity extends AppCompatActivity {
    private ImageView imgBack;
    private RoundImageView imgDarkTheme;
    private RoundImageView imgLightTheme;
    private ImageView imgDarkChecked;
    private ImageView imgLightChecked;
    private RelativeLayout top_frame;
    private LinearLayout root_view;
    private TextView txtTopBarHeader;
    private TextView txtThemeTitle;
    private TextView txtLanguageTitle;
    private TextView txtEnglish;
    private TextView txtVietnamese;
    private FrameLayout sign_out_frame;
    private RoundImageView imgSignOutBackground;

    private SettingManager settingManager = SettingManager.getInstance(this);

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        addControl();
        addEvent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateTheme();
        updateLanguage();
        updateUI();
    }

    private void updateUI() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            sign_out_frame.setVisibility(View.VISIBLE);
        } else {
            sign_out_frame.setVisibility(View.GONE);
        }
    }

    private void addControl() {
        imgBack = findViewById(R.id.imgBack);
        imgDarkTheme = findViewById(R.id.imgDarkTheme);
        imgLightTheme = findViewById(R.id.imgLightTheme);
        imgDarkChecked = findViewById(R.id.imgDarkChecked);
        imgLightChecked = findViewById(R.id.imgLightChecked);
        top_frame = findViewById(R.id.top_frame);
        root_view = findViewById(R.id.root_view);
        txtTopBarHeader = findViewById(R.id.txtTopBarHeader);
        txtThemeTitle = findViewById(R.id.txtThemeTitle);
        txtLanguageTitle = findViewById(R.id.txtLanguageTitle);
        txtEnglish = findViewById(R.id.txtEnglish);
        txtVietnamese = findViewById(R.id.txtVietnamese);
        sign_out_frame = findViewById(R.id.sign_out_frame);
        imgSignOutBackground = findViewById(R.id.imgSignOutBackground);

        mAuth = FirebaseAuth.getInstance();
        initialGoogleSignIn();
    }

    private void initialGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);
    }

    private void addEvent() {
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgBack.setImageResource(R.drawable.ic_previous);
                finish();
            }
        });

        imgDarkTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingManager.setTheme(true);
                updateTheme();
                settingManager.updateThemeFirebase();
            }
        });

        imgLightTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingManager.setTheme(false);
                updateTheme();
                settingManager.updateThemeFirebase();
            }
        });

        txtEnglish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingManager.setLanguageSP(true);
                EventBus.getDefault().post(new UpdateLanguageEvent());
                updateLanguage();
                updateTheme();
                settingManager.updateLanguageFirebase();
            }
        });

        txtVietnamese.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingManager.setLanguageSP(false);
                EventBus.getDefault().post(new UpdateLanguageEvent());
                updateLanguage();
                updateTheme();
                settingManager.updateLanguageFirebase();
            }
        });

        sign_out_frame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmSignOut();
            }
        });
    }

    private void showConfirmSignOut() {
        final Dialog dialog = new Dialog(this, R.style.RoundCornerDialogFragment);
        dialog.setContentView(R.layout.fragment_confirm_sign_out);

        TextView txtTitle = dialog.findViewById(R.id.txtTitle);

        Button cancel = dialog.findViewById(R.id.btnCancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

        Button signOut = dialog.findViewById(R.id.btnCreate);
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
                PlaylistManager.getInstance().deleteAllPlaylist();
                finish();
            }
        });

        if (settingManager.isDarkTheme()) {
            dialog.findViewById(R.id.root_view).setBackgroundResource(R.drawable.round_dark_dialog_background);
            txtTitle.setTextColor(getResources().getColor(R.color.colorLight1));

            cancel.setBackgroundResource(R.drawable.round_dark_dialog_button);
            signOut.setBackgroundResource(R.drawable.round_dark_dialog_button);

            cancel.setTextColor(getResources().getColor(R.color.colorLight1));
        } else {
            dialog.findViewById(R.id.root_view).setBackgroundResource(R.drawable.round_light_dialog_background);
            txtTitle.setTextColor(getResources().getColor(R.color.colorDark1));

            cancel.setBackgroundResource(R.drawable.round_light_dialog_button);
            signOut.setBackgroundResource(R.drawable.round_light_dialog_button);

            cancel.setTextColor(getResources().getColor(R.color.colorDark1));
        }

        dialog.show();
    }

    private void signOut() {
        mAuth.signOut();
        mGoogleSignInClient.signOut();
    }

    private void updateLanguage() {
        settingManager.updateLanguageConfiguration();

        txtTopBarHeader.setText(getString(R.string.settings));

        txtThemeTitle.setText(getString(R.string.theme));
        txtLanguageTitle.setText(getString(R.string.language));

        txtEnglish.setText(getString(R.string.english));
        txtVietnamese.setText(getString(R.string.vietnamese));
    }

    private void updateTheme() {
        if (settingManager.isDarkTheme()) {
            imgDarkChecked.setVisibility(View.VISIBLE);
            imgLightChecked.setVisibility(View.INVISIBLE);

            root_view.setBackgroundColor(getResources().getColor(R.color.colorDark1));

            top_frame.setBackgroundColor(getResources().getColor(R.color.colorDark2));
            txtTopBarHeader.setTextColor(getResources().getColor(R.color.colorLight1));
            imgBack.setColorFilter(getResources().getColor(R.color.colorLight1));

            txtThemeTitle.setTextColor(getResources().getColor(R.color.colorLight1));
            txtLanguageTitle.setTextColor(getResources().getColor(R.color.colorLight1));

            updateLanguageColor(R.color.colorDark5);

            imgDarkTheme.setImageResource(R.color.colorDark3);
            imgLightTheme.setImageResource(R.color.colorLight1);

            imgSignOutBackground.setImageResource(R.color.colorDark2);
        } else {
            imgDarkChecked.setVisibility(View.INVISIBLE);
            imgLightChecked.setVisibility(View.VISIBLE);

            root_view.setBackgroundColor(getResources().getColor(R.color.colorLight1));

            top_frame.setBackgroundColor(getResources().getColor(R.color.colorLight2));
            txtTopBarHeader.setTextColor(getResources().getColor(R.color.colorDark1));
            imgBack.setColorFilter(getResources().getColor(R.color.colorDark1));

            txtThemeTitle.setTextColor(getResources().getColor(R.color.colorDark1));
            txtLanguageTitle.setTextColor(getResources().getColor(R.color.colorDark1));

            updateLanguageColor(R.color.colorLight5);

            imgDarkTheme.setImageResource(R.color.colorDark1);
            imgLightTheme.setImageResource(R.color.colorLight2);

            imgSignOutBackground.setImageResource(R.color.colorLight2);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.animation_finish_enter, R.anim.animation_finish_leave);
    }

    private void updateLanguageColor(int notSelectId) {
        if (settingManager.isEnglish()) {
            txtEnglish.setTextColor(getResources().getColor(R.color.colorMain3));
            txtVietnamese.setTextColor(getResources().getColor(notSelectId));
        } else {
            txtEnglish.setTextColor(getResources().getColor(notSelectId));
            txtVietnamese.setTextColor(getResources().getColor(R.color.colorMain3));
        }
    }
}

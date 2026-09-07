package com.example.title;

import android.content.Context;
import android.content.Intent;
import android.app.Activity;
import android.media.AudioManager;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class settingsFragment extends Fragment {

    private SeekBar volumeSeekBar;
    private TextView volumeText;
    private AudioManager audioManager;
    private TextView username;
    private Button leaderboard;
    private ImageButton closeBtn;

    //יוצרים את הפרגמנט
    @Nullable //הפרגמנט יכול להיות ריק
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_settings, container, false);


        //אתחול משתנים
        username = view.findViewById(R.id.username_tv);
        leaderboard = view.findViewById(R.id.leaderboard_btn);
        volumeSeekBar = view.findViewById(R.id.volume_seekBar);
        volumeText = view.findViewById(R.id.volume_value_tv);
        closeBtn = view.findViewById(R.id.close_settings_btn);

        if (getActivity() != null) {
            audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        }
        if (audioManager != null) {
            setupVolumeControl();
        }
        // כשיוצאים מהפרגמנט
        closeBtn.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        gotoleaderboard();
        users_name();

        return view;
    }

    //מעבר ללוח המובילים
    private void gotoleaderboard()
    {
        leaderboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ScoreboardActivity.class);
                startActivity(intent);
            }
        });
    }

    //מציג את שם המשתמש שמחובר
    private void users_name()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            username.setText(user.getEmail().replace("@gmail.com", ""));
        }
        else
            username.setText("Guest");
    }

    //אחראי על הווליום
    private void setupVolumeControl() {
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        // הגדרת הווליום ללהיות הווליום הנכחי
        int percentage = (int) (((float) currentVolume / maxVolume) * 100);
        volumeSeekBar.setProgress(percentage);
        updateVolumeText(percentage);

        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            // שינוי הווליום בטלפון
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int systemVolume = (int) ((progress / 100.0f) * maxVolume);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, systemVolume, 0);
                    updateVolumeText(progress);
                }
            }

            // הרגישויות למגע של פס ההזזה
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    // משנה את הטקסט שמתחת לפס ההזזה לערך הווליום.
    private void updateVolumeText(int percentage) {
        volumeText.setText("Volume: " + percentage + "%");
    }
}
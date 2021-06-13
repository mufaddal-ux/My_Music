package com.example.syncmusic;

import androidx.annotation.LongDef;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MAinActivity";
    private Button play, stop;
    private static final Random RANDOM = new Random();
    private MediaPlayer mediaPlayer;
    private int pas, soundIndex;
    TextView startText, endText;
    SeekBar seekBar, end;
    Runnable runnable;
    Animation animation;
    com.mikhaellopez.circularimageview.CircularImageView mImageView;
    private Handler mHandler=new Handler();
    int SongTotalTime;

    public int getRandomSoundIndex() {
        int soundIndex;
        int[] song = {R.raw.song_1, R.raw.song_2, R.raw.song_3, R.raw.song_4, R.raw.song_5, R.raw.song_6, R.raw.song_7, R.raw.song_8};
        Random random = new Random();
        soundIndex = random.nextInt(song.length);
        return song[soundIndex];
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        play = (Button) findViewById(R.id.btn_play);
        stop = (Button) findViewById(R.id.btn_stop);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        mImageView = (com.mikhaellopez.circularimageview.CircularImageView) findViewById(R.id.image);
        animation = AnimationUtils.loadAnimation(this, R.anim.rotation);

        handler=new Handler();
        play.setOnClickListener(this);
        seekBar.setMax(100);
        stop.setOnClickListener(this);
        end = (SeekBar)findViewById(R.id.volume);
        end.setMax(100);
        startText = (TextView) findViewById(R.id.TextStart);
        endText = findViewById(R.id.TextEnd);
        //volume seekbar
        end.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float volume = progress / 100f;
                mediaPlayer.setVolume(volume, volume);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

        });
        //playline
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                    seekBar.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

    }



    public String createTimeText (long time){

        String timeText;
        int min = (int)time / 1000 / 60;
        int sec = (int)time / 1000 % 60;
        timeText = min + ":";
        if (sec < 10) timeText += "0";
        timeText += sec;
        Log.d(TAG, "createTimeText: TimeText"+timeText);
        return timeText;

    }
    public void playCycle () {
        seekBar.setProgress(mediaPlayer.getCurrentPosition());
        if (mediaPlayer.isPlaying()) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    playCycle();
                }
            };
            mHandler.postDelayed(runnable, 1000);
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler handler =  new Handler() {
        @Override
        public void handleMessage(Message message) {
            Log.d(TAG, "handleMessage: Handler Song TIme");
            int SeekBarPosition = message.what;

            //Update song seek bar
            seekBar.setProgress(SeekBarPosition);
            //Update Labels
            String Time = createTimeText(SeekBarPosition);
            Log.d(TAG, "handleMessage: Time"+Time);
            startText.setText(Time);
            //Time calculation

        }
    };
        @Override
        public void onClick (View v){
            switch (v.getId()) {
                case R.id.btn_play:
                    if (mediaPlayer==null)
                     {
                        soundIndex = getRandomSoundIndex();
                        mediaPlayer = MediaPlayer.create(MainActivity.this, soundIndex);
                        mediaPlayer.setVolume(0.5f, 0.5f);
                         Log.d(TAG, "onClick: Getting Duration");

                         Log.d(TAG, "onClick: Duration"+ SongTotalTime);
                        mImageView.startAnimation(animation);
                        play.setBackgroundResource(R.drawable.ic_baseline_pause_24);

                        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            public void onPrepared(MediaPlayer mediaPlayer) {
                                seekBar.setMax(mediaPlayer.getDuration());
                                SongTotalTime = mediaPlayer.getDuration();
                                
                                endText.setText(createTimeText((SongTotalTime)));
                                mediaPlayer.start();
                                playCycle();
                            }
                        });


                     } else if (!mediaPlayer.isPlaying()) {
                        play.setBackgroundResource(R.drawable.ic_baseline_pause_24);
                        mediaPlayer.seekTo(pas);
                        mediaPlayer.start();
//
                        playCycle();
                    }else{
                        mediaPlayer.pause();
                        pas = mediaPlayer.getCurrentPosition();
                        play.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
                        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (fromUser) {
                                    mediaPlayer.seekTo(progress);
                                    seekBar.setProgress(progress);
                                    pas = mediaPlayer.getCurrentPosition();
                                }
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {
                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {
                            }
                        });


//
                    }
                    break;
//
                case R.id.btn_stop:
                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                        mediaPlayer = null;
                        soundIndex = getRandomSoundIndex();
                        mediaPlayer = MediaPlayer.create(MainActivity.this, soundIndex);
                        play.setBackgroundResource(R.drawable.ic_baseline_pause_24);
                        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            public void onPrepared(MediaPlayer mediaPlayer) {
                                seekBar.setMax(mediaPlayer.getDuration());
                                SongTotalTime = mediaPlayer.getDuration();
                                endText.setText(createTimeText((SongTotalTime)));
                            }
                        });
                        mediaPlayer.start();

                    }
                    break;
            }
        }


}

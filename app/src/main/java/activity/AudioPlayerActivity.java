package activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.wzh.appplayer321.IMusicPlayService;
import com.example.wzh.appplayer321.R;
import com.example.wzh.appplayer321.view.LyricShowView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;

import domain.Lyric;
import domain.MediaItem;
import service.MusicPlayService;
import utils.LyricsUtils;
import utils.Utils;

import static com.example.wzh.appplayer321.R.id.iv_icon;

public class AudioPlayerActivity extends AppCompatActivity implements View.OnClickListener {


    private RelativeLayout rlTop;
    private ImageView ivIcon;
    private TextView tvArtist;
    private TextView tvAudioname;
    private LinearLayout llBottom;
    private TextView tvTime;
    private SeekBar seekbarAudio;
    private Button btnPlaymode;
    private Button btnPre;
    private Button btnStartPause;
    private Button btnNext;
    private Button btnLyric;
    private IMusicPlayService service;
    private int position;
    private MyReceiver receiver;
    private Utils utils;
    private final  static  int PROGRESS = 0;
    private boolean notification;
    private static final int SHOW_LYRIC = 1;
    private LyricShowView lyric_show_view;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case SHOW_LYRIC:
                    try {
                        int currentPosition = service.getCurrentPosition();
                        //调用歌词显示控件的setNextShowLyric
                        lyric_show_view.setNextShowLyric(currentPosition);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    removeMessages(SHOW_LYRIC);
                    sendEmptyMessage(SHOW_LYRIC);
                    break;
                case PROGRESS:
                    try {
                        int currentPosition = service.getCurrentPosition();
                        seekbarAudio.setProgress(currentPosition);
                        //设置更新时间
                        tvTime.setText(utils.stringForTime(currentPosition)+"/"+utils.stringForTime(service.getDuration()));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    //每秒中更新一次
                    removeMessages(PROGRESS);
                    sendEmptyMessageDelayed(PROGRESS,1000);
                    break;
            }
        }
    };

    private ServiceConnection conon = new ServiceConnection() {
        //绑定服务成功
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            service =  IMusicPlayService.Stub.asInterface(iBinder);
            if(service != null){
                try {
                    if (notification) {
                        setViewData(null);
                    } else {
                        service.openAudio(position);//打开播放第0个音频
                        //service.getDuration();//能直接调用了-不能
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        //断开链接
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };
    private void findViews() {

        setContentView(R.layout.activity_audio_player);
        ivIcon = (ImageView)findViewById(iv_icon);
        ivIcon.setBackgroundResource(R.drawable.animation_bg);
        AnimationDrawable background = (AnimationDrawable) ivIcon.getBackground();
        background.start();
        rlTop = (RelativeLayout)findViewById( R.id.rl_top );
        ivIcon = (ImageView)findViewById( iv_icon );
        tvArtist = (TextView)findViewById( R.id.tv_artist );
        tvAudioname = (TextView)findViewById( R.id.tv_audioname );
        llBottom = (LinearLayout)findViewById( R.id.ll_bottom );
        tvTime = (TextView)findViewById( R.id.tv_time );
        seekbarAudio = (SeekBar)findViewById( R.id.seekbar_audio );
        btnPlaymode = (Button)findViewById( R.id.btn_playmode );
        btnPre = (Button)findViewById( R.id.btn_pre );
        btnStartPause = (Button)findViewById( R.id.btn_start_pause );
        btnNext = (Button)findViewById( R.id.btn_next );
        btnLyric = (Button)findViewById( R.id.btn_lyric );
        lyric_show_view = (LyricShowView)findViewById(R.id.lyric_show_view);

        btnPlaymode.setOnClickListener( this );
        btnPre.setOnClickListener( this );
        btnStartPause.setOnClickListener( this );
        btnNext.setOnClickListener( this );
        btnLyric.setOnClickListener( this );

        seekbarAudio.setOnSeekBarChangeListener(new MyOnSeekBarChangeListener());

    }
    class MyOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener{
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(fromUser){
                try {
                    service.seekTo(progress);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initData();
        findViews();
        getData();
        startAndBindService();

        Intent intent = new Intent(this, MusicPlayService.class);
        startService(intent);
    }

    private void initData() {
        receiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicPlayService.OPEN_COMPLETE);
        registerReceiver(receiver,intentFilter);
        utils = new Utils();
        EventBus.getDefault().register(this);
    }
    class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            setViewData(null);
        }
    }
    @Subscribe(threadMode= ThreadMode.MAIN)
    public void setViewData(MediaItem mediaItem) {
        try {


            tvArtist.setText(service.getArtistName());
            tvAudioname.setText(service.getAudioName());
            setButtonImage();

            int duration = service.getDuration();
            seekbarAudio.setMax(duration);

            String audioPath = service.getAudioPath();//mnt/sdcard/audio/beijingbeijing.mp3

            String lyricPath = audioPath.substring(0,audioPath.lastIndexOf("."));//mnt/sdcard/audio/beijingbeijing
            File file = new File(lyricPath+".lrc");
            if(!file.exists()){
                file = new File(lyricPath+".txt");
            }
            LyricsUtils lyricsUtils = new LyricsUtils();
            lyricsUtils.readFile(file);

            //2.传入解析歌词的工具类
            ArrayList<Lyric> lyrics = lyricsUtils.getLyrics();
            lyric_show_view.setLyrics(lyrics);

            //3.如果有歌词，就歌词同步

            if(lyricsUtils.isLyric()){
                handler.sendEmptyMessage(SHOW_LYRIC);
            }




        } catch (RemoteException e) {
            e.printStackTrace();
        }
        handler.sendEmptyMessage(PROGRESS);
        handler.sendEmptyMessage(SHOW_LYRIC);
    }

    private void startAndBindService() {
        Intent intent = new Intent(this, MusicPlayService.class);
        //保证只有一个实例
        bindService(intent,conon, Context.BIND_AUTO_CREATE);
        startService(intent);
    }

    @Override
    public void onClick(View view) {
        if ( view == btnPlaymode ) {
            setPlayMode();
            // Handle clicks for btnPlaymode
        } else if ( view == btnPre ) {
            try {
                service.pre();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            // Handle clicks for btnPre
        } else if ( view == btnStartPause ) {
            // Handle clicks for btnStartPause
            try {
                if(service.isPlaying()){
                    //暂停
                    service.pause();
                    //显示播放按钮
                    btnStartPause.setBackgroundResource(R.drawable.btn_audio_start_selector);
                }else{
                    //播放
                    service.start();
                    //显示暂停按钮
                    btnStartPause.setBackgroundResource(R.drawable.btn_audio_pause_selector);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        } else if ( view == btnNext ) {
            try {
                service.next();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            // Handle clicks for btnNext
        } else if ( view == btnLyric ) {
            // Handle clicks for btnLyric
        }
    }
    public void getData() {
        notification = getIntent().getBooleanExtra("notification", false);
        if(!notification ){
            position = getIntent().getIntExtra("position",0);
        }
    }
    private void setButtonImage() {
        try {
            //从服务得到播放模式
            int playmode = service.getPlaymode();
            if (playmode == MusicPlayService.REPEAT_NORMAL) {
                btnPlaymode.setBackgroundResource(R.drawable.btn_playmode_normal_selector);
            } else if (playmode == MusicPlayService.REPEAT_SINGLE) {
                btnPlaymode.setBackgroundResource(R.drawable.btn_playmode_single_selector);
            } else if (playmode == MusicPlayService.REPEAT_ALL) {
                btnPlaymode.setBackgroundResource(R.drawable.btn_playmode_all_selector);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    private void setPlayMode() {
        try {
            int playmode = service.getPlaymode();
            if (playmode == MusicPlayService.REPEAT_NORMAL) {
                playmode = MusicPlayService.REPEAT_SINGLE;
            } else if (playmode == MusicPlayService.REPEAT_SINGLE) {
                playmode = MusicPlayService.REPEAT_ALL;
            } else if (playmode == MusicPlayService.REPEAT_ALL) {
                playmode = MusicPlayService.REPEAT_NORMAL;
            }
            //保存到服务里面
            service.setPlaymode(playmode);
            setButtonImage();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(conon != null){
            unbindService(conon);
            conon = null;
        }
        if(receiver != null){
            unregisterReceiver(receiver);
            receiver = null;
        }
        if(handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        EventBus.getDefault().unregister(this);

    }
}

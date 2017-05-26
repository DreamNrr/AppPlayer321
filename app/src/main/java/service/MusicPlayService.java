package service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.example.wzh.appplayer321.IMusicPlayService;
import com.example.wzh.appplayer321.R;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;

import activity.AudioPlayerActivity;
import domain.MediaItem;

public class MusicPlayService extends Service {

private IMusicPlayService.Stub stub = new IMusicPlayService.Stub() {
    MusicPlayService service = MusicPlayService.this;
    @Override
    public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

    }

    @Override
    public void openAudio(int position) throws RemoteException {
        service.openAudio(position);
    }

    @Override
    public void start() throws RemoteException {
        service.start();
    }

    @Override
    public void pause() throws RemoteException {
        service.pause();
    }

    @Override
    public String getAudioName() throws RemoteException {
        return service.getAudioName();
    }

    @Override
    public String getArtistName() throws RemoteException {
        return service.getArtistName();
    }

    @Override
    public int getCurrentPosition() throws RemoteException {
        return service.getCurrentPosition();
    }

    @Override
    public int getDuration() throws RemoteException {
        return service.getDuration();
    }

    @Override
    public void next() throws RemoteException {
        service.next();
    }

    @Override
    public void pre() throws RemoteException {
        service.pre();
    }
    @Override
    public boolean isPlaying() throws RemoteException {
        return mediaPlayer.isPlaying();
    }

    @Override
    public void seekTo(int position) throws RemoteException {
        service.seekTo(position);
    }
    @Override
    public int getPlaymode() throws RemoteException {
        return service.getPlaymode();
    }

    @Override
    public void setPlaymode(int playmode) throws RemoteException {
        service.setPlaymode(playmode);
    }

    @Override
    public String getAudioPath() throws RemoteException {
        return service.getAudioPath();
    }


};
    private ArrayList<MediaItem> mediaItems;
    private MediaPlayer mediaPlayer;
    private int position;
    private MediaItem mediaItem;
    public static final String OPEN_COMPLETE = "com.example.wzh.appplayer321.service.MUSICPLAYSERVICE";
    private NotificationManager nm;
    private boolean isCompletion = false;

    private SharedPreferences sp;


    //按顺序播放
    public static final int REPEAT_NORMAL = 1;

    //设置单曲循环
    public static final int REPEAT_SINGLE = 2;

   //设置列表循环
    public static final int REPEAT_ALL = 3;

   //设置播放的模式
    private int playmode = REPEAT_NORMAL;
    @Override
    public void onCreate() {
        super.onCreate();
        sp = getSharedPreferences("atguigu",MODE_PRIVATE);
      playmode = sp.getInt("playmode",getPlaymode());
        getData();
    }
    @Override
    public IBinder onBind(Intent intent) {
       return stub;
    }

    //根据索引位置打开音频
    public void openAudio(int position) {
        this.position = position;
        if (mediaItems != null && mediaItems.size() > 0) {

            if(position < mediaItems.size()){
                mediaItem = mediaItems.get(position);

                //如果不为空释放之前的播放音频的资源
                if (mediaPlayer != null) {
                    mediaPlayer.reset();
                    mediaPlayer = null;
                }
                try {
                    mediaPlayer = new MediaPlayer();
                    //设置播放地址
                    mediaPlayer.setDataSource(mediaItem.getData());
                    mediaPlayer.setOnPreparedListener(new MyOnPreparedListener());
                    mediaPlayer.setOnErrorListener(new MyOnErrorListener());
                    mediaPlayer.setOnCompletionListener(new MyOnCompletionListener());
                    //准备
                    mediaPlayer.prepareAsync();
                    if(playmode== MusicPlayService.REPEAT_SINGLE){
                        isCompletion = false;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } else {
            Toast.makeText(MusicPlayService.this, "音频还没有加载完成", Toast.LENGTH_SHORT).show();
        }
    }

    class MyOnPreparedListener implements MediaPlayer.OnPreparedListener{

        @Override
        public void onPrepared(MediaPlayer mp) {


            EventBus.getDefault().post(mediaItem);
           // notifyChange(OPEN_COMPLETE);
            start();
        }
    }
    private void notifyChange(String action) {
        Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    class MyOnErrorListener implements MediaPlayer.OnErrorListener{
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {

            next();//播放下一个
            return true;
        }
    }

    class MyOnCompletionListener implements MediaPlayer.OnCompletionListener{

        @Override
        public void onCompletion(MediaPlayer mp) {
            isCompletion = true;
            //播放下一个
            next();
        }
    }


    //开始播放音频

    public void start(){
        mediaPlayer.start();
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, AudioPlayerActivity.class);
        intent.putExtra("notification",true);
        PendingIntent pi = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notifation = new Notification.Builder(this)
                .setSmallIcon(R.drawable.notification_music_playing)
                .setContentTitle("321音乐")
                .setContentText("正在播放："+getAudioName())
                .setContentIntent(pi)
                .build();
        nm.notify(1,notifation);
    }
    //暂停
    public void pause() {

        mediaPlayer.pause();
        nm.cancel(1);
    }

    //得到歌曲的名字
    public String getAudioName() {
        return mediaItem.getName();
    }
    //得到演唱者的名字
    public String getArtistName() {
        return mediaItem.getArtist();
    }
    //得到歌曲当前播放的进度
    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }
    //得到歌曲的总进度
    public int getDuration() {
        return mediaPlayer.getDuration();
    }
   //播放下一首
    public void next() {
        //1.根据不同的播放模式设置不同的下标位置
        setNextPosition();

        //2.根据不同的下标位置打开对应的音频并且播放，边界处理
        openNextPosition();
    }
    private void openNextPosition() {
        int playmode = getPlaymode();

        if (playmode == MusicPlayService.REPEAT_NORMAL) {
            if(position < mediaItems.size()){
                //合法范围
                openAudio(position);

            }else{
                //变为合法
                position = mediaItems.size()-1;
            }
        } else if (playmode == MusicPlayService.REPEAT_SINGLE) {
            if(position < mediaItems.size()){
                //合法范围
                openAudio(position);
            }else{
                //变为合法
                position = mediaItems.size()-1;
            }

        } else if (playmode == MusicPlayService.REPEAT_ALL) {
            openAudio(position);
        }
    }
    private void setNextPosition() {
        int playmode = getPlaymode();

        if (playmode == MusicPlayService.REPEAT_NORMAL) {
            //还没有越界处理
            position ++;
        } else if (playmode == MusicPlayService.REPEAT_SINGLE) {
            if(!isCompletion){
                position ++;
            }

        } else if (playmode == MusicPlayService.REPEAT_ALL) {
            //合法的位置
            position ++;
            if(position > mediaItems.size()-1){
                position = 0;
            }
        }
    }
    //播放上一首
    public void pre() {
        setPrePosition();

        openPrePosition();
    }
    private void openPrePosition() {
        int playmode = getPlaymode();

        if (playmode == MusicPlayService.REPEAT_NORMAL) {
            if(position >= 0){
                //合法范围
                openAudio(position);

            }else{
                //变为合法
                position = 0;
            }
        } else if (playmode == MusicPlayService.REPEAT_SINGLE) {
            if(position >=0 ){
                //合法范围
                openAudio(position);
            }else{
                //变为合法
                position = 0;
            }

        } else if (playmode == MusicPlayService.REPEAT_ALL) {
            openAudio(position);
        }
    }

    private void setPrePosition() {
        int playmode = getPlaymode();

        if (playmode == MusicPlayService.REPEAT_NORMAL) {
            //还没有越界处理
            position --;
        } else if (playmode == MusicPlayService.REPEAT_SINGLE) {
            if(!isCompletion){
                position --;
            }

        } else if (playmode == MusicPlayService.REPEAT_ALL) {
            //合法的位置
            position --;
            if(position < 0){
                position = mediaItems.size()-1;
            }
        }
    }
    public int getPlaymode() {
        return playmode;
    }



    public void setPlaymode(int playmode) {
        this.playmode = playmode;
        sp.edit().putInt("playmode",playmode).commit();
    }
        //拖动
    private void seekTo(int position) {
        mediaPlayer.seekTo(position);
    }
    public void getData() {
        new Thread() {
            public void run() {
                mediaItems = new ArrayList<MediaItem>();
                ContentResolver resolver = getContentResolver();
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] objs = {
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.SIZE,
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.ARTIST
                };
                Cursor cursor = resolver.query(uri, objs, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {

                        long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                        String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));

                        long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
                        String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                        String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                        Log.e("TAG", "name==" + name + ",duration==" + duration + ",data===" + data+",artist=="+artist);

                        if(duration > 10*1000){
                            mediaItems.add(new MediaItem(name, duration, size, data,artist));
                        }

                    }

                    cursor.close();
                }

            }
        }.start();
    }



    private String getAudioPath() {
        return mediaItem.getData();
    }
}

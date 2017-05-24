package service;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;

import com.example.wzh.appplayer321.IMusicPlayService;

import java.util.ArrayList;

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
    public int getPlayMode() throws RemoteException {
        return service.getPlayMode();
    }

    @Override
    public void setPlayMode(int mode) throws RemoteException {
        
    }
};
    private ArrayList<MediaItem> mediaItems;
    @Override
    public void onCreate() {
        super.onCreate();
        getData();
    }

    @Override
    public IBinder onBind(Intent intent) {
       return stub;
    }

    //根据索引位置打开音频
    public void openAudio(int position) {
    }
    //开始播放音频
    public void start(){
    }
    //暂停
    public void pause() {
    }

    //得到歌曲的名字
    public String getAudioName() {
        return "";
    }
    //得到演唱者的名字
    public String getArtistName() {
        return "";
    }
    //得到歌曲当前播放的进度
    public int getCurrentPosition() {
        return 0;
    }
    //得到歌曲的总进度
    public int getDuration() {
        return 0;
    }
   //播放下一首
    public void next() {
    }
    //播放下一首
    public void pre() {
    }
    //得到播放模式
    public int getPlayMode() {
        return 0;
    }
    //设置播放模式
    public void setPlayMode(int mode) {
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
}

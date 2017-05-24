// IMusicPlayService.aidl
package com.example.wzh.appplayer321;

// Declare any non-default types here with import statements

interface IMusicPlayService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);


    //根据索引位置打开音频
    void openAudio(int position);
    //开始播放音频
   void start();
    //暂停
    void pause();

    //得到歌曲的名字
   String getAudioName();
    //得到演唱者的名字
   String getArtistName();
    //得到歌曲当前播放的进度
   int getCurrentPosition();
    //得到歌曲的总进度
    int getDuration();
   //播放下一首
    void next();
    //播放下一首
    void pre();
    //得到播放模式
   int getPlayMode();
    //设置播放模式
   void setPlayMode(int mode);

   boolean isPlaying();


}

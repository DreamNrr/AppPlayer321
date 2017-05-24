package pager;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.wzh.appplayer321.R;

import java.util.ArrayList;

import activity.AudioPlayerActivity;
import adapter.LocalVideoAdapter;
import domain.MediaItem;
import fragment.BaseFragment;



public class LocalAudioPager extends BaseFragment {
    private ListView lv;
    private TextView tv_nodata;
    private ArrayList<MediaItem> mediaItems;
    private LocalVideoAdapter adapter;

    //重写视图-返回View
    @Override
    public View initView() {
        //Log.e("TAG", "LocalVideoPager-initView");
        View view = View.inflate(context, R.layout.fragment_local_video_pager, null);
        lv = (ListView) view.findViewById(R.id.lv);
        tv_nodata = (TextView) view.findViewById(R.id.tv_nodata);
        //设置item的点击事件
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(context, AudioPlayerActivity.class);

                Bundle bunlder = new Bundle();
                bunlder.putSerializable("videolist",mediaItems);
                intent.putExtra("position",position);
                //放入Bundler
                intent.putExtras(bunlder);
                startActivity(intent);


            }
        });
        return view;
    }

    @Override
    public void initData() {
        super.initData();
       // Log.e("TAG", "LocalVideoPager-initData");
        //加载本地所有的视频
        getData();


    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (mediaItems != null && mediaItems.size() > 0) {
                //有数据
                tv_nodata.setVisibility(View.GONE);
                //设置适配器
                adapter = new LocalVideoAdapter(context, mediaItems,false);
                lv.setAdapter(adapter);
            } else {
                //没有数据
                tv_nodata.setVisibility(View.VISIBLE);
            }
        }
    };

    /**
     * 得到数据
     */
    private void getData() {
        new Thread() {
            public void run() {
                mediaItems = new ArrayList<MediaItem>();
                ContentResolver resolver = context.getContentResolver();
                Uri uri =  MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                String[] objs = {
                        MediaStore.Audio.Media.DISPLAY_NAME,//视频在sdcard上的名称
                        MediaStore.Audio.Media.DURATION,//视频时长
                        MediaStore.Audio.Media.SIZE,//视频文件的大小
                        MediaStore.Audio.Media.DATA//视频播放地址
                };
                Cursor cursor = resolver.query(uri, objs, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {

                        long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                        String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));

                        long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
                        String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                       // Log.e("TAG", "name==" + name + ",duration==" + duration + ",data===" + data);

                        if(duration > 10*1000){
                            mediaItems.add(new MediaItem(name, duration, size, data));
                        }

                    }

                    cursor.close();
                }

                //使用handler
                handler.sendEmptyMessage(0);
            }
        }.start();
    }
}

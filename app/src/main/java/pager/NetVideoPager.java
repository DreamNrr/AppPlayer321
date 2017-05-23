package pager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.cjj.MaterialRefreshLayout;
import com.cjj.MaterialRefreshListener;
import com.example.wzh.appplayer321.R;
import com.google.gson.Gson;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import activity.SystemVideoPlayerActivity;
import adapter.NetVideoAdapter;
import domain.MediaItem;
import domain.MoveInfo;
import fragment.BaseFragment;



public class NetVideoPager extends BaseFragment {
    private ListView lv;
    private TextView tv_nodata;
    private NetVideoAdapter adapter;
    //private ArrayList<MediaItem> mediaItems;
    private ArrayList<MediaItem> mediaItems;
    private MaterialRefreshLayout materialRefreshLayout;
    //判断当前是下拉还是上拉
    private boolean isLoadMore = false;
    private List<MoveInfo.TrailersBean> datas;

    //重写视图
    @Override
    public View initView() {
        View view = View.inflate(context, R.layout.fragment_net_video_pager,null);
        lv = (ListView) view.findViewById(R.id.lv);
        tv_nodata = (TextView) view.findViewById(R.id.tv_nodata);
        materialRefreshLayout = (MaterialRefreshLayout) view.findViewById(R.id.refresh);

        materialRefreshLayout.setMaterialRefreshListener(new MaterialRefreshListener() {
            //下拉刷新
            @Override
            public void onRefresh(MaterialRefreshLayout materialRefreshLayout) {
                isLoadMore = false;
                Log.e("TAG","下拉刷新了");
                getDataFromNet();
            }
//加载更多
            @Override
            public void onRefreshLoadMore(MaterialRefreshLayout materialRefreshLayout) {
                super.onRefreshLoadMore(materialRefreshLayout);
                isLoadMore = true;
                getMoreData();
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
               Intent intent = new Intent(context,SystemVideoPlayerActivity.class);
                if(mediaItems != null && mediaItems.size() >0){
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("videolist",mediaItems);
                    intent.putExtra("position",position);
                    intent.putExtras(bundle);
//                    intent.setDataAndType(Uri.parse(item.getData()),"video/*");
                    startActivity(intent);
                }
            }
        });

        return view;
    }

    private void getMoreData() {
        //配置联网请求地址
        final RequestParams request = new RequestParams("http://api.m.mtime.cn/PageSubArea/TrailerList.api");
        x.http().get(request, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.e("TAG", "加载更多xUtils联网成功==" + result);
                processData(result);
                // 结束上拉刷新...
                materialRefreshLayout.finishRefreshLoadMore();

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.e("TAG", "加载更xUtils联网失败==" + ex.getMessage());
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    @Override
    public void initData() {
        super.initData();
        getDataFromNet();
    }

    public void getDataFromNet() {
        final RequestParams request = new RequestParams("http://api.m.mtime.cn/PageSubArea/TrailerList.api");
        x.http().get(request, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.e("TAG","下拉刷新联网成功了");

                processData(result);
                //下拉刷新结束
                materialRefreshLayout.finishRefresh();
            }
            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Log.e("TAG","下拉刷新联网失败了");
            }
            @Override
            public void onCancelled(CancelledException cex) {
            }
            @Override
            public void onFinished() {
            }
        });
    }
    
    private void processData(String result) {

//        mediaItems = new ArrayList<>();
//        try {
//            JSONObject jsonObject = new JSONObject(result);
//            JSONArray trailers = jsonObject.getJSONArray("trailers");
//            for(int i = 0; i < trailers.length(); i++) {
//
//                String movieName = trailers.getJSONObject(i).getString("movieName");
//                String videoTitle = trailers.getJSONObject(i).getString("videoTitle");
//                String coverImg = trailers.getJSONObject(i).getString("coverImg");
//                int videoLength =trailers.getJSONObject(i).getInt("videoLength");
//                String url = trailers.getJSONObject(i).getString("url");
//                Log.e("TAG","uri===" + url);
//                MediaItem mediaItemss = new MediaItem(movieName, coverImg, videoLength,videoTitle,url);
//                mediaItems.add(mediaItemss);
//            }
//
//           adapter = new NetVideoAdapter(context,mediaItems);
//            lv.setAdapter(adapter);
//            tv_nodata.setVisibility(View.GONE);
//
//            }catch (JSONException e) {
//                e.printStackTrace();
//        }
        MoveInfo moveInfo = new Gson().fromJson(result, MoveInfo.class);
//        List<MoveInfo.TrailersBean> datas = moveInfo.getTrailers();
        if(!isLoadMore) {
            datas = moveInfo.getTrailers();
            if(datas != null && datas.size() >0){
                //集合数据MediaItem
                mediaItems = new ArrayList<>();
                for(int i = 0; i <datas.size() ; i++) {
                    MediaItem mediaItem = new MediaItem();
                    mediaItem.setData(datas.get(i).getUrl());
                    mediaItem.setName(datas.get(i).getMovieName());
                    mediaItems.add(mediaItem);
                }
                tv_nodata.setVisibility(View.GONE);
                //有数据-适配器
                adapter = new NetVideoAdapter(context,datas);
                lv.setAdapter(adapter);
            }else{
                tv_nodata.setVisibility(View.VISIBLE);
            }
        }else {
            //加载更多得到的数据新数据
            List<MoveInfo.TrailersBean> trailersBeanList = moveInfo.getTrailers();
            for (int i = 0; i < trailersBeanList.size(); i++) {
                MediaItem mediaItem = new MediaItem();
                mediaItem.setData(trailersBeanList.get(i).getUrl());
                mediaItem.setName(trailersBeanList.get(i).getMovieName());
                mediaItems.add(mediaItem);
            }
//加入原来的集合
            datas.addAll(trailersBeanList);
//            datas = trailersBeanList;
            //刷新适配器
            adapter.notifyDataSetChanged();
        }

    }
}

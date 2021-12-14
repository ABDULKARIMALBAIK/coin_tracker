package com.example.asus.androidcointracker;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import com.example.asus.androidcointracker.Adapter.CoinAdapter;
import com.example.asus.androidcointracker.Interface.ILoadMore;
import com.example.asus.androidcointracker.Model.CoinModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.supercharge.shimmerlayout.ShimmerLayout;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    List<CoinModel> items = new ArrayList<>();
    CoinAdapter adapter;
    RecyclerView recycler_coins;
    ShimmerLayout shimmerLayout;
    SwipeRefreshLayout swipeRefreshLayout;

    OkHttpClient client;
    Request request;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        shimmerLayout = (ShimmerLayout)findViewById(R.id.shimmerLayout);

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.rootLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (isConnectedToInternet()){

                    items.clear();
                    loadFirst10Coin(0);
                    setupAdapter();
                }
                else
                    Toast.makeText(MainActivity.this, "Please check your connection !", Toast.LENGTH_SHORT).show();
               
            }
        });

        recycler_coins = (RecyclerView)findViewById(R.id.recycler_coins);
        recycler_coins.setHasFixedSize(true);
        recycler_coins.setLayoutManager(new LinearLayoutManager(this));


        if (isConnectedToInternet()){

            shimmerLayout.startShimmerAnimation();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    loadFirst10Coin(0);
                    setupAdapter();
                }
            } , 3000);

        }


    }

    private void setupAdapter() {

        adapter = new CoinAdapter(recycler_coins , this , items);
        recycler_coins.setAdapter(adapter);



        adapter.setiLoadMore(new ILoadMore() {
            @Override
            public void onLoadMore() {

                if (items.size() <= 1000){

                    loadNext10Coin(items.size());
                }
                else
                    Toast.makeText(MainActivity.this, "Max items is 1000 !", Toast.LENGTH_SHORT).show();
            }
        });

        shimmerLayout.stopShimmerAnimation();
        shimmerLayout.setVisibility(View.GONE);
    }

    public void loadNext10Coin(int index){

        client = new OkHttpClient();
        request = new Request.Builder().url(String.format(Locale.US,".......................",index))
                .build();

        swipeRefreshLayout.setRefreshing(true);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String body = response.body().string();
                Gson gson = new Gson();
                final List<CoinModel> newItems = gson.fromJson(body , new TypeToken<List<CoinModel>>(){}.getType());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                       items.addAll(newItems);
                       adapter.setLoaded();
                       adapter.updateData(items);
                       swipeRefreshLayout.setRefreshing(false);


                    }
                });
            }
        });
    }

    private void loadFirst10Coin(int index) {

        client = new OkHttpClient();
        request = new Request.Builder().url(String.format(Locale.US,".............................",index))
                .build();

        swipeRefreshLayout.setRefreshing(true);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String body = response.body().string();
                Gson gson = new Gson();
                final List<CoinModel> newItems = gson.fromJson(body , new TypeToken<List<CoinModel>>(){}.getType());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        adapter.updateData(newItems);

                        Context context = recycler_coins.getContext();
                        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(context , R.anim.layout_item_fall_down);

                        recycler_coins.setLayoutAnimation(controller);
                        recycler_coins.getAdapter().notifyDataSetChanged();
                        recycler_coins.scheduleLayoutAnimation();
                    }
                });
            }
        });

       if (swipeRefreshLayout.isRefreshing())
           swipeRefreshLayout.setRefreshing(false);
    }

    private boolean isConnectedToInternet(){

        ConnectivityManager manager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        if (manager != null){

            NetworkInfo[] info = manager.getAllNetworkInfo();

            if (info != null){

                for (int i = 0; i <info.length ; i++) {

                    if(info[i].getState() == NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
        }

        return false;
    }
}

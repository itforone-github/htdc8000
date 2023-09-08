package com.cafe24.hdtc8000;

import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import util.BackPressCloseHandler;
import util.Common;
import util.retrofit.LoginData;
import util.retrofit.RetrofitService;
import util.retrofit.TokenData;

public class MainActivity extends AppCompatActivity {
    private BackPressCloseHandler backPressCloseHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        backPressCloseHandler = new BackPressCloseHandler(this);
        //토큰 값 생성
        FirebaseApp.initializeApp(this);//firebase 등록함
        FirebaseMessaging.getInstance().subscribeToTopic("hdtc8000");
        //토큰 생성
        Common.TOKEN= FirebaseInstanceId.getInstance().getToken();
        try {
            if (Common.TOKEN.equals("") || Common.TOKEN.equals(null)) {
                //토큰 값 재생성
                refreshToken();
            } else {

            }
        }catch (Exception e){
            //토큰 값 재생성
            refreshToken();
        }
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                .build();
        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl(getString(R.string.domain))
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();


        Map map=new HashMap();
        map.put("mb_id",Common.getPref(getApplicationContext(),"ss_mb_id",""));
        map.put("token",Common.TOKEN);

        RetrofitService retrofitService=retrofit.create(RetrofitService.class);
        Call<TokenData> call=retrofitService.postToken(map);
        call.enqueue(new Callback<TokenData>() {

            @Override
            public void onResponse(Call<TokenData> call, Response<TokenData> response) {
                if(response.isSuccessful()){
                    TokenData repo=response.body();
                    Log.d("response",response+"");

                }else{

                }
            }

            @Override
            public void onFailure(Call<TokenData> call, Throwable t) {

            }
        });
    }
    @OnClick({R.id.menu1Img,R.id.menu2Img,R.id.menu3Img,R.id.menu4Img})
    public void goIntent(View view){
        String url=getString(R.string.domain);
        switch (view.getId()){
            case R.id.menu1Img:
                url+="bbs/mobile_write.php?bbs_id=cargo";
                break;
            case R.id.menu2Img:
                url+="bbs/mobile_list.php?bbs_id=cargo";
                break;
            case R.id.menu3Img:
                if(!Common.getPref(getApplicationContext(),"ss_mb_level","").equals("10")){
                    Toast.makeText(this, "최고관리자만 접속이 가능합니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                url+="bbs/mobile_list.php?bbs_id=cargo";
                break;
            case R.id.menu4Img:
                url+="bbs/mobile_list.php?bbs_id=qnaa";
                break;
        }
        Intent intent =new Intent(getApplicationContext(),WebActivity.class);
        intent.putExtra("url",url);
        startActivity(intent);
    }
    private void refreshToken(){
        FirebaseMessaging.getInstance().subscribeToTopic("hdtc8000");
        Common.TOKEN= FirebaseInstanceId.getInstance().getToken();

    }
    //뒤로가기를 눌렀을 때
    public void onBackPressed() {
        //super.onBackPressed();
        //웹뷰에서 히스토리가 남아있으면 뒤로가기 함

        backPressCloseHandler.onBackPressed();
    }
}

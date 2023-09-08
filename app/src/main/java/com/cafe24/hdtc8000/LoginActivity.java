package com.cafe24.hdtc8000;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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
import util.Common;
import util.SSLConnect;
import util.retrofit.LoginData;
import util.retrofit.RetrofitService;

public class LoginActivity extends AppCompatActivity {
    @BindView(R.id.idEdt)
    EditText idEdt;
    @BindView(R.id.passwordEdt)
    EditText passwordEdt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        SSLConnect ssl =new SSLConnect();
        ssl.postHttps(getString(R.string.domain),1000,1000);

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
    }
    @OnClick({R.id.loginBtn,R.id.joinBtn})
    public void onclick(View view){
        switch (view.getId()) {
            //로그인
            case R.id.loginBtn:
                String mb_id=idEdt.getText().toString().trim();
                String mb_password=passwordEdt.getText().toString().trim();
                if(mb_id.equals("")||mb_id.equals(null)){
                    Toast.makeText(this, "아이디를 입력해 주세요", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(mb_password.equals("")||mb_password.equals(null)){
                    Toast.makeText(this, "비밀번호를 입력해 주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
                httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                OkHttpClient client = new OkHttpClient.Builder()
                        .addInterceptor(httpLoggingInterceptor)
                        .build();
                Retrofit retrofit=new Retrofit.Builder()
                        .baseUrl(getString(R.string.domain))
                        .client(getUnsafeOkHttpClient().build())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();


                Map map=new HashMap();
                map.put("mb_id",mb_id);
                map.put("mb_password",mb_password);
                map.put("token",Common.TOKEN);

                RetrofitService retrofitService=retrofit.create(RetrofitService.class);
                Call<LoginData> call=retrofitService.postLogin(map);
                call.enqueue(new Callback<LoginData>() {

                    @Override
                    public void onResponse(Call<LoginData> call, Response<LoginData> response) {
                        if(response.isSuccessful()){
                            LoginData repo=response.body();
                            Log.d("response",response+"");
                            if(repo.getSs_login_ok().equals("not")){
                                Toast.makeText(LoginActivity.this, "로그인에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                                return;
                            }else{

                                Common.savePref(getApplicationContext(),"ss_mb_id",repo.getSs_mb_id());
                                Common.savePref(getApplicationContext(),"ss_mb_level",repo.getSs_mb_level());
                                Common.savePref(getApplicationContext(),"ss_mb_num",repo.getSs_mb_num());
                                Intent intent=new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }else{
                            Toast.makeText(LoginActivity.this, "로그인에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginData> call, Throwable t) {

                    }
                });


                break;
            //회원가입
            case R.id.joinBtn:
                Intent intent =new Intent(getApplicationContext(), WebActivity.class);
                intent.putExtra("url",getString(R.string.domain)+"bbs/mobile_mb_join.php");
                startActivity(intent);
                break;
        }

    }
    private void refreshToken(){
        FirebaseMessaging.getInstance().subscribeToTopic("hdtc8000");
        Common.TOKEN= FirebaseInstanceId.getInstance().getToken();

    }
    public static OkHttpClient.Builder getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            return builder;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}


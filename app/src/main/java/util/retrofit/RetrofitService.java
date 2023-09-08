package util.retrofit;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface RetrofitService {
    @FormUrlEncoded
    @POST("/bbs/json.mb_login.php")
    Call<LoginData> postLogin(
            @FieldMap Map<String, String> option
    );
    @FormUrlEncoded
    @POST("/bbs/app.mb_token.php")
    Call<TokenData> postToken(
            @FieldMap Map<String, String> option
    );
}
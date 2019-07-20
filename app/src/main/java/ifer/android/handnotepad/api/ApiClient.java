package ifer.android.handnotepad.api;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import ifer.android.handnotepad.AppController;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by ifer on 20/6/2017.
 */

public class ApiClient {
    private static Gson gson = new GsonBuilder()
                                    .setDateFormat("dd/MM/yyyy")
                                    .create();

    private static Retrofit.Builder builder ;
    public static Retrofit retrofit;
    private static OkHttpClient.Builder httpClient;




//    public static <S> S createService(Class<S> serviceClass) {
//        return createService(serviceClass);
//    }

    private static void setupRetrofit () throws Exception {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();
//Log.d("DRAW", "AppController.getApiDomain()=" + AppController.getApiDomain());
        try {
            builder = new Retrofit.Builder()
                    .baseUrl(AppController.getApiDomain())
                    .client(client)
                    .addConverterFactory(ScalarsConverterFactory.create())      //in order to accept String values as response
                    .addConverterFactory(GsonConverterFactory.create(gson));    //GSON convereer, gson value can be removed if not needed
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        retrofit = builder.build();
//        httpClient = new OkHttpClient.Builder();
    }

    public static <S> S createService(Class<S> serviceClass) throws Exception {

        setupRetrofit();

       return(retrofit.create(serviceClass));
    }

//    public static <S> S createService(Class<S> serviceClass, final String authToken) {
//        if (!TextUtils.isEmpty(authToken)) {
//            AuthenticationInterceptor interceptor =  new AuthenticationInterceptor(authToken);
//
//            if (!httpClient.interceptors().contains(interceptor)) {
//                httpClient.addInterceptor(interceptor);
//
//                builder.client(httpClient.build());
//                retrofit = builder.build();
//            }
//        }
//
//        return retrofit.create(serviceClass);
//    }

    public static Retrofit getRetrofit() {
        return retrofit;
    }
}

package ifer.android.handnotepad.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by ifer on 19/6/2017.
 */

public interface ApiInterface {
    @GET("/readimage")
    Call<String> readImage ();


    @POST("/saveimage")
    Call<String> saveImage(@Body Drawing img );

    @GET("/connection_alive")
    Call<String> testConnection ();

    @GET("/require_lock")
    Call <ResponseMessage> requireLock(@Query("ip_address") String ip_address);

    @GET("/release_lock")
    Call <ResponseMessage> releaseLock(@Query("ip_address") String ip_address);
}

package it.davidepalladino.airanalyzer.controller;

import java.util.ArrayList;

import it.davidepalladino.airanalyzer.model.MeasureAverage;
import it.davidepalladino.airanalyzer.model.Login;
import it.davidepalladino.airanalyzer.model.MeasureFull;
import it.davidepalladino.airanalyzer.model.Room;
import it.davidepalladino.airanalyzer.model.Signup;
import it.davidepalladino.airanalyzer.model.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface API {
    String BASE_URL_LOCAL = "http://192.168.0.2:8008/";
    String BASE_URL_REMOTE = "http://airanalyzer.servehttp.com:50208/";

    @POST("api/login")
    Call<Login.Response> login(@Body Login login);

    @POST("api/signupAirAnalyzer")
    Call<Signup.NoResponse> signup(@Body Signup signup);

    @GET("api/checkUsername")
    Call<Signup.NoResponse> checkUsername(@Query("username") String username);

    @GET("api/checkEmail")
    Call<Signup.NoResponse> checkEmail(@Query("email") String email);

    @GET("api/getUser")
    Call<User> getUser(@Header("Authorization") String token);

    @GET("api/airanalyzer/getActiveRooms")
    Call<ArrayList<Room>> getActiveRooms(@Header("Authorization") String token);

    @GET("api/airanalyzer/getInactiveRooms")
    Call<ArrayList<Room>> getInactiveRooms(@Header("Authorization") String token);

    @POST("api/airanalyzer/renameRoom")
    Call<Room.NoResponse> renameRoom(@Header("Authorization") String token, @Body Room room);

    @POST("api/airanalyzer/addRoom")
    Call<Room.NoResponse> addRoom(@Header("Authorization") String token, @Body Room room);

    @POST("api/airanalyzer/removeRoom")
    Call<Room.NoResponse> removeRoom(@Header("Authorization") String token, @Body Room room);

    @GET("api/airanalyzer/getMeasureDateLatest")
    Call<MeasureFull> getMeasureDateLatest(@Header("Authorization") String token, @Query("room") String room, @Query("day") String day, @Query("month") String month, @Query("year") String year, @Query("legal") int legal);

    @GET("api/airanalyzer/getMeasuresDateAverage")
    Call<ArrayList<MeasureAverage>> getMeasuresDateAverage(@Header("Authorization") String token, @Query("room") String room, @Query("day") String day, @Query("month") String month, @Query("year") String year, @Query("legal") int legal);
}
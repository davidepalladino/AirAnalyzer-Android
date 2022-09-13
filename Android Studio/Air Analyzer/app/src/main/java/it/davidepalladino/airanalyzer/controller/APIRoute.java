/*
 * This control class provides several constants for the API communication with the Server.
 *
 * Copyright (c) 2020 Davide Palladino.
 * All right reserved.
 *
 * Copyright (c) 2022 Davide Palladino.
 * All right reserved.
 *
 * @author Davide Palladino
 * @contact davidepalladino@hotmail.com
 * @website https://davidepalladino.github.io/
 * @version 3.0.0
 * @date 4th September, 2022
 *
 */

package it.davidepalladino.airanalyzer.controller;

import com.google.gson.JsonArray;

import java.util.ArrayList;

import it.davidepalladino.airanalyzer.model.Authorization;
import it.davidepalladino.airanalyzer.model.Measure;
import it.davidepalladino.airanalyzer.model.Notification;
import it.davidepalladino.airanalyzer.model.Room;
import it.davidepalladino.airanalyzer.model.User;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface APIRoute {
//    String BASE_URL = "http://192.168.0.4:10000/";
    String BASE_URL = "http://10.11.11.117:10000/";

    @POST("user/login")
    @Headers({"Content-Type: application/json"})
    Call<Authorization> login(@Body User user);

    @GET("user/getMe")
    Call<User> getMe(@Header("Authorization") String token);

    @POST("user/register")
    @Headers({"Content-Type: application/json"})
    Call<User> signup(@Body User user);

    @GET("measure/getLatestDay")
    Call<ArrayList<Measure>> getLatestDayMeasures(@Header("Authorization") String token, @Query("date") String date, @Query("room_number") Integer roomNumber);

    @GET("measure/getAverageDay")
    Call<ArrayList<Measure>> getAverageDayMeasures(@Header("Authorization") String token, @Query("date") String date, @Query("room_number") Integer roomNumber);

    @GET("room/getAll")
    Call<ArrayList<Room>> getAllRooms(@Header("Authorization") String token, @Query("is_active") byte isActive);

    @FormUrlEncoded
    @PATCH("/room/changeName")
    Call<Room> changeNameRoom(@Header("Authorization") String token, @Field("number") int number, @Field("name") String name);










    // FIXME

    @POST("api/airanalyzer/renameRoom")
    Call<ResponseBody> renameRoom(@Header("Authorization") String token, @Query("ID") byte roomID, @Query("Name") String roomName);

    @POST("api/airanalyzer/activateRoom")
    Call<ResponseBody> activateRoom(@Header("Authorization") String token, @Query("ID") byte roomID);

    @POST("api/airanalyzer/deactivateRoom")
    Call<ResponseBody> deactivateRoom(@Header("Authorization") String token, @Query("ID") byte roomID);

    @GET("api/airanalyzer/getNotificationsLatest")
    Call<ArrayList<Notification>> getNotificationsLatest(@Header("Authorization") String token, @Query("UTC") int utc);

    @Headers({"Accept: application/json"})
    @PUT("api/airanalyzer/setStatusNotifications")
    Call<ResponseBody> setStatusNotifications(@Header("Authorization") String token, @Body JsonArray notifications);
}
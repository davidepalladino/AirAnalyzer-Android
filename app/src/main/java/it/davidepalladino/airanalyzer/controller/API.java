/*
 * This control class provides several constants for the API communication with the Server.
 *
 * Copyright (c) 2020 Davide Palladino.
 * All right reserved.
 *
 * @author Davide Palladino
 * @contact me@davidepalladino.com
 * @website www.davidepalladino.com
 * @version 2.0.0
 * @date 15th December, 2021
 *
 * This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public
 *  License as published by the Free Software Foundation; either
 *  version 3.0 of the License, or (at your option) any later version
 *
 * This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Lesser General Public License for more details.
 *
 */

package it.davidepalladino.airanalyzer.controller;

import com.google.gson.JsonArray;

import java.util.ArrayList;

import it.davidepalladino.airanalyzer.model.MeasuresDateAverage;
import it.davidepalladino.airanalyzer.model.MeasuresDateLatest;
import it.davidepalladino.airanalyzer.model.MeasuresTodayLatest;
import it.davidepalladino.airanalyzer.model.Notification;
import it.davidepalladino.airanalyzer.model.User;
import it.davidepalladino.airanalyzer.model.Room;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface API {
    String BASE_URL_LOCAL = "http://192.168.0.2:8008/";
    String BASE_URL_REMOTE = "http://airanalyzer.servehttp.com:50208/";

    @POST("api/login")
    Call<User> login(@Body User user);

    @POST("api/signupAirAnalyzer")
    Call<ResponseBody> signup(@Body User user);

    @GET("api/checkUsername")
    Call<ResponseBody> checkUsername(@Query("username") String username);

    @GET("api/checkEmail")
    Call<ResponseBody> checkEmail(@Query("email") String email);

    @GET("api/airanalyzer/getRooms")
    Call<ArrayList<Room>> getRooms(@Header("Authorization") String token, @Query("IsActive") byte isActive);

    @POST("api/airanalyzer/renameRoom")
    Call<ResponseBody> renameRoom(@Header("Authorization") String token, @Query("ID") byte roomID, @Query("Name") String roomName);

    @POST("api/airanalyzer/activateRoom")
    Call<ResponseBody> activateRoom(@Header("Authorization") String token, @Query("ID") byte roomID);

    @POST("api/airanalyzer/deactivateRoom")
    Call<ResponseBody> deactivateRoom(@Header("Authorization") String token, @Query("ID") byte roomID);

    @GET("api/airanalyzer/getMeasuresTodayLatest")
    Call<ArrayList<MeasuresTodayLatest>> getMeasuresTodayLatest(@Header("Authorization") String token, @Query("Date") String date, @Query("UTC") int utc);

    @GET("api/airanalyzer/getMeasuresDateLatest")
    Call<MeasuresDateLatest> getMeasuresDateLatest(@Header("Authorization") String token, @Query("Room") byte roomID, @Query("Date") String date, @Query("UTC") int utc);

    @GET("api/airanalyzer/getMeasuresDateAverage")
    Call<ArrayList<MeasuresDateAverage>> getMeasuresDateAverage(@Header("Authorization") String token, @Query("Room") byte roomID, @Query("Date") String date, @Query("UTC") int utc);

    @GET("api/airanalyzer/getNotificationsLatest")
    Call<ArrayList<Notification>> getNotificationsLatest(@Header("Authorization") String token, @Query("UTC") int utc);

    @Headers({"Accept: application/json"})
    @PUT("api/airanalyzer/setStatusNotifications")
    Call<ResponseBody> setStatusNotifications(@Header("Authorization") String token, @Body JsonArray notifications);
}
package it.davidepalladino.airanalyzer.controller;

import android.app.Service;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.text.format.Formatter;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;

import it.davidepalladino.airanalyzer.model.Login;
import it.davidepalladino.airanalyzer.model.MeasureAverage;
import it.davidepalladino.airanalyzer.model.MeasureFull;
import it.davidepalladino.airanalyzer.model.Room;
import it.davidepalladino.airanalyzer.model.Signup;
import it.davidepalladino.airanalyzer.model.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static it.davidepalladino.airanalyzer.controller.IntentConst.INTENT_BROADCAST;
import static it.davidepalladino.airanalyzer.controller.IntentConst.INTENT_MEASURE;
import static it.davidepalladino.airanalyzer.controller.IntentConst.INTENT_ROOM;
import static it.davidepalladino.airanalyzer.controller.IntentConst.INTENT_USER;
import static it.davidepalladino.airanalyzer.controller.Setting.NAMEPREFERENCE_TOKEN;

public class DatabaseService extends Service {
    public static final String DATABASE_SERVICE = "DATABASE_SERVICE";
    public static final String REQUEST_CODE_SERVICE = "REQUEST_CODE_SERVICE";
    public static final String STATUS_CODE_SERVICE = "STATUS_CODE_SERVICE";

    private API api;

    public IBinder binder = new LocalBinder();
    public class LocalBinder extends Binder {
        public DatabaseService getService() {
            return DatabaseService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(DATABASE_SERVICE, "Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String localIP = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());

        String baseURL;
        if (localIP.substring(0, 3).compareTo("192") == 0 || localIP.substring(0, 2).compareTo("10") == 0) {
            baseURL = API.BASE_URL_LOCAL;
        } else {
            baseURL = API.BASE_URL_REMOTE;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(API.class);

        api = retrofit.create(API.class);

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("DatabaseService", "Service destroyed");
    }

    public void login(Login login, String requestCodeBroadcast) {
        Call<Login.Response> call = api.login(login);
        call.enqueue(new Callback<Login.Response>() {
            @Override
            public void onResponse(Call<Login.Response> call, Response<Login.Response> response) {
                Intent intentBroadcast = new Intent(INTENT_BROADCAST);
                intentBroadcast.putExtra(REQUEST_CODE_SERVICE, requestCodeBroadcast);

                if (response.code() == 200) {
                    intentBroadcast.putExtra(NAMEPREFERENCE_TOKEN, response.body().getToken());
                }

                intentBroadcast.putExtra(STATUS_CODE_SERVICE, response.code());
                sendBroadcast(intentBroadcast);
            }

            @Override
            public void onFailure(Call<Login.Response> call, Throwable t) {
            }
        });
    }

    public void signup(Signup signup, String requestCodeBroadcast) {
        Call<Signup.NoResponse> call = api.signup(signup);
        call.enqueue(new Callback<Signup.NoResponse>() {
            @Override
            public void onResponse(Call<Signup.NoResponse> call, Response<Signup.NoResponse> response) {
                Intent intentBroadcast = new Intent(INTENT_BROADCAST);
                intentBroadcast.putExtra(REQUEST_CODE_SERVICE, requestCodeBroadcast);
                intentBroadcast.putExtra(STATUS_CODE_SERVICE, response.code());
                sendBroadcast(intentBroadcast);
            }

            @Override
            public void onFailure(Call<Signup.NoResponse> call, Throwable t) {

            }
        });
    }

    public void checkUsername(String username, String requestCodeBroadcast) {
        if (!username.isEmpty()) {
            Call<Signup.NoResponse> call = api.checkUsername(username.toString());
            call.enqueue(new Callback<Signup.NoResponse>() {
                @Override
                public void onResponse(Call<Signup.NoResponse> call, Response<Signup.NoResponse> response) {
                    Intent intentBroadcast = new Intent(INTENT_BROADCAST);
                    intentBroadcast.putExtra(REQUEST_CODE_SERVICE, requestCodeBroadcast);
                    intentBroadcast.putExtra(STATUS_CODE_SERVICE, response.code());
                    sendBroadcast(intentBroadcast);
                }

                @Override
                public void onFailure(Call<Signup.NoResponse> call, Throwable t) {

                }
            });
        }
    }

    public void checkEmail(String email, String requestCodeBroadcast) {
        if (!email.isEmpty()) {
            Call<Signup.NoResponse> call = api.checkEmail(email.toString());
            call.enqueue(new Callback<Signup.NoResponse>() {
                @Override
                public void onResponse(Call<Signup.NoResponse> call, Response<Signup.NoResponse> response) {
                    Intent intentBroadcast = new Intent(INTENT_BROADCAST);
                    intentBroadcast.putExtra(REQUEST_CODE_SERVICE, requestCodeBroadcast);
                    intentBroadcast.putExtra(STATUS_CODE_SERVICE, response.code());
                    sendBroadcast(intentBroadcast);
                }

                @Override
                public void onFailure(Call<Signup.NoResponse> call, Throwable t) {

                }
            });
        }
    }

    public void getUser(String token, String requestCodeBroadcast) {
        Call<User> call = api.getUser("Bearer " + token);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                User user = response.body();

                Intent intentBroadcast = new Intent(INTENT_BROADCAST);
                intentBroadcast.putExtra(REQUEST_CODE_SERVICE, requestCodeBroadcast);
                intentBroadcast.putExtra(STATUS_CODE_SERVICE, response.code());
                intentBroadcast.putExtra(INTENT_USER, user);
                sendBroadcast(intentBroadcast);
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

            }
        });
    }

    public void getActiveRooms(String token, String requestCodeBroadcast) {
        Call<ArrayList<Room>> call = api.getActiveRooms("Bearer " + token);
        call.enqueue(new Callback<ArrayList<Room>>() {
            @Override
            public void onResponse(Call<ArrayList<Room>> call, Response<ArrayList<Room>> response) {
                ArrayList<Room> listRooms = response.body();

                Intent intentBroadcast = new Intent(INTENT_BROADCAST);
                intentBroadcast.putExtra(REQUEST_CODE_SERVICE, requestCodeBroadcast);
                intentBroadcast.putExtra(STATUS_CODE_SERVICE, response.code());
                intentBroadcast.putParcelableArrayListExtra(INTENT_ROOM, listRooms);
                sendBroadcast(intentBroadcast);
            }

            @Override
            public void onFailure(Call<ArrayList<Room>> call, Throwable t) {

            }
        });
    }

    public void getInactiveRooms(String token, String requestCodeBroadcast) {
        Call<ArrayList<Room>> call = api.getInactiveRooms("Bearer " + token);
        call.enqueue(new Callback<ArrayList<Room>>() {
            @Override
            public void onResponse(Call<ArrayList<Room>> call, Response<ArrayList<Room>> response) {
                ArrayList<Room> listRooms = response.body();

                Intent intentBroadcast = new Intent(INTENT_BROADCAST);
                intentBroadcast.putExtra(REQUEST_CODE_SERVICE, requestCodeBroadcast);
                intentBroadcast.putExtra(STATUS_CODE_SERVICE, response.code());
                intentBroadcast.putParcelableArrayListExtra(INTENT_ROOM, listRooms);
                sendBroadcast(intentBroadcast);
            }

            @Override
            public void onFailure(Call<ArrayList<Room>> call, Throwable t) {

            }
        });
    }

    public void renameRoom(String token, Room room, String requestCodeBroadcast) {
        Call<Room.NoResponse> call = api.renameRoom("Bearer " + token, room);
        call.enqueue(new Callback<Room.NoResponse>() {
            @Override
            public void onResponse(Call<Room.NoResponse> call, Response<Room.NoResponse> response) {
                Intent intentBroadcast = new Intent(INTENT_BROADCAST);
                intentBroadcast.putExtra(REQUEST_CODE_SERVICE, requestCodeBroadcast);
                intentBroadcast.putExtra(STATUS_CODE_SERVICE, response.code());
                sendBroadcast(intentBroadcast);
            }

            @Override
            public void onFailure(Call<Room.NoResponse> call, Throwable t) {

            }
        });
    }

    public void addRoom(String token, Room room, String requestCodeBroadcast) {
        Call<Room.NoResponse> call = api.addRoom("Bearer " + token, room);
        call.enqueue(new Callback<Room.NoResponse>() {
            @Override
            public void onResponse(Call<Room.NoResponse> call, Response<Room.NoResponse> response) {
                Intent intentBroadcast = new Intent(INTENT_BROADCAST);
                intentBroadcast.putExtra(REQUEST_CODE_SERVICE, requestCodeBroadcast);
                intentBroadcast.putExtra(STATUS_CODE_SERVICE, response.code());
                sendBroadcast(intentBroadcast);
            }

            @Override
            public void onFailure(Call<Room.NoResponse> call, Throwable t) {

            }
        });
    }

    public void removeRoom(String token, Room room, String requestCodeBroadcast) {
        Call<Room.NoResponse> call = api.removeRoom("Bearer " + token, room);
        call.enqueue(new Callback<Room.NoResponse>() {
            @Override
            public void onResponse(Call<Room.NoResponse> call, Response<Room.NoResponse> response) {
                Intent intentBroadcast = new Intent(INTENT_BROADCAST);
                intentBroadcast.putExtra(REQUEST_CODE_SERVICE, requestCodeBroadcast);
                intentBroadcast.putExtra(STATUS_CODE_SERVICE, response.code());
                sendBroadcast(intentBroadcast);
            }

            @Override
            public void onFailure(Call<Room.NoResponse> call, Throwable t) {

            }
        });
    }

    public void getMeasureDateLatest(String token, String roomID, Calendar calendarSelected, String requestCodeBroadcast) {
        Call<MeasureFull> call = api.getMeasureDateLatest(
                "Bearer " + token,
                roomID,
                String.valueOf(calendarSelected.get(Calendar.DAY_OF_MONTH)),
                String.valueOf(calendarSelected.get(Calendar.MONTH) + 1),
                String.valueOf(calendarSelected.get(Calendar.YEAR))
        );

        call.enqueue(new Callback<MeasureFull>() {
            @Override
            public void onResponse(Call<MeasureFull> call, Response<MeasureFull> response) {
                MeasureFull measure = response.body();

                Intent intentBroadcast = new Intent(INTENT_BROADCAST);
                intentBroadcast.putExtra(REQUEST_CODE_SERVICE, requestCodeBroadcast);
                intentBroadcast.putExtra(STATUS_CODE_SERVICE, response.code());
                intentBroadcast.putExtra(INTENT_MEASURE, measure);
                sendBroadcast(intentBroadcast);
            }

            @Override
            public void onFailure(Call<MeasureFull> call, Throwable t) {

            }
        });
    }

    public void getMeasuresDateAverage(String token, String roomID, Calendar calendarSelected, String requestCodeBroadcast) {
        Call<ArrayList<MeasureAverage>> call = api.getMeasuresDateAverage(
                "Bearer " + token,
                roomID,
                String.valueOf(calendarSelected.get(Calendar.DAY_OF_MONTH)),
                String.valueOf(calendarSelected.get(Calendar.MONTH) + 1),
                String.valueOf(calendarSelected.get(Calendar.YEAR))
        );

        call.enqueue(new Callback<ArrayList<MeasureAverage>>() {
            @Override
            public void onResponse(Call<ArrayList<MeasureAverage>> call, Response<ArrayList<MeasureAverage>> response) {
                ArrayList<MeasureAverage> listMeasures = response.body();

                Intent intentBroadcast = new Intent(INTENT_BROADCAST);
                intentBroadcast.putExtra(REQUEST_CODE_SERVICE, requestCodeBroadcast);
                intentBroadcast.putExtra(STATUS_CODE_SERVICE, response.code());
                intentBroadcast.putParcelableArrayListExtra(INTENT_MEASURE, listMeasures);
                sendBroadcast(intentBroadcast);
            }

            @Override
            public void onFailure(Call<ArrayList<MeasureAverage>> call, Throwable t) {
            }
        });
    }
}
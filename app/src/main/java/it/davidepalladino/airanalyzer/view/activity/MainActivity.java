package it.davidepalladino.airanalyzer.view.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import it.davidepalladino.airanalyzer.R;
import it.davidepalladino.airanalyzer.controller.DatabaseService;
import it.davidepalladino.airanalyzer.view.widget.Toast;
import it.davidepalladino.airanalyzer.view.widget.ViewPagerRoom;
import it.davidepalladino.airanalyzer.controller.Setting;
import it.davidepalladino.airanalyzer.model.Room;
import it.davidepalladino.airanalyzer.view.dialog.RemoveRoomDialog;
import it.davidepalladino.airanalyzer.view.dialog.RenameRoomDialog;
import it.davidepalladino.airanalyzer.view.fragment.AddFragment;
import it.davidepalladino.airanalyzer.view.fragment.RoomFragment;

import static it.davidepalladino.airanalyzer.controller.DatabaseService.REQUEST_CODE_SERVICE;
import static it.davidepalladino.airanalyzer.controller.DatabaseService.STATUS_CODE_SERVICE;
import static it.davidepalladino.airanalyzer.controller.IntentConst.INTENT_BROADCAST;
import static it.davidepalladino.airanalyzer.controller.IntentConst.INTENT_MESSAGE_TOAST;
import static it.davidepalladino.airanalyzer.controller.IntentConst.INTENT_ROOM;
import static it.davidepalladino.airanalyzer.controller.Setting.NAMEPREFERENCE_TOKEN;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, DatePickerDialog.OnDateSetListener, ViewPager.OnPageChangeListener {
    public static final String BROADCAST_REQUEST_CODE_MASTER = "MainActivity";
    public static final String BROADCAST_REQUEST_CODE_EXTENSION_GET_ACTIVE_ROOM = "GetActiveRoom";
    private static final String BROADCAST_REQUEST_CODE_EXTENSION_LOGIN = "Login";
    private static final String TAB_ADD_ID = "10";
    private static final String TAB_ADD_NAME = "+";
    private static final int MAX_ATTEMPTS_LOGIN = 3;
    private static final int TIMEOUT_TRY_LOGIN = 5000;

    private SwipeRefreshLayout swipeRefreshLayout;
    private Toolbar toolbarMain;
    private TabLayout tabLayoutRooms;
    private ViewPagerRoom viewPagerRooms;
    private ExtendedFloatingActionButton floatingActionButtonCalendar;

    private Calendar calendarSelected;

    private Toast toast;
    private Setting setting;

    private AddFragment addFragment;

    private int attemptsLogin = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);

        toolbarMain = (Toolbar) findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbarMain);

        viewPagerRooms = (ViewPagerRoom) findViewById(R.id.viewPagerRooms);
        viewPagerRooms.addOnPageChangeListener(this);
        viewPagerRooms.setPageMargin(10);
        //viewPagerRooms.setClipToPadding(false);

        tabLayoutRooms = (TabLayout) findViewById(R.id.tabLayoutRooms);
        tabLayoutRooms.setupWithViewPager(viewPagerRooms);

        floatingActionButtonCalendar = (ExtendedFloatingActionButton) findViewById(R.id.floatingActionButtonCalendar);
        floatingActionButtonCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setting.saveRoomPage(viewPagerRooms.getCurrentItem());

                new DatePickerDialog(
                        MainActivity.this,
                        MainActivity.this,
                        calendarSelected.get(Calendar.YEAR),
                        calendarSelected.get(Calendar.MONTH),
                        calendarSelected.get(Calendar.DAY_OF_MONTH)
                ).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        toast = new Toast(MainActivity.this, getLayoutInflater());
        setting = new Setting(MainActivity.this);
        calendarSelected = Calendar.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(broadcastReceiver, new IntentFilter(INTENT_BROADCAST));

        Intent intentDatabaseService = new Intent(MainActivity.this, DatabaseService.class);
        bindService(intentDatabaseService, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unbindService(serviceConnection);
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onRefresh() {
        setting.saveRoomPage(viewPagerRooms.getCurrentItem());
        viewPagerRooms.getAdapter().notifyDataSetChanged();

        databaseService.getActiveRooms(setting.readToken(), BROADCAST_REQUEST_CODE_MASTER + BROADCAST_REQUEST_CODE_EXTENSION_GET_ACTIVE_ROOM);

        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        calendarSelected.set(Calendar.YEAR, year);
        calendarSelected.set(Calendar.MONTH, month);
        calendarSelected.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        viewPagerRooms.getAdapter().notifyDataSetChanged();
        viewPagerRooms.setCurrentItem(setting.readRoomPage());
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuItemRenameThisRoom:
                setting.saveRoomPage(viewPagerRooms.getCurrentItem());

                RenameRoomDialog renameRoomDialog = new RenameRoomDialog();
                renameRoomDialog.setRoom(pagerAdapterRoom.getRoomAtPosition(setting.readRoomPage()));
                renameRoomDialog.setToken(setting.readToken());
                renameRoomDialog.show(getSupportFragmentManager(), "");

                break;
            case R.id.menuItemRemoveThisRoom:
                setting.saveRoomPage(viewPagerRooms.getCurrentItem());

                RemoveRoomDialog removeRoomDialog = new RemoveRoomDialog();
                removeRoomDialog.setRoom(pagerAdapterRoom.getRoomAtPosition(setting.readRoomPage()));
                removeRoomDialog.setToken(setting.readToken());
                removeRoomDialog.show(getSupportFragmentManager(), "");

                if (setting.readRoomPage() != 0) {
                    setting.saveRoomPage(setting.readRoomPage() - 1);
                } else {
                    setting.saveRoomPage(0);
                }

                break;
            case R.id.menuItemLogout:
                setting.saveToken("");
                setting.saveLogin(null);

                Intent intentTo = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intentTo);
                finish();

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.menu_toolbar_main, menu);

        return true;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        Menu menuToolbarMain = toolbarMain.getMenu();

        /* Disabling the management of room and the Swipe if the page is 'AddFragment'. */
        if (pagerAdapterRoom.getRoomAtPosition(position).getId().compareTo(TAB_ADD_ID) != 0) {
            swipeRefreshLayout.setEnabled(true);
            floatingActionButtonCalendar.setVisibility(View.VISIBLE);

            onCreateOptionsMenu(menuToolbarMain);

            setting.saveRoomPage(position);
        } else {
            swipeRefreshLayout.setEnabled(false);
            floatingActionButtonCalendar.setVisibility(View.GONE);

            menuToolbarMain.removeItem(R.id.menuItemRenameThisRoom);
            menuToolbarMain.removeItem(R.id.menuItemRemoveThisRoom);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (swipeRefreshLayout != null && swipeRefreshLayout.isEnabled()) {
            swipeRefreshLayout.setEnabled(state == ViewPager.SCROLL_STATE_IDLE);
        }
    }

    public void createViewPagerRoom(ArrayList<Room> listRooms) {
        pagerAdapterRoom = new PageAdapterRoom(getSupportFragmentManager(), MainActivity.this, listRooms);
        viewPagerRooms.setAdapter(pagerAdapterRoom);
    }

    private PageAdapterRoom pagerAdapterRoom;
    public class PageAdapterRoom extends FragmentStatePagerAdapter {
        private Activity activity;

        private int nPage;
        private ArrayList<Room> listTabs;

        public PageAdapterRoom(FragmentManager fragmentManager, Activity activity, ArrayList<Room> listRooms) {
            super(fragmentManager);

            this.activity = activity;

            if (listRooms != null) {
                listTabs = listRooms;
                nPage = listTabs.size() + 1;
            } else {
                listTabs = new ArrayList<Room>();
                nPage = 1;
            }

            listTabs.add(new Room(TAB_ADD_ID, TAB_ADD_NAME));
        }

        @Override
        public int getCount() {
            return nPage;
        }

        @Override
        public Fragment getItem(int position) {
            if (position != (nPage - 1)) {
                return RoomFragment.newInstance(listTabs.get(position), calendarSelected.getTimeInMillis());
            } else {
                addFragment = AddFragment.newInstance();
                return addFragment;
            }
        }

        @Override
        public int getItemPosition(Object object) {
            if (object instanceof AddFragment) {
                return POSITION_UNCHANGED;
            } else {
                return POSITION_NONE;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position != (nPage - 1)) {
                if (listTabs.get(position).getName() != null) {
                    return listTabs.get(position).getId() + " | " + listTabs.get(position).getName();
                } else {
                    SpannableString spannableString = new SpannableString(listTabs.get(position).getId() + getString(R.string.roomNoName));
                    spannableString.setSpan(new RelativeSizeSpan(0.6f), 4, 13, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    return spannableString;
                }
            } else {
                return listTabs.get(position).getName();
            }
        }

        public Room getRoomAtPosition(int position) {
            return listTabs.get(position);
        }

        public int getPositionRoom(Room room) {
            Iterator<Room> iteratorRoom = listTabs.iterator();

            int position = 0;
            while (iteratorRoom.hasNext()) {
                if (iteratorRoom.next().getId().compareTo(room.getId()) == 0) {
                    return position;
                }

                position++;
            }

            return position;
        }
    }

    public DatabaseService databaseService;
    public ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DatabaseService.LocalBinder localBinder = (DatabaseService.LocalBinder) service;
            databaseService = localBinder.getService();

            databaseService.getActiveRooms(setting.readToken(), BROADCAST_REQUEST_CODE_MASTER + BROADCAST_REQUEST_CODE_EXTENSION_GET_ACTIVE_ROOM);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context contextFrom, Intent intentFrom) {
            if (intentFrom != null) {
                if (intentFrom.hasExtra(REQUEST_CODE_SERVICE) && intentFrom.hasExtra(STATUS_CODE_SERVICE)) {
                    int statusCode = intentFrom.getIntExtra(STATUS_CODE_SERVICE, 0);
                    switch (statusCode) {
                        case 200:
                            // GET ACTIVE ROOMS BROADCAST from this Activity
                            if (intentFrom.getStringExtra(REQUEST_CODE_SERVICE).compareTo(BROADCAST_REQUEST_CODE_MASTER + BROADCAST_REQUEST_CODE_EXTENSION_GET_ACTIVE_ROOM) == 0) {
                                ArrayList<Room> listRooms = intentFrom.getParcelableArrayListExtra(INTENT_ROOM);

                                createViewPagerRoom(listRooms);
                                viewPagerRooms.setCurrentItem(setting.readRoomPage());

                            // GET ACTIVE ROOMS BROADCAST from this AddFragment
                            } else if (intentFrom.getStringExtra(REQUEST_CODE_SERVICE).compareTo(AddFragment.BROADCAST_REQUEST_CODE_MASTER + AddFragment.BROADCAST_REQUEST_CODE_EXTENSION_GET_ACTIVE_ROOM) == 0) {
                                ArrayList<Room> listRooms = intentFrom.getParcelableArrayListExtra(INTENT_ROOM);

                                Room roomSelected = addFragment.getRoomSelected();

                                createViewPagerRoom(listRooms);
                                viewPagerRooms.setCurrentItem(pagerAdapterRoom.getPositionRoom(roomSelected));

                                swipeRefreshLayout.setEnabled(true);
                                floatingActionButtonCalendar.setVisibility(View.VISIBLE);

                                onCreateOptionsMenu(toolbarMain.getMenu());

                            // SET ROOM BROADCAST
                            // REMOVE ROOM BROADCAST
                            } else if (
                                    (intentFrom.getStringExtra(REQUEST_CODE_SERVICE).compareTo(RenameRoomDialog.BROADCAST_REQUEST_CODE_MASTER + RenameRoomDialog.BROADCAST_REQUEST_CODE_EXTENSION_SET_ROOM) == 0) ||
                                     (intentFrom.getStringExtra(REQUEST_CODE_SERVICE).compareTo(RemoveRoomDialog.BROADCAST_REQUEST_CODE_MASTER + RemoveRoomDialog.BROADCAST_REQUEST_CODE_EXTENSION_REMOVE_ROOM) == 0)
                            ) {
                                databaseService.getActiveRooms(setting.readToken(), BROADCAST_REQUEST_CODE_MASTER + BROADCAST_REQUEST_CODE_EXTENSION_GET_ACTIVE_ROOM);

                            // LOGIN BROADCAST
                            } else if (intentFrom.getStringExtra(REQUEST_CODE_SERVICE).compareTo(BROADCAST_REQUEST_CODE_MASTER + BROADCAST_REQUEST_CODE_EXTENSION_LOGIN) == 0) {
                                setting.saveToken(intentFrom.getStringExtra(NAMEPREFERENCE_TOKEN));
                                attemptsLogin = 1;
                            }

                            break;
                        case 204:
                            // GET ACTIVE ROOMS BROADCAST from this Activity
                            if (intentFrom.getStringExtra(REQUEST_CODE_SERVICE).compareTo(BROADCAST_REQUEST_CODE_MASTER + BROADCAST_REQUEST_CODE_EXTENSION_GET_ACTIVE_ROOM) == 0) {
                                createViewPagerRoom(null);
                                viewPagerRooms.setCurrentItem(setting.readRoomPage());
                            }

                            break;
                        case 401:
                            if (attemptsLogin <= MAX_ATTEMPTS_LOGIN) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        databaseService.login(setting.readLogin(), BROADCAST_REQUEST_CODE_MASTER + BROADCAST_REQUEST_CODE_EXTENSION_LOGIN);
                                        attemptsLogin++;
                                    }
                                }, TIMEOUT_TRY_LOGIN);
                            } else {
                                Intent intentTo = new Intent(MainActivity.this, LoginActivity.class);
                                intentTo.putExtra(INTENT_MESSAGE_TOAST, getString(R.string.toastUserError));
                                startActivity(intentTo);
                                finish();
                            }

                            break;
                        case 404:
                        case 500:
                            toast.makeToastBlue(R.drawable.ic_baseline_error_24, getString(R.string.toastServerOffline));
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    };
}
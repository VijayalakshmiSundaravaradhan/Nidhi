package kiosk.android.econ.mcrbooking;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;


import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.econ.kannan.DBReqHandler;

import android.view.ViewGroup;
import android.widget.EditText;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import jp.wasabeef.blurry.Blurry;


public class MainActivity extends AppCompatActivity implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener{

    MaterialCalendarView widget;
    AlertDialog.Builder builder;
    AlertDialog cancelDialog;
    Activity mActivity;

    CoordinatorLayout eventsList;
    LinearLayout noEvents;

    EditText bookingIDWidget;
    EditText userWidget;
    TextView currentDateText;

    String bookingID;
    String user;

    int previousGroup;
    EventDecorator mDecorator;

    String cancelResponseMessageType;
    String cancelResponseString;

    JSONObject cancelRequest;

    int height;
    int width;

    public static final String[] months = new String[]{"jan", "feb", "mar", "apr", "may", "jun",
            "jul", "aug", "sep", "oct", "nov", "dec"};
    public static final String[] Months = new String[]{"January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"};

    int selectedYear;
    int selectedMonth;
    int selectedDay;

    String selectedMonthString;

    int monthViewed;

    JSONObject monthRequest;
    String monthResponseString;
    String monthRequestMessageType;
    String monthResponseMessageType;
    JSONArray daysBooked;


    int currentYear;
    int currentMonth;
    int currentDay;

    JSONObject dayRequest;
    String dayResponseString;
    String dayRequestMessageType;
    String dayResponseMessageType;
    JSONObject bookings;
    JSONArray roomsBooked;

    String cancelRoom;
    String[] roomsActuallyBooked;

    final int noOfRooms = 5;

    String[] roomNames = new String[noOfRooms];

    DBReqHandler dbReqHandler;

    private RecyclerView recyclerView;
    private List<Item> eventList;
    private EventListAdapter mAdapter;

    int eventNumber = 0;
    Item deletedItem;
    int deletedIndex;

//    CountDownTimer inActivityTimer;

//    @Override
//    public void onUserInteraction() {
//        super.onUserInteraction();
//        inActivityTimer.cancel();
//        inActivityTimer.start();
//    }

    public void daySubscribe() {

        eventsList.setVisibility(View.INVISIBLE);
        noEvents.setVisibility(View.VISIBLE);

        dayRequest = new JSONObject();

        try {

            Random r = new Random();
            String clientID = String.valueOf(r.nextInt(999999 - 100000) + 100000);

            dayRequest.put("Client_ID",clientID);
            dayRequest.put("msg_type", dayRequestMessageType);
            dayRequest.put("year", selectedYear);
            dayRequest.put("month", selectedMonthString);
            dayRequest.put("day", selectedDay);

            dbReqHandler.dbRequest(DBReqHandler.MSG_ID_PARSE_DATE,dayRequest.toString());

        }catch (JSONException e){
            e.printStackTrace();
        }


        }

        public void OnDaySubscription()
        {

//            int[] bookingsInEachRoom;


            int t;

            try {
                JSONObject dayResponse = new JSONObject(dayResponseString);
//                Log.d("No.of bookings : ", "On day sub" + dayResponse.optString("Client_ID"));

                if (dayResponse.optString("msg_type").equals(dayResponseMessageType) && dayResponse.optString("result").equals("success")) {

                    bookings = dayResponse.getJSONObject("bookings");
//                    Log.d("No.of bookings : ", String.valueOf(bookings.length()));

                    if (bookings.length() <= 0) {
                        return;
                    }

                    t = 0;
//                    bookingsInEachRoom = new int[bookings.length()];
                    roomsActuallyBooked = new String[bookings.length()];

                    for (int k = 0; k < roomNames.length; k++) {
                        if (bookings.has(roomNames[k])) {
                            roomsBooked = bookings.getJSONArray(roomNames[k]);
//                            Log.d("Actual rooms booked", roomNames[k]);

//                            bookingsInEachRoom[t] = roomsBooked.length();
                            roomsActuallyBooked[t] = roomNames[k];
                            t++;
                        }
                    }


                    JSONArray respArray = new JSONArray();

                    for (int j = 0; j < bookings.length(); j++) {
                        roomsBooked = bookings.getJSONArray(roomsActuallyBooked[j]);
//                        Log.d("No.of bookings : ", String.valueOf(roomsBooked.length()));

                        for (int i = 0; i < roomsBooked.length(); i++) {
                            JSONObject event = roomsBooked.getJSONObject(i);

//                            Log.d("room", roomsActuallyBooked[j]);
//                            Log.d("time",event.optString("ST") + " - " + event.optString("ET"));
//                            Log.d("person", "Booked by " + event.optString("user"));

                            JSONObject eventEntry = new JSONObject();
                            eventEntry.put("room", "Booked at " + roomsActuallyBooked[j]);
                            eventEntry.put("time",event.optString("ST") + " - " + event.optString("ET"));
                            eventEntry.put("person", "Booked by " + event.optString("user"));

                            respArray.put(eventEntry);

                        }
//                        Log.d("List",respArray.toString());
                    }

//                    Log.d("List-- Final",respArray.toString());

                    List<Item> items = new Gson().fromJson(respArray.toString(), new TypeToken<List<Item>>() {
                    }.getType());

                    // adding items to list
                    eventList.clear();
                    eventList.addAll(items);

                    mActivity.runOnUiThread(new Runnable() {
                        public void run() {
//                            Log.d("======= ", "====Setting list");
                            // refreshing recycler view
                            eventsList.setVisibility(View.VISIBLE);
                            noEvents.setVisibility(View.INVISIBLE);
                            mAdapter.notifyDataSetChanged();
                        }
                    });

                }

            } catch (JSONException e)
            {
                e.printStackTrace();
            }

        }


    public void monthSubscribe() {
        monthRequest = new JSONObject();
        try {
            Random r = new Random();
            String clientID = String.valueOf(r.nextInt(999999 - 100000) + 100000);

            monthRequest.put("Client_ID", clientID);
            monthRequest.put("msg_type", monthRequestMessageType);
            monthRequest.put("year", selectedYear);
            monthRequest.put("month", months[monthViewed]);

//            Log.d("Month string : ", months[monthViewed]);
            dbReqHandler.dbRequest(DBReqHandler.MSG_ID_PARSE_MONTH,monthRequest.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    public void OnMonthSubscription(){
        try {
            JSONObject monthResponse = new JSONObject(monthResponseString);
//            mActivity.runOnUiThread(new Runnable() {
//                public void run() {
//                    widget.removeDecorators();
//                }
//            });
            Log.d("============",monthResponseString);
            if(monthResponse.optString("msg_type").equals(monthResponseMessageType) && monthResponse.optString("result").equals("success")) {
                daysBooked = monthResponse.optJSONArray("days");

                    final HashSet<CalendarDay> datesHighlighted = new HashSet<>();
                    for (int i = 0; i < daysBooked.length(); i++) {
                        Log.d("daysBooked", daysBooked.getString(i));
                        Log.d("daysBooked", months[monthViewed]);
                        datesHighlighted.add(CalendarDay.from(selectedYear, monthViewed, Integer.parseInt(daysBooked.getString(i))));
                    }
                    mActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            mDecorator = new EventDecorator(Color.parseColor("#FF4500"), 3, datesHighlighted);
                            widget.addDecorator(mDecorator);
                        }
                    });

            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume()
    {
        Log.d("debug","onResume" );
        super.onResume();
        widget.setTileWidth((width)/28);
        monthSubscribe();
        daySubscribe();
    }

    public class reqHandler implements DBReqHandler.IDBReqHandler, Serializable {

       @Override
            public void testCallback(String ans)
            {
                JSONObject response;
                try {
                    response = new JSONObject(ans);

                    if(response.optString("msg_type").equals(monthResponseMessageType)) {
                        monthResponseString = ans;
                        OnMonthSubscription();
                    }

                    if(response.optString("msg_type").equals(dayResponseMessageType)) {
                        dayResponseString = ans;
                        OnDaySubscription();
                    }

                    if(response.optString("msg_type").equals(cancelResponseMessageType)) {
                        cancelResponseString = ans;
                        OnCancelling();
                    }

                }catch (JSONException e){
                    e.printStackTrace();
                }


            }
    }

    public void OnCancelling()
    {
        try {
            final JSONObject cancelResponse = new JSONObject(cancelResponseString);
            if (cancelResponse.optString("result").equals("success")) {

                mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        widget.removeDecorators();

                        SweetAlertDialog pDialog = new SweetAlertDialog(mActivity, SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Booking Cancelled!")
                                .setContentText(cancelResponse.optString("err_msg"));
                        pDialog.getProgressHelper().setBarColor(Color.parseColor("#555555"));
                        pDialog.show();

                        mAdapter.removeItem(eventNumber);
                        monthSubscribe();

                    }
                });
            } else {
                mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        new SweetAlertDialog(mActivity, SweetAlertDialog.WARNING_TYPE)
                                .setTitleText("Cancelling Failed!")
                                .setContentText(cancelResponse.optString("err_msg"))
                                .show();
                    }
                });
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View decorView = getWindow().getDecorView();


//        inActivityTimer = new CountDownTimer(300000, 300000) {
//            @Override
//            public void onTick(long l) {
//            }
//
//            @Override
//            public void onFinish() {
//                Toast.makeText(getApplicationContext(),"5 mins of inactivity", Toast.LENGTH_LONG).show();
//            }
//        }.start();

        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        recyclerView = findViewById(R.id.recycler_view);
        eventList = new ArrayList<>();
        mAdapter = new EventListAdapter(this, eventList);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(mAdapter);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        mActivity = this;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;

        widget = findViewById(R.id.calendarView);
        widget.setAllowClickDaysOutsideCurrentMonth(true);
        widget.setTileWidth((width)/30);
        widget.setDynamicHeightEnabled(true);

        Calendar c = Calendar.getInstance();
        widget.setCurrentDate(c);
        widget.setSelectedDate(c);
        currentYear = c.get(Calendar.YEAR);
        currentMonth = c.get(Calendar.MONTH);
        currentDay = c.get(Calendar.DAY_OF_MONTH);

        roomNames = this.getResources().getStringArray(R.array.confRooms);
//        roomNames[0] = "Main Conference Room";
//        roomNames[1] = "Camera Conference Room";
//        roomNames[2] = "Product Conference Room";
//        roomNames[3] = "Adjacent Room to MCR";
//        roomNames[4] = "Reception Area Room";
//        roomNames[5] = "MiscRoom";

        selectedMonth = currentMonth;
        monthViewed = currentMonth;
        selectedYear = currentYear;
        selectedDay = currentDay;
        selectedMonthString = months[selectedMonth];

        Log.d("Month : ", monthViewed + " " + selectedMonth);

        monthRequestMessageType = "RQ_RD_MN";
        monthResponseMessageType = "RP_RD_MN";
        cancelResponseMessageType = "RP_CN_CNF";
        dayRequestMessageType = "RQ_RD_DAY";
        dayResponseMessageType = "RP_RD_DAY";

        previousGroup = -1;
        eventsList = findViewById(R.id.eventsList);
        noEvents = findViewById(R.id.noEvents);
        Blurry.with(MainActivity.this)
                .radius(25)
                .sampling(1)
                .async()
                .animate(500)
                .onto((ViewGroup) findViewById(R.id.eventsList));

        builder = new AlertDialog.Builder(this);
        builder.setTitle("Cancel Event");

        LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
        View dialogView = layoutInflater.inflate(R.layout.cancel_dialog, null);

        builder.setView(dialogView);

        bookingIDWidget = dialogView.findViewById(R.id.bookingID);
        userWidget = dialogView.findViewById(R.id.user);
        currentDateText = findViewById(R.id.currentDateText);
        String currentDateString = Months[currentMonth] + " " + currentDay + ", " + currentYear;
        currentDateText.setText(currentDateString);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                user = userWidget.getText().toString();
                bookingID = bookingIDWidget.getText().toString();

                if(user.isEmpty())
                    Toast.makeText(getApplicationContext(),"User name not specified!!" , Toast.LENGTH_SHORT).show();
                else if(bookingID.isEmpty())
                    Toast.makeText(getApplicationContext(),"Booking ID not specified!!" , Toast.LENGTH_SHORT).show();
                else
                {
                    if(selectedYear > currentYear || (selectedYear == currentYear && (selectedMonth > currentMonth || (selectedMonth == currentMonth && selectedDay >= currentDay)))){
                        cancelRequest = new JSONObject();
                        Random r = new Random();
                        String clientID = String.valueOf(r.nextInt(999999 - 100000) + 100000);

                        try {
                            cancelRequest.put("Client_ID", clientID);
                            cancelRequest.put("msg_type", "RQ_CL");
                            cancelRequest.put("Book_ID", bookingID+"rm_bk");
                            cancelRequest.put("user", user);
                            cancelRequest.put("year", selectedYear);
                            cancelRequest.put("month", months[selectedMonth]);
                            cancelRequest.put("day", selectedDay);
                            cancelRequest.put("room", cancelRoom);

                            dbReqHandler.dbRequest(DBReqHandler.MSG_ID_CANCEL, cancelRequest.toString());

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"Please select a valid date ", Toast.LENGTH_SHORT).show();
                    }

                }

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        cancelDialog = builder.create();

        DBReqHandler.IDBReqHandler requestHandler = new reqHandler();
        dbReqHandler = new DBReqHandler(getApplicationContext(),requestHandler);

        widget.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {

                selectedDay = date.getDay();
                selectedMonth = date.getMonth();
                selectedMonthString = months[selectedMonth];
                selectedYear = date.getYear();

                daySubscribe();
            }

        });

        widget.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                monthViewed = date.getMonth() ;
//                Log.d("Month Changed", monthViewed + " " + date.getMonth());

               monthSubscribe();
            }
        });

    }

    public void gotoformActivity(View v)    {
        Intent formActivity = new Intent(this,Main2Activity.class);

        Log.d("month issue", currentMonth + " " + currentDay + " " + currentYear + " " + selectedMonth + " " + selectedDay);
        if(selectedYear > currentYear || (selectedYear == currentYear && (selectedMonth > currentMonth || (selectedMonth == currentMonth && selectedDay >= currentDay))))
        {
            formActivity.putExtra("selectedYear", selectedYear);
            formActivity.putExtra("selectedMonth", selectedMonth);
            formActivity.putExtra("selectedMonthString", selectedMonthString);
            formActivity.putExtra("selectedDay", selectedDay);
            startActivity(formActivity);
        }
        else {
            Toast.makeText(getApplicationContext(),"Please select a valid date ", Toast.LENGTH_SHORT).show();

        }

    }

    /**
     * callback when recycler view is swiped
     * item will be removed on swiped
     * undo option will be provided in snackbar to restore the item
     */
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof EventListAdapter.MyViewHolder) {
            // get the removed item name to display it in snack bar
            cancelRoom = eventList.get(viewHolder.getAdapterPosition()).getRoom();
            Log.d("cancelRoom", cancelRoom);
            // backup of removed item for undo purpose
            deletedItem = eventList.get(viewHolder.getAdapterPosition());
            deletedIndex = viewHolder.getAdapterPosition();

            // remove the item from recycler view
            eventNumber = viewHolder.getAdapterPosition();


            userWidget.setText("");
            bookingIDWidget.setText("");
            cancelDialog.show();
            mAdapter.removeItem(viewHolder.getAdapterPosition());
            mAdapter.restoreItem(deletedItem, deletedIndex);

        }
    }

}

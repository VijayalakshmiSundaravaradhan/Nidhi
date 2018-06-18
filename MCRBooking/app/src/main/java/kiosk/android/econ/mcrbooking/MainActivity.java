package kiosk.android.econ.mcrbooking;

import android.os.Handler;
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


    EditText bookingIDWidget;
    EditText userWidget;
    TextView currentDateText;

    int screenHeight;
    int screenWidth;

    public static final String[] months = new String[]{"jan", "feb", "mar", "apr", "may", "jun",
            "jul", "aug", "sep", "oct", "nov", "dec"};
    public static final String[] Months = new String[]{"January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"};
    String selectedMonthString;

    int selectedYear;
    int selectedMonth;
    int selectedDay;
    int currentYear;
    int currentMonth;
    int currentDay;
    int monthViewed;

    JSONArray daysBooked;

    JSONObject cancelRequest;
    JSONObject monthRequest;
    JSONObject dayRequest;
    JSONObject bookings;
    JSONArray roomsBooked;

    String dayResponseString;
    String dayRequestMessageType;
    String dayResponseMessageType;
    String monthResponseString;
    String monthRequestMessageType;
    String monthResponseMessageType;
    String cancelResponseMessageType;
    String cancelResponseString;

    String cancelRoom;
    String[] roomsActuallyBooked;
    String bookingID;
    String user;

    final int noOfRooms = 5;
    String[] roomNames = new String[noOfRooms];

    private List<Item> eventList;
    private EventListAdapter mAdapter;
    MaterialCalendarView widget;
    AlertDialog.Builder builder;
    AlertDialog cancelDialog;
    EventDecorator mDecorator;
    CoordinatorLayout eventsList;
    LinearLayout noEvents;
    Item deletedItem;
    int eventNumber = 0;
    int deletedIndex;

    Handler handler;
    Runnable refreshThread;

    DBReqHandler dbReqHandler;
    Activity mActivity;

    @Override
    public void onUserInteraction() {
        // TODO Auto-generated method stub
        super.onUserInteraction();
        Log.d("inactivity", "Usr interacted");
        stopHandler();//stop first and then start
        startHandler();
    }
    public void stopHandler() {
        handler.removeCallbacks(refreshThread);
    }
    public void startHandler() {
        handler.postDelayed(refreshThread, 5*60*1000); //for 5 minutes
    }

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
            try {
                int t;
                JSONObject dayResponse = new JSONObject(dayResponseString);
                if (dayResponse.optString("msg_type").equals(dayResponseMessageType) && dayResponse.optString("result").equals("success")) {

                    bookings = dayResponse.getJSONObject("bookings");

                    if (bookings.length() <= 0) {
                        return;
                    }

                    t = 0;
                    roomsActuallyBooked = new String[bookings.length()];

                    for (int k = 0; k < roomNames.length; k++) {
                        if (bookings.has(roomNames[k])) {
                            roomsBooked = bookings.getJSONArray(roomNames[k]);

                            roomsActuallyBooked[t] = roomNames[k];
                            t++;
                        }
                    }


                    JSONArray respArray = new JSONArray();

                    for (int j = 0; j < bookings.length(); j++) {
                        roomsBooked = bookings.getJSONArray(roomsActuallyBooked[j]);

                        for (int i = 0; i < roomsBooked.length(); i++) {
                            JSONObject event = roomsBooked.getJSONObject(i);
                            JSONObject eventEntry = new JSONObject();
                            eventEntry.put("room", "Booked at " + roomsActuallyBooked[j]);
                            eventEntry.put("time",event.optString("ST") + " - " + event.optString("ET"));
                            eventEntry.put("person", "Booked by " + event.optString("user"));

                            respArray.put(eventEntry);

                        }
                    }

                    List<Item> items = new Gson().fromJson(respArray.toString(), new TypeToken<List<Item>>() {
                    }.getType());

                    // adding items to list
                    eventList.clear();
                    eventList.addAll(items);

                    mActivity.runOnUiThread(new Runnable() {
                        public void run() {
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

            dbReqHandler.dbRequest(DBReqHandler.MSG_ID_PARSE_MONTH,monthRequest.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    public void OnMonthSubscription(){
        try {
            JSONObject monthResponse = new JSONObject(monthResponseString);
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
        widget.setTileWidth((screenWidth)/28);
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

        handler = new Handler();
        refreshThread = new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                monthSubscribe();
                daySubscribe();
                startHandler();
            }
        };
        startHandler();

        View decorView = getWindow().getDecorView();
        final int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        RecyclerView recyclerView;

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
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;

        widget = findViewById(R.id.calendarView);
        widget.setAllowClickDaysOutsideCurrentMonth(true);
        widget.setTileWidth((screenWidth)/30);
        widget.setDynamicHeightEnabled(true);

        Calendar c = Calendar.getInstance();
        widget.setCurrentDate(c);
        widget.setSelectedDate(c);
        currentYear = c.get(Calendar.YEAR);
        currentMonth = c.get(Calendar.MONTH);
        currentDay = c.get(Calendar.DAY_OF_MONTH);

        roomNames = this.getResources().getStringArray(R.array.confRooms);

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

    public void refresh(View v) {
        monthSubscribe();
        daySubscribe();
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

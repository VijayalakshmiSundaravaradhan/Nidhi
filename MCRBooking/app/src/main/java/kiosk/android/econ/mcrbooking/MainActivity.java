package kiosk.android.econ.mcrbooking;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
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
import android.widget.ExpandableListView;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

import java.util.HashMap;
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


public class MainActivity extends AppCompatActivity implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener, SurfaceHolder.Callback, MediaPlayer.OnPreparedListener {

    MaterialCalendarView widget;
    AlertDialog.Builder builder;
    AlertDialog cancelDialog;
    Activity mActivity;

    CoordinatorLayout eventsList;
//    SimpleExpandableListAdapter eventsAdapter;

    EditText bookingIDWidget;
    EditText userWidget;
    TextView currentDateText;
//    TextView cancelUser;
//    TextView cancelBookingID;

    String bookingID;
    String user;

    private MediaPlayer mediaPlayer;
    private SurfaceHolder vidHolder;
    private SurfaceView vidSurface;
    String vidAddress = "https://archive.org/download/ksnn_compilation_master_the_internet/ksnn_compilation_master_the_internet_512kb.mp4";

    int previousGroup;
    EventDecorator mDecorator;

//    ImageButton cancelButton;

    String cancelResponseMessageType;
    String cancelResponseString;

    JSONObject cancelRequest;

    int height;
    int width;

    int groupExpanded=0;


    private static final String NAME = "NAME";
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

    final int noOfRooms = 2;

    String[] roomNames = new String[noOfRooms];

    DBReqHandler dbReqHandler;

    private RecyclerView recyclerView;
    private List<Item> eventList;
    private EventListAdapter mAdapter;

    int eventNumber = 0;
    Item deletedItem;
    int deletedIndex;

    public void daySubscribe() {

        eventsList.setVisibility(View.INVISIBLE);
//        cancelButton.setVisibility(View.INVISIBLE);

        dayRequest = new JSONObject();

        try {

            Random r = new Random();
            String clientID = String.valueOf(r.nextInt(999999 - 100000) + 100000);

            dayRequest.put("Client_ID",clientID);
            dayRequest.put("msg_type", dayRequestMessageType);
            dayRequest.put("year", selectedYear);
            dayRequest.put("month", selectedMonthString);
            dayRequest.put("day", selectedDay);

            //Toast.makeText(getApplicationContext(), dayRequest.toString(), Toast.LENGTH_SHORT).show();

            dbReqHandler.dbRequest(DBReqHandler.MSG_ID_PARSE_DATE,dayRequest.toString());

        }catch (JSONException e){
            e.printStackTrace();
        }


        }

        public void OnDaySubscription()
        {

            int[] bookingsInEachRoom;


            //bookingsInEachRoom = new int[noOfRooms];

            int t;

            try {
                JSONObject dayResponse = new JSONObject(dayResponseString);
                Log.d("No.of bookings : ", "On day sub" + dayResponse.optString("Client_ID"));

                if (dayResponse.optString("msg_type").equals(dayResponseMessageType) && dayResponse.optString("result").equals("success")) {

                    bookings = dayResponse.getJSONObject("bookings");
                    Log.d("No.of bookings : ", String.valueOf(bookings.length()));

                    if (bookings.length() <= 0) {
                        return;
                    }

                    t = 0;
                    bookingsInEachRoom = new int[bookings.length()];
                    roomsActuallyBooked = new String[bookings.length()];

                    for (int k = 0; k < roomNames.length; k++) {
                        if (bookings.has(roomNames[k])) {
                            roomsBooked = bookings.getJSONArray(roomNames[k]);
                            Log.d("Actual rooms booked", roomNames[k]);

                            bookingsInEachRoom[t] = roomsBooked.length();
                            roomsActuallyBooked[t] = roomNames[k];
                            t++;
                        }
                    }

//                    String[][] bookingDetails;
//                    bookingDetails = new String[bookings.length()][100];
                    List<Map<String, String>> groupData = new ArrayList<>();
                    List<List<Map<String, String>>> childData = new ArrayList<>();

                    JSONArray respArray = new JSONArray();

                    for (int j = 0; j < bookings.length(); j++) {
                        roomsBooked = bookings.getJSONArray(roomsActuallyBooked[j]);
                        Log.d("No.of bookings : ", String.valueOf(roomsBooked.length()));

                        t = 0;
//                        int i = 0;

                        for (int i = 0; i < roomsBooked.length(); i++) {
                            JSONObject event = roomsBooked.getJSONObject(i);

                            Log.d("room", roomsActuallyBooked[j]);
                            Log.d("time","Booked from " + event.optString("ST") + " to " + event.optString("ET"));
                            Log.d("person", "Booked by " + event.optString("user"));

                            JSONObject eventEntry = new JSONObject();
                            eventEntry.put("room", roomsActuallyBooked[j]);
                            eventEntry.put("time","Booked from " + event.optString("ST") + " to " + event.optString("ET"));
                            eventEntry.put("person", "Booked by " + event.optString("user"));

                            respArray.put(eventEntry);

//                            bookingDetails[i][t] = event.optString("ST") + " - " + event.optString("ET");
//                            Map<String, String> curGroupMap = new HashMap<>();
//                            groupData.add(curGroupMap);
//                            curGroupMap.put(NAME, roomsActuallyBooked[j]);
//                            curGroupMap.put("time", bookingDetails[i][t]);
//                            Log.d("Timing: ", bookingDetails[i][t] );
//                            t++;
//
////                            bookingDetails[i][t] = roomsActuallyBooked[j];
////                            t++;
//
//                            bookingDetails[i][t] = "Booking ID : " + event.optString("Book_ID");
//                            List<Map<String, String>> children = new ArrayList<>();
////                            Map<String, String> idChildMap = new HashMap<>();
////                            children.add(idChildMap);
////                            idChildMap.put(NAME, bookingDetails[i][t]);
//                            Log.d("Booking ID : ", bookingDetails[i][t]);
//                            t++;
//
//
//                            bookingDetails[i][t] = "Person: " + event.optString("user");
//                            Map<String, String> personChildMap = new HashMap<>();
//                            children.add(personChildMap);
//                            personChildMap.put(NAME, bookingDetails[i][t]);
//                            Log.d("Person: ", bookingDetails[i][t]);
//                            t++;
//
//                            bookingDetails[i][t] = "Status: " + event.optString("status");
//                            Map<String, String> statusChildMap = new HashMap<>();
//                            children.add(statusChildMap);
//                            statusChildMap.put(NAME, bookingDetails[i][t]);
//
//                            childData.add(children);
//                            Log.d("Status: ", bookingDetails[i][t]);
//                            t++;
//
//
                        }
                        Log.d("List",respArray.toString());
                    }

                    Log.d("List-- Final",respArray.toString());

                    List<Item> items = new Gson().fromJson(respArray.toString(), new TypeToken<List<Item>>() {
                    }.getType());

                    // adding items to cart list
                    eventList.clear();
                    eventList.addAll(items);

                    mActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            Log.d("======= ", "====Setting list");
                            // refreshing recycler view
                            eventsList.setVisibility(View.VISIBLE);
                            mAdapter.notifyDataSetChanged();
                        }
                    });





                // add data in group and child list
                /*for (int i = 0; i < bookings.length(); i++) {
                    Map<String, String> curGroupMap = new HashMap<>();
                    groupData.add(curGroupMap);
                    curGroupMap.put(NAME, roomNames[i]);

                    List<Map<String, String>> children = new ArrayList<>();
                    for (int j = 0; j < bookingsInEachRoom[i]; j++) {
                        Map<String, String> curChildMap = new HashMap<>();
                        children.add(curChildMap);
                        curChildMap.put(NAME, bookingDetails[i][j]);
                    }
                    childData.add(children);
                }*/
                // define arrays for displaying data in Expandable list view
//                String groupFrom[] = {NAME,"time"};
//                int groupTo[] = {R.id.parent_layout,R.id.timing};
//                String childFrom[] = {NAME};
//                int childTo[] = {R.id.child_layout};
//
//                    Log.d("======= ", "====Setting adapter");
//
//                // Set up the adapter
//                    eventsAdapter = new SimpleExpandableListAdapter(this, groupData,
//                            R.layout.list_group,
//                            groupFrom, groupTo,
//                            childData, R.layout.list_child,
//                            childFrom, childTo);
//
//                    mActivity.runOnUiThread(new Runnable() {
//                        public void run() {
//                            Log.d("======= ", "====Setting list");
//                            eventsList.setAdapter(eventsAdapter);
//                            eventsList.setVisibility(View.VISIBLE);
//                        }
//                    });

                }

            } catch (JSONException e)
            {
                e.printStackTrace();
            }

        }


    public void monthSubscribe() {
        Log.d("Mn Sub", "On monthSubscribe-----");
        monthRequest = new JSONObject();
        Log.d("DECORATOR", "---------------");
        try {
            Random r = new Random();
            String clientID = String.valueOf(r.nextInt(999999 - 100000) + 100000);

            monthRequest.put("Client_ID", clientID);
            monthRequest.put("msg_type", monthRequestMessageType);
            monthRequest.put("year", selectedYear);
            monthRequest.put("month", months[monthViewed]);
            //Toast.makeText(getApplicationContext(), monthRequest.toString(10), Toast.LENGTH_LONG).show();

            Log.d("Month string : ", months[monthViewed]);
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

//                if(daysBooked.length() != 0) {
//                    Log.d("============",String.valueOf(daysBooked.length()));

//                    int[] daysToHighlight = new int[daysBooked.length()];
                    final HashSet<CalendarDay> datesHighlighted = new HashSet<>();
                    for (int i = 0; i < daysBooked.length(); i++) {
//                        daysToHighlight[i] = Integer.parseInt(daysBooked.getString(i));
                        Log.d("daysBooked", daysBooked.getString(i));
                        Log.d("daysBooked", months[monthViewed]);
                        datesHighlighted.add(CalendarDay.from(selectedYear, monthViewed, Integer.parseInt(daysBooked.getString(i))));
                    }
                    mActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            mDecorator = new EventDecorator(Color.RED, 3, datesHighlighted);
                            widget.addDecorator(mDecorator);
                        }
                    });
//                }

            }
        }catch (JSONException e){
            Log.d("---------------------","=============");
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
                        //Toast.makeText(MainActivity.this, "Month - " + monthResponseString, Toast.LENGTH_SHORT).show();
                        //monthResponseString = "{\"client_id\": 432234,\"msg_type\": \"RP_RD_MN\",\"days\": [\"16\",\"17\",\"30\"],\"result\": \"ok\",\"err_code\": 400}";

                        OnMonthSubscription();
                    }

                    if(response.optString("msg_type").equals(dayResponseMessageType)) {
                        dayResponseString = ans;
                        //Toast.makeText(MainActivity.this, "Day - " + ans, Toast.LENGTH_SHORT).show();
                        //dayResponseString = "{\"client_id\": 432234,\"msg_type\":\"RP_RD_DAY\",\"bookings\":{\"MCR\":[{\"Book_ID\":\"B1\",\"ST\":\"11.30\",\"ET\":\"12.0\",\"user\":\"vishnu\", \"status\":\"BUSY\"},{\"Book_ID\":\"B2\",\"ST\":\"12.30\",\"ET\":\"13.0\",\"user\":\"vishnu\",\"status\":\"BOOKED\" }],\"CameraConferenceRoom\": [{\"Book_ID\":\"B3\",\"ST\":\"12.30\",\"ET\":\"13.45\",\"user\":\"vishnu\",\"status\":\"BUSY\"}]},\"result\":\"ok\",\"err_code\":400 }";
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
        Log.d("cancel dialogue", "...oncanelling");
        try {
            final JSONObject cancelResponse = new JSONObject(cancelResponseString);
            if (cancelResponse.optString("result").equals("success")) {

//                Toast.makeText(getApplicationContext(), "Booking cancelled", Toast.LENGTH_SHORT).show();
                mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        widget.removeDecorators();
//                        daySubscribe();
//                        --groupExpanded;
//                        cancelButton.setVisibility(View.INVISIBLE);
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
//                        mAdapter.restoreItem(deletedItem, deletedIndex);
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

        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        recyclerView = findViewById(R.id.recycler_view);
//        cancelUser = findViewById(R.id.user);
//        cancelBookingID = findViewById(R.id.bookingID);
        eventList = new ArrayList<>();
        mAdapter = new EventListAdapter(this, eventList);

        vidSurface = (SurfaceView) findViewById(R.id.surfView);
        vidHolder = vidSurface.getHolder();
        vidHolder.addCallback(this);

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

        Log.e("Sc ht px", String.valueOf(height));
        Log.e("Sc wd px", String.valueOf(width));


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

        roomNames[0] = "MCR";
        roomNames[1] = "CCR";
//        roomNames[2] = "ProductConferenceRoom";
//        roomNames[3] = "MCRAdjacentRoom";
//        roomNames[4] = "ReceptionAreaRoom";
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
        Blurry.with(MainActivity.this)
                .radius(25)
                .sampling(1)
                .async()
                .animate(500)
                .onto((ViewGroup) findViewById(R.id.eventsList));
//        cancelButton = findViewById(R.id.cancelEvent);
//        cancelButton.setImageDrawable(android.R.drawable.ic_delete);
//        cancelButton.setImageResource(android.R.drawable.ic_menu_delete);

        builder = new AlertDialog.Builder(this);
        builder.setTitle("Cancel");

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
//                mActivity.runOnUiThread(new Runnable() {
//                    public void run() {
////                        mAdapter.restoreItem(deletedItem, deletedIndex);
//                    }
//                });
            }
        });

        cancelDialog = builder.create();
//
//        eventsList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
//            @Override
//            public boolean onGroupClick(ExpandableListView expandableListView, View view, int groupPosition, long id) {
//                cancelRoom = ((TextView) view.findViewById(R.id.parent_layout)).getText().toString();
////                Button cancelButton = view.findViewById(R.id.cancelEvent);
////                cancelButton.setVisibility(View.VISIBLE);
//                return false;
//            }
//        });
//
//        eventsList.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
//
//            @Override
//            public void onGroupExpand(int groupPosition) {
//                ++groupExpanded;
//                Log.d("groupExpand ------ ", "On Expand " + groupExpanded + " " + previousGroup);
//                if(groupPosition != previousGroup && previousGroup >= 0)
//                    eventsList.collapseGroup(previousGroup);
//                previousGroup = groupPosition;
//                cancelButton.setVisibility(View.VISIBLE);
//            }
//        });
//
//        eventsList.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
//            @Override
//            public void onGroupCollapse(int i) {
//                groupExpanded--;
//                Log.d("groupExpand --------- ", "On Collapse " + groupExpanded);
//                if(groupExpanded <= 0) {
//                    cancelButton.setVisibility(View.INVISIBLE);
//                    previousGroup = -1;
//                }
//            }
//        });
//
//        eventsList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
//            @Override
//            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
//
////                cancelRoom = ((TextView) view.findViewById(R.id.parent_layout)).getText().toString();
//
//                return false;
//            }
//        });


        DBReqHandler.IDBReqHandler reqhandler = new reqHandler();
        dbReqHandler = new DBReqHandler(getApplicationContext(),reqhandler);
//        monthSubscribe();
//        daySubscribe();

        widget.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {

                //Log.d("day change", ".....");
                selectedDay = date.getDay();
                selectedMonth = date.getMonth();
                selectedMonthString = months[selectedMonth];
                selectedYear = date.getYear();



                //Toast.makeText(getApplicationContext(), "Date Changed : " + selectedDay+"-"+selectedMonth+"-"+selectedYear, Toast.LENGTH_SHORT).show();
                daySubscribe();
            }

        });

        widget.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                monthViewed = date.getMonth() ;
                Log.d("Month Changed", monthViewed + " " + date.getMonth());

                //Toast.makeText(getApplicationContext(),"Month Changed : " + monthViewed, Toast.LENGTH_SHORT).show();

               monthSubscribe();
            }
        });

    }

    public void  cancelEvent(View v){

        cancelDialog.show();
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
    public int dp2px(float dips)
    {
        return (int) (dips * this.getResources().getDisplayMetrics().density + 0.5f);
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



            // showing snack bar with Undo option
//            Snackbar snackbar = Snackbar
//                    .make(coordinatorLayout, room + "Event removed from cart!", Snackbar.LENGTH_LONG);
//            snackbar.setAction("UNDO", new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//
//                    // undo is selected, restore the deleted item
//                    mAdapter.restoreItem(deletedItem, deletedIndex);
//                }
//            });
//            snackbar.setActionTextColor(Color.YELLOW);
//            snackbar.show();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        // TODO Auto-generated method stub
    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
    //setup
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDisplay(vidHolder);
            mediaPlayer.setDataSource(vidAddress);
            Log.d("Video", vidAddress);
            mediaPlayer.prepare();
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //start playback
        mediaPlayer.start();
    }
}

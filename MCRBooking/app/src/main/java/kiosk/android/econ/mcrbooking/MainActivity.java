package kiosk.android.econ.mcrbooking;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;


import android.graphics.drawable.ColorDrawable;
import android.icu.text.SimpleDateFormat;
import android.nfc.Tag;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.econ.kannan.DBReqHandler;

import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import android.widget.ImageButton;
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

import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

public class MainActivity extends AppCompatActivity {

    MaterialCalendarView widget;
    AlertDialog.Builder builder;
    AlertDialog cancelDialog;
    Activity mActivity;

    ExpandableListView eventsList;
    SimpleExpandableListAdapter eventsAdapter;

    EditText bookingIDWidget;
    EditText userWidget;
    TextView homeText;

    String bookingID;
    String user;
    String Person;
    int previousGroup;
    EventDecorator mDecorator;

    ImageButton cancelButton;

    String cancelResponseMessageType;
    String cancelResponseString;

    JSONObject cancelRequest;

    int height;
    int width;

    int groupExpanded=0;

    private String[][] bookingDetails;

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

    public void daySubscribe() {

        eventsList.setVisibility(View.INVISIBLE);
        cancelButton.setVisibility(View.INVISIBLE);

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

            dbReqHandler.dbRequest(dbReqHandler.MSG_ID_PARSE_DATE,dayRequest.toString());

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

                    bookingDetails = new String[bookings.length()][100];
                    List<Map<String, String>> groupData = new ArrayList<>();
                    List<List<Map<String, String>>> childData = new ArrayList<>();


                    for (int j = 0; j < bookings.length(); j++) {
                        roomsBooked = bookings.getJSONArray(roomsActuallyBooked[j]);
                        Log.d("No.of bookings : ", String.valueOf(roomsBooked.length()));

                        t = 0;
//                        int i = 0;
                        for (int i = 0; i < roomsBooked.length(); i++) {
                            JSONObject event = roomsBooked.getJSONObject(i);


                            bookingDetails[j][t] = event.optString("ST") + " - " + event.optString("ET");
                            Map<String, String> curGroupMap = new HashMap<>();
                            groupData.add(curGroupMap);
                            curGroupMap.put(NAME, roomsActuallyBooked[j]);
                            curGroupMap.put("time", bookingDetails[j][t]);
                            Log.d("Timing: ", bookingDetails[j][t] );
                            t++;

                            bookingDetails[j][t] = roomsActuallyBooked[j];
                            t++;

                            bookingDetails[j][t] = "Booking ID : " + event.optString("Book_ID");
                            List<Map<String, String>> children = new ArrayList<>();
//                            Map<String, String> idChildMap = new HashMap<>();
//                            children.add(idChildMap);
//                            idChildMap.put(NAME, bookingDetails[i][t]);
                            Log.d("Booking ID : ", bookingDetails[j][t]);
                            t++;


                            bookingDetails[j][t] = "Person: " + event.optString("user");
                            Map<String, String> personChildMap = new HashMap<>();
                            children.add(personChildMap);
                            personChildMap.put(NAME, bookingDetails[j][t]);
                            Log.d("Person: ", bookingDetails[j][t]);
                            t++;

                            bookingDetails[j][t] = "Status: " + event.optString("status");
                            Map<String, String> statusChildMap = new HashMap<>();
                            children.add(statusChildMap);
                            statusChildMap.put(NAME, bookingDetails[j][t]);

                            childData.add(children);
                            Log.d("Status: ", bookingDetails[j][t]);
                            t++;

                        }

                    }
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
                String groupFrom[] = {NAME,"time"};
                int groupTo[] = {R.id.parent_layout,R.id.timing};
                String childFrom[] = {NAME};
                int childTo[] = {R.id.child_layout};

                    Log.d("======= ", "====Setting adapter");

                // Set up the adapter
                    eventsAdapter = new SimpleExpandableListAdapter(this, groupData,
                            R.layout.list_group,
                            groupFrom, groupTo,
                            childData, R.layout.list_child,
                            childFrom, childTo);

                    mActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            Log.d("======= ", "====Setting list");
                            eventsList.setAdapter(eventsAdapter);
                            eventsList.setVisibility(View.VISIBLE);
                        }
                    });

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
            dbReqHandler.dbRequest(dbReqHandler.MSG_ID_PARSE_MONTH,monthRequest.toString());

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
                            mDecorator = new EventDecorator(Color.RED, 1, datesHighlighted);
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
        try {
            final JSONObject cancelResponse = new JSONObject(cancelResponseString);
            if (cancelResponse.optString("result").equals("success")) {

//                Toast.makeText(getApplicationContext(), "Booking cancelled", Toast.LENGTH_SHORT).show();
                mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        widget.removeDecorators();
                        monthSubscribe();
                        daySubscribe();
                        --groupExpanded;
                        cancelButton.setVisibility(View.INVISIBLE);
                        SweetAlertDialog pDialog = new SweetAlertDialog(mActivity, SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Booking Cancelled!")
                                .setContentText(cancelResponse.optString("err_msg"));
                        pDialog.getProgressHelper().setBarColor(Color.parseColor("#555555"));
                        pDialog.show();
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

        eventsList = findViewById(R.id.events);
        cancelButton = findViewById(R.id.cancelEvent);
//        cancelButton.setImageDrawable(android.R.drawable.ic_delete);
        cancelButton.setImageResource(android.R.drawable.ic_menu_delete);

        builder = new AlertDialog.Builder(this);
        builder.setTitle("Cancel");

        LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
        View dialogView = layoutInflater.inflate(R.layout.cancel_dialog, null);

        builder.setView(dialogView);

        bookingIDWidget = dialogView.findViewById(R.id.bookingID);
        userWidget = dialogView.findViewById(R.id.user);
        homeText = (TextView) findViewById(R.id.homeText);
        homeText.setText("" + Months[currentMonth] + " " + currentDay + ", " + currentYear);

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
                    if(selectedYear >= currentYear && selectedMonth >= currentMonth && selectedDay >= currentDay) {
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

                            dbReqHandler.dbRequest(dbReqHandler.MSG_ID_CANCEL, cancelRequest.toString());

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

        eventsList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int groupPosition, long id) {
                cancelRoom = ((TextView) view.findViewById(R.id.parent_layout)).getText().toString();
//                Button cancelButton = view.findViewById(R.id.cancelEvent);
//                cancelButton.setVisibility(View.VISIBLE);
                return false;
            }
        });

        eventsList.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
                ++groupExpanded;
                Log.d("groupExpand ------ ", "On Expand " + groupExpanded + " " + previousGroup);
                if(groupPosition != previousGroup && previousGroup >= 0)
                    eventsList.collapseGroup(previousGroup);
                previousGroup = groupPosition;
                cancelButton.setVisibility(View.VISIBLE);
            }
        });

        eventsList.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int i) {
                groupExpanded--;
                Log.d("groupExpand --------- ", "On Collapse " + groupExpanded);
                if(groupExpanded <= 0) {
                    cancelButton.setVisibility(View.INVISIBLE);
                    previousGroup = -1;
                }
            }
        });

        eventsList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {

//                cancelRoom = ((TextView) view.findViewById(R.id.parent_layout)).getText().toString();

                return false;
            }
        });


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

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                        0xCE)));
                // set item width
                openItem.setWidth(dp2px(90));
                // set item title
                openItem.setTitle("Open");
                // set item title fontsize
                openItem.setTitleSize(18);
                // set item title font color
                openItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(openItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(dp2px(90));
                // set a icon
                deleteItem.setIcon(android.R.drawable.ic_menu_delete);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };

    }

    public void  cancelEvent(View v){

        cancelDialog.show();
    }

    public void gotoformActivity(View v)    {
        Intent formActivity = new Intent(this,Main2Activity.class);

        Log.d("month issue", currentMonth + " " + currentDay + " " + currentYear + " " + selectedMonth + " " + selectedDay);
        if(selectedYear >= currentYear && selectedMonth >= currentMonth && selectedDay >= currentDay)
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

}

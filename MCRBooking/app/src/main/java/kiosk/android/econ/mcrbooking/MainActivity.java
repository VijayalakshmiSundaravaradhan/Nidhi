package kiosk.android.econ.mcrbooking;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;


import android.icu.text.SimpleDateFormat;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.econ.kannan.DBReqHandler;

import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import android.widget.SimpleExpandableListAdapter;
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

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

public class MainActivity extends AppCompatActivity {

    MaterialCalendarView widget;

    Activity mActivity;

    ExpandableListView eventsList;
    SimpleExpandableListAdapter eventsAdapter;

    int height;
    int width;

    private String[][] bookingDetails;

    private static final String NAME = "NAME";
    public static final String[] months = new String[]{"jan", "feb", "mar", "apr", "jun",
            "jul", "aug", "sep", "oct", "nov", "dec"};

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
    int[] daysToHighlight;

    int currentYear;
    int currentMonth;
    int currentDay;

    HashSet<CalendarDay> datesHighlighted;

    JSONObject dayRequest;
    String dayResponseString;
    String dayRequestMessageType;
    String dayResponseMessageType;
    JSONObject bookings;
    JSONArray roomsBooked;

    final int noOfRooms = 2;

    String[] roomNames = new String[noOfRooms];


    DBReqHandler dbReqHandler;



    public void daySubscribe() {

        dayRequest = new JSONObject();

        try {
            dayRequest.put("Client_ID","000000");
            dayRequest.put("msg_type", dayRequestMessageType);
            dayRequest.put("year", selectedYear);
            dayRequest.put("month", selectedMonthString);
            dayRequest.put("day", selectedDay);

            //Toast.makeText(getApplicationContext(), dayRequest.toString(10), Toast.LENGTH_SHORT).show();

            dbReqHandler.dbRequest(dbReqHandler.MSG_ID_PARSE_DATE,dayRequest.toString(10));

        }catch (JSONException e){
            e.printStackTrace();
        }


        }

        public void OnDaySubscription()
        {

            int[] bookingsInEachRoom;
            String[] roomsActuallyBooked;

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
                        for (int i = 0; i < roomsBooked.length(); i++) {
                            JSONObject event = roomsBooked.getJSONObject(i);


                            bookingDetails[j][t] = event.optString("ST") + " - " + event.optString("ET");
                            Map<String, String> curGroupMap = new HashMap<>();
                            groupData.add(curGroupMap);
                            curGroupMap.put(NAME, roomsActuallyBooked[j]);
                            curGroupMap.put("time", bookingDetails[j][t]);
                            Log.d("Timing: ", bookingDetails[j][t] );
                            t++;


                            bookingDetails[j][t] = "Booking ID : " + event.optString("Book_ID");
                            List<Map<String, String>> children = new ArrayList<>();
                            Map<String, String> idChildMap = new HashMap<>();
                            children.add(idChildMap);
                            idChildMap.put(NAME, bookingDetails[j][t]);
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
        monthRequest = new JSONObject();

        try {
            monthRequest.put("Client_ID", "000000");
            monthRequest.put("msg_type", monthRequestMessageType);
            monthRequest.put("year", selectedYear);
            monthRequest.put("month", months[monthViewed-1]);
            //Toast.makeText(getApplicationContext(), monthRequest.toString(10), Toast.LENGTH_LONG).show();

            dbReqHandler.dbRequest(dbReqHandler.MSG_ID_PARSE_MONTH,monthRequest.toString(10));

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

                daysToHighlight = new int[daysBooked.length()];

                for (int i = 0; i < daysBooked.length(); i++) {
                    daysToHighlight[i] = Integer.parseInt(daysBooked.getString(i).toString());
                    Log.d("daysBooked", daysBooked.getString(i));
                    Log.d("daysBooked", String.valueOf(monthViewed));
                    datesHighlighted.add(CalendarDay.from(selectedYear, monthViewed -1, daysToHighlight[i]));
                }
                        mActivity.runOnUiThread(new Runnable() {
                            public void run(){
                                widget.addDecorator(new EventDecorator(Color.RED, datesHighlighted));
                            }
                        });

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
        widget.setTileWidth((width*3)/28);
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

                }catch (JSONException e){
                    e.printStackTrace();
                }


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
        datesHighlighted = new HashSet<>();

        widget.setAllowClickDaysOutsideCurrentMonth(true);
        widget.setTileWidth((width*3)/28);


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

        selectedMonth = currentMonth + 1;
        monthViewed = currentMonth + 1;
        selectedYear = currentYear;
        selectedDay = currentDay;
        selectedMonthString = months[selectedMonth-1];


        monthRequestMessageType = new String("RQ_RD_MN");
        monthResponseMessageType = new String("RP_RD_MN");

        dayRequestMessageType = new String("RQ_RD_DAY");
        dayResponseMessageType = new String("RP_RD_DAY");


        eventsList = findViewById(R.id.events);

        eventsList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int groupPosition, long id) {
                //Toast.makeText(getApplicationContext(), "Room Name Is :" + roomNames[groupPosition], Toast.LENGTH_SHORT).show();
                return false;       }
        });



        DBReqHandler.IDBReqHandler reqhandler = new reqHandler();
        dbReqHandler = new DBReqHandler(getApplicationContext(),reqhandler);
        monthSubscribe();
        daySubscribe();

        widget.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {


                selectedDay = date.getDay();
                selectedMonth = date.getMonth() + 1;
                selectedMonthString = months[selectedMonth-1];
                selectedYear = date.getYear();

                eventsList.setVisibility(View.INVISIBLE);

                Toast.makeText(getApplicationContext(), "Date Changed : " + selectedDay+"-"+selectedMonth+"-"+selectedYear, Toast.LENGTH_SHORT).show();
                daySubscribe();
            }

        });

        widget.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                monthViewed = date.getMonth() + 1 ;

                Toast.makeText(getApplicationContext(),"Month Changed : " + monthViewed, Toast.LENGTH_SHORT).show();

               monthSubscribe();
            }
        });

    }

    public void gotoformActivity(View v)    {
        Intent formActivity = new Intent(this,Main2Activity.class);

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

}

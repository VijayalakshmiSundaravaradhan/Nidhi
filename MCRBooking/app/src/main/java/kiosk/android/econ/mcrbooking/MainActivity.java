package kiosk.android.econ.mcrbooking;

import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import android.widget.CalendarView;
import android.widget.ExpandableListView;

import android.widget.SimpleExpandableListAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
//import com.econ.kannan.DBReqHandler;

public class MainActivity extends AppCompatActivity {

    MaterialCalendarView widget;

    ExpandableListView eventsList;
    SimpleExpandableListAdapter eventsAdapter;

    int height;
    int width;

    //private String groupItems[] = {"Animals", "Birds"};
    //private String[][] childItems = {{"Dog", "Cat", "Tiger"}, {"Crow", "Sparrow"}};

    private String[][] bookingDetails;

    private static final String NAME = "NAME";

    int selectedYear;
    int selectedMonth;
    int selectedDay;

    int monthViewed;
    //CalendarView calendarMain;

//    SimpleDateFormat sdf;

    JSONObject monthRequest;
    JSONObject monthResponse;
    String monthResponseString;
    String monthRequestMessageType;
    String monthResponseMessageType;
    JSONArray daysBooked;
    int[] daysToHighlight;

    int currentYear;
    int currentMonth;
    int currentDay;

    HashSet<CalendarDay> datesHighlighted;
//    String selectedDate;

    JSONObject dayRequest;
    JSONObject dayResponse;
    String dayResponseString;
    String dayRequestMessageType;
    String dayResponseMessageType;
    JSONObject bookings;
    JSONArray roomsBooked;
    String[] roomNames = new String[6];

    //subscriptions mSubscription;


    //DBReqHandler dbReqHandler;

    int t;

    public void daySubscribe() {

        dayRequest = new JSONObject();

        try {
            dayRequest.put("Client_ID","000000");
            dayRequest.put("msg_type", dayResponseMessageType);
            dayRequest.put("year", selectedYear);
            dayRequest.put("month", selectedMonth);
            dayRequest.put("day", selectedDay);
        }catch (JSONException e){
            e.printStackTrace();
        }

        //dbReqHandler2.dbRequest(dbReqHandler2.MSG_ID_PARSE_DATE,dayRequest.toString());


        }

        public void OnDaySubscription()
        {

            int[] bookingsInEachRoom;
            String[] roomsActuallyBooked;

            bookingsInEachRoom = new int[6];

            try {
            dayResponse = new JSONObject(dayResponseString);

            if(dayResponse.optString("msg_type").equals(dayResponseMessageType) && dayResponse.optString("result").equals("ok")) {

                bookings = dayResponse.getJSONObject("bookings");
                Log.d("No.of bookings : ", String.valueOf(bookings.length()));


//                for (int a = 0; a < bookings.length(); a++)
//                    for (int b = 0; b < 10; b++)
//                        bookingDetails[a][b] = "0";


                t = 0;
                bookingsInEachRoom = new int[bookings.length()];
                roomsActuallyBooked = new String[bookings.length()];

                    for (int k = 0; k < roomNames.length; k++) {
                        if(bookings.has(roomNames[k])) {
                            roomsBooked = bookings.getJSONArray(roomNames[k]);
                            Log.d("Actual rooms booked", roomNames[k]);

                            bookingsInEachRoom[t] = roomsBooked.length() * 5;
                            roomsActuallyBooked[t] = roomNames[k];
                            t++;
                        }
                    }

                bookingDetails = new String[bookings.length()][100];

                for (int j = 0; j < bookings.length(); j++) {
                    roomsBooked = bookings.getJSONArray(roomsActuallyBooked[j]);
                    Log.d("No.of bookings : ", String.valueOf(roomsBooked.length()));

                    t = 0;
                    for (int i = 0; i < roomsBooked.length(); i++) {
                        JSONObject event = roomsBooked.getJSONObject(i);
                        bookingDetails[j][t] = "Booking ID : " + event.optString("Book_ID");
                        t++;
                        Log.d("Booking ID : ", event.optString("Book_ID"));

                        bookingDetails[j][t] = "Starting Time: " + event.optString("ST");
                        t++;
                        Log.d("Starting Time: ", event.optString("ST"));

                        bookingDetails[j][t] = "Ending Time: " + event.optString("ET");
                        t++;
                        Log.d("Ending Time: ", event.optString("ET"));

                        bookingDetails[j][t] = "Person: " + event.optString("user");
                        t++;
                        Log.d("Person: ", event.optString("user"));

                        bookingDetails[j][t] = "Status: " + event.optString("status");
                        t++;
                        Log.d("Status: ", event.optString("status"));

                    }

                }
            } } catch (JSONException e)
            {

            }

        List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();
        List<List<Map<String, String>>> childData = new ArrayList<List<Map<String, String>>>();
        // add data in group and child list
        for (int i = 0; i < bookings.length(); i++) {
            Map<String, String> curGroupMap = new HashMap<String, String>();
            groupData.add(curGroupMap);
            curGroupMap.put(NAME, roomNames[i]);

            List<Map<String, String>> children = new ArrayList<Map<String, String>>();
            for (int j = 0; j < bookingsInEachRoom[i] ; j++) {
                Map<String, String> curChildMap = new HashMap<String, String>();
                children.add(curChildMap);
                curChildMap.put(NAME, bookingDetails[i][j]);
            }
            childData.add(children);
        }
        // define arrays for displaying data in Expandable list view
        String groupFrom[] = {NAME};
        int groupTo[] = {R.id.parent_layout};
        String childFrom[] = {NAME};
        int childTo[] = {R.id.child_layout};


        // Set up the adapter
        eventsAdapter = new SimpleExpandableListAdapter(this, groupData,
                R.layout.list_group,
                groupFrom, groupTo,
                childData, R.layout.list_child,
                childFrom, childTo);
        eventsList.setAdapter(eventsAdapter);

        }


    public void monthSubscribe() {
        monthRequest = new JSONObject();

        try {
            monthRequest.put("msg_type", monthRequestMessageType);
            monthRequest.put("year", selectedYear);
            monthRequest.put("month", monthViewed);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Message msg = new Message();
        //msg.obj = monthRequest;
        //DBReqHandler.sendMessage(msg);

    }

    public void OnMonthSubscription(){
        try {
            monthResponse = new JSONObject(monthResponseString);
        }catch (JSONException e){
            e.printStackTrace();
        }

        if(monthResponse.optString("msg_type").equals(monthResponseMessageType) && monthResponse.optString("result").equals("ok")) {
            daysBooked = monthResponse.optJSONArray("days");

            daysToHighlight = new int[daysBooked.length()];

            for (int i = 0; i < daysBooked.length(); i++) {
                try {
                    daysToHighlight[i] = Integer.parseInt(daysBooked.getString(i).toString());
                    Log.d("daysBooked", daysBooked.getString(i));
                    datesHighlighted.add(CalendarDay.from(selectedYear, monthViewed, daysToHighlight[i]));
                    widget.addDecorator(new EventDecorator(Color.RED, datesHighlighted));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            // Highlight the dates
        }
    }

    @Override
    protected void onResume()
    {
        Log.e("on resume","Back tomainactivity");
        super.onResume();
        widget.setTileWidth((width*9)/60);
        monthSubscribe();
        daySubscribe();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;

        Log.e("Sc ht px", String.valueOf(height));
        Log.e("Sc wd px", String.valueOf(width));


        widget = findViewById(R.id.calendarView);
        datesHighlighted = new HashSet<>();
//        datesHighlighted.add(CalendarDay.from(2018, 1, 4));
//        datesHighlighted.add(CalendarDay.from(2018, 1, 10));
        widget.setAllowClickDaysOutsideCurrentMonth(true);
        widget.setTileWidth((width*9)/60);


        Calendar c = Calendar.getInstance();
        widget.setCurrentDate(c);
        widget.setSelectedDate(c);
        currentYear = c.get(Calendar.YEAR);
        currentMonth = c.get(Calendar.MONTH);
        currentDay = c.get(Calendar.DAY_OF_MONTH);

        roomNames[0] = "MCR";
        roomNames[1] = "CameraConferenceRoom";
        roomNames[2] = "ProductConferenceRoom";
        roomNames[3] = "ReceptionAreaRoom1";
        roomNames[4] = "ReceptionAreaRoom2";
        roomNames[5] = "ReceptionAreaRoom3";

        selectedMonth = currentMonth;
        monthViewed = currentMonth;
        selectedYear = currentYear;
        selectedDay = currentDay;

        monthRequestMessageType = new String("RQ_RD_MN");
        monthResponseMessageType = new String("RP_RD_MN");

        dayRequestMessageType = new String("RQ_RD_DAY");
        dayResponseMessageType = new String("RP_RD_DAY");

//        currentMonthString = DateFormat.getDateInstance().format(new Date());

//        calendarMain = findViewById(R.id.calendarMain);

//        calendarMain.getDate();
//        sdf  = new SimpleDateFormat("dd/MM/yyyy");
//        selectedDate = sdf.format(new Date(calendarMain.getDate()));

        eventsList = findViewById(R.id.events);

        eventsList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int groupPosition, long id) {
                Toast.makeText(getApplicationContext(), "Room Name Is :" + roomNames[groupPosition], Toast.LENGTH_SHORT).show();
                return false;       }
        });

        //mSubscription = new subscriptions("HANDLER");
        //mSubscription.start();

        /*
        dbReqHandler = new DBReqHandler(getApplicationContext(), new DBReqHandler.IDBReqHandler() {
            @Override
            public void testCallback(String ans)
            {
                JSONObject response;
                try {
                    response = new JSONObject(ans);
                }catch (JSONException e){
                    e.printStackTrace();
                }

                if(response.optString("msg_type").equals(monthResponseMessageType)) {
                    monthResponseString = ans;
                    monthResponseString = "{\"client_id\": 432234,\"msg_type\": \"RP_RD_MN\",\"days\": [\"16\",\"17\",\"30\"],\"result\": \"ok\",\"err_code\": 400}";
                    OnMonthSubscription();
                }

                if(response.optString("msg_type").equals(dayResponseMessageType)) {
                    dayResponseString = ans;
                    dayResponseString = "{\"client_id\": 432234,\"msg_type\":\"RP_RD_DAY\",\"bookings\":{\"MCR\":[{\"Book_ID\":\"B1\",\"ST\":\"11.30\",\"ET\":\"12.0\",\"user\":\"vishnu\", \"status\":\"BUSY\"},{\"Book_ID\":\"B2\",\"ST\":\"12.30\",\"ET\":\"13.0\",\"user\":\"vishnu\",\"status\":\"BOOKED\" }],\"CameraConferenceRoom\": [{\"Book_ID\":\"B3\",\"ST\":\"12.30\",\"ET\":\"13.45\",\"user\":\"vishnu\",\"status\":\"BUSY\"}]},\"result\":\"ok\",\"err_code\":400 }";
                    OnDaySubscription();
                }

                Toast.makeText(MainActivity.this, "SM IS" + ans, Toast.LENGTH_SHORT).show();
            }
        });
        */

        daySubscribe();
        monthSubscribe();

//        calendarMain.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
//            @Override
//            public void onSelectedDayChange(CalendarView view, int year, int month, int dayofMonth) {
////            if(selectedDay != dayofMonth)
//                daySubscribe();
//
//             selectedDay = dayofMonth;
//             selectedMonth = month + 1;
//             selectedYear = year;
//
//             monthSubscribe();
//             Toast.makeText(getApplicationContext(),selectedDay+"-"+selectedMonth+"-"+selectedYear, Toast.LENGTH_SHORT).show();             Toast.makeText(getApplicationContext(),selectedDay+"-"+selectedMonth+"-"+selectedYear, Toast.LENGTH_LONG).show();
//            }
//        });

        widget.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                daySubscribe();

                selectedDay = date.getDay();
                selectedMonth = date.getMonth() + 1;
                selectedYear = date.getYear();

                Toast.makeText(getApplicationContext(), "Date Changed : " + selectedDay+"-"+selectedMonth+"-"+selectedYear, Toast.LENGTH_SHORT).show();

            }

        });

        widget.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                monthViewed = date.getMonth() + 1 ;
                monthSubscribe();
                Toast.makeText(getApplicationContext(),"Month Changed : " + monthViewed, Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void gotoformActivity(View v)    {
        Intent formActivity = new Intent(this,Main2Activity.class);

        if(selectedYear < currentYear || selectedMonth < currentMonth || selectedDay < currentDay)
        {
            Toast.makeText(getApplicationContext(),"Please select a valid date ", Toast.LENGTH_SHORT).show();
        }
        else
        {
            formActivity.putExtra("selectedYear", selectedYear);
            formActivity.putExtra("selectedMonth", selectedMonth);
            formActivity.putExtra("selectedDay", selectedDay);
            //formActivity.putExtra("dbReqHandler", dbReqHandler);
            startActivity(formActivity);
        }
    }

}

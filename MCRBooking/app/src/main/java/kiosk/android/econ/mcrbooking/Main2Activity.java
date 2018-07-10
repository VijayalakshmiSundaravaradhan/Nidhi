package kiosk.android.econ.mcrbooking;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.econ.kannan.DBReqHandler;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class Main2Activity extends AppCompatActivity {

    TextView dateText;
    TextView selectedDateText;
    Activity m2Activity;
    NumberPicker s_hours;
    NumberPicker s_minutes;
    NumberPicker e_hours;
    NumberPicker e_minutes;
    EditText nameInput;
    Spinner room;

    int selectedYear;
    int selectedMonth;
    int selectedDay;
    final int MAX_MINUTES = 60;
    final int MIN_MINUTES = 0;
    final int MAX_HOURS = 20;
    final int MIN_HOURS = 0;


    String selectedRoom;
    String Person;
    String bookingID;
    String user;
    String currentDateString;
    String selectedMonthString;

    String bookResponseMessageType;
    String bookRoomResponseString;

    JSONObject bookRequest;

    String selectedSHour;
    String selectedSMinute;
    String selectedEHour;
    String selectedEMinute;
    String[] startTimes = new String[12];

    DBReqHandler dbReqHandler;

    public class reqHandler implements DBReqHandler.IDBReqHandler {

        @Override
        public void testCallback(String ans)
        {
            JSONObject response;
            try {
                response = new JSONObject(ans);


                if(response.optString("msg_type").equals(bookResponseMessageType)) {
                    bookRoomResponseString = ans;
                    OnBookingRoom();
                }

            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        m2Activity = this;
        currentDateString = DateFormat.getDateInstance().format(new Date());
        dateText = findViewById(R.id.dateText);
        selectedDateText = findViewById(R.id.selectedDateText);
        bookResponseMessageType = "RP_BK_CNF";

        s_hours = findViewById(R.id.shours);
        s_minutes= findViewById(R.id.sminutes);
        e_hours = findViewById(R.id.hours);
        e_minutes = findViewById(R.id.minutes);

        nameInput = findViewById(R.id.nameInput);

        selectedYear = getIntent().getIntExtra("selectedYear",0);
        selectedMonth = getIntent().getIntExtra("selectedMonth",0);
        selectedMonthString = getIntent().getStringExtra("selectedMonthString");
        selectedDay = getIntent().getIntExtra("selectedDay",0);

        DBReqHandler.IDBReqHandler requestHandler = new reqHandler();

        dbReqHandler = new DBReqHandler(getApplicationContext(),requestHandler);

        String dateString = "Today's date : " + currentDateString;
        dateText.setText(dateString);
        selectedMonth++;
        dateString = "Selected Date : "+selectedDay+"-"+selectedMonth+"-"+selectedYear;
        selectedDateText.setText(dateString);

        room = findViewById(R.id.spinner);
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.confRooms, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        room.setAdapter(adapter);

        room.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView spinner, View view, int position, long l) {
                selectedRoom = (spinner.getItemAtPosition(position)).toString();
            }

            public void onNothingSelected(AdapterView spinner){}
        });


        int s_timeLimit = 9;

        for (int i = 0; i < 12; i++)
            startTimes[i] = String.valueOf(s_timeLimit + i);

        s_hours.setMinValue(0);
        s_hours.setMaxValue(startTimes.length-1);
        s_hours.setDisplayedValues(startTimes);

        s_hours.setWrapSelectorWheel(true);
        e_hours.setWrapSelectorWheel(true);
        s_minutes.setWrapSelectorWheel(true);
        e_minutes.setWrapSelectorWheel(true);

        s_minutes.setMinValue(0);
        s_minutes.setMaxValue(59);

        selectedSHour = startTimes[s_hours.getValue()];
        selectedSMinute = String.valueOf(s_minutes.getValue());
        //setEndTime();

        updateHours();
        updateMinutes();


        s_hours.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
                selectedSHour = startTimes[s_hours.getValue()];
                updateHours();
                //updateMinutes();
            }
        });


        s_minutes.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
                selectedSMinute = String.valueOf(newValue);
                updateHours();
                //updateMinutes();
            }
        });

        e_hours.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {

                updateMinutes();
            }
        });

        e_minutes.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
                selectedEMinute = String.valueOf(newValue);
                setEndTime();
            }
        });

   }

    @Override
    public void onDestroy() {
        dbReqHandler = null;
        super.onDestroy();
    }

    public void bookRoom(View v)
    {
        Calendar c = Calendar.getInstance();
        int currentYear = c.get(Calendar.YEAR);
        int currentMonth = c.get(Calendar.MONTH);
        int currentDay = c.get(Calendar.DAY_OF_MONTH);
        int currentHour = c.get(Calendar.HOUR_OF_DAY);
        int currentMinute = c.get(Calendar.MINUTE);
        Person = nameInput.getText().toString().toUpperCase();
        if(Person.isEmpty())
            Toast.makeText(getApplicationContext(),"Please enter your good name !" , Toast.LENGTH_SHORT).show();
        else if(selectedSHour.equals(selectedEHour) && selectedSMinute.equals(selectedEMinute))
            Toast.makeText(getApplicationContext(),"Please enter valid duration !" , Toast.LENGTH_SHORT).show();
        else if((selectedDay == currentDay && (selectedMonth-1) == currentMonth && selectedYear == currentYear)  && (currentHour > Integer.parseInt(selectedSHour) || (currentHour == Integer.parseInt(selectedSHour) && currentMinute > Integer.parseInt(selectedSMinute))))
            Toast.makeText(getApplicationContext(),"Please select valid time !" , Toast.LENGTH_SHORT).show();
        else
        {
            bookRequest = new JSONObject();
            Random r = new Random();
            String clientID = String.valueOf(r.nextInt(999999 - 100000) + 100000);

            try {
                bookRequest.put("msg_type", "RQ_BK");
                bookRequest.put("Client_ID", clientID);
                bookRequest.put("year", selectedYear);
                bookRequest.put("month", selectedMonthString);
                bookRequest.put("day", selectedDay);
                bookRequest.put("room", selectedRoom);
                bookRequest.put("ST", selectedSHour+"."+selectedSMinute);
                bookRequest.put("ET", selectedEHour+"."+selectedEMinute);
                bookRequest.put("user", Person);
                dbReqHandler.dbRequest(DBReqHandler.MSG_ID_ADD,bookRequest.toString(10));

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private void setEndTime() {
        int totalMinutes = (s_minutes.getValue() + e_minutes.getValue());
        int hrs = totalMinutes / MAX_MINUTES;
        int minutes = totalMinutes - (hrs * MAX_MINUTES);
        selectedEHour = String.valueOf(e_hours.getValue() + Integer.parseInt(startTimes[s_hours.getValue()]) + hrs);
        selectedEMinute = String.valueOf(minutes);
    }

    public void OnBookingRoom()
    {
        try {
            final JSONObject bookingResponse = new JSONObject(bookRoomResponseString);
            if(bookingResponse.optString("msg_type").equals(bookResponseMessageType) && bookingResponse.optString("result").equals("SUCCESS")) {

                m2Activity.runOnUiThread(new Runnable() {
                    public void run() {
                                new SweetAlertDialog(m2Activity, SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Booking Successful!")
                                .setContentText("Book Id: " + bookingResponse.optString("Book_Id").substring(0,6))
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        Main2Activity.super.onBackPressed();
                                    }
                                }).show();
                    }
                });
            }
            else {
                        new SweetAlertDialog(m2Activity, SweetAlertDialog.WARNING_TYPE)
                                .setTitleText("Booking Failed!")
                                .setContentText(bookingResponse.optString("err_msg"))
                                .show();
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    public void updateHours()
    {
        e_hours.setMinValue(MIN_HOURS);
        if(s_minutes.getValue() != MIN_MINUTES)
            e_hours.setMaxValue(MAX_HOURS - Integer.parseInt(startTimes[s_hours.getValue()]));
        else
            e_hours.setMaxValue(MAX_HOURS + 1 - Integer.parseInt(startTimes[s_hours.getValue()]));
        updateMinutes();
    }

    public void updateMinutes()
    {

        if((Integer.parseInt(startTimes[s_hours.getValue()]) + e_hours.getValue()) < MAX_HOURS)
        {
            e_minutes.setMinValue(MIN_MINUTES);
            e_minutes.setMaxValue(MAX_MINUTES - 1);
        }
        else if((Integer.parseInt(startTimes[s_hours.getValue()]) + e_hours.getValue()) == MAX_HOURS)
        {
            e_minutes.setMinValue(MIN_MINUTES);
            if(s_minutes.getValue() == MIN_MINUTES)
                e_minutes.setMaxValue(MAX_MINUTES - 1 - s_minutes.getValue());
            else
                e_minutes.setMaxValue(MAX_MINUTES - s_minutes.getValue());
        }
        else
        {

            e_minutes.setMinValue(0);
            e_minutes.setMaxValue(0);
        }

        setEndTime();
    }

    public void goBack(View v)
     {
        super.onBackPressed();
     }
}

package kiosk.android.econ.mcrbooking;

import android.content.DialogInterface;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;

public class Main2Activity extends AppCompatActivity {

    String currentDateString;
    TextView dateText;
    TextView selectedDateText;

    NumberPicker s_hours;
    NumberPicker s_minutes;
    NumberPicker e_hours;
    NumberPicker e_minutes;

    EditText nameInput;
    EditText bookingIDWidget;
    EditText userWidget;
    Spinner room;

    String[] startTimes = new String[12];

    int selectedYear;
    int selectedMonth;
    int selectedDay;

    String selectedMonthString;

    String selectedRoom;
    String Person;
    String bookingID;
    String user;

    String bookResponseMessageType;
    String cancelResponseMessageType;

    String bookRoomResponseString;
    String cancelResponseString;

    JSONObject bookRequest;
    JSONObject cancelRequest;

    String selectedSHour;
    String selectedSMinute;

    String selectedEHour;
    String selectedEMinute;

    AlertDialog.Builder builder;
    AlertDialog cancelDialog;
    DBReqHandler dbReqHandler;


    public class reqHandler implements DBReqHandler.IDBReqHandler {

        @Override
        public void testCallback(String ans)
        {
            Log.d("On callback", "-----");
            JSONObject response;
            try {
                response = new JSONObject(ans);


                if(response.optString("msg_type").equals(bookResponseMessageType)) {
                    bookRoomResponseString = ans;
                    //bookRoomResponseString = "{\"client_id\": 000000,\"msg_type\": \"RP_BK_CNF\",\"Book_id\": \"B1\",\"result\": \"ok\",\"err_code\": 400}";
                    OnBookingRoom();
                }

                if(response.optString("msg_type").equals(cancelResponseMessageType)) {
                    cancelResponseString = ans;
                    //cancelResponseString = "{\"client_id\": 000000,\"msg_type\": \"RP_BK_CNF\",\"Book_id\": \"B1\",\"result\": \"ok\",\"err_code\": 400}";
                    OnCancelling();
                }

            }catch (JSONException e){
                e.printStackTrace();
            }

            Toast.makeText(getApplicationContext(), "SM IS" + ans, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        currentDateString = DateFormat.getDateInstance().format(new Date());
        dateText = findViewById(R.id.dateText);
        selectedDateText = findViewById(R.id.selectedDateText);
        bookResponseMessageType = "RP_BK_CNF";
        cancelResponseMessageType = "RP_CL";

        s_hours = findViewById(R.id.shours);
        s_minutes= findViewById(R.id.sminutes);
        e_hours = findViewById(R.id.hours);
        e_minutes = findViewById(R.id.minutes);

        nameInput = findViewById(R.id.nameInput);

        selectedYear = getIntent().getIntExtra("selectedYear",0);
        selectedMonth = getIntent().getIntExtra("selectedMonth",0);
        selectedMonthString = getIntent().getStringExtra("selectedMonthString");
        selectedDay = getIntent().getIntExtra("selectedDay",0);

        DBReqHandler.IDBReqHandler reqhandler = new reqHandler();

        dbReqHandler = new DBReqHandler(getApplicationContext(),reqhandler);

        builder = new AlertDialog.Builder(this);
        builder.setTitle("Cancel");

        LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
        View dialogView = layoutInflater.inflate(R.layout.cancel_dialog, null);

        builder.setView(dialogView);


        bookingIDWidget = dialogView.findViewById(R.id.bookingID);
        userWidget = dialogView.findViewById(R.id.user);

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
                    Toast.makeText(getApplicationContext(),"{\"request\":\"RQ_CL\",\"Book_id\":\""+bookingID+"\",\"user\":\""+user+"\"}",Toast.LENGTH_SHORT).show();

                    cancelRequest = new JSONObject();

                    try {
                        cancelRequest.put("Client_ID","000000");
                        cancelRequest.put("msg_type", "RQ_CL");
                        cancelRequest.put("Book_id", bookingID);
                        cancelRequest.put("user", Person);

                        dbReqHandler.dbRequest(dbReqHandler.MSG_ID_ADD,bookRequest.toString(10));

                    } catch (JSONException e) {
                        e.printStackTrace();
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

        dateText.setText("Current Date : "+currentDateString);
        selectedDateText.setText("Selected Date : "+selectedDay+"-"+selectedMonth+"-"+selectedYear);

        room = findViewById(R.id.spinner);
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.confRooms, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
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
        setEndTime();


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
        Person = nameInput.getText().toString().toUpperCase();
        if(Person.isEmpty())
            Toast.makeText(getApplicationContext(),"Please enter your good name" , Toast.LENGTH_SHORT).show();
        else
        {
            //Toast.makeText(getApplicationContext(),"{\"msg_type\":\"RQ_BK_CNF\",\"year\":\""+selectedYear+"\",\"month\":\"" + selectedMonth+"\",\"day\":\""+selectedDay+"\",\"person\":\""+Person+"\",\"room\":\""+selectedRoom+"\"}",Toast.LENGTH_SHORT).show();

            bookRequest = new JSONObject();

            try {
                bookRequest.put("msg_type", "RQ_BK");
                bookRequest.put("Client_ID", "000000");
                bookRequest.put("year", selectedYear);
                bookRequest.put("month", selectedMonthString);
                bookRequest.put("day", selectedDay);
                bookRequest.put("room", selectedRoom);
                bookRequest.put("ST", selectedSHour+"."+selectedSMinute);
                bookRequest.put("ET", selectedEHour+"."+selectedEMinute);
                bookRequest.put("user", Person);
                dbReqHandler.dbRequest(dbReqHandler.MSG_ID_ADD,bookRequest.toString(10));
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }

    private void setEndTime() {
        int totalMinutes = (s_minutes.getValue() + e_minutes.getValue());
        int hrs = totalMinutes % 60;
        int minutes = totalMinutes - (hrs * 60);
        selectedEHour = String.valueOf(e_hours.getValue() + Integer.parseInt(startTimes[s_hours.getValue()]) + hrs);
        selectedEMinute = String.valueOf(minutes);
    }

    public void OnBookingRoom()
    {
        try {
            JSONObject bookingResponse = new JSONObject(bookRoomResponseString);

            Log.d("============",bookRoomResponseString);
            if(bookingResponse.optString("msg_type").equals(bookResponseMessageType) && bookingResponse.optString("result").equals("SUCCESS")) {
                Toast.makeText(getApplicationContext(), "Booking Successful" , Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(getApplicationContext(), "Booking Failed" , Toast.LENGTH_SHORT).show();
            }
        }catch (JSONException e){
            Log.d("---------------------","=============");
            e.printStackTrace();
        }

    }

    public void cancel(View v)
    {
        Toast.makeText(getApplicationContext(),"You have no rights to cancel any events. Sorry!",Toast.LENGTH_SHORT).show();

        //cancelDialog.show();

    }

    public void OnCancelling()
    {
        Toast.makeText(getApplicationContext(),"Booking cancelled",Toast.LENGTH_SHORT).show();
    }

    public void updateHours()
    {
        e_hours.setMinValue(0);
        if(s_minutes.getValue() != 0)
            e_hours.setMaxValue(20 - Integer.parseInt(startTimes[s_hours.getValue()]));
        else
            e_hours.setMaxValue(21 - Integer.parseInt(startTimes[s_hours.getValue()]));
    }

    public void updateMinutes()
    {

        if((Integer.parseInt(startTimes[s_hours.getValue()]) + e_hours.getValue()) < 20)
        {
            e_minutes.setMinValue(0);
            e_minutes.setMaxValue(59);
        }
        else if((Integer.parseInt(startTimes[s_hours.getValue()]) + e_hours.getValue()) == 20)
        {
            e_minutes.setMinValue(0);
            if(s_minutes.getValue() == 0)
                e_minutes.setMaxValue(59 - s_minutes.getValue());
            else
                e_minutes.setMaxValue(60 - s_minutes.getValue());
        }
        else
        {

            e_minutes.setMinValue(0);
            e_minutes.setMaxValue(0);
        }

        setEndTime();
    }

}

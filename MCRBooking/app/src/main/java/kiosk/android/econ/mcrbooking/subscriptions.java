package kiosk.android.econ.mcrbooking;

import android.os.HandlerThread;
import android.os.Message;

import android.os.Handler ;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nidhi on 19/2/18.
 */

public class subscriptions extends HandlerThread {
    Handler msghandler;

    public subscriptions(String name) {
        super(name);
    }

    @Override
    protected void onLooperPrepared() {

        msghandler = new Handler(getLooper()) {
            MainActivity main_activity = new MainActivity();
            Main2Activity main_2_activity = new Main2Activity();

            JSONObject response;
            public void handleMessage(Message msg) {
                try {
                    response = new JSONObject(msg.toString());
                }catch (JSONException e){
                    e.printStackTrace();
                }

                if(response.optString("msg_type").equals(main_activity.monthResponseMessageType))
                    main_activity.OnMonthSubscription();

                if(response.optString("msg_type").equals(main_activity.dayResponseMessageType))
                    main_activity.OnDaySubscription();

                if(response.optString("msg_type").equals(main_2_activity.bookResponseMessageType))
                    main_2_activity.OnBookingRoom();

                if(response.optString("msg_type").equals(main_2_activity.cancelResponseMessageType))
                    main_2_activity.OnCancelling();
                // process incoming messages here
                // this will run in non-ui/background thread
            }
        };
    }
}

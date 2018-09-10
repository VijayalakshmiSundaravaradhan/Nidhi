package kiosk.android.econ.mcrbooking;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.econ.kannan.DBReqHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import io.moquette.BrokerConstants;
import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.InterceptConnectMessage;
import io.moquette.interception.messages.InterceptDisconnectMessage;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.moquette.interception.messages.InterceptSubscribeMessage;
import io.moquette.interception.messages.InterceptUnsubscribeMessage;
import io.moquette.proto.messages.PublishMessage;
import io.moquette.server.config.MemoryConfig;


public class MQTTService extends Service {
    private static final String TAG = "MQTT SERVICE";
    public static DBReqHandler dbReqHandler;

    static class PublisherListener extends AbstractInterceptHandler {
        @Override
        public void onConnect(InterceptConnectMessage msg) {
            Log.d(TAG, msg.getClientID() + " client connected");
        }

        @Override
        public void onDisconnect(InterceptDisconnectMessage msg) {
            Log.d(TAG, msg.getClientID() + " client disconnected");
        }

        @Override
        public void onSubscribe(InterceptSubscribeMessage msg) {
            if(msg.getTopicFilter().equals("roomBooking")) {
                Log.d(TAG,  msg.getClientID() + " client subscribed " + msg.getTopicFilter());
            }
        }

        @Override
        public void onUnsubscribe(InterceptUnsubscribeMessage msg) {
            Log.d(TAG,  msg.getClientID() + " client unsubscribed " + msg.getTopicFilter());
        }
        @Override
        public void onPublish(InterceptPublishMessage message) {
            String topic = message.getTopicName();
            Log.d(TAG, "" + "Message published ,\n topic: " + topic
                    + ", \ncontent: " + new String(message.getPayload().array()) + "\nfrom client " + message.getClientID());

            String msg =  message.getPayload().array().toString();
            if(msg != null) {
                Log.d(TAG, "----------" +  msg);
                try {
                    JSONObject bookObj = new JSONObject(msg);
                    if (topic.equals("RQ_BK_CNF")) {
                        Log.d(TAG, "\nbooking request received" + message.toString());
                        //JSONObject bookObj = new JSONObject(message.toString());
                        if (bookObj.optString("msg_type").equals("RQ_BK_CNF")) {
                            Log.d(TAG,  "book request for room " + bookObj.optString("room"));
                            MQTTService.dbReqHandler.dbRequest(DBReqHandler.MSG_ID_ADD, bookObj.toString());
                        }
                        Log.d(TAG, "requested room = " + bookObj.getString("room"));
                    } else if (topic.equals("RQ_RD_MN")) {
                        Log.d(TAG, "\nmonth request received" + message.toString());
                        MQTTService.dbReqHandler.dbRequest(DBReqHandler.MSG_ID_PARSE_MONTH, bookObj.toString());
                    } else if (topic.equals("RQ_RD_DAY")) {
                        Log.d(TAG, "\nday request received" + message.toString());
                        MQTTService.dbReqHandler.dbRequest(DBReqHandler.MSG_ID_PARSE_DATE, bookObj.toString());
                    } else
                        Log.d(TAG, "\ninternal topic: " + topic + " message= " + message.toString());
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG,  "MQTT service started.......");

        final io.moquette.server.Server server = new io.moquette.server.Server();
        final List<? extends InterceptHandler> userHandlers = Arrays.asList(new PublisherListener());

        try {
            MemoryConfig memoryConfig = new MemoryConfig(new Properties());
            String dbName = "/storage/emulated/0"+ File.separator + BrokerConstants.DEFAULT_MOQUETTE_STORE_MAP_DB_FILENAME;
            if(new File(dbName).exists()) {
                memoryConfig.setProperty(BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME, "/storage/emulated/0" + File.separator + BrokerConstants.DEFAULT_MOQUETTE_STORE_MAP_DB_FILENAME);
                server.startServer(memoryConfig, userHandlers);
                Log.d(TAG,  "Server Started");
            }
        }
        catch (IOException e) { e.printStackTrace(); }

        dbReqHandler = new DBReqHandler(getApplicationContext(), new DBReqHandler.IDBReqHandler() {
            @Override
            public void testCallback(String ans) {
                JSONObject response;
                try {
                    response = new JSONObject(ans);

                    if (response.optString("Client_ID").equals("000")) {
                        Log.d(TAG,ans);
                        Log.d(TAG,response.optString("err_msg"));

                    } else {
                        Log.d(TAG, "Publish to client");
                        PublishMessage msgToBePublished = new PublishMessage();
                        msgToBePublished.setTopicName(response.getString("msg_type")+"_"+response.getString("Client_ID"));
                        msgToBePublished.setPayload(ByteBuffer.wrap(ans.getBytes(Charset.forName("UTF-8" ))));
                        server.internalPublish(msgToBePublished);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }});

        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

}



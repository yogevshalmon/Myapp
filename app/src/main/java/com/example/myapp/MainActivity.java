package com.example.myapp;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class MainActivity extends AppCompatActivity implements  BlankFragment.OnFragmentInteractionListener{

    public static final String EXTRA_MESSAGE = "com.example.myapp.MESSAGE";

    private static final Strategy STRATEGY = Strategy.P2P_CLUSTER; //strategy used
    private static final String SERVICE_ID = "com.example.myapp.AD-HOC"; //strategy used
    private static final String TAG = "MyActivity";
    private static String mEndPointId = null;
    private static String userNickName; //see notebook, unique need to make unique!!!!

    private ConnectionsClient mConnectionsClient; //ni

    private static final String[] REQUIRED_PERMISSIONS =
            new String[] {
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
            };

    private final Map<String,String> mRequestConnections = new HashMap<>(); //so we want have to deal with problem of discovery and advertising together

    ListView listView;
     ArrayList<EndPoint> connectedList = new ArrayList<>();
    ArrayAdapter arrayAdapter;

    private FrameLayout fragmentContainer;
    private Button button;
    private Button button2;

    private void startAdvertising() {
        AdvertisingOptions advertisingOptions =
                new AdvertisingOptions.Builder().setStrategy(STRATEGY).build();
        mConnectionsClient
                .startAdvertising(
                        getUserNickname(), SERVICE_ID, connectionLifecycleCallback, advertisingOptions)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unusedResult) {
                                Log.v(TAG, "Now advertising endpoint ");
                                TextView textView = findViewById(R.id.isAdvertisning);
                                textView.setText("yes");
                            }
                            })
                .addOnFailureListener(
                         new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.v(TAG, "advertising failed ");
                                Log.e(TAG, "exception", e);
                            }
                        });
    }

    private void startDiscovery() {
        DiscoveryOptions discoveryOptions =
                new DiscoveryOptions.Builder().setStrategy(STRATEGY).build();
        mConnectionsClient.
        startDiscovery(
                SERVICE_ID,
                new EndpointDiscoveryCallback() {
                    @Override
                    public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
                        logD(
                                String.format(
                                        "onEndpointFound(endpointId=%s, serviceId=%s, endpointName=%s)",
                                        endpointId, info.getServiceId(), info.getEndpointName()));

                        logD(String.valueOf((info.getEndpointName().compareTo(getUserNickname()))));
                        if(
                            // mRequestConnections.containsKey(endpointId) ||
                                info.getEndpointName().compareTo(getUserNickname())>=0) { // ==0 meaning same name

                           logD("endpoint name was larger");

                        }
                         else {

                             //mConnectionsClient.stopDiscovery();
                             mConnectionsClient
                                     .requestConnection(getUserNickname(), endpointId, connectionLifecycleCallback)
                                     .addOnSuccessListener(
                                             (new OnSuccessListener<Void>() {
                                                 @Override
                                                 public void onSuccess(Void unusedResult) {
                                                     logD("request connection succed");
                                                 }
                                             })
                                     )
                                     .addOnFailureListener(
                                             new OnFailureListener() {
                                                 @Override
                                                 public void onFailure(@NonNull Exception e) {
                                                     logD("request connection failed");
                                                     Log.e(TAG, "exception", e);

                                                 }
                                             });
                         }

                    }

                    @Override
                    public void onEndpointLost(String endpointId) {

                    }
                },
                discoveryOptions)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unusedResult) {
                                Log.v(TAG, "Now discovering endpoints");
                                TextView textView = findViewById(R.id.isDiscovering);
                                textView.setText("yes");
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "exception", e);
                            }
                        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mConnectionsClient = Nearby.getConnectionsClient(this);
        Random r = new Random();
        int a = r.nextInt(1024 + 1);
        userNickName = String.valueOf(a);
        arrayAdapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,connectedList);

        //------------------------------------------------------------
        listView = (ListView)findViewById(R.id.connectedListView);
        listView.setAdapter(arrayAdapter);


        button = (Button)findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               startAdvertising();
            }
        });

        button2 = (Button)findViewById(R.id.button2);

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDiscovery();
            }
        });


        fragmentContainer = (FrameLayout) findViewById(R.id.fragment_container);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub

                openFragment(connectedList.get(position));
            }
        });
    }

    public void openFragment(EndPoint endPoint){
        BlankFragment fragment = BlankFragment.newInstance(endPoint);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right,R.anim.exit_to_right,R.anim.enter_from_right,R.anim.exit_to_right);
        transaction.addToBackStack(null);
        transaction.add(R.id.fragment_container, fragment , "BLANK_FRAGMENT").commit();
    }

    @Override
    public void onFragmentInteraction() {

        onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT < 23) {
            ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS,1);
        } else {
            requestPermissions(REQUIRED_PERMISSIONS,1);

        }

    }


    private final ConnectionLifecycleCallback connectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(String endpointId,ConnectionInfo info) {

                   // mConnectionsClient.stopDiscovery();

                    logD("connection initiated by" + endpointId + " , name: " + info.getEndpointName());
                    mRequestConnections.put(endpointId, info.getEndpointName());
                    mConnectionsClient
                            .acceptConnection(endpointId, payloadCallback);

                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    switch (result.getStatus().getStatusCode()) {
                        case ConnectionsStatusCodes.STATUS_OK:
                            String s=mRequestConnections.get(endpointId); //get name form requestConnnections Map

                            EndPoint endPoint = new EndPoint(endpointId,s,endpointId,1);
                            connectedList.add(endPoint);
                              // if(mEndPointId!=null && s.compareTo(getUserNickname())>=0){ //only who initate the connection will initiate the exchange
                             //      getMEndPointId(endpointId,true); //temp for getMyendPointId
                             //  }

                            connectedListUpdateView();

                            updateRoutingTable(connectedList);// update the table in the routing algorithym

                            // We're connected! Can now start sending and receiving data.
                            logD("connection result status ok");
                            break;
                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            logD("connection result status rejected");
                            // The connection was rejected by one or both sides.
                            break;
                        case ConnectionsStatusCodes.STATUS_ERROR:
                            logD("connection result status error");
                            // The connection broke before it was able to be accepted.
                            break;
                        default:
                            // Unknown status code
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    logD("endpoint disconnected: " + endpointId );

                    removeEndPoint(connectedList,endpointId);
                    listView = (ListView)findViewById(R.id.connectedListView);
                    listView.setAdapter(arrayAdapter);
                    arrayAdapter.notifyDataSetChanged();

                    // We've been disconnected from this endpoint. No more data can be
                    // sent or received.
                }
            };

    private final PayloadCallback payloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(String endpointId, Payload payload) {
                    byte[] receivedBytes = payload.asBytes();
                    String s = new String (receivedBytes);
                    try {
                        JSONObject jsonObject = new JSONObject(s);
                        switch (jsonObject.get("messageType").toString()){
                            case("updateRoutingTable"):
                                logD("updateRoutongTable type entered");
                                logD(s);
                                String jsonText = jsonObject.getString("connectedList");
                                Type listType = new TypeToken<List<EndPoint>>() {}.getType();

                                List<EndPoint> routingtable = new Gson().fromJson(jsonText , listType);


                                for(int i = 0; i < routingtable.size(); i++){
                                   if(!routingtable.get(i).endPointName.equals(getUserNickname())) {//if equals mean its our endpoint and we dont check..

                                       if (getEndPointPos(connectedList, routingtable.get(i).endPointId) == -1) {//we didnt find the endpoint so we add this to our list
                                           logD("found new endpoind by routing table");
                                           routingtable.get(i).Dist = routingtable.get(i).Dist + 1; //update dist
                                           connectedList.add(routingtable.get(i));
                                           connectedListUpdateView();//update vissually the table
                                       } else {// need to check if it shorter distance
                                           logD("endpoind by routing table exist");
                                       }
                                   }
                                }

                                ;
                            /*case("getMEndPointId"):  //test for get myEndPointId service
                                String mId = jsonObject.getString("endPointId");
                                boolean ack = jsonObject.getBoolean("ack");
                                if(mEndPointId!=null){
                                    mEndPointId=mId;
                                }
                                if(ack){
                                    getMEndPointId(endpointId,false);
                                }
                             ; */
                            case("message"):
                                String mes = jsonObject.getString("message");
                                Message m = new Message(mes,"");
                                int pos = getEndPointPos(connectedList,endpointId);
                                connectedList.get(pos).inbox.messages.add(m);   //need to check if endpoint exist

                                FragmentManager fm = getSupportFragmentManager();
                                BlankFragment fragment = (BlankFragment)fm.findFragmentById(R.id.fragment_container);
                                if(fragment != null && fragment.isVisible()){
                                    fragment.updateMessages();
                                }


                                logD("message recived from: " + endpointId);
                            ;
                        }


                    }catch (JSONException err){
                        Log.d("Error", err.toString());
                    }

                }

                @Override
                public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {

                }

                };


    public  void sendMessage(Message message,String sender,String reciver) {  //sender use for Ad-hoc routing
        JSONObject obj = new JSONObject();
        JSONObject mes = new JSONObject();

        try{
            obj.put("messageType","message");
            obj.put("message" , message.message);
            byte [] b = obj.toString().getBytes();
            Payload bytesPayload = Payload.fromBytes(b);
            mConnectionsClient.sendPayload(reciver,bytesPayload); //send to reciver
            logD("send message: " + message);

        }
        catch(JSONException e)
        {
            Log.e(TAG, "updateRoutingTable put object failed: ", e);;
        }


    }


    public void startSearchConnection(View view){
        startDiscovery();
        startAdvertising();

    }


    public void enterName(View view){
        EditText editText = (EditText) findViewById(R.id.name_Id);
        userNickName = editText.getText().toString();
    }

     public String mapToString(final Map<String,String> m){
        String t="";
        if(m!=null){
            for (Map.Entry<String,String> entry : m.entrySet()){
               t=t + entry.getValue() + "/n";
             }
             return t;
     }else{
            return t;
        }


     }

    @CallSuper
    protected void logD(String msg) {
        Log.d(TAG, msg);
    }

    public class EndPoint implements Serializable {
       private String endPointId;
       private String endPointName;
       public Inbox inbox=new Inbox();
       public String nextEndPoint;
       public int Dist;

       public EndPoint(){
           this.endPointId="";
           this.endPointName="";
       }
       public EndPoint(String endPointId,String endPointName,String nextendpoint,int dist){
           this.endPointId=endPointId;
           this.endPointName=endPointName;
           this.nextEndPoint = nextendpoint;
           this.Dist = dist;
       }
        @Override
        public String toString() {
            return endPointName;
        }

        public String getEndPointId(){
           return this.endPointId;
        }

        public String getEndPointName(){
            return this.endPointName;
        }
    }

    public class Inbox{
        public ArrayList<Message> messages;

        public Inbox(){
            messages= new ArrayList<>();
        }
    }

    public void updateRoutingTable(ArrayList<EndPoint> connectedList){
        JSONObject obj = new JSONObject();

        try{
            obj.put("messageType","updateRoutingTable");

            String jsonText =  new Gson().toJson(connectedList);
            logD("jsArrary:" + jsonText);
            obj.put("connectedList" , jsonText);
            byte [] b = obj.toString().getBytes();
            ArrayList<String> sendList = getMultiCastEndPoints(connectedList);
            if(sendList.size()>0){
                Payload bytesPayload = Payload.fromBytes(b);
                mConnectionsClient.sendPayload(sendList,bytesPayload); //send to all possibale endpoints
            }



        }
        catch(JSONException e)
        {
            Log.e(TAG, "updateRoutingTable put object failed: ", e);;
        }

    }

    public void connectedListUpdateView(){
        listView = (ListView)findViewById(R.id.connectedListView);
        listView.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();
    }

    public void removeEndPoint(ArrayList<EndPoint> arr,String endPointId){
        int size=arr.size();
        logD("arr size:" + size);
         for(int i=0;i<size;i++){
             if(endPointId.equals(arr.get(i).getEndPointId())){
                 arr.remove(i);
             }

         }
    }

    public int getEndPointPos(ArrayList<EndPoint> arr,String endPointId){
        int size=arr.size();
        int ret = -1;
        for(int i=0;i<size;i++){
            if(endPointId.equals(arr.get(i).getEndPointId())){
                ret=i;
            }

        }
        return ret;
    }

    public void getMEndPointId(String endPoint,boolean ack){
        JSONObject obj = new JSONObject();
        try{
            obj.put("messageType","getMEndPointId");
            obj.put("endPointId" , endPoint);
            obj.put("ack" , ack); //if we send first,on back call it will be false
            byte [] b = obj.toString().getBytes();
            Payload bytesPayload = Payload.fromBytes(b);
            mConnectionsClient.sendPayload(endPoint,bytesPayload); //send to all possibale endpoints


        }
        catch(JSONException e)
        {
            Log.e(TAG, "updateRoutingTable put object failed: ", e);;
        }

    }

    public ArrayList<String> getMultiCastEndPoints(ArrayList<EndPoint> arr){
        ArrayList<String> t= new ArrayList<String>();
        int size=arr.size();
        logD("arr size:" + size);
        for(int i=0;i<size;i++){
            if(1==(arr.get(i).Dist)){
                t.add(arr.get(i).getEndPointId());
            }

        }
        return t;
    }

    public String getUserNickname(){
        return userNickName;
    }
    public static String getmEndPointId(){
        return mEndPointId;
    }

}

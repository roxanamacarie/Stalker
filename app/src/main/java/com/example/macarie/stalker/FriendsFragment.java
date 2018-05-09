package com.example.macarie.stalker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

/**
 * Created by macarie on 28/03/2018.
 */

public class FriendsFragment extends Fragment {
    private ListView friendsListView;
    Set<String> friendsList = new HashSet<>();
    String globalLongitude = "";
    String globalLatitude = "";
    private View v;
    FirebaseListAdapter listAdapter;

    public static FriendsFragment newInstance() {
        FriendsFragment fragment = new FriendsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.friends, container, false);
        friendsListView = (ListView) v.findViewById(R.id.friendsList);


        LocalBroadcastManager.getInstance(inflater.getContext()).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String latitude = intent.getStringExtra(LocationService.EXTRA_LATITUDE);
                        String longitude = intent.getStringExtra(LocationService.EXTRA_LONGITUDE);

                        if (latitude != null && longitude != null) {
                            globalLongitude = longitude;
                            globalLatitude = latitude;
                            if (listAdapter != null)
                                listAdapter.notifyDataSetChanged();
                        }
                    }
                }, new IntentFilter(LocationService.ACTION_LOCATION_BROADCAST)
        );

        AccessToken accessToken = AccessToken.getCurrentAccessToken();

        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.d("Friends", object.toString());
                        Log.d("ResponseFriends", response.toString());

                        JSONObject graphObject = response.getJSONObject();
                        try {
                            JSONArray data = graphObject.getJSONObject("friends").getJSONArray("data");

                            for (int i = 0; i < data.length(); i++) {
                                JSONObject user = data.getJSONObject(i);

                                String friendName = user.getString("name");
                                friendsList.add(friendName);

                            }

                            getAppFriends(friendsList);


                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "friends,friendlists");
        request.setParameters(parameters);
        request.executeAsync();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("friends").child(Profile.getCurrentProfile().getName());


        listAdapter = new FirebaseListAdapter<User>(getActivity(),User.class,R.layout.friend, ref) {
            @Override
            protected void populateView(View v, User model, int position) {
                ((TextView) v.findViewById(R.id.username)).setText(model.username);
                ((TextView) v.findViewById(R.id.longitude)).setText("Longitude: " + model.longitude);
                ((TextView) v.findViewById(R.id.latitude)).setText("Latitude: " + model.latitude);
                ((TextView) v.findViewById(R.id.date)).setText("Date: " + model.day + " " + model.hour);

                String lastTime = "Last time online: ";
                try {
                    lastTime += getTime(model.day, model.hour);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                lastTime += " ago";

                String distance = "Last position: ";
                if (globalLatitude != "" && globalLongitude !="")
                    distance += getDistance(Float.valueOf(model.latitude), Float.valueOf(model.longitude), Float.valueOf(globalLatitude), Float.valueOf(globalLongitude));
                else
                    distance += "NA";

                distance += " away";
                ((TextView) v.findViewById(R.id.lastTime)).setText(lastTime);
                ((TextView) v.findViewById(R.id.distance)).setText(distance);
            }

        };
        friendsListView.setAdapter(listAdapter);

        return v;

    }
    private static DecimalFormat df2 = new DecimalFormat(".##");

    private String getDistance(float lat_a, float lng_a, float lat_b, float lng_b) {
        float pk = (float) (180.f/Math.PI);

        float a1 = lat_a / pk;
        float a2 = lng_a / pk;
        float b1 = lat_b / pk;
        float b2 = lng_b / pk;

        double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
        double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
        double t3 = Math.sin(a1) * Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);

        double distance = 6366000 * tt;

        if(distance < 1000) {
            return df2.format(6366000 * tt) + " meters";
        }
        else {
            return df2.format(6366000 * tt / 1000) + " km";
        }

    }
    private String getTime(String day, String hour) throws ParseException {
        String date = day + " " + hour;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date last = sdf.parse(date);
        String currentDateandTime = sdf.format(Calendar.getInstance().getTime());
        Date now = sdf.parse(currentDateandTime);
        String typeDate = "";

        long diffInMillies = Math.abs(now.getTime() - last.getTime());
        long diff = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);
        typeDate = " minutes";

        if (diff > 60) {
            diff = TimeUnit.HOURS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            typeDate = " hours";

            if (diff > 24) {
                diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                typeDate = " days";
            }
        }



        return Long.toString(diff) + typeDate ;
    }

    public void getAppFriends(final Set<String> friends) {

        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference friendsRef = databaseRef.child("friends");

        final String currentUser = Profile.getCurrentProfile().getName();

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()){

                    Map<String, Object> dataHash = (Map<String, Object>) data.getValue();

                    if( friends.contains(data.getKey())) {

                        String username = (String)dataHash.get("username");
                        String latitude = (String)dataHash.get("latitude");
                        String longitude = (String)dataHash.get("longitude");
                        String day = (String)dataHash.get("day");
                        String hour = (String)dataHash.get("hour");

                        friendsRef.child(currentUser).child(username).setValue(new User(username, latitude, longitude, day, hour));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Canceled", "loadPost:onCancelled", databaseError.toException());
            }
        };

        FirebaseDatabase.getInstance().getReference("users").addListenerForSingleValueEvent(listener);

    }
}

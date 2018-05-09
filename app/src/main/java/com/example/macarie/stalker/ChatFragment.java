package com.example.macarie.stalker;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by macarie on 28/03/2018.
 */

public class ChatFragment extends Fragment {
    private ListView friendsListChatView;
    Set<String> friendsList = new HashSet<>();
    String globalLongitude = "";
    String globalLatitude = "";
    private View v;
    FirebaseListAdapter listAdapter;

    public static ChatFragment newInstance() {
        ChatFragment fragment = new ChatFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.friends_list_chat, container, false);
        friendsListChatView = (ListView) v.findViewById(R.id.friendsChatList);


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("friends").child(Profile.getCurrentProfile().getName());


        listAdapter = new FirebaseListAdapter<User>(getActivity(),User.class,R.layout.friend_chat, ref) {
            @Override
            protected void populateView(View v, User model, int position) {

                ((TextView) v.findViewById(R.id.friendName)).setText(model.username);

                DatabaseReference lastMessageRef = FirebaseDatabase.getInstance()
                        .getReference("chat").child(Profile.getCurrentProfile().getName()).child(model.username);

                final View view_ref = v;
                final User model_ref = model;


                lastMessageRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ChatMessage lastMessage = null;
                        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                            lastMessage = postSnapshot.getValue(ChatMessage.class);
                        }

                        String messageText = "";
                        String lastTime = "";

                        if (lastMessage != null) {
                            messageText = lastMessage.getMessageText();
                            if (messageText.length() >= 15) {
                                messageText = messageText.substring(0,15);
                                messageText += "...";
                            }
                            lastTime = "" + DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                                    lastMessage.getMessageTime());
                        }

                        ((TextView) view_ref.findViewById(R.id.lastMessage)).setText(messageText);
                        ((TextView) view_ref.findViewById(R.id.lastDateMessage)).setText(lastTime);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

        };
        friendsListChatView.setAdapter(listAdapter);

        friendsListChatView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Intent intent = new Intent(getActivity(), ChatFriendsActivity.class);
                User us = (User)listAdapter.getItem(position);

                intent.putExtra("friendName", us.getUsername());
                startActivity(intent);
            }
        });

        return v;

    }
}

package com.example.macarie.stalker;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.Profile;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by macarie on 07/05/2018.
 */

public class ChatFriendsActivity extends AppCompatActivity {

    FirebaseListAdapter listAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.chat_friend);


        Intent intent = getIntent();

        final String friendName = intent.getStringExtra("friendName");
        displayChatMessages(friendName);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        ((TextView)findViewById(R.id.friendChatName)).setText(friendName);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText input = (EditText)findViewById(R.id.input);

                FirebaseDatabase.getInstance().getReference("chat").child(FirebaseAuth.getInstance().getCurrentUser().getDisplayName()).child(friendName).push().setValue(new ChatMessage(input.getText().toString(), FirebaseAuth.getInstance().getCurrentUser().getDisplayName()));
                FirebaseDatabase.getInstance().getReference("chat").child(friendName).child(FirebaseAuth.getInstance().getCurrentUser().getDisplayName()).push().setValue(new ChatMessage(input.getText().toString(), FirebaseAuth.getInstance().getCurrentUser().getDisplayName()));

                FirebaseDatabase.getInstance().getReference("message").child(FirebaseAuth.getInstance().getCurrentUser().getDisplayName()).child(friendName).setValue(new ChatMessage(input.getText().toString(), FirebaseAuth.getInstance().getCurrentUser().getDisplayName()));
                FirebaseDatabase.getInstance().getReference("message").child(friendName).child(FirebaseAuth.getInstance().getCurrentUser().getDisplayName()).setValue(new ChatMessage(input.getText().toString(), FirebaseAuth.getInstance().getCurrentUser().getDisplayName()));


                input.setText("");
            }
        });


    }

    private void displayChatMessages(String friendName) {
        ListView listOfMessages = (ListView)findViewById(R.id.list_of_messages);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("chat").child(FirebaseAuth.getInstance().getCurrentUser().getDisplayName()).child(friendName);
        listAdapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class,
                R.layout.message, ref) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                // Get references to the views of message.xml
                TextView messageText = (TextView)v.findViewById(R.id.message_text);
                TextView messageUser = (TextView)v.findViewById(R.id.message_user);
                TextView messageTime = (TextView)v.findViewById(R.id.message_time);

                // Set their text
                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());

                // Format the date before showing it
                messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                        model.getMessageTime()));
            }
        };

        listOfMessages.setAdapter(listAdapter);
    }
}


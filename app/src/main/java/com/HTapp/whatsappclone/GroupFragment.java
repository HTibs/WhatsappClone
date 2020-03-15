package com.HTapp.whatsappclone;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupFragment extends Fragment {

    private View groupFragmentView;
    private ListView listView;
    private ArrayAdapter<String> arrayadapter;
    private ArrayList<String> listofGroups = new ArrayList<>();
    private DatabaseReference groupref;



    public GroupFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        groupFragmentView = inflater.inflate(R.layout.fragment_group2, container, false);

        groupref = FirebaseDatabase.getInstance().getReference().child("Group");
        initialzfields();
        retrieve_display_groups();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String currentGroupName = parent.getItemAtPosition(position).toString();
                Intent grpchatintent = new Intent(getContext(),GroupChatActivity.class);
                grpchatintent.putExtra("groupname", currentGroupName);
                startActivity(grpchatintent);




            }
        });


        return groupFragmentView;
    }

    private void initialzfields(){

        listView = (ListView) groupFragmentView.findViewById(R.id.list_view_groups);
        arrayadapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, listofGroups);
        listView.setAdapter(arrayadapter);


    }

    private void retrieve_display_groups(){

        groupref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Set<String> set = new HashSet<>();
                Iterator iterator = dataSnapshot.getChildren().iterator();

                while (iterator.hasNext()){

                    set.add(((DataSnapshot)iterator.next()).getKey());
                }

                listofGroups.clear();
                listofGroups.addAll(set);
                arrayadapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}

package com.example.myapp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BlankFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BlankFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BlankFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";


    // TODO: Rename and change types of parameters

    private static final String TAG = "MyFragment";
    private Button buttonFragment;
    private Button sendMessagebutton;
    private static final String currEndPointKey = "currEndPoint";
    private MainActivity.EndPoint mendPoint;
    private OnFragmentInteractionListener mListener;
    ListView listView;
    EditText messageText;
    ArrayAdapter messageAdapter;
    Message message= new Message();


    ArrayList<MainActivity.EndPoint> connectedList = new ArrayList<>();
    public BlankFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static BlankFragment newInstance(MainActivity.EndPoint endPoint) {
        BlankFragment fragment = new BlankFragment();
        Bundle args = new Bundle();
        args.putSerializable(currEndPointKey,endPoint);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mendPoint = (MainActivity.EndPoint) bundle.getSerializable(currEndPointKey);

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_blank, container, false);

        TextView textView = view.findViewById(R.id.chatNameText);
        //textView.setText(mendPoint.getEndPointName());
        textView.setText(MainActivity.getmEndPointId());
        buttonFragment = view.findViewById(R.id.button_fragment);
        sendMessagebutton = view.findViewById(R.id.sendMessageBtn);
        messageText = (EditText)view.findViewById(R.id.edittext_fragment);
        buttonFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonPressed();
            }
        });

        sendMessagebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message.message= messageText.getText().toString();
                 message.date = "";
                 String reciver = mendPoint.getEndPointId();
                 String sender =  ((MainActivity)getActivity()).getUserNickname();
                ((MainActivity)getActivity()).sendMessage(message,sender,reciver);

            }
        });

        messageAdapter=new ArrayAdapter(getContext(),android.R.layout.simple_list_item_1,mendPoint.inbox.messages);
        //------------------------------------------------------------
        listView = (ListView)view.findViewById(R.id.messagesListView);
        listView.setAdapter(messageAdapter);


        Log.d(TAG, mendPoint.inbox.messages.toString());

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed() {
        if (mListener != null) {
            mListener.onFragmentInteraction();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction();
    }

    public void updateMessages(){
        Log.d(TAG, "update messaeg on fragment");

        listView.setAdapter(messageAdapter);
        messageAdapter.notifyDataSetChanged();
    }

    public void checkm(){
        Log.d(TAG, "checkm on fragment");
    }


}

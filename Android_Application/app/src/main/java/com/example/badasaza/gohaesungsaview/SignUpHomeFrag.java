package com.example.badasaza.gohaesungsaview;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.badasaza.gohaesungsacustomer.R;

/**
 * Created by Badasaza on 2015-11-30.
 */
public class SignUpHomeFrag extends Fragment implements View.OnClickListener {

    EditText name;
    EditText tel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.fragment_sign_up_home, container, false);
        Button nextButton = (Button) rootView.findViewById(R.id.signup_to_next1);
        nextButton.setOnClickListener(this);
        name = (EditText) rootView.findViewById(R.id.signup_name_edit);
        tel = (EditText) rootView.findViewById(R.id.signup_tel_edit);
        return rootView;
    }

    @Override
    public void onClick(View v) {
        FragmentManager fm = getFragmentManager();
        SignUpWarnFrag suf = new SignUpWarnFrag();
        Bundle a = new Bundle();
        /* ToDo: gotta put animator later */
        String n = name.getText().toString();
        String t = tel.getText().toString();
        if(n == null || n.matches("") || t == null || t.matches("") || !isTelephone(t)){
            AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
            ab.setMessage(R.string.signup_alert_content).setTitle(R.string.signup_alert_title);
            ab.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            ab.create().show();
        }else {
            a.putString(SignUpWarnFrag.NAME_KEY, n);
            a.putString(SignUpWarnFrag.TEL_KEY, t);
            suf.setArguments(a);
            fm.beginTransaction().replace(R.id.signup_frag_container, suf).addToBackStack(null).commit();
        }
    }

    private boolean isTelephone(String str){/*
        if(str.length() < 10) {
            Log.i("tel","short");
            return false;
        }*/
        try{
            Integer.parseInt(str);
        }catch (NumberFormatException e){
            return false;
        }
        return true;
    }
}

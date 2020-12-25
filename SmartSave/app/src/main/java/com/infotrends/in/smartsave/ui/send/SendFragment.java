package com.infotrends.in.smartsave.ui.send;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.infotrends.in.smartsave.R;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

public class SendFragment extends Fragment implements View.OnClickListener {

    View root;
    Context context;
    Activity activity;
    private NavController navController;
    private NavHostFragment navHostFragment;

    private Button send;
    private RatingBar rBar;
    private EditText contentField;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_send, container, false);
        context = getActivity();
        activity = getActivity();

        navHostFragment = (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        send = root.findViewById(R.id.send_button);
        send.setOnClickListener(this);

        rBar = root.findViewById(R.id.ratingBar);
        contentField = root.findViewById(R.id.review_content);
        return root;
    }

    @Override
    public void onClick(View v) {
        String TO = "infotrends.india@gmail.com";
        String CC = "";
        Toast.makeText(context, String.valueOf(rBar.getRating()), Toast.LENGTH_SHORT).show();
//        Intent review = new Intent(Intent.ACTION_SENDTO);
//        review.setData(Uri.parse("mailto:"));
//        review.setType("txt/plain");
//        review.putExtra(Intent.EXTRA_EMAIL, TO);
//        review.putExtra(Intent.EXTRA_CC, CC);
//        review.putExtra(Intent.EXTRA_SUBJECT, "App Review");
//        review.putExtra(Intent.EXTRA_TEXT, "The Rating for the app is: " + String.valueOf(rBar.getRating()) + ". \n The App Review is: \n" + contentField.getText());
        String mailto = "mailto:" + TO +
                "?cc=" + CC +
                "&subject=" + Uri.encode("App Review") +
                "&body=" + Uri.encode("The Rating for the SmartSave app is: " + String.valueOf(rBar.getRating()) + ". \n The App Review is: \n" + contentField.getText());

        Intent review = new Intent(Intent.ACTION_SENDTO);
        review.setData(Uri.parse(mailto));
        try {
            startActivity(Intent.createChooser(review, "Send mail..."));
//            activity.finish();
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(context, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }
}

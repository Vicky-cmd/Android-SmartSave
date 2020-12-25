package com.infotrends.in.smartsave.ui.send;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.infotrends.in.smartsave.R;
import com.infotrends.in.smartsave.orchestrator.SendOrc;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class ShareFragment extends Fragment {

    Context context;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        Toast.makeText(context, "Share via...", Toast.LENGTH_SHORT);
        SendOrc sOrc = new SendOrc();sOrc.shareApp(context);
        NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
        navController.navigateUp();
    }

}

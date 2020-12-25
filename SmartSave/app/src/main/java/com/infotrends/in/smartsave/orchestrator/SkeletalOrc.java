package com.infotrends.in.smartsave.orchestrator;

import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.infotrends.in.smartsave.R;
import com.infotrends.in.smartsave.database.DBHelper;
import com.infotrends.in.smartsave.utils.AppProps;

import androidx.recyclerview.widget.RecyclerView;
import io.supercharge.shimmerlayout.ShimmerLayout;

public class SkeletalOrc {
    DBHelper dbHelper = DBHelper.getInstance();
    private AppProps oAppProps = AppProps.getInstance();
    private Context context = oAppProps.getContext();


    private LinearLayout skeletonLayout;
    private ShimmerLayout shimmer;
    private LayoutInflater inflater;

    public SkeletalOrc() {
    }

    public SkeletalOrc(LinearLayout skeletonLayout, ShimmerLayout shimmer, LayoutInflater inflater) {
        this.skeletonLayout = skeletonLayout;
        this.shimmer = shimmer;
        this.inflater = inflater;
    }

    public void setSkeletonLayout(LinearLayout skeletonLayout) {
        this.skeletonLayout = skeletonLayout;
    }

    public void setShimmer(ShimmerLayout shimmer) {
        this.shimmer = shimmer;
    }

    public void setInflater(LayoutInflater inflater) {
        this.inflater = inflater;
    }

    public void showSkeleton(boolean show) {

        if (show) {

            skeletonLayout.removeAllViews();

            int skeletonRows = getSkeletonRowCount(context);
            for (int i = 0; i <= skeletonRows; i++) {
                ViewGroup rowLayout = (ViewGroup) inflater
                        .inflate(R.layout.skeletal_loading_screen, null);
                skeletonLayout.addView(rowLayout);
            }
            shimmer.setVisibility(View.VISIBLE);
            shimmer.startShimmerAnimation();
            skeletonLayout.setVisibility(View.VISIBLE);
            skeletonLayout.bringToFront();
        } else {
            shimmer.stopShimmerAnimation();
            shimmer.setVisibility(View.GONE);
        }
    }


    public int getSkeletonRowCount(Context context) {
        int pxHeight = getDeviceHeight(context);
        int skeletonRowHeight = (int) context.getResources()
                .getDimension(R.dimen.skeletal_height); //converts to pixel
        return (int) Math.ceil(pxHeight / skeletonRowHeight) +1;
    }
    public int getDeviceHeight(Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return metrics.heightPixels;
    }


    public void animateReplaceSkeleton(RecyclerView listView) {

        listView.setVisibility(View.VISIBLE);
        listView.setAlpha(0f);
        listView.animate().alpha(1f).setDuration(1000).start();

        skeletonLayout.animate().alpha(0f).setDuration(1000).withEndAction(new Runnable() {
            @Override
            public void run() {
                showSkeleton(false);
            }
        }).start();

    }
}

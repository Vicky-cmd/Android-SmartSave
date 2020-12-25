package com.infotrends.in.smartsave.database;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.infotrends.in.smartsave.R;
import com.infotrends.in.smartsave.models.FileModel;
import com.infotrends.in.smartsave.orchestrator.FilesModOrc;
import com.infotrends.in.smartsave.utils.AppProps;
import com.infotrends.in.smartsave.utils.IntegProperties;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SharableContactsAdapter extends RecyclerView.Adapter<SharableContactsAdapter.SharableContactsHolder> {

    private Context mContext;
    private List<String> mCursor = new LinkedList<String>();
    private int fLen = 0;
    private SimpleDateFormat sf = new SimpleDateFormat("MM-dd-yyyy, H:mm:ss");
    private FilesModOrc oFilesModOrc = new FilesModOrc();
    private AppProps oAppProps = AppProps.getInstance();
    private IntegProperties oInteg = IntegProperties.getInstance();
    private String rootMediaDir = Environment.getExternalStorageDirectory()
            .getAbsolutePath()  +  oInteg.getString("app_base_dir") + oInteg.getString("app_download_media_dir");

    public SharableContactsAdapter(Context context, List<String> cursor) {
        mContext = context;
        mCursor = cursor;
        if(cursor!=null)
            fLen = cursor.size();
    }


    public class SharableContactsHolder extends RecyclerView.ViewHolder {

        public TextView sharedphno;
        public SharableContactsHolder(@NonNull View itemView) {
            super(itemView);
            sharedphno = itemView.findViewById(R.id.shared_ph_no);
        }
    }

    @NonNull
    @Override
    public SharableContactsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.show_shared_list_recycle_view, parent, false);

        return new SharableContactsAdapter.SharableContactsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SharableContactsHolder holder, int position) {

        if(position>=fLen) {
            return;
        }

        holder.sharedphno.setText(mCursor.get(position));
        holder.itemView.setTag(R.string.shared_no, position);
    }

    @Override
    public int getItemCount() {
        return mCursor.size();
    }

    public void removeSelectedItem(int pos) {
        if(mCursor!=null && mCursor.size() > 0) {
            mCursor.remove(pos);
            fLen --;
        }
    }
    public boolean addItem(String item) {
        if(mCursor==null) {
            mCursor = new LinkedList<String>();
        } else {
            if(mCursor.contains(item)) {
                return false;
            }
        }
        mCursor.add(item);
        fLen++;
        return true;
    }
    public List<String> getFilesList() {
        return mCursor;
    }

}

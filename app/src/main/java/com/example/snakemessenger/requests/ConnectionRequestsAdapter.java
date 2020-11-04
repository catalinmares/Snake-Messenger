package com.example.snakemessenger.connectionRequests;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snakemessenger.DateManager;
import com.example.snakemessenger.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

class ConnectionRequestsAdapter extends RecyclerView.Adapter<ConnectionRequestsViewHolder> {
    private Context mContext;
    private List<ConnectionRequest> connectionRequests;

    public ConnectionRequestsAdapter(Context mContext, List<ConnectionRequest> connectionRequests) {
        this.mContext = mContext;
        this.connectionRequests = connectionRequests;
    }

    @NonNull
    @Override
    public ConnectionRequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.connection_request_item, parent, false);

        return new ConnectionRequestsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ConnectionRequestsViewHolder holder, int position) {
        final ConnectionRequest currentConnectionRequest = connectionRequests.get(position);

        String userName = currentConnectionRequest.getSenderName();

        holder.getConnectionRequestMessage().setText(userName);

        Date currentDate = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        holder.getConnectionRequestTimestamp().setText(DateManager.getLastActiveText(df.format(currentDate), currentConnectionRequest.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return connectionRequests.size();
    }
}

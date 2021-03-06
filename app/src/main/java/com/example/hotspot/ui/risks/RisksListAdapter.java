package com.example.hotspot.ui.risks;
/**
 * List adapter for risk object, used by listview on risk fragment page.
 */

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotspot.R;
import com.example.hotspot.data.Risk;

import java.util.ArrayList;

public class RisksListAdapter extends RecyclerView.Adapter<RisksListAdapter.ViewHolder> {
    /**
     * List of risks to convert to a UI element.
     */
    private ArrayList<Risk> risks;

    public RisksListAdapter(ArrayList<Risk> risks) {
        this.risks = risks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.risks_list_layout,
                parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Risk risk = risks.get(position);

        View view = holder.view;
        TextView userAdd = view.findViewById(R.id.tv_user_add);
        TextView outbreakAdd = view.findViewById(R.id.tv_outbreak_add);

        userAdd.setText("\n" + "User location: " + risk.getUserAdd());
        outbreakAdd.setText("Outbreak location: " + risk.getOutbreakAdd());
    }

    @Override
    public int getItemCount() {
        return risks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private View view;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
        }
    }
}

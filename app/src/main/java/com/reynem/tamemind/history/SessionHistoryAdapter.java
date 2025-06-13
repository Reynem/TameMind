package com.reynem.tamemind.history;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.reynem.tamemind.R;

import java.util.ArrayList;
import java.util.List;

public class SessionHistoryAdapter extends RecyclerView.Adapter<SessionHistoryAdapter.SessionViewHolder> {
    private final List<TimerSession> sessions = new ArrayList<>();

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_session_history, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        TimerSession session = sessions.get(position);
        holder.bind(session);
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    public void updateSessions(List<TimerSession> newSessions) {
        final SessionDiffCallback diffCallback = new SessionDiffCallback(this.sessions, newSessions);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        this.sessions.clear();
        this.sessions.addAll(newSessions);
        diffResult.dispatchUpdatesTo(this);
    }

    public static class SessionViewHolder extends RecyclerView.ViewHolder {
        TextView dateText;
        TextView timeText;
        TextView durationText;

        SessionViewHolder(View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.sessionDate);
            timeText = itemView.findViewById(R.id.sessionTime);
            durationText = itemView.findViewById(R.id.sessionDuration);
        }

        @SuppressLint("SetTextI18n")
        public void bind(TimerSession session) {
            dateText.setText(session.getDate());
            timeText.setText(session.getStartTimeFormatted() + " - " + session.getEndTimeFormatted());
            durationText.setText(session.getFormattedDuration());
        }
    }

    private static class SessionDiffCallback extends DiffUtil.Callback {

        private final List<TimerSession> oldList;
        private final List<TimerSession> newList;

        public SessionDiffCallback(List<TimerSession> oldList, List<TimerSession> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getStartTime() == newList.get(newItemPosition).getStartTime();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
        }
    }
}
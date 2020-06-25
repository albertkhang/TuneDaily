package com.albertkhang.tunedaily.callbacks;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.albertkhang.tunedaily.R;
import com.albertkhang.tunedaily.adapters.TrackAdapter;
import com.albertkhang.tunedaily.models.Track;
import com.albertkhang.tunedaily.services.MediaPlaybackService;
import com.google.android.material.snackbar.Snackbar;

public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
    private TrackAdapter mAdapter;

    private Drawable icon;

    public SwipeToDeleteCallback(TrackAdapter mAdapter) {
        super(0, ItemTouchHelper.RIGHT);
        this.mAdapter = mAdapter;

        icon = ContextCompat.getDrawable(mAdapter.getContext(),
                R.drawable.ic_delete);
        icon.mutate().setColorFilter(mAdapter.getContext().getResources().getColor(R.color.colorSt3), PorterDuff.Mode.MULTIPLY);
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        View itemView = viewHolder.itemView;

        int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconBottom = iconTop + icon.getIntrinsicHeight();

        if (dX > 0) { // Swiping to the right
            int iconLeft = itemView.getLeft() + iconMargin;
            int iconRight = itemView.getLeft() + iconMargin + icon.getIntrinsicWidth();
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

        } else { // view is unSwiped
            icon.setBounds(0, 0, 0, 0);
        }

        icon.draw(c);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();

        onSwipedListener.onSwipedListener(position);
    }

    public interface OnSwipedListener {
        void onSwipedListener(int position);
    }

    private OnSwipedListener onSwipedListener;

    public void setOnSwipedListener(OnSwipedListener onSwipedListener) {
        this.onSwipedListener = onSwipedListener;
    }
}

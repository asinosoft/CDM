package ru.n0de.manager;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.IntRange;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewMargin extends RecyclerView.ItemDecoration {
    private final int columns;
    private int margin;

    public RecyclerViewMargin(@IntRange(from=0)int margin , @IntRange(from=0) int columns ) {
        this.margin = margin;
        this.columns=columns;

    }

    /**
     * Установка разных отступов для элементов.
     */
    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildLayoutPosition(view);
        outRect.right = margin;
        outRect.left = margin;
        outRect.bottom = margin;
        outRect.top = margin;
    }
}

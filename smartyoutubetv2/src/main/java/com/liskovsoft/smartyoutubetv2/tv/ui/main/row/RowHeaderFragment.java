package com.liskovsoft.smartyoutubetv2.tv.ui.main.row;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.leanback.app.RowsSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.OnItemViewSelectedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.smartyoutubetv2.common.mvp.models.Video;
import com.liskovsoft.smartyoutubetv2.common.mvp.models.VideoGroup;
import com.liskovsoft.smartyoutubetv2.common.mvp.presenters.MainPresenter;
import com.liskovsoft.smartyoutubetv2.tv.adapter.VideoGroupObjectAdapter;
import com.liskovsoft.smartyoutubetv2.tv.ui.base.UriBackgroundManager;
import com.liskovsoft.smartyoutubetv2.tv.ui.main.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RowHeaderFragment extends RowsSupportFragment {
    private static final String TAG = RowHeaderFragment.class.getSimpleName();
    private UriBackgroundManager mBackgroundManager;
    private Handler mHandler;
    private ArrayObjectAdapter mRowsAdapter;
    private Map<Integer, VideoGroupObjectAdapter> mMediaGroupAdapters;
    private List<VideoGroup> mPendingUpdates = new ArrayList<>();
    private MainPresenter mMainPresenter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mMediaGroupAdapters = new HashMap<>();
        mHandler = new Handler();
        mBackgroundManager = UriBackgroundManager.instance(getActivity());
        mMainPresenter = MainPresenter.instance(context);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Prepare the manager that maintains the same background image between activities.
        //prepareBackgroundManager();

        setupEventListeners();

        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        setAdapter(mRowsAdapter);

        for (VideoGroup group : mPendingUpdates) {
            updateRow(group);
        }

        mPendingUpdates.clear();

        getMainFragmentAdapter().getFragmentHost().notifyDataReady(getMainFragmentAdapter());
    }

    private void setupEventListeners() {
        setOnItemViewClickedListener(new ItemViewClickedListener());
        setOnItemViewSelectedListener(new ItemViewSelectedListener());
    }

    //@Override
    //public void onCreate(Bundle savedInstanceState) {
    //    super.onCreate(savedInstanceState);
    //    getMainFragmentAdapter().getFragmentHost().notifyDataReady(getMainFragmentAdapter());
    //}

    public void updateRow(VideoGroup group) {
        if (mMediaGroupAdapters == null) {
            mPendingUpdates.add(group);
            return;
        }

        HeaderItem rowHeader = new HeaderItem(group.getTitle());
        int mediaGroupId = group.getId(); // Create unique int from category.

        VideoGroupObjectAdapter existingAdapter = mMediaGroupAdapters.get(mediaGroupId);

        if (existingAdapter == null) {
            VideoGroupObjectAdapter mediaGroupAdapter = new VideoGroupObjectAdapter(group);

            mMediaGroupAdapters.put(mediaGroupId, mediaGroupAdapter);

            ListRow row = new ListRow(rowHeader, mediaGroupAdapter);
            mRowsAdapter.add(row);
        } else {
            existingAdapter.append(group); // continue row
        }
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Video) {
                if (getActivity() instanceof MainActivity) {
                    boolean longClick = ((MainActivity) getActivity()).isLongClick();
                    Log.d(TAG, "Is long click: " + longClick);

                    if (longClick) {
                        mMainPresenter.onVideoItemLongClick((Video) item);
                    } else {
                        mMainPresenter.onVideoItemClick((Video) item);
                    }
                }
            } else {
                Toast.makeText(getActivity(), item.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                   RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof Video) {
                Uri backgroundURI = Uri.parse(((Video) item).bgImageUrl);
                mBackgroundManager.startBackgroundTimer(backgroundURI);
            }
        }
    }
}

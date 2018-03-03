package com.framgia.mysoundcloud.screen.musicgenres;


import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import com.framgia.mysoundcloud.R;
import com.framgia.mysoundcloud.data.model.Track;
import com.framgia.mysoundcloud.screen.BaseFragment;
import com.framgia.mysoundcloud.screen.main.MainActivity;
import com.framgia.mysoundcloud.screen.playmusic.PlayMusicActivity;
import com.framgia.mysoundcloud.service.MusicService;
import com.framgia.mysoundcloud.utils.Constant;
import com.framgia.mysoundcloud.utils.Navigator;
import com.framgia.mysoundcloud.widget.DialogManager;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */

public class MusicGenresFragment extends BaseFragment implements
        MusicGenresContract.View, AdapterView.OnItemSelectedListener,
        MusicGenresAdapter.ItemClickListener, View.OnClickListener {

    private MusicGenresPresenter mPresenter;
    private MusicGenresAdapter mMusicGenresAdapter;
    private DialogManager mDialogManager;
    private ProgressDialog mProgressDialog;
    private Spinner mSpinnerGenres;
    private List<Track> mTrackList;

    public MusicGenresFragment() {
        // Required empty public constructor
    }

    @Override
    protected void initializeUI(View view) {
        mPresenter = new MusicGenresPresenter();
        mPresenter.setView(this);

        ArrayAdapter<CharSequence> genresAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.music_genres, android.R.layout.simple_spinner_item);
        genresAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerGenres = view.findViewById(R.id.spinner_genres);
        mSpinnerGenres.setAdapter(genresAdapter);
        mSpinnerGenres.setOnItemSelectedListener(this);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        mMusicGenresAdapter = new MusicGenresAdapter(getContext(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(mMusicGenresAdapter);

        mDialogManager = new DialogManager(getContext());

        ImageView imagePlayingList = view.findViewById(R.id.action_image_play_list);
        imagePlayingList.setOnClickListener(this);
    }

    @Override
    protected int getLayoutFragmentId() {
        return R.layout.fragment_music_genres;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.spinner_genres:
                mPresenter.loadTrack(Constant.MUSIC_GENRES[position],
                        Constant.LIMIT_DEFAULT, Constant.OFFSET_DEFAULT);
                break;
            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public void showTracks(ArrayList<Track> trackList) {
        mMusicGenresAdapter.replaceData(trackList);
        this.mTrackList = trackList;
    }

    @Override
    public void showNoTrack() {
        mMusicGenresAdapter.replaceData(new ArrayList<Track>());

        if (mDialogManager != null) {
            mDialogManager.dialogMessage(getString(R.string.msg_no_internet_connection),
                    getString(R.string.title_error));
        }
    }

    @Override
    public void showLoadingTracksError(String message) {
    }

    @Override
    public void showLoadingIndicator() {
        mProgressDialog = ProgressDialog.show(getContext(),
                Constant.MUSIC_GENRES[mSpinnerGenres.getSelectedItemPosition()],
                getString(R.string.msg_loading), true);
    }

    @Override
    public void hideLoadingIndicator() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onItemClicked(Track track) {
        playTracks(track);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.action_image_play_list:
                if (this.mTrackList == null || mTrackList.isEmpty()) return;
                playTracks(mTrackList.toArray(new Track[mTrackList.size()]));
                break;
            default:
                break;
        }
    }

    private void playTracks(Track... tracks) {
        if (tracks == null || tracks.length == 0) return;

        Intent intent = new Intent(getActivity(), MusicService.class);
        getActivity().startService(intent);

        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity == null) return;

        mainActivity.playTracks(tracks);
        new Navigator(getActivity()).startActivity(PlayMusicActivity.class, false);
    }
}

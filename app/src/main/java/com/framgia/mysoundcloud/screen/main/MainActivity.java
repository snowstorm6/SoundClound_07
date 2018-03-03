package com.framgia.mysoundcloud.screen.main;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.PorterDuff;
import android.os.IBinder;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.framgia.mysoundcloud.R;
import com.framgia.mysoundcloud.data.model.Track;
import com.framgia.mysoundcloud.service.MusicService;
import com.framgia.mysoundcloud.utils.music.PlaybackInfoListener;
import com.framgia.mysoundcloud.widget.DialogManager;

public class MainActivity extends AppCompatActivity implements MainViewConstract.View,
        SearchView.OnQueryTextListener, TabLayout.OnTabSelectedListener, View.OnClickListener {

    private MainViewConstract.Presenter mPresenter;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private MusicService mMusicService;
    private boolean mBound;
    private ImageView mImageChangeState;
    private ProgressBar mProgressBar;
    private ImageView mImageSong;
    private TextView mTextTitle;
    private TextView mTextArtist;
    private ConstraintLayout mLayoutMiniControl;
    private Track mTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeToolbar();
        findView();
        setupUI();
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this,
                MusicService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);
        mBound = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu_search, menu);
        initializeSearchView(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void updateTitle(String title) {
        setTitle(title);
    }

    @Override
    public void showMiniControlMusic() {

    }

    @Override
    public void hideMiniControlMusic() {

    }

    @Override
    public void showSearchResults() {

    }

    @Override
    public void showTablayout() {
        mTabLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideTablayout() {
        mTabLayout.setVisibility(View.GONE);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        new DialogManager(this).dialogMessage(getString(R.string.msg_feature_is_coming),
                getString(R.string.msg_opps));
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_action_next:
                if (!mBound) break;
                mMusicService.playNext();
                break;
            case R.id.image_action_previous:
                if (!mBound) break;
                mMusicService.playPrevious();
                break;
            case R.id.image_play_pause:
                if (!mBound) break;
                mMusicService.changeMediaState();
                break;
            default:
                break;
        }
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        int tabIconColor = ContextCompat.getColor(this, R.color.colorAccent);
        tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);

        if (tab.getPosition() == 0) {
            updateTitle(getString(R.string.music_genres));
        } else updateTitle(getString(R.string.action_download));

        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        int tabIconColor = ContextCompat.getColor(this, R.color.colorPrimary);
        tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }

    private void initializeToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        updateTitle(getString(R.string.music_genres));
    }

    private void findView() {
        mTabLayout = findViewById(R.id.tab_layout);
        mViewPager = findViewById(R.id.viewpager);
        mLayoutMiniControl = findViewById(R.id.layout_mini_control);
        mTextArtist = findViewById(R.id.text_mini_artist);
        mTextTitle = findViewById(R.id.text_mini_title);
        mImageSong = findViewById(R.id.image_mini_track);
        mImageChangeState = findViewById(R.id.image_play_pause);
        mProgressBar = findViewById(R.id.progress_bar);
        ImageView imageNext = findViewById(R.id.image_action_next);
        ImageView imagePrevious = findViewById(R.id.image_action_previous);
        imageNext.setOnClickListener(this);
        imagePrevious.setOnClickListener(this);
    }

    private void setupUI() {
        mPresenter = new MainViewPresenter();
        mPresenter.setView(this);

        mViewPager.setAdapter(new MusicGenresPagerAdapter(getSupportFragmentManager()));
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.addOnTabSelectedListener(this);

        mProgressBar.getIndeterminateDrawable()
                .setColorFilter(ContextCompat.getColor(
                        this, R.color.colorAccent), PorterDuff.Mode.SRC_IN);

        mImageChangeState.setOnClickListener(this);
    }

    private void initializeSearchView(Menu menu) {
        MenuItem searchMenu = menu.findItem(R.id.action_search);
        if (searchMenu == null) return;

        SearchView searchView = (SearchView) searchMenu.getActionView();
        searchView.setQueryHint(getString(R.string.msg_finding_tracks));
        searchView.setOnQueryTextListener(this);

        // Hanlde when searchView closed
        searchMenu.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // do something
                hideTablayout();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                // do something
                showTablayout();
                return true;
            }
        });
    }


    public void playTracks(Track... tracks) {
        if (mBound) {
            mMusicService.playTracks(tracks);
        }
    }

    private void updateUIWithState(@PlaybackInfoListener.State int state) {
        switch (state) {
            case PlaybackInfoListener.State.PREPARE:
                showLoadingIndicator();
                break;
            case PlaybackInfoListener.State.PLAYING:
                hideLoadingIndicator();
                mImageChangeState.setImageDrawable(getResources()
                        .getDrawable(R.drawable.ic_pause_white_48dp));
                break;
            case PlaybackInfoListener.State.PAUSE:
                hideLoadingIndicator();
                mImageChangeState.setImageDrawable(getResources()
                        .getDrawable(R.drawable.ic_play_arrow_white_48dp));
                break;
            default:
                break;
        }
    }

    public void showLoadingIndicator() {
        mProgressBar.setVisibility(View.VISIBLE);
        mImageChangeState.setVisibility(View.INVISIBLE);
    }

    public void hideLoadingIndicator() {
        mProgressBar.setVisibility(View.GONE);
        mImageChangeState.setVisibility(View.VISIBLE);
    }

    private void updateUIWithTrack(Track track) {
        if (track == null) return;

        mLayoutMiniControl.setVisibility(View.VISIBLE);
        if (track == mTrack) return;

        Glide.with(this).load(track.getArtworkUrl()).into(mImageSong);
        mTextTitle.setText(track.getTitle());
        mTextArtist.setText(track.getUserName());
        mTrack = track;
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBound = true;
            mMusicService = ((MusicService.LocalBinder) service).getService();

            mMusicService.setPlaybackListener(new PlaybackInfoListener() {
                @Override
                public void onTrackChanged(Track track) {
                    updateUIWithTrack(track);
                }

                @Override
                public void onStateChanged(int state) {
                    updateUIWithState(state);
                }
            });

            updateUIWithTrack(mMusicService.getCurrentTrack());
            updateUIWithState(mMusicService.getMediaState());
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            mMusicService.setPlaybackListener(null);
        }
    };


}

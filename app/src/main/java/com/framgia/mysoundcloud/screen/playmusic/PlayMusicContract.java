package com.framgia.mysoundcloud.screen.playmusic;

import com.framgia.mysoundcloud.data.model.Track;
import com.framgia.mysoundcloud.screen.BasePresenter;

/**
 * Created by sonng266 on 03/03/2018.
 */

public interface PlayMusicContract {
    /**
     * View
     */
    interface View {
        void notifyCantDownload();
    }

    /**
     * Presenter
     */
    interface Presenter extends BasePresenter<PlayMusicContract.View> {
        void downloadTrack(Track currentTrack);
    }
}

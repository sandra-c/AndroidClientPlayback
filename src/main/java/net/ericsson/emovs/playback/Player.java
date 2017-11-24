package net.ericsson.emovs.playback;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;

import net.ericsson.emovs.playback.interfaces.ITech;
import net.ericsson.emovs.utilities.analytics.AnalyticsPlaybackConnector;
import net.ericsson.emovs.utilities.interfaces.IPlayer;

import java.util.UUID;


/**
 *
 * Created by Joao Coelho on 2017-08-30.
 */
public class Player extends PlaybackEventListenerAggregator implements IPlayer {
    protected AnalyticsPlaybackConnector analyticsConnector;
    protected Activity context;
    protected ViewGroup host;
    protected PlaybackProperties properties;
    protected TechFactory techFactory;
    protected ITech tech;
    protected UUID playbackUUID;

    /**
     * Constructor of a player instance - must be instanciated from a PlayerFactory or extended
     *
     * @param analyticsConnector reference to an analytics connector instance
     * @param techFactory reference to a factory of specific techs that will play the media
     * @param context activity that holds the player
     * @param host reference to the ViewGroup that wraps the player (can have several players per activity)
     */
    public Player(AnalyticsPlaybackConnector analyticsConnector, TechFactory techFactory, Activity context, ViewGroup host) {
        this.host = host;
        this.context = context;
        this.analyticsConnector = analyticsConnector;
        this.techFactory = techFactory;

        if (analyticsConnector != null) {
            this.analyticsConnector.bindPlayer(this);
            addListener(new AnalyticsHolder(this.analyticsConnector));
        }
    }

    /**
     * Plays media given a playlist/manifest URL
     *
     * @param streamUrl
     * @param properties
     * @throws Exception
     */
    public void play(String streamUrl, PlaybackProperties properties) throws Exception {
        init(properties);
        this.tech.init(this, this.context, "", properties);
        tech.load(UUID.randomUUID().toString(), streamUrl, false);
    }

    /**
     * Unregisters and clears all listeners of a player
     *
     */
    @Override
    public void clearListeners() {
        super.clearListeners();
        if (this.analyticsConnector != null) {
            addListener(new AnalyticsHolder(this.analyticsConnector));
        }
    }

    /**
     * Returns wrapper ViewGroup of a specific player
     *
     * @return
     */
    public ViewGroup getViewGroup() {
        return host;
    }

    /**
     * Release a player instance and its tech if instanciated
     *
     */
    public void release() {
        if (this.tech != null) {
            this.tech.release();
            this.tech = null;
        }
        super.clearListeners();
    }

    /**
     * Pauses an ongoing playback
     *
     */
    public void pause() {
        if (this.tech != null) {
            this.tech.pause();
        }
    }

    /**
     * Resumes an ongoing playback
     *
     */
    public void resume() {
        if (this.tech != null) {
            this.tech.resume();
        }
    }

    /**
     * Stops ongoing playback
     *
     */
    public void stop() {
        if (this.tech != null) {
            this.tech.stop();
        }
    }

    /**
     * Seeks the media to a position and continues playback from there
     *
     * @param positionMs position in milliseconds
     */
    public void seekTo(long positionMs) {
        if (this.tech != null) {
            this.tech.seekTo(positionMs);
        }
    }

    /**
     * Returns the current time of the media being displayed
     *
     * @return
     */
    public long getCurrentTime() {
        if (this.tech != null) {
            return this.tech.getCurrentTime();
        }
        return -1;
    }

    /**
     * Returns the duration of the media
     *
     * @return
     */
    public long getDuration() {
        if (this.tech != null) {
            return this.tech.getDuration();
        }
        return -1;
    }

    /**
     * Returns a boolean to indicate if the media is currently playing or paused
     *
     * @return
     */
    public boolean isPlaying() {
        return this.tech != null && this.tech.isPlaying();
    }

    /**
     * Returns current playback properties
     *
     * @return
     */
    public PlaybackProperties getPlaybackProperties() {
        return properties;
    }

    /**
     * Returns current bitrate being displayed
     *
     * @return
     */
    public int getCurrentBitrate() {
        return this.tech == null ? -1 : tech.getCurrentBitrate();
    }

    /**
     * Returns the Context associated with the player
     *
     * @return
     */
    public Context getContext() {
        return context;
    }

    /**
     * Returns current tech version
     *
     * @return
     */
    public String getTechVersion() {
        if (this.tech == null) {
            return null;
        }
        return this.tech.getVersion();
    }

    /**
     * Returns an identifier of the type of the tech being used - null if no tech is loaded yet
     *
     * @return
     */
    public String getTechIdentifier() {
        if (this.tech == null) {
            return null;
        }
        return this.tech.getIdentifier();
    }

    /**
     * Returns an identifier of the type of the player being used
     *
     * @return
     */
    public String getIdentifier() {
        return context.getString(R.string.emplayer_name);
    }

    /**
     * Returns the version of the player
     *
     * @return
     */
    public String getVersion() {
        return context.getString(R.string.emplayer_version);
    }

    @Override
    public void mute() {
        if (this.tech == null) {
            return;
        }
        this.tech.mute();
    }

    /**
     * Unmutes the audio (volume will be last heard volume [0..1]
     */
    @Override
    public void unmute() {
        if (this.tech == null) {
            return;
        }
        this.tech.unmute();
    }

    /**
     * Sets the audio volume level
     *
     * @param volume volume level [0..1]
     */
    @Override
    public void setVolume(float volume) {
        if (this.tech == null) {
            return;
        }
        this.tech.setVolume(volume);
    }

    /**
     * Returns if the playback is going to auto play or not
     *
     * @return
     */
    @Override
    public boolean isAutoPlay() {
        return getPlaybackProperties().isAutoplay();
    }

    /**
     * Returns a list of languages available to be chosen
     *
     * @return
     */
    public String[] getAudioTracks() {
        if (this.tech == null) {
            return null;
        }
        return this.tech.getAudioTracks();
    }

    /**
     * @param language language code to select the audio track (e.g.: en, pt, es, fr)
     */
    @Override
    public void selectAudioTrack(String language) {
        if (this.tech == null) {
            return;
        }
        this.tech.selectAudioTrack(language);
    }

    /**
     * Returns selected audio track
     *
     * @return selected audio language
     */
    @Override
    public String getSelectedAudioTrack() {
        if (this.tech == null) {
            return null;
        }
        return this.tech.getSelectedAudioTrack();
    }

    /**
     * Returns selected text track
     *
     * @return selected text language
     */
    @Override
    public String getSelectedTextTrack() {
        if (this.tech == null) {
            return null;
        }
        return this.tech.getSelectedTextTrack();
    }

    protected boolean init(PlaybackProperties properties) throws Exception {
        this.properties = properties;
        this.playbackUUID = null;

        if (this.tech != null) {
            this.release();
        }

        this.tech = techFactory.build();

        super.onInit();
        return true;
    }
}

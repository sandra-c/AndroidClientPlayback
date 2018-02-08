package net.ericsson.emovs.playback;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import junit.framework.Assert;

import net.ericsson.emovs.exposure.clients.exposure.ExposureClient;
import net.ericsson.emovs.exposure.metadata.EMPMetadataProvider;
import net.ericsson.emovs.playback.helpers.FakeEMPMetadataProvider;
import net.ericsson.emovs.playback.helpers.FakeEntitlementProvider;
import net.ericsson.emovs.playback.helpers.FakeExposureClient;
import net.ericsson.emovs.playback.helpers.FakeTech;
import net.ericsson.emovs.playback.interfaces.ITech;
import net.ericsson.emovs.utilities.analytics.AnalyticsPlaybackConnector;
import net.ericsson.emovs.utilities.entitlements.Entitlement;
import net.ericsson.emovs.utilities.entitlements.IEntitlementCallback;
import net.ericsson.emovs.utilities.entitlements.IEntitlementProvider;
import net.ericsson.emovs.utilities.errors.ErrorRunnable;
import net.ericsson.emovs.utilities.interfaces.IPlayable;
import net.ericsson.emovs.utilities.models.EmpChannel;
import net.ericsson.emovs.utilities.models.EmpProgram;
import net.ericsson.emovs.utilities.test.TestUtils;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.matchers.InstanceOf;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;


/*
 * Copyright (c) 2017 Ericsson. All Rights Reserved
 *
 * This SOURCE CODE FILE, which has been provided by Ericsson as part
 * of an Ericsson software product for use ONLY by licensed users of the
 * product, includes CONFIDENTIAL and PROPRIETARY information of Ericsson.
 *
 * USE OF THIS SOFTWARE IS GOVERNED BY THE TERMS AND CONDITIONS OF
 * THE LICENSE STATEMENT AND LIMITED WARRANTY FURNISHED WITH
 * THE PRODUCT.
 */

@RunWith(RobolectricTestRunner.class)
public class EMPPlayerTest {
    PlaybackProperties DEFAULT_PLAYBACK_PROPS = PlaybackProperties.DEFAULT;
    PlaybackProperties BEGINNING_PLAYBACK_PROPS = new PlaybackProperties().withPlayFrom(PlaybackProperties.PlayFrom.BEGINNING);
    PlaybackProperties BOOKMARK_PLAYBACK_PROPS = new PlaybackProperties().withPlayFrom(PlaybackProperties.PlayFrom.BOOKMARK);
    PlaybackProperties STARTTIME_PLAYBACK_PROPS = new PlaybackProperties().withPlayFrom(new PlaybackProperties.PlayFrom.StartTime(10000L));
    PlaybackProperties LIVE_PLAYBACK_PROPS = new PlaybackProperties().withPlayFrom(new PlaybackProperties.PlayFrom.LiveEdge());
    PlaybackProperties SUBS_AND_MAXBITRATE_PLAYBACK_PROPS = new PlaybackProperties().withMaxBitrate(1000000).withPreferredAudioLanguage("pt").withPreferredTextLanguage("pt");

    @Mock
    EmpProgram live_program;
    @Mock
    EmpProgram catchup_program;
    @Mock
    EmpChannel live_channel;
    @Mock
    TechFactory techFactory;
    @Mock
    Activity dummyActivity;

    Entitlement entitlement_no_bookmark;
    Entitlement entitlement_with_bookmark_emup;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        when(live_program.liveNow()).thenReturn(true);
        when(catchup_program.liveNow()).thenReturn(false);
        when(techFactory.build()).thenReturn(new FakeTech());

        live_program.startDateTime = DateTime.now();
        live_program.endDateTime = DateTime.now();
        catchup_program.startDateTime = DateTime.now();
        catchup_program.endDateTime = DateTime.now();

        entitlement_no_bookmark = new Entitlement();
        entitlement_with_bookmark_emup = new Entitlement();
        entitlement_with_bookmark_emup.mediaLocator = ".isml";
        entitlement_with_bookmark_emup.lastViewedOffset = 1234L;
        entitlement_with_bookmark_emup.lastViewedTime = 54321L;
        entitlement_with_bookmark_emup.liveTime = 67890L;

        TestUtils.mockProvider(ExposureClient.class, new FakeExposureClient());
    }

    @Test
    public void playback_properties_test() throws Exception {
        PlaybackProperties props = new PlaybackProperties();
        props.withAutoplay(true).withNativeControls(false);
        Assert.assertFalse(props.hasNativeControls());
        Assert.assertTrue(props.isAutoplay());
    }

    @Test
    public void playback_with_subs_and_max_bitrare_props_test() throws Exception {
        FakeEntitlementProvider fakeEE = new FakeEntitlementProvider();
        EMPPlayerTechGetter player = new EMPPlayerTechGetter(null, fakeEE, techFactory, dummyActivity, null);

        player.play(live_program, SUBS_AND_MAXBITRATE_PLAYBACK_PROPS);
        Thread.sleep(50);

        FakeTech tech = player.getTech();
        Assert.assertTrue(tech.propsFedToTech.getMaxBitrate() == SUBS_AND_MAXBITRATE_PLAYBACK_PROPS.getMaxBitrate());
        Assert.assertTrue(tech.propsFedToTech.getPreferredTextLanguage() == SUBS_AND_MAXBITRATE_PLAYBACK_PROPS.getPreferredTextLanguage());
        Assert.assertTrue(tech.propsFedToTech.getPreferredAudioLanguage() == SUBS_AND_MAXBITRATE_PLAYBACK_PROPS.getPreferredAudioLanguage());
    }

    // TODO: make unit tests to ensure cases where EmpProgram is passed without all information: like programStartTime and programEndTime

    @Test
    public void playback_play_from_live_program_test() throws Exception {
        FakeEntitlementProvider fakeEE = new FakeEntitlementProvider();
        EMPPlayerTechGetter player = new EMPPlayerTechGetter(null, fakeEE, techFactory, dummyActivity, null);

        // Test Case 1: Live Program plays from Live Edge by DEFAULT
        player.play(live_program, DEFAULT_PLAYBACK_PROPS);
        Thread.sleep(50);
        Assert.assertTrue(player.getPlaybackProperties().getPlayFrom() instanceof PlaybackProperties.PlayFrom.LiveEdge);

        // Test Case 2: Live Program plays from Beginning if props are set to start from BEGINNING
        player.reset();
        player.play(live_program, BEGINNING_PLAYBACK_PROPS);
        Thread.sleep(50);
        Assert.assertTrue(player.getPlaybackProperties().getPlayFrom() instanceof PlaybackProperties.PlayFrom.Beginning);
        Assert.assertTrue(((PlaybackProperties.PlayFrom.Beginning) player.getPlaybackProperties().getPlayFrom()).startTime == live_program.startDateTime.getMillis());

        // Test Case 3: Live Program plays from Bookmark if props are set to start from BOOKMARK
        fakeEE.setEntitlement(entitlement_with_bookmark_emup);
        player.reset();
        player.play(live_program, BOOKMARK_PLAYBACK_PROPS);
        Thread.sleep(50);
        Assert.assertTrue(player.getPlaybackProperties().getPlayFrom() instanceof PlaybackProperties.PlayFrom.Bookmark);
        Assert.assertTrue(((PlaybackProperties.PlayFrom.StartTime) player.getPlaybackProperties().getPlayFrom()).startTime == entitlement_with_bookmark_emup.liveTime);

        // Test Case 4: Live Program plays from Live Edge if props are set to start from BOOKMARK but no bookmark is sent from Exposure
        fakeEE.setEntitlement(entitlement_no_bookmark);
        player.reset();
        player.play(live_program, BOOKMARK_PLAYBACK_PROPS);
        Thread.sleep(50);
        Assert.assertTrue(player.getPlaybackProperties().getPlayFrom() instanceof PlaybackProperties.PlayFrom.LiveEdge);

        // Test Case 5: Live Program plays from Live Edge if props are set to start from LIVE_EDGE
        player.reset();
        player.play(live_program, LIVE_PLAYBACK_PROPS);
        Thread.sleep(50);
        Assert.assertTrue(player.getPlaybackProperties().getPlayFrom() instanceof PlaybackProperties.PlayFrom.LiveEdge);

        // Test Case 6: Live Program plays from Start Time if props are set to start from specific StartTime
        player.reset();
        player.play(live_program, STARTTIME_PLAYBACK_PROPS);
        Thread.sleep(50);
        Assert.assertTrue(player.getPlaybackProperties().getPlayFrom() instanceof PlaybackProperties.PlayFrom.StartTime);
        Assert.assertTrue(((PlaybackProperties.PlayFrom.StartTime) player.getPlaybackProperties().getPlayFrom()).startTime == ((PlaybackProperties.PlayFrom.StartTime) STARTTIME_PLAYBACK_PROPS.getPlayFrom()).startTime);
    }

    @Test
    public void playback_play_from_catchup_test() throws Exception {
        FakeEntitlementProvider fakeEE = new FakeEntitlementProvider();
        EMPPlayerTechGetter player = new EMPPlayerTechGetter(null, fakeEE, techFactory, dummyActivity, null);

        // Test Case 1: Catchup plays from Beginning by DEFAULT
        player.play(catchup_program, DEFAULT_PLAYBACK_PROPS);
        Thread.sleep(50);
        Assert.assertTrue(player.getPlaybackProperties().getPlayFrom() instanceof PlaybackProperties.PlayFrom.Beginning);
        Assert.assertTrue(((PlaybackProperties.PlayFrom.Beginning) player.getPlaybackProperties().getPlayFrom()).startTime == catchup_program.startDateTime.getMillis());

        // Test Case 2: Catchup plays from Beginning if props are set to start from BEGINNING
        player.reset();
        player.play(catchup_program, BEGINNING_PLAYBACK_PROPS);
        Thread.sleep(50);
        Assert.assertTrue(player.getPlaybackProperties().getPlayFrom() instanceof PlaybackProperties.PlayFrom.Beginning);
        Assert.assertTrue(((PlaybackProperties.PlayFrom.Beginning) player.getPlaybackProperties().getPlayFrom()).startTime == catchup_program.startDateTime.getMillis());

        // Test Case 3: Catchup plays from Bookmark if props are set to start from BOOKMARK
        fakeEE.setEntitlement(entitlement_with_bookmark_emup);
        player.reset();
        player.play(catchup_program, BOOKMARK_PLAYBACK_PROPS);
        Thread.sleep(50);
        Assert.assertTrue(player.getPlaybackProperties().getPlayFrom() instanceof PlaybackProperties.PlayFrom.Bookmark);
        Assert.assertTrue(((PlaybackProperties.PlayFrom.StartTime) player.getPlaybackProperties().getPlayFrom()).startTime == entitlement_with_bookmark_emup.liveTime);

        // Test Case 4: Catchup plays from Beginning if props are set to start from BOOKMARK but no bookmark is sent from Exposure
        fakeEE.setEntitlement(entitlement_no_bookmark);
        player.reset();
        player.play(catchup_program, BOOKMARK_PLAYBACK_PROPS);
        Thread.sleep(50);
        Assert.assertTrue(player.getPlaybackProperties().getPlayFrom() instanceof PlaybackProperties.PlayFrom.Beginning);
        Assert.assertTrue(((PlaybackProperties.PlayFrom.Beginning) player.getPlaybackProperties().getPlayFrom()).startTime == catchup_program.startDateTime.getMillis());

        // Test Case 5: Catchup plays from Beginning if props are set to start from BEGINNING
        player.reset();
        player.play(catchup_program, BEGINNING_PLAYBACK_PROPS);
        Thread.sleep(50);
        Assert.assertTrue(player.getPlaybackProperties().getPlayFrom() instanceof PlaybackProperties.PlayFrom.Beginning);
        Assert.assertTrue(((PlaybackProperties.PlayFrom.Beginning) player.getPlaybackProperties().getPlayFrom()).startTime == catchup_program.startDateTime.getMillis());

        // Test Case 6: Catchup plays from Start Time if props are set to start from specific StartTime
        player.reset();
        player.play(catchup_program, STARTTIME_PLAYBACK_PROPS);
        Thread.sleep(50);
        Assert.assertTrue(player.getPlaybackProperties().getPlayFrom() instanceof PlaybackProperties.PlayFrom.StartTime);
        Assert.assertTrue(((PlaybackProperties.PlayFrom.StartTime) player.getPlaybackProperties().getPlayFrom()).startTime == ((PlaybackProperties.PlayFrom.StartTime) STARTTIME_PLAYBACK_PROPS.getPlayFrom()).startTime);
    }

    @Test
    public void playback_play_from_channel_test() throws Exception {
        FakeEntitlementProvider fakeEE = new FakeEntitlementProvider();
        FakeEMPMetadataProvider fakeMetadataProvider = new FakeEMPMetadataProvider();
        TestUtils.mockProvider(EMPMetadataProvider.class, fakeMetadataProvider);

        ArrayList<EmpProgram> singleProgramEpg = new ArrayList<EmpProgram>();
        singleProgramEpg.add(live_program);

        EMPPlayerTechGetter player = new EMPPlayerTechGetter(null, fakeEE, techFactory, dummyActivity, null);

        // Test Case 1: Channel plays from LIVE_EDGE by DEFAULT
        player.play(live_channel, DEFAULT_PLAYBACK_PROPS);
        Thread.sleep(50);
        Assert.assertTrue(player.getPlaybackProperties().getPlayFrom() instanceof PlaybackProperties.PlayFrom.LiveEdge);

        // Test Case 2: Channel plays from Bookmark if props are set to start from BOOKMARK
        fakeEE.setEntitlement(entitlement_with_bookmark_emup);
        player.reset();
        player.play(live_channel, BOOKMARK_PLAYBACK_PROPS);
        Thread.sleep(50);
        Assert.assertTrue(player.getPlaybackProperties().getPlayFrom() instanceof PlaybackProperties.PlayFrom.Bookmark);
        Assert.assertTrue(((PlaybackProperties.PlayFrom.StartTime) player.getPlaybackProperties().getPlayFrom()).startTime == entitlement_with_bookmark_emup.liveTime);

        // Test Case 3: Channel plays from Live Edge if props are set to start from BOOKMARK but no bookmark is sent from Exposure
        fakeEE.setEntitlement(entitlement_no_bookmark);
        player.reset();
        player.play(live_channel, BOOKMARK_PLAYBACK_PROPS);
        Thread.sleep(50);
        Assert.assertTrue(player.getPlaybackProperties().getPlayFrom() instanceof PlaybackProperties.PlayFrom.LiveEdge);

        // Test Case 4: Channel plays from StartTime if props are set to StartTime - No EPG
        fakeMetadataProvider.mockEpg(null);
        player.reset();
        player.play(live_channel, STARTTIME_PLAYBACK_PROPS);
        Thread.sleep(50);
        Assert.assertTrue(player.getPlaybackProperties().getPlayFrom() instanceof PlaybackProperties.PlayFrom.StartTime);
        Assert.assertTrue(((PlaybackProperties.PlayFrom.StartTime) player.getPlaybackProperties().getPlayFrom()).startTime == ((PlaybackProperties.PlayFrom.StartTime) STARTTIME_PLAYBACK_PROPS.getPlayFrom()).startTime);

        // Test Case 5: Channel plays from StartTime if props are set to StartTime - With EPG
        fakeMetadataProvider.mockEpg(singleProgramEpg);
        player.reset();
        player.play(live_channel, STARTTIME_PLAYBACK_PROPS);
        Thread.sleep(50);
        Assert.assertTrue(player.getPlaybackProperties().getPlayFrom() instanceof PlaybackProperties.PlayFrom.StartTime);
        Assert.assertTrue(((PlaybackProperties.PlayFrom.StartTime) player.getPlaybackProperties().getPlayFrom()).startTime == ((PlaybackProperties.PlayFrom.StartTime) STARTTIME_PLAYBACK_PROPS.getPlayFrom()).startTime);

        // Test Case 6: Channel plays from Beginning if props are set to Beginning and the Channel has EPG
        fakeMetadataProvider.mockEpg(singleProgramEpg);
        player.reset();
        player.play(live_channel, BEGINNING_PLAYBACK_PROPS);
        Thread.sleep(50);
        Assert.assertTrue(player.getPlaybackProperties().getPlayFrom() instanceof PlaybackProperties.PlayFrom.Beginning);
        Assert.assertTrue(((PlaybackProperties.PlayFrom.Beginning) player.getPlaybackProperties().getPlayFrom()).startTime == live_program.startDateTime.getMillis());

        // Test Case 7: Channel plays from Live Edge if props are set to Beginning and the Channel has no EPG
        fakeMetadataProvider.mockEpg(null);
        player.reset();
        player.play(live_channel, BEGINNING_PLAYBACK_PROPS);
        Thread.sleep(50);
        Assert.assertTrue(player.getPlaybackProperties().getPlayFrom() instanceof PlaybackProperties.PlayFrom.LiveEdge);

        // Test Case 8: Channel plays from Live Edge if props are set to start from LIVE_EDGE - No EPG
        fakeMetadataProvider.mockEpg(null);
        player.reset();
        player.play(live_program, LIVE_PLAYBACK_PROPS);
        Thread.sleep(50);
        Assert.assertTrue(player.getPlaybackProperties().getPlayFrom() instanceof PlaybackProperties.PlayFrom.LiveEdge);

        // Test Case 9: Channel plays from Live Edge if props are set to start from LIVE_EDGE - With EPG
        fakeMetadataProvider.mockEpg(singleProgramEpg);
        player.reset();
        player.play(live_program, LIVE_PLAYBACK_PROPS);
        Thread.sleep(50);
        Assert.assertTrue(player.getPlaybackProperties().getPlayFrom() instanceof PlaybackProperties.PlayFrom.LiveEdge);
    }

    class EMPPlayerTechGetter extends EMPPlayer {
        public EMPPlayerTechGetter(AnalyticsPlaybackConnector analyticsConnector, IEntitlementProvider entitlementProvider, TechFactory techFactory, Activity context, ViewGroup host) {
            super(analyticsConnector, entitlementProvider, techFactory, context, host);
        }

        public FakeTech getTech() {
            return (FakeTech) tech;
        }

        public void reset() {
            this.lastPlayTimeMs = 0L;
        }
    }
}
/*
 *
 * Copyright 2015-2017 magiclen.org
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.magiclen.magicaudioplayer;

import java.io.File;
import java.net.URI;
import java.net.URL;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

/**
 * 新型(JavaFX)的聲音播放器，支援部份系統支援格式的音訊，需使用在JavaFX應用程式中。
 *
 * @author Magic Len
 */
public class FXPlayer implements AudioPlayer {

    //-----物件變數-----
    private MediaPlayer clip;
    private int playCount = 1, playCountBuffer = 1;
    private int volume, balance;
    private Status status = null;
    private boolean autoClose = false, playing = false;
    private StatusChangedListener statusListener;

    // -----建構子-----
    /**
     * 建構子，傳入檔案。
     *
     * @param file 傳入聲音檔案
     */
    public FXPlayer(final File file) {
        try {
            final URI uri = file.getAbsoluteFile().toURI();
            init(uri);
        } catch (final Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    /**
     * 建構子，傳入URL。
     *
     * @param url 傳入聲音URL
     */
    public FXPlayer(final URL url) {
        try {
            init(url.toURI());
        } catch (final Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    /**
     * 建構子，傳入URL String
     *
     * @param str 傳入聲音URL String
     */
    public FXPlayer(final String str) {
        try {
            final URI uri = URI.create(str);
            init(uri);
        } catch (final Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    // -----物件方法-----
    /**
     * 改變目前音訊播放器的狀態。
     *
     * @param newStatus 傳入新的音訊播放器狀態
     */
    private void changeStatus(final Status newStatus) {
        final Status preStatus = status;
        status = newStatus;
        if (statusListener != null) {
            statusListener.statusChanged(preStatus, status);
        }
    }

    /**
     * 初始化AudioPlayer
     *
     * @param uri 傳入聲音URI
     * @throws Exception 拋出例外
     */
    private void init(final URI uri) throws Exception {
        try {
            clip = new MediaPlayer(new Media(uri.toString()));
        } catch (final Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
        final Runnable stopOrPause = () -> {
            boolean stop = true;
            if (clip.getCurrentTime().equals(clip.getTotalDuration())) {
                clip.seek(clip.getStartTime());
                if (playing && playCount == 0 || (playCount > 0 && playCountBuffer < playCount)) {
                    ++playCountBuffer;
                    clip.play();
                    stop = false;
                }
            }
            status = Status.STOP;
            if (stop) {
                playing = false;
                playCountBuffer = 1;
                if (autoClose) {
                    clip.dispose();
                }
            }
        };
        clip.setOnPlaying(() -> {
            changeStatus(Status.START);
        });
        clip.setOnReady(() -> {
            changeStatus(Status.OPEN);
        });
        clip.setOnHalted(() -> {
            changeStatus(Status.CLOSE);
        });
        clip.setOnStopped(stopOrPause);
        clip.setOnPaused(stopOrPause);
        clip.setOnEndOfMedia(stopOrPause);

        halfPower();
        balance();
    }

    /**
     * 開始播放音訊，可以回復暫停時的狀態。
     */
    @Override
    public void play() {
        if (!playing) {
            playing = true;
            clip.play();
        }
    }

    /**
     * 判斷是否正在播放中。
     *
     * @return 傳回是否正在播放中
     */
    @Override
    public boolean isPlaying() {
        return playing;
    }

    /**
     * 暫停播放音訊。
     */
    @Override
    public void pause() {
        if (playing) {
            playing = false;
            clip.pause();
        }
    }

    /**
     * 停止播放音訊，下次播放將會重頭開始。
     */
    @Override
    public void stop() {
        if (playing) {
            playing = false;
            clip.stop();
        } else {
            clip.seek(clip.getStartTime());
        }
    }

    /**
     * 設定播放次數，0為無限次播放。
     *
     * @param playCount 傳入播放次數
     */
    @Override
    public void setPlayCount(final int playCount) {
        if (playCount < 0) {
            throw new RuntimeException("PlayCount must be at least 0!");
        }
        this.playCount = playCount;
    }

    /**
     * 設定音量，範圍是0~100，數值愈大愈大聲。
     *
     * @param volume 傳入音量
     */
    @Override
    public void setVolume(final int volume) {
        if (volume < 0 || volume > 100) {
            throw new RuntimeException("Volumn must be at least 0 and at most 100!");
        }
        this.volume = volume;
        final float v = volume / 100.0f;
        clip.setVolume(v);
    }

    /**
     * 取得音量。
     *
     * @return 傳回音量
     */
    @Override
    public int getVolume() {
        return volume;
    }

    /**
     * 取得目前音訊播放器的狀態。
     *
     * @return 傳回狀態
     */
    @Override
    public Status getStatus() {
        return status;
    }

    /**
     * 設定聲道音量的平衡，範圍-100~100，數值愈大愈靠近右邊，0為平衡狀態。
     *
     * @param balance 傳入聲道音量的平衡值
     */
    @Override
    public void setBalance(final int balance) {
        if (volume < 0 || volume > 100) {
            throw new RuntimeException("Balance must be at least -100 and at most 100!");
        }
        this.balance = balance;
        try {
            final float pan = balance / 100.0f;
            clip.setBalance(pan);
        } catch (final Exception ex) {
            //可能是單聲道音訊檔造成的例外
        }
    }

    /**
     * 取得聲道音量的平衡值。
     *
     * @return 傳回聲道音量的平衡值
     */
    @Override
    public int getBalance() {
        return balance;
    }

    /**
     * 取得音訊的長度(微秒)。
     *
     * @return 傳回音訊的長度
     */
    @Override
    public long getAudioLength() {
        return (long) (clip.getTotalDuration().toMillis() * 1000);
    }

    /**
     * 取得音訊目前的位置(微秒)。
     *
     * @return 傳回音訊目前的位置
     */
    @Override
    public long getAudioPosition() {
        return (long) (clip.getCurrentTime().toMillis() * 1000);
    }

    /**
     * 設定音訊的位置(微秒)。
     *
     * @param position 傳入音訊的位置
     *
     */
    @Override
    public void setAudioPosition(final long position) {
        clip.seek(Duration.millis(position / 1000f));
    }

    /**
     * 關閉音訊。
     */
    @Override
    public void close() {
        playing = false;
        clip.dispose();
    }

    /**
     * 設定播放結束後是否自動關閉。
     *
     * @param autoClose 傳入播放結束後是否自動關閉
     */
    @Override
    public void setAutoClose(final boolean autoClose) {
        this.autoClose = autoClose;
    }

    /**
     * 取得播放結束後是否自動關閉。
     *
     * @return 傳回播放結束後是否自動關閉
     */
    @Override
    public boolean isAutoClose() {
        return autoClose;
    }

    /**
     * 設定狀態改變後的監聽事件。
     *
     * @param listener 傳入狀態改變的監聽事件
     */
    @Override
    public void setStatusChangedListener(final StatusChangedListener listener) {
        this.statusListener = listener;
    }

    /**
     * 取得狀態改變後的監聽事件。
     *
     * @return 傳回狀態改變後的監聽事件
     */
    @Override
    public StatusChangedListener getStatusChangedListener() {
        return statusListener;
    }
}

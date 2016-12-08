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
import java.net.URL;

/**
 * 聲音播放器。
 *
 * @author Magic Len
 */
public interface AudioPlayer {

    // -----介面列舉-----
    /**
     * 音訊播放器的狀態。OPEN：聲音被載入的時候；START：聲音播放的時候；STOP：聲音停止播放的時候；STOP：聲音資源被釋放的時候。
     */
    public static enum Status {

        OPEN, START, STOP, CLOSE;
    }

    // -----介面介面-----
    /**
     * 狀態改變監聽者。
     */
    public static interface StatusChangedListener {

        /**
         * 當播放器狀態改變時。
         *
         * @param before 改變前的狀態
         * @param current 改變後，也就是目前的狀態
         */
        public void statusChanged(final Status before, final Status current);
    }

    // -----介面預設方法-----
    /**
     * 建立聲音播放器。
     *
     * @param file 傳入聲音檔案
     * @return 傳回合適的聲音播放器
     */
    static AudioPlayer createPlayer(final File file) {
        try {
            return new TraditionalPlayer(file);
        } catch (final Exception ex) {
            return new FXPlayer(file);
        }
    }

    /**
     * 建立聲音播放器。
     *
     * @param url 傳入聲音URL
     * @return 傳回合適的聲音播放器
     */
    static AudioPlayer createPlayer(final URL url) {
        try {
            return new TraditionalPlayer(url);
        } catch (final Exception ex) {
            return new FXPlayer(url);
        }
    }

    /**
     * 建立聲音播放器。
     *
     * @param str 傳入聲音URL String
     * @return 傳回合適的聲音播放器
     */
    static AudioPlayer createPlayer(final String str) {
        try {
            return new TraditionalPlayer(str);
        } catch (final Exception ex) {
            return new FXPlayer(str);
        }
    }

    /**
     * 將音量換算為dB。
     *
     * @param volume 傳入音量，範圍是0~100
     * @return dB單位的音量
     */
    static float volumeToDB(final int volume) {
        return (float) (Math.log10(volume * 0.039) * 10);
    }

    // -----介面方法-----
    /**
     * 開始播放音訊，可以回復暫停時的狀態。
     */
    public void play();

    /**
     * 重頭開始播放音訊。
     */
    default void playOver() {
        setAudioPosition(0);
        play();
    }

    /**
     * 判斷是否正在播放中。
     *
     * @return 傳回是否正在播放中
     */
    public boolean isPlaying();

    /**
     * 等待播放暫停、停止或中止。
     *
     * @param checkInterval 檢查間隔時間(毫秒)
     */
    default void waitForPlaying(final int checkInterval) {
        if (checkInterval < 20 || checkInterval > 10000) {
            throw new RuntimeException("The checking time interval must be at least 20(ms) and at most 10000(ms)!");
        }
        try {
            while (isPlaying()) {
                Thread.sleep(checkInterval);
            }
        } catch (final Exception ex) {
            throw new RuntimeException("There are some problems when waiting for playing. Exception: ".concat(ex.getMessage()));
        }
    }

    /**
     * 等待播放暫停、停止或中止。
     */
    default void waitForPlaying() {
        waitForPlaying(200);
    }

    /**
     * 暫停播放音訊。
     */
    public void pause();

    /**
     * 停止播放音訊，下次播放將會重頭開始。
     */
    public void stop();

    /**
     * 設定播放次數，0為無限次播放。
     *
     * @param playCount 傳入播放次數
     */
    public void setPlayCount(final int playCount);

    /**
     * 設定最大音量。
     */
    default void fullPower() {
        setVolume(100);
    }

    /**
     * 設定一半音量。
     */
    default void halfPower() {
        setVolume(50);
    }

    /**
     * 設定靜音。
     */
    default void mute() {
        setVolume(0);
    }

    /**
     * 是否靜音。
     *
     * @return 傳回是否靜音
     */
    default boolean isMute() {
        return getVolume() == 0;
    }

    /**
     * 設定音量，範圍是0~100，數值愈大愈大聲。
     *
     * @param volume 傳入音量
     */
    public void setVolume(final int volume);

    /**
     * 取得音量。
     *
     * @return 傳回音量
     */
    public int getVolume();

    /**
     * 取得目前音訊播放器的狀態。
     *
     * @return 傳回狀態
     */
    public Status getStatus();

    /**
     * 設定聲道音量的平衡，範圍-100~100，數值愈大愈靠近右邊，0為平衡狀態。
     *
     * @param balance 傳入聲道音量的平衡值
     */
    public void setBalance(final int balance);

    /**
     * 取得聲道音量的平衡值。
     *
     * @return 傳回聲道音量的平衡值
     */
    public int getBalance();

    /**
     * 只開啟右聲道。
     */
    default void onlyRight() {
        setBalance(100);
    }

    /**
     * 是否只開啟右聲道
     *
     * @return 傳回是否只開啟右聲道
     */
    default boolean isOnlyRight() {
        return getBalance() == 100;
    }

    /**
     * 只開啟左聲道。
     */
    default void onlyLeft() {
        setBalance(-100);
    }

    /**
     * 是否只開啟左聲道
     *
     * @return 傳回是否只開啟左聲道
     */
    default boolean isOnlyLeft() {
        return getBalance() == -100;
    }

    /**
     * 設定聲道音量為平衡狀態。
     */
    default void balance() {
        setBalance(0);
    }

    /**
     * 聲道音量是否為平衡狀態。
     *
     * @return 傳回聲道音量是否為平衡狀態
     */
    default boolean isBalance() {
        return getBalance() == 0;
    }

    /**
     * 取得音訊的長度(微秒)。
     *
     * @return 傳回音訊的長度
     */
    public long getAudioLength();

    /**
     * 取得音訊目前的位置(微秒)。
     *
     * @return 傳回音訊目前的位置
     */
    public long getAudioPosition();

    /**
     * 設定音訊的位置(微秒)。
     *
     * @param position 傳入音訊的位置
     *
     */
    public void setAudioPosition(final long position);

    /**
     * 關閉音訊。
     */
    public void close();

    /**
     * 設定播放結束後是否自動關閉。
     *
     * @param autoClose 傳入播放結束後是否自動關閉
     */
    public void setAutoClose(final boolean autoClose);

    /**
     * 取得播放結束後是否自動關閉。
     *
     * @return 傳回播放結束後是否自動關閉
     */
    public boolean isAutoClose();

    /**
     * 設定狀態改變後的監聽事件。
     *
     * @param listener 傳入狀態改變的監聽事件
     */
    public void setStatusChangedListener(final StatusChangedListener listener);

    /**
     * 取得狀態改變後的監聽事件。
     *
     * @return 傳回狀態改變後的監聽事件
     */
    public StatusChangedListener getStatusChangedListener();
}

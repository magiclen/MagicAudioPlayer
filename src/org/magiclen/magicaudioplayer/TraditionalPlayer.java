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
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;

/**
 * 傳統的聲音播放器，支援WAV、AIFF、AU等未壓縮格式的音訊。
 *
 * @author Magic Len
 */
public class TraditionalPlayer implements AudioPlayer {

    //-----物件變數-----
    private AudioInputStream audioInputStream;
    private AudioFormat audioFormat;
    private DataLine.Info dataLineInfo;
    private Clip clip;
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
    public TraditionalPlayer(final File file) {
        try {
            final URL url = file.getAbsoluteFile().toURI().toURL();
            init(url);
        } catch (final Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    /**
     * 建構子，傳入URL。
     *
     * @param url 傳入聲音URL
     */
    public TraditionalPlayer(final URL url) {
        try {
            init(url);
        } catch (final Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    /**
     * 建構子，傳入URL String
     *
     * @param str 傳入聲音URL String
     */
    public TraditionalPlayer(final String str) {
        try {
            final URL url = URI.create(str).toURL();
            init(url);
        } catch (final Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    // -----物件方法-----
    /**
     * 初始化。
     *
     * @param url 傳入聲音URL
     * @throws Exception 拋出例外
     */
    private void init(final URL url) throws Exception {
        //讀取音樂輸入串流
        try {
            audioInputStream = AudioSystem.getAudioInputStream(url);
        } catch (final Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
        //進行播放設定
        audioFormat = audioInputStream.getFormat();
        int bufferSize = (int) Math.min(audioInputStream.getFrameLength() * audioFormat.getFrameSize(), Integer.MAX_VALUE); //緩衝大小，如果音訊檔案不大，可以全部存入緩衝空間。這個數值應該要按照用途來決定
        dataLineInfo = new DataLine.Info(Clip.class, audioFormat, bufferSize);
        clip = (Clip) AudioSystem.getLine(dataLineInfo);
        clip.addLineListener(e -> {
            final LineEvent.Type type = e.getType();
            final Status preStatus = status;
            if (type.equals(LineEvent.Type.START)) {
                status = Status.START;
            } else if (type.equals(LineEvent.Type.STOP)) {
                boolean stop = true;
                if (clip.getMicrosecondPosition() == clip.getMicrosecondLength()) {
                    clip.setMicrosecondPosition(0);
                    if (playing && playCount == 0 || (playCount > 0 && playCountBuffer < playCount)) {
                        ++playCountBuffer;
                        clip.start();
                        stop = false;
                    }
                }
                status = Status.STOP;
                if (stop) {
                    playing = false;
                    playCountBuffer = 1;
                    if (autoClose) {
                        clip.close();
                    }
                }
            } else if (type.equals(LineEvent.Type.OPEN)) {
                status = Status.OPEN;
            } else if (type.equals(LineEvent.Type.CLOSE)) {
                status = Status.CLOSE;
            } else {
                return;
            }
            if (statusListener != null) {
                statusListener.statusChanged(preStatus, status);
            }
        });
        clip.open(audioInputStream);
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
            clip.start();
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
            clip.stop();
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
            while (clip.getMicrosecondPosition() != 0) {
                clip.setMicrosecondPosition(0);
            }
        } else {
            clip.setMicrosecondPosition(0);
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
        final FloatControl floatControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        final float db = AudioPlayer.volumeToDB(volume);
        floatControl.setValue(db);
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
            final FloatControl floatControl = (FloatControl) clip.getControl(FloatControl.Type.PAN);
            final float pan = balance / 100.0f;
            floatControl.setValue(pan);
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
        return clip.getMicrosecondLength();
    }

    /**
     * 取得音訊目前的位置(微秒)。
     *
     * @return 傳回音訊目前的位置
     */
    @Override
    public long getAudioPosition() {
        return clip.getMicrosecondPosition();
    }

    /**
     * 設定音訊的位置(微秒)。
     *
     * @param position 傳入音訊的位置
     *
     */
    @Override
    public void setAudioPosition(final long position) {
        clip.setMicrosecondPosition(position);
    }

    /**
     * 關閉音訊。
     */
    @Override
    public void close() {
        playing = false;
        clip.close();
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

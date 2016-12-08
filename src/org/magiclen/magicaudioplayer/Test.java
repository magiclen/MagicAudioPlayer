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
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * <p>
 * 測試用的JavaFX應用程式。</p>
 *
 * <p>
 * 在命令列傳入一個或兩個參數：第一個參數為要播放的聲音檔案路徑；第二個參數為各項功能測試的間隔時間(毫秒)。</p>
 *
 * @author Magic Len
 */
public class Test extends Application {

    static String[] args;

    public static void main(final String[] args) {
        Test.args = args;
        launch(args);
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        if (args == null || args.length == 0) {
            Platform.exit();
            return;
        }

        File testFile;
        try {
            testFile = new File(args[0]);
        } catch (final Exception ex) {
            ex.printStackTrace(System.out);
            Platform.exit();
            return;
        }
        final File fixedTextFile = testFile;
        System.out.println("Test file: ".concat(fixedTextFile.getAbsolutePath()));

        int waitingTime;
        try {
            waitingTime = Integer.parseInt(args[1]);
            if (waitingTime <= 0) {
                throw new Exception();
            }
        } catch (final Exception ex) {
            waitingTime = 2000;
        }
        final int fixedWaitingTime = waitingTime;
        System.out.println("Waiting time: ".concat(String.valueOf(fixedWaitingTime)).concat("ms"));

        new Thread(() -> {
            final AudioPlayer player;
            try {
                player = AudioPlayer.createPlayer(fixedTextFile);
            } catch (final Exception ex) {
                ex.printStackTrace(System.out);
                Platform.exit();
                return;
            }
            System.out.println("Player class: ".concat(player.getClass().getCanonicalName()));
            player.setPlayCount(0);

            System.out.println("play");
            player.play();
            new Thread(() -> {
                try {
                    Thread.sleep(fixedWaitingTime);
                } catch (final Exception ex) {

                }
                System.out.println("pause");
                player.pause();
                try {
                    Thread.sleep(fixedWaitingTime);
                } catch (final Exception ex) {

                }
                System.out.println("play");
                player.play();
                try {
                    Thread.sleep(fixedWaitingTime);
                } catch (final Exception ex) {

                }
                System.out.println("pause");
                player.pause();
                try {
                    Thread.sleep(fixedWaitingTime);
                } catch (final Exception ex) {

                }
                System.out.println("playOver");
                player.playOver();
                try {
                    Thread.sleep(fixedWaitingTime);
                } catch (final Exception ex) {

                }
                System.out.println("stop");
                player.stop();
                try {
                    Thread.sleep(fixedWaitingTime);
                } catch (final Exception ex) {

                }
                System.out.println("play(loop forever)");
                player.play();
                player.waitForPlaying();
            }).start();
            player.waitForPlaying();
            System.out.println("(caller thread end)");
        }).start();
    }
}

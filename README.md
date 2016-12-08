MagicAudioPlayer
=================================

# Introduction

**MagicAudioPlayer** is a Java library used for playing audio in Java programs including JavaFX application. It supports such uncompressed audio formats as WAV, AIFF, AU and raw PCM. Moreover, if you use it in your JavaFX application, it **may** also support compressed audio formats like MP3 and AAC.

# Usage

## AudioPlayer interface

**AudioPlayer** interface is in the *org.magiclen.magicaudioplayer* package. It can help you create and control your audio player.

### Initialize

To create an **AudioPlayer** instance, you can use `new` operator to create the object of its subclass: **TraditionalPlayer** and **FXPlayer**. But I recommend you to use the static method `createPlayer` in **AudioPlayer** interface because you don't need to know what type of instance it is.

The audio source can be a file or an url.

For example, to load an audio file whose path is `/home/magiclen/test.wav`, you can write this code as below,

    File audioFile = new File("/home/magiclen/test.wav");
    AudioPlayer player = AudioPlayer.createPlayer(audioFile);

### Control

After initializing an **AudioPlayer** instance, you can use its `play` method to play it.

    player.play();

The playing of audio is asynchronous, and it will be halted immediately when the java program finishes. You can use its `waitForPlaying` method to let your program wait for its playing.

    player.waitForPlaying();
    // The code below the waiting method won't execute until the player stops or pauses playing.

There are also `pause`, `stop`, `playOver`, `setPlayCount`, `setVolume`, `setBalance`, `setAudioPosition` you can use.

### Listener

If you want to know the event of opening, starting(playing), stopping(pausing) and closing, you can use the `setStatusChangedListener` method to listen that.

    player.setStatusChangedListener((previousStatus, currentStatus) -> {
        switch (currentStatus) {
            case OPEN:
                break;
            case START:
                break;
            case STOP:
                break;
            case CLOSE:
                break;
        }
    });

### Test

To test whether your audio can be played or not, you can execute **MagicAudioPlayer** from your command line in your system.

For example, to test an audio file whose path is `/home/magiclen/test.wav`, you can run this command as below,

    java -jar MagicAudioPlayer.jar '/home/magiclen/test.wav'

If **MagicAudioPlayer** in your environment supports this audio file, you will hear the sound!

# License

    Copyright 2015-2017 magiclen.org

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

# What's More?

Please check out our web page at

https://magiclen.org/java-audio/

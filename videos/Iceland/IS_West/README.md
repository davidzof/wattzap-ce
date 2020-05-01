# ReadMe  IS_West

Creating new courses

With my installation of TTS2.07 I have found that if I create a new subcourse, the slope will no longer be in sync with the video. The problem arises because the video can only start at set points whilst the slope track can start anywhere. For those familiar with video encoding this is similar to keyframes (although it doesnt appear to be anything to do with keyframes because I made a version of this video with keyframes every 0.1 second and it was the same). Therefore, if you start a subcourse between these points the slope track will be out of sync with the video. The solution is to start new courses on the last frame before TTS would jump to a new starting point. Then everything remains in sync. If you start on the next frame the video is out of sync by 10 seconds, which goes down to zero as you move towards the next permissible start point.

As far as I can tell, this is not just a problem with my videos but with all videos including the official Tacx videos. The problem is more noticeable with my videos because I filmed at 50 frames per second at a relatively fast driving speed. This problem also arises with the simulate function in TTS3.5 but not with actually riding the videos in TTS3.2. I havent done any more testing on other TTS versions. So if you notice the videos and slope out of sync on a sub course, try making the subcourse start a bit later.

If you like this video you can email me at adam_elbourne at hotmail.com.  Finally, many thanks to my wife Iwona for helping to make this RLV.

Happy cycling

Adam

PS. The original .gpx file is also included in case anyone wants to import this into ErgoPlanet or whatever. The video file starts on the 3rd gpx point and there are cuts in the video file at gpx points: 2432-2968, 4603-4800, 5744-5917 and 6452-7507 (inclusive). The elevation data in the .gpx file is way off, I spent a long time adjusting the .pgmf to have a reasonable slope. So use the slope data out of the .pgmf file.
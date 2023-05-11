# AudioUse
音乐播放器，刚开始学习MediaCodec音频解码和AudioTrack播放时写的；
刚开始只是想写着学一下就没有提交代码，现在用着播放音乐也没有出现过什么bug，除了界面简陋了一点功能都还行；
可以搜索存储中后缀名为.mp3.cda.wav.m4a的文件，建立音乐分组和列表；
使用MediaCodec进行播放，播放时开启一个前台服务避免线程被杀死；
重复退出进入主Activity不会影响播放；
支持播放时在任意界面拖动进度条，切歌切换循环模式；

[演示视频zhuanlan.zhihu.com/p/628689881](https://zhuanlan.zhihu.com/p/628689881)

开发开始时间：2021.10.17

![播放列表](images/%E6%92%AD%E6%94%BE%E5%88%97%E8%A1%A8.jpg)

![音频分组](images/%E9%9F%B3%E9%A2%91%E5%88%86%E7%BB%84.jpg)

![音频列表](images/%E9%9F%B3%E9%A2%91%E5%88%97%E8%A1%A8.jpg)

![搜索列表](images/%E6%90%9C%E7%B4%A2%E5%88%97%E8%A1%A8.jpg)

![前台服务](images/%E5%89%8D%E5%8F%B0%E6%9C%8D%E5%8A%A1.jpg)
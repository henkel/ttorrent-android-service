ttorrent-android-service
================================================================

[![Build Status](https://travis-ci.org/henkel/ttorrent-android-service.svg?branch=master)](https://travis-ci.org/henkel/ttorrent-android-service) [![Download](https://api.bintray.com/packages/henkel/maven/ttorrent-android-service/images/download.svg) ](https://bintray.com/henkel/maven/ttorrent-android-service/_latestVersion)

Description
-----------

ttorrent-android-service is an Android BitTorrent library with strong focus on ease of use and simple integration into your projects. All requests are handled on a single worker thread in an IntentService and thus your application can perform long-running download operations in the background by design.

The implementation of the BitTorrent protocol itself is provided by [ttorrent](https://github.com/mpetazzoni/ttorrent).

How to use
----------

To get started with ttorrent-android-service in your Android project, all you need is to
declare the Gradle dependency.

```groovy
dependencies {
    compile 'de.sulaco.bittorrent:ttorrent-android-service:0.2.3'
}
```


#### Example Code

You can either use ``BitTorrentDownloadService`` directly or use ``BitTorrentDownloadManager`` that encapsulates the communication with the IntentService. More details are available in our [example projects](https://github.com/henkel/ttorrent-android-service/tree/master/examples).

```java
    BitTorrentDownloadManager bitTorrentDownloadManager = new BitTorrentDownloadManager(context);
    
    DownloadRequest request = new DownloadRequest()
        .setTorrentFile(Uri.parse( "/storage/emulated/0/test/baden-wuerttemberg.torrent"))
        .setDestinationDirectory(Uri.parse( "/storage/emulated/0/test/"));
    
    bitTorrentDownloadManager.enqueue(request);
```

License
-------

Copyright 2016 Philipp Henkel

Licensed under the Apache License, Version 2.0. See LICENSE file for more details.


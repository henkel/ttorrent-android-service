ttorrent-android-service, a BitTorrent IntentService for Android
================================================================

[![Build Status](https://travis-ci.org/henkel/ttorrent-android-service.svg?branch=master)](https://travis-ci.org/henkel/ttorrent-android-service)

Description
-----------

Android IntentService for downloading BitTorrent files. The service is based on ttorrent.


How to use
----------

To use ``ttorrent-android-service`` in your Android project, all you need is to
declare the dependency.

```groovy
dependencies {
    compile 'de.sulaco.ttorrent:ttorrent-android-service:0.2.0'
}
```



#### Example code

TODO

```java

    BitTorrentDownloadService.requestDownload(
       this, 
       "/storage/emulated/0/test/baden-wuerttemberg.torrent", 
       "/storage/emulated/0/test/");

```

License
-------

This library is distributed under the terms of the Apache Software 
License version 2.0. See LICENSE file for more details.


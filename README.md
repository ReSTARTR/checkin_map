Check-in Map
====
intro
----
This is a sample websocket application that is build with scala and jetty.
By using twitter streaming api, plot foursquare's check-in data.

screenshot
----
![check-in map](http://dl.dropbox.com/u/156025/github/check-in-map-screenshot.png)

requires
----
 * sbt 0.7.5.RC1
 * scala 2.8.1
 * twitter4j 2.2.1
 * jetty 7.3.1.v20110307
 * sjson 0.8 
 
 
build project
----

initialize the project

    git clone git@github.com:ReSTARTR/checkin_map.git
    cd checkin_map
    sbt reload update

download twitter4j and copy to lib/ 

    wget http://twitter4j.org/en/twitter4j-2.2.1.zip
    unzip ./twitter4j-2.2.1.zip
    cp ./twitter4j-2.2.1/lib/*.jar checkin_map/lib/

add your oauth keys to TwConfig

src/main/scala/servlet.scala

    val conf = new TwConfig(
      "", // CONSUMER_KEY
      "", // CONSUMER_SECRET
      "", // ACCESS_TOKEN
      "" // ACCESS_TOKEN_SECRET
    )

compile the source

    sbt compile

and if you success to compile, start servlet.

    sbt ~jetty-run

and access the URL

    http://localhost:8080/



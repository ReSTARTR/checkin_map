package com.restartr.stream

import twitter4j._
import twitter4j.conf._
import scala.collection.mutable.Set
import javax.servlet.http._
import org.eclipse.jetty.websocket._
import org.eclipse.jetty.websocket.WebSocket.Outbound
import org.eclipse.jetty.server._

case class TwConfig (
  CONSUMER_KEY: String,
  CONSUMER_SECRET: String,
  ACCESS_TOKEN: String,
  ACCESS_TOKEN_SECRET: String,
  BASE_URL: String  = "https://userstream.twitter.com/2/"
)

object Logger extends scala.actors.Actor {
  def act {
    loop {
      react {
        case msg: String => println("["+(new java.util.Date)+"]STRM:\t" + msg)
        case _ => //
      }
    }
  }
  start()
}

class MyListener extends StatusAdapter {
  override def onDeletionNotice(sdn: StatusDeletionNotice) {}
  override def onScrubGeo(userId: Long, upToStatusId: Long) {}
  override def onException(e: Exception) = Logger ! "got an exception: " + e.toString
  override def onTrackLimitationNotice(num: Int) = Logger ! "[LIMIT] " + num
  override def onStatus(status: Status) = Logger ! status.getText
}

class Stream(conf: TwConfig, filter: String) {
	val builder = new ConfigurationBuilder
	builder.setOAuthConsumerKey( conf.CONSUMER_KEY )
	builder.setOAuthConsumerSecret( conf.CONSUMER_SECRET )
	builder.setOAuthAccessToken( conf.ACCESS_TOKEN )
	builder.setOAuthAccessTokenSecret( conf.ACCESS_TOKEN_SECRET )
	builder.setUserStreamBaseURL( conf.BASE_URL )
	val buildOption = builder.build
  val stream = new TwitterStreamFactory(buildOption).getInstance
	
	def loopWithWhile(listener: StatusAdapter) {
	  Logger ! "Stream::start"
    stream.addListener(listener)
    val q = new twitter4j.FilterQuery
    q.track(Array(filter))
    val fs = stream.getFilterStream(q)
    while(true) {
      try {
        fs.next(listener)
      } catch {
        case e: twitter4j.TwitterException => println(e) //
        case e: Exception => {
          Logger ! e.toString; 
          Logger ! "caught Exception!!!"; 
        }
      }
    }
	}
	
	def loopWithFilter(listener: StatusAdapter) {
	  Logger ! "Stream::start"
    stream.addListener(listener)
    val q = new twitter4j.FilterQuery
    q.track(Array(filter))
    stream.filter(q)
	}
	
	def start(listener: StatusAdapter) {
	  loopWithFilter(listener);
	  //loopWithWhile(listener);
	}
	
	def stop = stream.shutdown() 
}


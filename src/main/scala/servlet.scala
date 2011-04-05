package com.restartr.servlet

import scala.collection.mutable.Set
import javax.servlet.http._
import org.eclipse.jetty.websocket._
import org.eclipse.jetty.websocket.WebSocket.Outbound
import org.eclipse.jetty.server._
import twitter4j.{Status,GeoLocation}
import org.eclipse.jetty.io.EofException
import sjson.json.Serializer.SJSON

import com.restartr.stream._

object Logger extends scala.actors.Actor {
  def act { loop { react {
    case msg: String => println("["+(new java.util.Date)+"]SERV:\t" + msg)
    case _ => //
  }}}
  start
}

// message type
case class Start()
case class End()
case class Message(frame:Byte, data:String)
case class Subscribe(subscriber: TwitterStreamSocket)
case class Unsubscribe(subscriber: TwitterStreamSocket)

class Geo(lat: Double, lon: Double, status: String, image_url: String) {
  def json = new String(SJSON.out(Map(
    "lat"->lat,
    "lon"->lon,
    "status"->status,
    "image_url"->image_url)))
}

class TwStatusListener extends MyListener{
  override def onStatus(status: Status) = {
    status.getGeoLocation match {
      case loc: GeoLocation => {
        Publisher ! Message(0, 
                      (new Geo(loc.getLatitude, loc.getLongitude, status.getText,
                        status.getUser.getProfileImageURL.toString)).json)
      }
      case _ => //
    }
  }
  override def onException(e: Exception) = {
    Logger ! "error and stopped"
    Publisher ! End()
  }
}

object Publisher extends scala.actors.Actor {
  val conf = new TwConfig(
    "",                             // CONSUMER_KEY
    "",        // CONSUMER_SECRET
    "",  // ACCESS_TOKEN
    ""         // ACCESS_TOKEN_SECRET
  )
  var subscribers = Set.empty[TwitterStreamSocket]
  val stream = new Stream(conf, "4sq")

  def begin = stream.start(new TwStatusListener)
  
  def countConnection = subscribers.count( _=>true )
  
  def pub(msg: Message) = 
    subscribers.foreach{ subscriber => 
      try {
        subscriber.outbound.sendMessage( msg.frame, msg.data )
      } catch {
        case e: EofException  => Logger ! "closed by browser" // closed by browser.
        case _                => Logger ! _ // 
      }}
      
  def act { loop { react {
    case msg: Start         => begin
    case sub: Subscribe     => subscribers += sub.subscriber; Logger ! "connection:"+countConnection
    case unsub: Unsubscribe => subscribers.remove(unsub.subscriber); Logger ! "connection"+countConnection
    case msg: Message       => pub(msg)
    case msg: End           => stream.stop
    case _                  => Logger ! "error@Publisher"
  }}}
  
  start
}

class TwitterStreamSocket extends WebSocket {
  var outbound:Outbound = _
  
  override def onFragment(more: Boolean, opcode: Byte, date: Array[Byte], offset: Int, length: Int) = {}
  override def onMessage(frame:Byte, data:Array[Byte], offset:Int, length:Int ) = {}
  override def onConnect(outbound:Outbound ) = {
    this.outbound = outbound
    Publisher ! Message(0, "accepted")
    Publisher ! Subscribe(this)
  }
  override def onMessage(frame:Byte, data:String ) = 
    Publisher ! Message(frame, data)
  override def onDisconnect =
    Publisher ! Unsubscribe(this)
}

class TwitterStreamServlet extends WebSocketServlet {
  Publisher ! Start()
  
  override def doGet(req:HttpServletRequest, res:HttpServletResponse ) =
    getServletContext.getNamedDispatcher("default").forward(req, res)
  
  override def doWebSocketConnect(req:HttpServletRequest, protocol:String ) = 
    new TwitterStreamSocket
}

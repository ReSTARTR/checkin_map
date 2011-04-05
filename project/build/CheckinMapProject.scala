import sbt._

class CheckinMapProject(info: ProjectInfo) extends DefaultWebProject(info) {
  //val JETTY7 = "7.0.2.v20100331"
  //val JETTY7 = "7.1.6.v20100715"
  //val JETTY7 = "7.2.2.v20101205"
  val JETTY7 = "7.3.1.v20110307"
  
  val servletapi      = "javax.servlet" % "servlet-api" % "2.5" % "compile"
  val jetty7Server    = "org.eclipse.jetty" % "jetty-server" % JETTY7 % "compile,test"
  val jetty7Servlets  = "org.eclipse.jetty" % "jetty-servlets" % JETTY7 % "compile,test"
  val jetty7Webapp    = "org.eclipse.jetty" % "jetty-webapp" % JETTY7 % "compile,test" 
  val jetty7WebSocket = "org.eclipse.jetty" % "jetty-websocket" % JETTY7 % "compile,test"
  val sjson           = "net.debasishg" % "sjson_2.8.0" % "0.8"
  val commonsLang     = "commons-lang" % "commons-lang" % "2.5"
  val specs2          = "org.specs2" %% "specs2" % "1.0.1"
  def specs2Framework = new TestFramework("org.specs2.runner.SpecsFramework")

  override def testFrameworks = super.testFrameworks ++ Seq(specs2Framework)
  val snapshots = "snapshots" at "http://scala-tools.org/repo-snapshots"
  //val releases  = "releases" at "http://scala-tools.org/repo-releases"
}

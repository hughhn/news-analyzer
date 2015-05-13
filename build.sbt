name := """news-analyzer"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

resolvers ++= Seq(
  "boilerpipe-m2-repo" at "http://boilerpipe.googlecode.com/svn/repo/",
  Resolver.sonatypeRepo("snapshots"))

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  "edu.uci.ics" % "crawler4j" % "4.1",
  "org.neo4j" % "neo4j" % "2.2.0",
  "org.apache.opennlp" % "opennlp-tools" % "1.5.3",
  "de.l3s.boilerpipe" % "boilerpipe" % "1.2.0",
  "xerces" % "xercesImpl" % "2.11.x",
  "net.sourceforge.nekohtml" % "nekohtml" % "1.9.21"
)

javaOptions ++= Seq("-Xms512M", "-Xmx8G", "-XX:MaxPermSize=4G", "-XX:-UseGCOverheadLimit")
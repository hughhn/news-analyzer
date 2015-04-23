name := """news-analyzer"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"))

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  "edu.uci.ics" % "crawler4j" % "4.1",
  "org.neo4j" % "neo4j" % "2.2.0",
  "org.apache.opennlp" % "opennlp-tools" % "1.5.3"
)

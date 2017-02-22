name := """crauth"""

version := "0.1-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, SbtWeb)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  filters,
  "org.scalikejdbc" %% "scalikejdbc"             % "2.4.2",
  "org.scalikejdbc" %% "scalikejdbc-config"      % "2.4.2",
  "org.scalikejdbc" %% "scalikejdbc-test"   % "2.4.2"   % "test",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.11" % "test",
  "org.flywaydb" %% "flyway-play" % "3.0.0",
  "com.h2database"  %  "h2"                % "1.4.191",
  "ch.qos.logback"  %  "logback-classic"   % "1.1.3",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.2.2" % "test",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "net.codingwell" %% "scala-guice" % "4.0.1",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "org.webjars" %% "webjars-play" % "2.5.0",
  "org.webjars" % "bootstrap" % "4.0.0-alpha.2",
  "org.webjars" % "font-awesome" % "4.6.3",
  "org.webjars" % "react" % "15.1.0",
  "com.pauldijou" %% "jwt-play-json" % "0.9.2",
  "org.postgresql" % "postgresql" % "9.4.1208.jre7",
  "commons-validator" % "commons-validator" % "1.5.1",
  "com.typesafe.play" %% "play-mailer" % "5.0.0",
  "com.typesafe" % "config" % "1.3.0"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

coverageEnabled in Test:= true

val testSettings = Seq(
  fork in Test := false
)
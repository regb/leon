name := "Leon"

version := "3.0"

organization := "ch.epfl.lara"

scalaVersion := "2.11.5"

scalacOptions ++= Seq(
    "-deprecation",
    "-unchecked",
    "-feature"
)

javacOptions += "-Xlint:unchecked"


if(System.getProperty("sun.arch.data.model") == "64") {
  unmanagedBase <<= baseDirectory { base => base / "unmanaged" / "64" }
} else {
  unmanagedBase <<= baseDirectory { base => base / "unmanaged" / "32" }
}

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-compiler" % "2.11.5",
    "org.scalatest" %% "scalatest" % "2.2.0" % "it,test",
    "com.typesafe.akka" %% "akka-actor" % "2.3.4"
)

Keys.fork in run := true

Keys.fork in Test := true

Keys.fork in IntegrationTest := true

logBuffered in Test := false

logBuffered in IntegrationTest := false

testOptions in Test += Tests.Argument("-oDF")

testOptions in IntegrationTest += Tests.Argument("-oDF")

javaOptions in (Test,run) ++= Seq("-Xss32M", "-Xmx3G", "-XX:MaxPermSize=1024M")

javaOptions in (IntegrationTest,run) += "-Xss32M"

parallelExecution in Test := false

parallelExecution in IntegrationTest := false

sourcesInBase in Compile := false

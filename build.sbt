scalaVersion := "2.11.1"

organization := "net.s_mach"

name := "concurrent"

version := "1.0.0"

scalacOptions ++= Seq("-feature","-unchecked", "-deprecation")

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.0" % "test"

//testOptions in Test += Tests.Argument("-l s_mach.concurrent.DelayAccuracyTest")

parallelExecution in Test := false
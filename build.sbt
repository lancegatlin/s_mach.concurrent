import scoverage.ScoverageSbtPlugin.instrumentSettings
import org.scoverage.coveralls.CoverallsPlugin.coverallsSettings

scalaVersion := "2.11.1"

organization := "net.s_mach"

name := "concurrent"

version := "0.1-SNAPSHOT"

scalacOptions ++= Seq("-feature","-unchecked", "-deprecation")

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.0" % "test"

//testOptions in Test += Tests.Argument("-l s_mach.concurrent.DelayAccuracyTest")

parallelExecution in Test := false

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

instrumentSettings

ScoverageKeys.minimumCoverage := 60

ScoverageKeys.failOnMinimumCoverage := true

ScoverageKeys.highlighting := true

coverallsSettings
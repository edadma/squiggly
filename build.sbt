import xerial.sbt.Sonatype.sonatypeCentralHost

ThisBuild / licenses               := Seq("ISC" -> url("https://opensource.org/licenses/ISC"))
ThisBuild / versionScheme          := Some("semver-spec")
ThisBuild / evictionErrorLevel     := Level.Warn
ThisBuild / scalaVersion           := "3.8.4"
ThisBuild / organization           := "io.github.edadma"
ThisBuild / organizationName       := "edadma"
ThisBuild / organizationHomepage   := Some(url("https://github.com/edadma"))
ThisBuild / version                := "0.3.2"
ThisBuild / description            := "A Scala 3 string templating engine inspired by Hugo and Liquid"
ThisBuild / sonatypeCredentialHost := sonatypeCentralHost

ThisBuild / publishConfiguration := publishConfiguration.value.withOverwrite(true).withChecksums(Vector.empty)
ThisBuild / resolvers += Resolver.mavenLocal
ThisBuild / resolvers += Resolver.sonatypeCentralSnapshots
ThisBuild / resolvers += Resolver.sonatypeCentralRepo("releases")

ThisBuild / sonatypeProfileName := "io.github.edadma"

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/edadma/squiggly"),
    "scm:git@github.com:edadma/squiggly.git",
  ),
)
ThisBuild / developers := List(
  Developer(
    id = "edadma",
    name = "Edward A. Maxedon, Sr.",
    email = "edadma@gmail.com",
    url = url("https://github.com/edadma"),
  ),
)

ThisBuild / homepage := Some(url("https://github.com/edadma/squiggly"))

ThisBuild / publishTo := sonatypePublishToBundle.value

lazy val squiggly = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .in(file("."))
  .settings(
    name := "squiggly",
    scalacOptions ++=
      Seq(
        "-deprecation",
        "-feature",
        "-unchecked",
        "-language:postfixOps",
        "-language:implicitConversions",
        "-language:existentials",
        "-language:dynamics",
      ),
    libraryDependencies ++= Seq(
      "org.scalatest"          %%% "scalatest"                % "3.2.19" % "test",
      "com.github.scopt"       %%% "scopt"                    % "4.1.0",
      "com.lihaoyi"            %%% "pprint"                   % "0.9.0"  % "test",
      "org.scala-lang.modules" %%% "scala-parser-combinators" % "2.4.0",
      "io.github.edadma"       %%% "char_reader" % "0.1.30",
      "io.github.edadma"       %%% "cross_platform" % "0.1.9",
      "io.github.edadma"       %%% "markdown" % "0.4.9",
      "io.github.edadma"       %%% "emoji" % "0.1.4",
      "dev.zio"                %%% "zio-json"                 % "0.7.42",
    ),
    publishMavenStyle      := true,
    Test / publishArtifact := false,
  )
  .jvmSettings(
    libraryDependencies += "org.scala-js" %% "scalajs-stubs" % "1.1.0" % "provided",
  )
  .nativeSettings(
    libraryDependencies += "org.scala-js" %% "scalajs-stubs" % "1.1.0" % "provided",
    // scala-java-time provides java.time on Scala Native; the en-US CLDR
    // locale data (separate jar from the scala-java-locales API) is what
    // makes DateTimeFormatter resolve month/day names — without it, MMM /
    // MMMM / EEEE render as numeric stubs ("M03" instead of "Mar").
    libraryDependencies += "io.github.cquiroz" %%% "scala-java-time"          % "2.6.0",
    libraryDependencies += "io.github.cquiroz" %%% "locales-minimal-en_us-db" % "1.5.4",
  )
  .jsSettings(
    jsEnv := new org.scalajs.jsenv.nodejs.NodeJSEnv(),
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
    scalaJSLinkerConfig ~= { _.withSourceMap(false) },
    Test / scalaJSUseMainModuleInitializer := false,
    Test / scalaJSUseTestModuleInitializer := true,
    scalaJSUseMainModuleInitializer        := true,
    libraryDependencies += "io.github.cquiroz" %%% "scala-java-time"          % "2.6.0",
    libraryDependencies += "io.github.cquiroz" %%% "locales-minimal-en_us-db" % "1.5.4",
  )

lazy val root = project
  .in(file("."))
  .aggregate(squiggly.jvm, squiggly.js, squiggly.native)
  .settings(
    name                := "squiggly",
    publish / skip      := true,
    publishLocal / skip := true,
  )

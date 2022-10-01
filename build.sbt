ThisBuild / licenses += "ISC" -> url("https://opensource.org/licenses/ISC")
ThisBuild / versionScheme := Some("semver-spec")

publish / skip := true

lazy val squiggly = crossProject( /*JSPlatform,*/ JVMPlatform /*, NativePlatform*/ )
  .in(file("."))
  .settings(
    name := "squiggly",
    version := "0.1.16",
    scalaVersion := "3.2.0",
    scalacOptions ++=
      Seq(
        "-deprecation",
        "-feature",
        "-unchecked",
        "-language:postfixOps",
        "-language:implicitConversions",
        "-language:existentials",
        "-language:dynamics",
        "-Xasync",
      ),
    organization := "io.github.edadma",
    githubOwner := "edadma",
    githubRepository := name.value,
    resolvers += Resolver.githubPackages("edadma"),
    mainClass := Some(s"${organization.value}.${name.value}.Main"),
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.2.12" % "test",
    libraryDependencies ++= Seq(
      "io.github.edadma" %%% "cross-platform" % "0.1.5",
      "io.github.edadma" %%% "char-reader" % "0.1.11",
      "io.github.edadma" %%% "datetime" % "0.1.17",
//      "io.github.edadma" %%% "commonmark" % "0.1.0-pre.20",
      "io.github.edadma" %%% "emoji" % "0.1.1",
    ),
    libraryDependencies ++= Seq(
      "com.github.scopt" %%% "scopt" % "4.0.1",
      "com.lihaoyi" %%% "pprint" % "0.7.3" % "test",
      "org.scala-lang.modules" %%% "scala-parser-combinators" % "2.1.1",
      // "org.parboiled" %%% "parboiled" % "2.4.0",
    ),
    publishMavenStyle := true,
    Test / publishArtifact := false,
    licenses += "ISC" -> url("https://opensource.org/licenses/ISC"),
  )
  .jvmSettings(
    libraryDependencies += "org.scala-js" %% "scalajs-stubs" % "1.1.0" % "provided",
    libraryDependencies += "io.github.edadma" %% "yaml" % "0.1.12",
  )
/*.nativeSettings(
    nativeLinkStubs := true,
    libraryDependencies += "io.github.edadma" %%% "libyaml" % "0.1.8"
  )*/
/*.jsSettings(
    jsEnv := new org.scalajs.jsenv.nodejs.NodeJSEnv(),
    //    Test / scalaJSUseMainModuleInitializer := true,
    //    Test / scalaJSUseTestModuleInitializer := false,
    Test / scalaJSUseMainModuleInitializer := false,
    Test / scalaJSUseTestModuleInitializer := true,
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies += "io.github.edadma" %%% "yaml" % "0.1.11"
  )*/

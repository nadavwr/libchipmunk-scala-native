lazy val commonSettings = Def.settings(
  scalaVersion := "2.11.8",
  organization := "com.github.nadavwr",
  version := "0.1.0-SNAPSHOT",
  publishArtifact in (Compile, packageDoc) := false
)

lazy val unpublished = Def.settings(
  publish := {},
  publishLocal := {},
  publishM2 := {}
)

lazy val `libchipmunk-scala-native` = project
  .enablePlugins(ScalaNativePlugin)
  .settings(
    commonSettings,
    resolvers += Resolver.bintrayRepo("nadavwr", "maven"),
    libraryDependencies += "com.github.nadavwr" %%% "libffi-scala-native" % "0.3.1"
  )

lazy val sample = project
  .enablePlugins(ScalaNativePlugin)
  .settings(
    commonSettings,
    unpublished,
    libraryDependencies += "com.github.nadavwr" %%% "makeshift" % "0.1.0"
  )
  .dependsOn(`libchipmunk-scala-native`)

lazy val `libchipmunk-scala-native-root` = (project in file("."))
  .aggregate(`libchipmunk-scala-native`, sample)
  .settings(
    commonSettings,
    unpublished,
    run := { (run in sample).evaluated }
  )


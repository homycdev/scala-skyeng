name := "scp2020"

organization := "skyeng"

version := "0.1"

scalaVersion := "2.13.3"

resolvers += Resolver.sonatypeRepo("snapshots")
resolvers += Resolver.bintrayIvyRepo("eed3si9n", "sbt-plugins")
resolvers += "Flyway" at "https://flywaydb.org/repo"

val CatsVersion = "2.2.0"
val CirceVersion = "0.13.0"
val CirceGenericExVersion = "0.13.0"
val CirceConfigVersion = "0.8.0"
val DoobieVersion = "0.9.0"
val EnumeratumCirceVersion = "1.6.1"

val KindProjectorVersion = "0.11.0"

val Http4sVersion = "0.21.9"

val LogbackVersion = "1.2.3"
val FlywayVersion = "7.2.0"

val Slf4jVersion = "1.7.30"

// testing
val ScalaCheckVersion = "1.15.1"
val ScalaTestVersion = "3.2.3"
val ScalaTestPlusVersion = "3.2.2.0"

//val Tapir = "0.16.16"
//val STTP = "2.2.8"

// cryptography
val TsecVersion = "0.2.1"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % CatsVersion,
  "io.circe" %% "circe-generic" % CirceVersion,
  "io.circe" %% "circe-literal" % CirceVersion,
  "io.circe" %% "circe-generic-extras" % CirceGenericExVersion,
  "io.circe" %% "circe-parser" % CirceVersion,
  "io.circe" %% "circe-config" % CirceConfigVersion,
  // Start with this one
  "org.tpolecat" %% "doobie-core" % DoobieVersion,
  // And add any of these as needed
  "org.tpolecat" %% "doobie-postgres-circe" % DoobieVersion, // H2 driver 1.4.200 + type mappings.
  "org.tpolecat" %% "doobie-hikari" % DoobieVersion, // HikariCP transactor.
  "org.tpolecat" %% "doobie-postgres" % DoobieVersion, // Postgres driver 42.2.12 + type mappings.
  "org.tpolecat" %% "doobie-quill" % DoobieVersion, // Support for Quill 3.5.1
  "org.tpolecat" %% "doobie-specs2" % DoobieVersion % "test", // Specs2 support for typechecking statements.
  "org.tpolecat" %% "doobie-scalatest" % DoobieVersion % "test", // ScalaTest support for typechecking statements.
  "com.beachape" %% "enumeratum-circe" % EnumeratumCirceVersion,
  "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % Http4sVersion % Test,
  "org.http4s" %% "http4s-circe" % Http4sVersion,
  "org.http4s" %% "http4s-dsl" % Http4sVersion,
  "ch.qos.logback" % "logback-classic" % LogbackVersion,
  "org.postgresql" % "postgresql" % "42.2.16",
  "org.flywaydb" % "flyway-core" % FlywayVersion,
  "org.scalacheck" %% "scalacheck" % ScalaCheckVersion % Test,
  "org.scalatest" %% "scalatest" % ScalaTestVersion % Test,
  "org.scalatestplus" %% "scalacheck-1-14" % ScalaTestPlusVersion % Test,
  // Authentication dependencies
  "io.github.jmcardon" %% "tsec-common" % TsecVersion,
  "io.github.jmcardon" %% "tsec-password" % TsecVersion,
  "io.github.jmcardon" %% "tsec-mac" % TsecVersion,
  "io.github.jmcardon" %% "tsec-signatures" % TsecVersion,
  "io.github.jmcardon" %% "tsec-jwt-mac" % TsecVersion,
  "io.github.jmcardon" %% "tsec-jwt-sig" % TsecVersion,
  "io.github.jmcardon" %% "tsec-http4s" % TsecVersion
)
dependencyOverrides += "org.slf4j" % "slf4j-api" % Slf4jVersion

addCompilerPlugin(
  ("org.typelevel" %% "kind-projector" % KindProjectorVersion)
    .cross(CrossVersion.full)
)
enablePlugins(
  ScalafmtPlugin,
  JavaAppPackaging,
  GhpagesPlugin,
  MicrositesPlugin,
  TutPlugin,
  AssemblyPlugin
)
enablePlugins(DockerPlugin, JavaAppPackaging, AshScriptPlugin)

//packageName in Docker := packageName.value
//dockerBaseImage in Docker := "openjdk:8-jre-alpine"
dockerUpdateLatest := true
//version in Docker := version.value

//dockerfile in docker := {
//  val appDir = stage.value
//  val targetDir = "/app"
//
//  new Dockerfile {
//    from("openjdk:8-jre")
//    entryPoint(s"$targetDir/bin/${executableScriptName.value}")
//    copy(appDir, targetDir)
//  }
//}
//
//buildOptions in docker := BuildOptions(
//  cache = false,
//  removeIntermediateContainers = BuildOptions.Remove.Always,
//  pullBaseImage = BuildOptions.Pull.Always,
//)

// Note: This fixes error with sbt run not loading config properly
fork in run := true

dockerExposedPorts ++= Seq(8080)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case _                             => MergeStrategy.first
}


name := "SCP2020"

version := "0.1"

crossScalaVersions := Seq("2.12.12", "2.13.3")


resolvers += Resolver.sonatypeRepo("snapshots")
resolvers += Resolver.bintrayIvyRepo("eed3si9n", "sbt-plugins")
resolvers += "Flyway" at "https://flywaydb.org/repo"

val CatsVersion = "2.2.0"
val CirceVersion = "0.13.0"
val CirceGenericExVersion = "0.13.0"
val CirceConfigVersion = "0.8.0"
val DoobieVersion = "0.9.2"
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

  "org.tpolecat" %% "doobie-core" % DoobieVersion,
  "org.tpolecat" %% "doobie-postgres" % DoobieVersion,
  "org.tpolecat" %% "doobie-scalatest" % DoobieVersion,
  "org.tpolecat" %% "doobie-hikari" % DoobieVersion,

  "com.beachape" %% "enumeratum-circe" % EnumeratumCirceVersion,

  "org.http4s"      %% "http4s-blaze-server" % Http4sVersion,
  "org.http4s"      %% "http4s-blaze-client" % Http4sVersion % Test,
  "org.http4s"      %% "http4s-circe"        % Http4sVersion,
  "org.http4s"      %% "http4s-dsl"          % Http4sVersion,

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
  "io.github.jmcardon" %% "tsec-http4s" % TsecVersion,



  //tapir

//  "com.softwaremill.sttp.tapir" %% "tapir-core"               %  Tapir,
//  "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs"       %  Tapir,
//  "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe"      %  Tapir,
//  "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml" %  Tapir,
//  "com.softwaremill.sttp.tapir" %% "tapir-sttp-client"        %  Tapir,
//  "com.softwaremill.sttp.tapir" %% "tapir-http4s-server"      %  Tapir,
//  "com.softwaremill.sttp.tapir" %% "tapir-json-circe"         %  Tapir,
//  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-http4s"  %  Tapir,
//
//  // STTP
//  "com.softwaremill.sttp.client" %% "async-http-client-backend-zio" % STTP,
//  "com.softwaremill.sttp.client" %% "circe"                         % STTP,
)
dependencyOverrides += "org.slf4j" % "slf4j-api" % Slf4jVersion


addCompilerPlugin(
  ("org.typelevel" %% "kind-projector" % KindProjectorVersion).cross(CrossVersion.full),
)
enablePlugins(ScalafmtPlugin, JavaAppPackaging, GhpagesPlugin, MicrositesPlugin, TutPlugin, DockerPlugin, AssemblyPlugin)

// Note: This fixes error with sbt run not loading config properly
fork in run := true

dockerExposedPorts ++= Seq(8080)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case _ => MergeStrategy.first
}
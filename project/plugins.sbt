// Makes our code tidy
//addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.0")

// Revolver allows us to use re-start and work a lot faster!
addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")

// Native Packager allows us to create standalone jar
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.7.6")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.15.0")

//addSbtPlugin("se.marcuslonnberg" % "sbt-docker" % "1.8.0")

//// Documentation plugins
//addSbtPlugin("org.tpolecat" % "tut-plugin" % "0.6.13")
//
//addSbtPlugin("com.47deg" % "sbt-microsites" % "1.2.1")
//
//addSbtPlugin("com.typesafe.sbt" % "sbt-ghpages" % "0.6.3")

// Easily manage scalac settings across scala versions with this:
addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.1.15")

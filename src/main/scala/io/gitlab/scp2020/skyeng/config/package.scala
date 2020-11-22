package io.gitlab.scp2020.skyeng

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

package object config {
  implicit val serverDecoder: Decoder[ServerConfig] = deriveDecoder
  implicit val dbConnectionDecoder: Decoder[DatabaseConnectionsConfig] = deriveDecoder
  implicit val dbDecoder: Decoder[DatabaseConfig] = deriveDecoder
  implicit val skyengDecoder: Decoder[SkyEngConfig] = deriveDecoder

}

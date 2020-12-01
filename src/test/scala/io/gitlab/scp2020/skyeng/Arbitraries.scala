package io.gitlab.scp2020.skyeng

import java.time.Instant

import cats.effect.IO
import io.gitlab.scp2020.skyeng.domain.authentication.SignupRequest
import io.gitlab.scp2020.skyeng.domain.users.{Role, _}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck._
import tsec.authentication.AugmentedJWT
import tsec.common.SecureRandomId
import tsec.jws.mac._
import tsec.jwt.JWTClaims
import tsec.mac.jca._

trait SkyEngArbitraries {
  val userNameLength = 16
  val userNameGen: Gen[String] = Gen.listOfN(userNameLength, Gen.alphaChar).map(_.mkString)
  val optionString: Gen[Option[String]] = Gen.some(arbitrary[String])

  implicit val instant: Arbitrary[Instant] = Arbitrary[Instant] {
    for {
      millis <- Gen.posNum[Long]
    } yield Instant.ofEpochMilli(millis)
  }

  implicit val role: Arbitrary[Role] = Arbitrary[Role](Gen.oneOf(Role.values.toIndexedSeq))

  implicit val user: Arbitrary[User] = Arbitrary[User] {
    for {
      userName <- userNameGen
      firstName <- optionString
      lastName <- optionString
      birthDate <- optionString
      gender <- optionString
      email <- arbitrary[String]
      password <- arbitrary[String]
      phone <- optionString
      role <- arbitrary[Role]
      id <- Gen.option(Gen.posNum[Long])

    } yield User(
      userName,
      firstName,
      lastName,
      birthDate,
      gender,
      email,
      password,
      phone,
      role,
        id = id)

  }

  case class AdminUser(value: User)

  case class CustomerUser(value: User)

  implicit val adminUser: Arbitrary[AdminUser] = Arbitrary {
    user.arbitrary.map(user => AdminUser(user.copy(role = Role.Admin)))
  }

  implicit val customerUser: Arbitrary[CustomerUser] = Arbitrary {
    user.arbitrary.map(user => CustomerUser(user.copy(role = Role.Student)))
  }

  implicit val userSignup: Arbitrary[SignupRequest] = Arbitrary[SignupRequest] {
    for {
      userName <- userNameGen
      firstName <- optionString
      lastName <- optionString
      birthDate <- optionString
      gender <- optionString
      email <- arbitrary[String]
      password <- arbitrary[String]
      phone <- optionString
      role <- arbitrary[Role]

    } yield SignupRequest(
      userName,
      firstName,
      lastName,
      birthDate,
      gender,
      email,
      password,
      phone,
      role,
      )
  }

  implicit val secureRandomId: Arbitrary[SecureRandomId] = Arbitrary[SecureRandomId] {
    arbitrary[String].map(SecureRandomId.apply)
  }

  implicit val jwtMac: Arbitrary[JWTMac[HMACSHA256]] = Arbitrary {
    for {
      key <- Gen.const(HMACSHA256.unsafeGenerateKey)
      claims <- Gen.finiteDuration.map(exp =>
        JWTClaims.withDuration[IO](expiration = Some(exp)).unsafeRunSync(),
      )
    } yield JWTMacImpure
      .build[HMACSHA256](claims, key)
      .getOrElse(throw new Exception("Inconceivable"))
  }

  implicit def augmentedJWT[A, I](implicit
                                  arb1: Arbitrary[JWTMac[A]],
                                  arb2: Arbitrary[I],
                                 ): Arbitrary[AugmentedJWT[A, I]] =
    Arbitrary {
      for {
        id <- arbitrary[SecureRandomId]
        jwt <- arb1.arbitrary
        identity <- arb2.arbitrary
        expiry <- arbitrary[Instant]
        lastTouched <- Gen.option(arbitrary[Instant])
      } yield AugmentedJWT(id, jwt, identity, expiry, lastTouched)
    }
}

object SkyEngArbitraries extends SkyEngArbitraries

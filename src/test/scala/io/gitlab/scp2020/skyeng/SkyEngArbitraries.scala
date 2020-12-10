package io.gitlab.scp2020.skyeng

import cats.effect.IO
import io.circe.Json
import io.gitlab.scp2020.skyeng.domain.authentication.{
  ReplenishRequest,
  SignupRequest
}
import io.gitlab.scp2020.skyeng.domain.courses.classes.{
  Homework,
  Lesson,
  LevelType
}
import io.gitlab.scp2020.skyeng.domain.courses.exercises.{
  Exercise,
  ExerciseType
}
import io.gitlab.scp2020.skyeng.domain.courses.tasks.{Task, TaskType}
import io.gitlab.scp2020.skyeng.domain.courses.{
  Course,
  CourseCategory,
  Enrollment
}
import io.gitlab.scp2020.skyeng.domain.users.student.StudentProfile
import io.gitlab.scp2020.skyeng.domain.users.teacher.{
  QualificationType,
  TeacherProfile
}
import io.gitlab.scp2020.skyeng.domain.users.{Role, _}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck._
import tsec.authentication.AugmentedJWT
import tsec.common.SecureRandomId
import tsec.jws.mac._
import tsec.jwt.JWTClaims
import tsec.mac.jca._

import java.time.{Instant, LocalDateTime}

trait SkyEngArbitraries {
  val userNameLength = 16
  val userNameGen: Gen[String] =
    Gen.listOfN(userNameLength, Gen.alphaChar).map(_.mkString)
  val optionString: Gen[Option[String]] = Gen.some(arbitrary[String])

  implicit val instant: Arbitrary[Instant] = Arbitrary[Instant] {
    for {
      millis <- Gen.posNum[Long]
    } yield Instant.ofEpochMilli(millis)
  }

  implicit val role: Arbitrary[Role] =
    Arbitrary[Role](Gen.oneOf(Role.values.toIndexedSeq))

  implicit val level: Arbitrary[LevelType] =
    Arbitrary[LevelType](Gen.oneOf(LevelType.values))

  implicit val qualificationType: Arbitrary[QualificationType] =
    Arbitrary[QualificationType](Gen.oneOf(QualificationType.values))

  implicit val taskType: Arbitrary[TaskType] =
    Arbitrary[TaskType](Gen.oneOf(TaskType.values))

  implicit val exerciseType: Arbitrary[ExerciseType] =
    Arbitrary[ExerciseType](Gen.oneOf(ExerciseType.values))

  implicit val jsonArbitrary: Arbitrary[Json] =
    Arbitrary[Json](Gen.oneOf(Seq(Json.True, Json.False)))

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
      id <- Gen.some(Gen.posNum[Long])

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
      id = id,
      created = LocalDateTime.now()
    )

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
      role
    )
  }

  implicit val secureRandomId: Arbitrary[SecureRandomId] =
    Arbitrary[SecureRandomId] {
      arbitrary[String].map(SecureRandomId.apply)
    }

  implicit val jwtMac: Arbitrary[JWTMac[HMACSHA256]] = Arbitrary {
    for {
      key <- Gen.const(HMACSHA256.unsafeGenerateKey)
      claims <- Gen.finiteDuration.map(exp =>
        JWTClaims.withDuration[IO](expiration = Some(exp)).unsafeRunSync()
      )
    } yield JWTMacImpure
      .build[HMACSHA256](claims, key)
      .getOrElse(throw new Exception("Inconceivable"))
  }

  implicit def augmentedJWT[A, I](implicit
      arb1: Arbitrary[JWTMac[A]],
      arb2: Arbitrary[I]
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

  implicit val teacher: Arbitrary[TeacherProfile] = Arbitrary[TeacherProfile] {
    for {
      id <- Gen.posNum[Long]
      bio <- arbitrary[String]
      greeting <- arbitrary[String]
      qualification <- arbitrary[QualificationType]
    } yield TeacherProfile(id, bio, greeting, qualification)
  }

  implicit val student: Arbitrary[StudentProfile] = Arbitrary[StudentProfile] {
    for {
      id <- Gen.posNum[Long]
      teacherId <- Gen.some(Gen.posNum[Long])
      balance <- Gen.posNum[Int]
    } yield StudentProfile(id, teacherId, balance)
  }

  case class AdminUser(value: User)

  case class StudentUser(value: User)

  implicit val adminUser: Arbitrary[AdminUser] = Arbitrary {
    user.arbitrary.map(user =>
      AdminUser(user.copy(role = Role.Admin, id = Some(1L)))
    )
  }

  implicit val studentUser: Arbitrary[StudentUser] = Arbitrary {
    user.arbitrary.map(user =>
      StudentUser(user.copy(role = Role.Student, id = Some(2L)))
    )
  }

  implicit val studentUser2: Arbitrary[User] = Arbitrary {
    user.arbitrary.map(user => user.copy(id = Some(3L), role = Role.Student))
  }

  implicit val courseCategory: Arbitrary[CourseCategory] =
    Arbitrary[CourseCategory] {
      for {
        id <- Gen.some(Gen.posNum[Long])
        title <- arbitrary[String]
      } yield CourseCategory(id = id, title = title)
    }

  implicit val course: Arbitrary[Course] = Arbitrary[Course] {
    for {
      id <- Gen.some(Gen.posNum[Long])
      title <- arbitrary[String]
      categoryId <- Gen.some(Gen.posNum[Long])
    } yield Course(id = id, title = title, categoryId = categoryId)
  }

  implicit val enrollment: Arbitrary[Enrollment] = Arbitrary[Enrollment] {
    for {
      id <- Gen.some(Gen.posNum[Long])
      studentId <- Gen.posNum[Long]
      courseId <- Gen.posNum[Long]
    } yield Enrollment(id, studentId, courseId)
  }

  implicit val lesson: Arbitrary[Lesson] = Arbitrary[Lesson] {
    for {
      id <- Gen.some(Gen.posNum[Long])
      title <- arbitrary[String]
      courseId <- Gen.some(Gen.posNum[Long])
      difficulty <- arbitrary[LevelType]
      listPosition <- Gen.posNum[Int]
    } yield Lesson(id, title, courseId, difficulty, listPosition)
  }

  implicit val homework: Arbitrary[Homework] = Arbitrary[Homework] {
    for {
      id <- Gen.some(Gen.posNum[Long])
      title <- arbitrary[String]
      courseId <- Gen.some(Gen.posNum[Long])
      lessonId <- Gen.some(Gen.posNum[Long])
      difficulty <- arbitrary[LevelType]
      listPosition <- Gen.posNum[Int]
    } yield Homework(id, title, courseId, lessonId, difficulty, listPosition)
  }

  implicit val task: Arbitrary[Task] = Arbitrary[Task] {
    for {
      id <- Gen.some(Gen.posNum[Long])
      classId <- Gen.some(Gen.posNum[Long])
      taskType <- arbitrary[TaskType]
      listPosition <- Gen.posNum[Int]
    } yield Task(id, classId, taskType, listPosition)
  }

  implicit val exercise: Arbitrary[Exercise] = Arbitrary[Exercise] {
    for {
      id <- Gen.some(Gen.posNum[Long])
      taskId <- Gen.some(Gen.posNum[Long])
      exerciseType <- arbitrary[ExerciseType]
      content <- arbitrary[Json]
    } yield Exercise(id, taskId, exerciseType, content)
  }

  implicit val replenish: Arbitrary[ReplenishRequest] =
    Arbitrary[ReplenishRequest] {
      for {
        amount <- Gen.posNum[Int]
      } yield ReplenishRequest(amount)
    }
}

object SkyEngArbitraries extends SkyEngArbitraries

package io.gitlab.scp2020.skyeng.domain.courses.classes

import cats.data.EitherT
import cats.syntax.functor._
import cats.{Functor, Monad}
import io.gitlab.scp2020.skyeng.domain.{
  ClassObjectAlreadyExistsError,
  ClassObjectNotFoundError
}

class ClassService[F[_]](
    classObjRepositoryAlgebra: ClassRepositoryAlgebra[F]
) {
  def createClass(classObj: Class)(implicit
      M: Monad[F]
  ): EitherT[F, ClassObjectAlreadyExistsError, Class] =
    for {
      saved <- EitherT.liftF(classObjRepositoryAlgebra.create(classObj))
    } yield saved

  def getClass(classId: Long)(implicit
      F: Functor[F]
  ): EitherT[F, ClassObjectNotFoundError.type, Class] =
    classObjRepositoryAlgebra
      .get(classId)
      .toRight(ClassObjectNotFoundError)

  def deleteClass(
      classId: Long
  )(implicit F: Functor[F]): F[Unit] =
    classObjRepositoryAlgebra
      .delete(classId)
      .value
      .void

  def updateClass(classObj: Class)(implicit
      M: Monad[F]
  ): EitherT[F, ClassObjectNotFoundError.type, Class] =
    for {
      saved <-
        classObjRepositoryAlgebra
          .update(classObj)
          .toRight(ClassObjectNotFoundError)
    } yield saved

  def listClasses(
      pageSize: Int,
      offset: Int
  ): F[List[Class]] =
    classObjRepositoryAlgebra.list(pageSize, offset)

  def getClassesByCourseId(courseId: Long): F[List[Class]] =
    classObjRepositoryAlgebra.getByCourseId(courseId)

  def getLessonsByCourseId(courseId: Long): F[List[Class]] =
    classObjRepositoryAlgebra.getByCourseIdAndClassType(
      courseId,
      ClassType.Lesson
    )

  def getHomeworksByCourseId(courseId: Long): F[List[Class]] =
    classObjRepositoryAlgebra.getByCourseIdAndClassType(
      courseId,
      ClassType.Homework
    )
}

object ClassService {
  def apply[F[_]](
      classObjRepositoryAlgebra: ClassRepositoryAlgebra[F]
  ): ClassService[F] =
    new ClassService(
      classObjRepositoryAlgebra
    )
}

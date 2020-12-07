package io.gitlab.scp2020.skyeng.domain.results

import cats.data.EitherT
import cats.syntax.functor._
import cats.{Functor, Monad}
import io.gitlab.scp2020.skyeng.domain.{
  ClassResultAlreadyExistsError,
  ClassResultNotFoundError
}

class ClassResultService[F[_]](
    resultRepositoryAlgebra: ClassResultRepositoryAlgebra[F]
) {
  def createClassResult(result: ClassResult)(implicit
      M: Monad[F]
  ): EitherT[F, ClassResultAlreadyExistsError, ClassResult] =
    for {
      saved <- EitherT.liftF(resultRepositoryAlgebra.create(result))
    } yield saved

  def getClassResult(resultId: Long)(implicit
      F: Functor[F]
  ): EitherT[F, ClassResultNotFoundError.type, ClassResult] =
    resultRepositoryAlgebra
      .get(resultId)
      .toRight(ClassResultNotFoundError)

  def deleteClassResult(resultId: Long)(implicit F: Functor[F]): F[Unit] =
    resultRepositoryAlgebra
      .delete(resultId)
      .value
      .void

  def updateClassResult(result: ClassResult)(implicit
      M: Monad[F]
  ): EitherT[F, ClassResultNotFoundError.type, ClassResult] =
    for {
      saved <-
        resultRepositoryAlgebra
          .update(result)
          .toRight(ClassResultNotFoundError)
    } yield saved

  def listClassResults(pageSize: Int, offset: Int): F[List[ClassResult]] =
    resultRepositoryAlgebra.list(pageSize, offset)

  def getClassResultsByStudentId(studentId: Long): F[List[ClassResult]] =
    resultRepositoryAlgebra.getByStudentId(studentId)

  def getClassResultsByClassId(classId: Long): F[List[ClassResult]] =
    resultRepositoryAlgebra.getByClassId(classId)
}

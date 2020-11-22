package io.gitlab.scp2020.skyeng.domain.hello

trait HelloRepositoryAlgebra[F[_]] {
  def hello(): F[Hello]
}

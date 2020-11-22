package io.gitlab.scp2020.skyeng.domain.hello

class HelloService[F[_]](helloRepo: HelloRepositoryAlgebra[F]) {
  def sayHello(): F[Hello] =
    helloRepo.hello()
}


object HelloService {
  def apply[F[_]](helloRepo: HelloRepositoryAlgebra[F]): HelloService[F] =
    new HelloService[F](helloRepo)
}

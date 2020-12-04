package io.gitlab.scp2020.skyeng.domain.results

case class ClassResult(
                        id: Option[Long],
                        studentId: Long,
                        classId: Long,
                        score: Int
                      )

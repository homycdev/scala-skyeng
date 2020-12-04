package io.gitlab.scp2020.skyeng.domain.courses.classes

case class ClassVocabulary(
                            id: Option[Long],
                            wordId: Long,
                            classId: Long,
                          )

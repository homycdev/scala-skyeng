package io.gitlab.scp2020.skyeng.domain.courses.vocabulary

case class Word(
                 id: Option[Long],
                 phrase: String,
                 imageUrl: Option[String],
                 transcript: Option[String],
                 englishMeaning: Option[String],
                 russianTranslation: Option[String],
               )

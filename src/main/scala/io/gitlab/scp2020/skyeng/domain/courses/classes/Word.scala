package io.gitlab.scp2020.skyeng.domain.courses.classes

case class Word(
                 id: Long,
                 phrase: String,
                 imageUrl: String,
                 transcript: String,
                 englishMeaning: String,
                 russianTranslation: String,
               )

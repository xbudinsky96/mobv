package com.example.zadanie.model

import androidx.room.TypeConverter


class Converters {

    @TypeConverter
    fun fromTag(tag: Tags): String {
        return  tag.name + "&&" +
                tag.amenity + "&&" +
                tag.phone + "&&" +
                tag.description + "&&" +
                tag.email + "&&" +
                tag.food + "&&" +
                tag.website + "&&" +
                tag.opening_hours
    }

    @TypeConverter
    fun toTag(detailString: String): Tags {
        val details = detailString.replace("null", "").split("&&")
        return Tags(
            details[0],
            details[1],
            details[2],
            details[3],
            details[4],
            details[5],
            details[6],
            details[7]
        )
    }

}
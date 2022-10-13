package com.example.zadanie.model

import com.google.gson.annotations.SerializedName

data class Company(
    val elements: MutableList<Element>
)

data class Element(
    val id: Long,
    val lat: Double,
    val lon: Double,
    val tags: Tags,
    val type: String
)

data class Tags(
    val access: String,
    @SerializedName("addr:city")
    val addr_city: String,
    @SerializedName("addr:conscriptionnumber")
    val addr_conscriptionnumber: String,
    @SerializedName("addr:country")
    val addr_country: String,
    @SerializedName("addr:floor")
    val addr_floor: String,
    @SerializedName("addr:housename")
    val addr_housename: String,
    @SerializedName("addr:housenumber")
    val addr_housenumber: String,
    @SerializedName("addr:postcode")
    val addr_postcode: String,
    @SerializedName("addr:street")
    val addr_street: String,
    @SerializedName("addr:streetnumber")
    val addr_streetnumber: String,
    @SerializedName("addr:suburb")
    val addr_suburb: String,
    val alt_name: String,
    val amenity: String,
    val bar: String,
    val beer_garden: String,
    val brewery: String,
    val capacity: String,
    val check_date: String,
    val club: String,
    val complete: String,
    @SerializedName("contact:email")
    val contact_email: String,
    @SerializedName("contact:facebook")
    val contact_facebook: String,
    @SerializedName("contact:instagram")
    val contact_instagram: String,
    @SerializedName("contact:phone")
    val contact_phone: String,
    @SerializedName("contact:twitter")
    val contact_twitter: String,
    @SerializedName("contact:website")
    val contact_website: String,
    val cuisine: String,
    val description: String,
    val disused_amenity: String,
    val disused_name: String,
    val drink_kofola: String,
    val drink_wine: String,
    val email: String,
    val fixme: String,
    val food: String,
    val indoor: String,
    val indoor_seating: String,
    val internet_access: String,
    val internet_access_fee: String,
    val layer: String,
    val leisure: String,
    val level: String,
    val microbrewery: String,
    val min_age: String,
    val name: String,
    val name_en: String,
    val note: String,
    val official_name: String,
    val old_name: String,
    val opening_hours: String,
    val opening_hours_covid19: String,
    val operator: String,
    val outdoor_seating: String,
    @SerializedName("payment:account_cards")
    val payment_account_cards: String,
    @SerializedName("payment:cash")
    val payment_cash: String,
    @SerializedName("payment:credit_cards")
    val payment_credit_cards: String,
    @SerializedName("payment:debit_cards")
    val payment_debit_cards: String,
    @SerializedName("payment:diners_club")
    val payment_diners_club: String,
    @SerializedName("payment:discover_card")
    val payment_discover_card: String,
    @SerializedName("payment:jcb")
    val payment_jcb: String,
    @SerializedName("payment:maestro")
    val payment_maestro: String,
    @SerializedName("payment:mastercard")
    val payment_mastercard: String,
    @SerializedName("payment:visa")
    val payment_visa: String,
    @SerializedName("payment:visa_debit")
    val payment_visa_debit: String,
    @SerializedName("payment:visa_electron")
    val payment_visa_electron: String,
    val phone: String,
    val reservation: String,
    val shop: String,
    val smoking: String,
    val source: String,
    val source_amenity: String,
    val start_date: String,
    val survey_date: String,
    val toilets_wheelchair: String,
    val url: String,
    val website: String,
    val wheelchair: String
)
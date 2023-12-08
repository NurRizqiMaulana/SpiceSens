package com.dicoding.spicesens.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class DataProduk(
    var dataNama: String? = null,
    var dataHarga: String? = null,
    var dataWa: String? = null,
    var dataDes: String? = null,
    var dataImage: String? = null,
    var dataLat: Double? = null,
    var dataLon: Double? = null,
    var key :String? = null
){

}

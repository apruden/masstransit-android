package com.monolito.masstransit.domain

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

@DatabaseTable(tableName = "Shapes")
data class Shape (@DatabaseField(id = true) val id: Int, @DatabaseField val lat: Double, @DatabaseField val lon: Double) {
    constructor() : this(0, 0.toDouble(), 0.toDouble())  //NOTICE: ORMLite needs a no-args constructor
}
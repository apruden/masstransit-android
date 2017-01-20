package com.monolito.masstransit.domain

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

@DatabaseTable(tableName = "Stops")
data class Stop (
        @DatabaseField(id = true) val id: String,
        @DatabaseField val lat: Double,
        @DatabaseField val lon: Double,
        @DatabaseField val name: String,
        @DatabaseField val code: String,
        @DatabaseField val routes: String) : Searchable {
    constructor() : this("", 0.toDouble(), 0.toDouble(), "", "", "")  //NOTICE: ORMLite needs a no-args constructor
    override fun getLabel() = this.name
}

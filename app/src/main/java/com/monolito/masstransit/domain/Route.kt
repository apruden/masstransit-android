package com.monolito.masstransit.domain

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

@DatabaseTable(tableName = "Routes")
data class Route(
        @DatabaseField val id: Int,
        @DatabaseField val code: String,
        @DatabaseField val service: String,
        @DatabaseField val shape: Int,
        @DatabaseField val name: String,
        @DatabaseField val stops: String) : Searchable {

    constructor() : this(0, "", "", 0, "", "") //NOTICE: ORMLite needs a no-args constructor

    override fun getLabel(): String? = name
}

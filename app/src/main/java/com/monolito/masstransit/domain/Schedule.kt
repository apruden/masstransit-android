package com.monolito.masstransit.domain

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

@DatabaseTable(tableName = "Schedule")
data class Schedule(@DatabaseField(generatedId=true) val id: Int,//
                    @DatabaseField val stop: String, //
                    @DatabaseField val route:String, //
                    @DatabaseField val service:String, //
                    @DatabaseField val code:String, //
                    @DatabaseField val times:String) {
    constructor() : this(0, "", "", "", "", "")  //NOTICE: ORMLite needs a no-args constructor
}

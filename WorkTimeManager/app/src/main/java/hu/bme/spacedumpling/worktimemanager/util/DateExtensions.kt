package hu.bme.spacedumpling.worktimemanager.util

import hu.bme.spacedumpling.worktimemanager.logic.models.TimeIntervalInput
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

const val ONLY_DATE_FORMAT = "yyyy.MM.dd."
const val HOUR_AND_MIN_FORMAT = "HH:mm"

fun Date.toDate() : String{
    return SimpleDateFormat(ONLY_DATE_FORMAT).format(this)
}

fun Date.toHourMin() : String{
    return SimpleDateFormat(HOUR_AND_MIN_FORMAT).format(this)
}

fun Calendar.minutesAgo(): Long {
    val today = Calendar.getInstance()
    return ChronoUnit.MINUTES.between(this.toInstant(), today.toInstant())
}

fun Calendar.hoursAgo(): Long {
    val today = Calendar.getInstance()
    return ChronoUnit.HOURS.between(this.toInstant(), today.toInstant())
}

fun Calendar.daysAgo(): Long {
    val today = Calendar.getInstance()
    val hours = this.hoursAgo()
    return hours / 24
}

fun Calendar.weeksAgo(): Long {
    val today = Calendar.getInstance()
    val days = this.daysAgo()
    return days / 7
}

fun Date.hoursBetween(other: Date) : Int?{
    val one = Calendar.getInstance()
    one.time = this
    val two = Calendar.getInstance()
    two.time = other
    val hours = ChronoUnit.HOURS.between(one.toInstant(), two.toInstant()).toInt()
    return if (hours < 0){
        null
    }else{
        hours
    }
}

fun Date.minutesBetweenModulo(other: Date) : Int?{
    val one = Calendar.getInstance()
    one.time = this
    val two = Calendar.getInstance()
    two.time = other
    val allMinutes = ChronoUnit.MINUTES.between(one.toInstant(), two.toInstant()).toInt()
    return if(allMinutes < 0) {
        null
    }else{
        allMinutes.mod(60)
    }
}
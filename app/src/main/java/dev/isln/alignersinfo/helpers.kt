package dev.isln.alignersinfo

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.round
import kotlin.time.ExperimentalTime

val secInDay = 60 * 60 * 24

var _error = ""
var _errorMessage = ""
var _replaceWarning = ""
var _replaceInDays = ""
var _replaceDate = ""
var _current = ""
var _total = ""
var _percent = ""
var _daysPassed = ""
var _daysTotal = ""
var _mainBlock = ""
var _completedBlock = ""

fun setMainBlockVisibility(visible: Boolean) {
    _mainBlock = if (visible) "visible" else "hidden"
}

fun setCompletedBlockVisibility(visible: Boolean) {
    _completedBlock = if (visible) "visible" else "hidden"
}

fun setReplaceWarningVisibility(visible: Boolean) {
    _replaceWarning = if (visible) "visible" else "hidden"
}

fun showError(message: String) {
    _error = "visible"
    _errorMessage = message
}

fun zeroPad(n: Int, digits: Int): String {
    return n.toString().padStart(digits, '0')
}

fun getTimeByString(dateString: String): Long {
    val zoneId = ZoneId.systemDefault()

    val date = LocalDate.parse(dateString)
    return date.atStartOfDay(zoneId).plusHours(6).toEpochSecond()
}

fun getCurrentPeriodIndex(startDates: List<String>): Int {
    val currentDate = LocalDate.now()

    var i = (startDates.size - 1)
    while (i >= 0) {
        val date = LocalDate.parse(startDates[i])
        if (date < currentDate) {
            return i
        }
        i--
    }

    return -1
}

fun getPreviousData(allStartDates: List<String>, allChangeIntervals:List<String>, indexOfCurrentPeriod: Int): Pair<Int, Int> {
    val startDates = allStartDates.slice(0 .. indexOfCurrentPeriod)
    val changeIntervals = allChangeIntervals.slice(0 .. indexOfCurrentPeriod)

    var allPreviousDays = 0
    var allPreviousAlignersCount = 0

    if (startDates.size == 1) {
        return Pair(allPreviousDays, allPreviousAlignersCount)
    }

    for (i in 1..<startDates.size) {
        val startTime = getTimeByString(startDates[i - 1])
        val nextStartTime = getTimeByString(startDates[i])

        val changeInterval = changeIntervals[i - 1].toInt()

        val msCount = nextStartTime - startTime
        val daysCount = (msCount / secInDay).toInt()
        val alignersCount = daysCount / changeInterval

        allPreviousDays += daysCount
        allPreviousAlignersCount += alignersCount
    }

    return Pair(allPreviousDays, allPreviousAlignersCount)
}

@OptIn(ExperimentalTime::class)
fun main() {
    val startDatesRaw = "2025-05-15,2025-12-01"
    val changeIntervalsRaw = "10,7"
    val totalAlignersRaw = "52"
    val totalAligners = totalAlignersRaw.toInt()

    if (startDatesRaw.isEmpty() || changeIntervalsRaw.isEmpty() || totalAlignersRaw.isEmpty()) {
        showError("not enough parameters")
        return
    }

    val startDates = startDatesRaw.split(",")
    val changeIntervals = changeIntervalsRaw.split(",")

    if (startDates.size != changeIntervals.size) {
        showError("incorrect parameters")
        return
    }

    val indexOfCurrentPeriod = getCurrentPeriodIndex(startDates)

    if (indexOfCurrentPeriod < 0) {
        showError("all dates are in future")
        return
    }

    val zoneId = ZoneId.systemDefault()

    val startTime = getTimeByString(startDates[indexOfCurrentPeriod])
    val currentDate = LocalDateTime.now()
    val currentTime = currentDate.atZone(zoneId).toEpochSecond()

    val msInUse = currentTime - startTime
    val (allPreviousDays, allPreviousAlignersCount) = getPreviousData(startDates, changeIntervals, indexOfCurrentPeriod)
    val currentTotalDaysInUse = msInUse / secInDay
    val totalDaysInUse = allPreviousDays + currentTotalDaysInUse

    val changeInterval = changeIntervals[indexOfCurrentPeriod].toInt()

    val currentAlignerIndex = currentTotalDaysInUse / changeInterval + 1 + allPreviousAlignersCount
    val totalDays = allPreviousDays + (totalAligners - allPreviousAlignersCount) * changeInterval
    val activeDays = currentTotalDaysInUse % changeInterval
    val replaceInDays = changeInterval - activeDays

    val lastReplaceTime = currentTime - (msInUse % (secInDay * changeInterval))
    val replaceTime = lastReplaceTime + secInDay * changeInterval

    val d = Instant.ofEpochSecond(replaceTime)
    val replaceDate = LocalDate.from(ZonedDateTime.parse(d.toString()))

    val percent = round(totalDaysInUse.toFloat() / (totalDays.toFloat() / 100) * 10) / 10

    val completed = totalDaysInUse >= totalDays

    _percent = if (percent > 100) "100" else percent.toString()
    _daysPassed = totalDaysInUse.toString()
    _daysTotal = totalDays.toString()

    if (completed) {
        setMainBlockVisibility(false)
        setCompletedBlockVisibility(true)
        return
    }

    if (activeDays == 0L) {
        setReplaceWarningVisibility(true)
    }

    _current = currentAlignerIndex.toString()
    _total = totalAligners.toString()

    _replaceInDays = replaceInDays.toString()
    val day = zeroPad(replaceDate.dayOfMonth, 2)
    val month = zeroPad(replaceDate.monthValue, 2)
    val year = replaceDate.year
    _replaceDate = "$day.$month.$year"

    println("_percent=$_percent")
    println("_daysPassed=$_daysPassed")
    println("_daysTotal=$_daysTotal")
    println("_current=$_current")
    println("_total=$_total")
    println("_replaceInDays=$_replaceInDays")
    println("_replaceDate=$_replaceDate")
    println("-----")
    println("_mainBlock=$_mainBlock")
    println("_completedBlock=$_completedBlock")
    println("_replaceWarning=$_replaceWarning")
    println("_error=$_error")
    println("_errorMessage=$_errorMessage")
}

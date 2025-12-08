package dev.isln.alignersinfo

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.round
import kotlin.time.ExperimentalTime

class MainScreenParams (
    var percent: String = "",
    var daysPassed: String = "",
    var daysTotal: String = "",
    var current: String = "",
    var total: String = "",
    var replaceInDays: String = "",
    var replaceDate: String = "",

    var errorMessage: String = "",
    var showReplaceWarning: Boolean = false,
    var showMainBlock: Boolean = true,
    var showCompletedBlock: Boolean = false,
)

const val SEC_IN_DAY = 86400

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
        val daysCount = (msCount / SEC_IN_DAY).toInt()
        val alignersCount = daysCount / changeInterval

        allPreviousDays += daysCount
        allPreviousAlignersCount += alignersCount
    }

    return Pair(allPreviousDays, allPreviousAlignersCount)
}

@OptIn(ExperimentalTime::class)
fun calculate(startDatesRaw: String, changeIntervalsRaw: String, totalAlignersRaw: String): MainScreenParams {
    val params = MainScreenParams()

    val totalAligners = totalAlignersRaw.toInt()

    if (startDatesRaw.isEmpty() || changeIntervalsRaw.isEmpty() || totalAlignersRaw.isEmpty()) {
        params.errorMessage = "not enough parameters"
        return params
    }

    val startDates = startDatesRaw.split(",")
    val changeIntervals = changeIntervalsRaw.split(",")

    if (startDates.size != changeIntervals.size) {
        params.errorMessage = "incorrect parameters"
        return params
    }

    val indexOfCurrentPeriod = getCurrentPeriodIndex(startDates)

    if (indexOfCurrentPeriod < 0) {
        params.errorMessage = "all dates are in future"
        return params
    }

    val zoneId = ZoneId.systemDefault()

    val startTime = getTimeByString(startDates[indexOfCurrentPeriod])
    val currentDate = LocalDateTime.now()
    val currentTime = currentDate.atZone(zoneId).toEpochSecond()

    val msInUse = currentTime - startTime
    val (allPreviousDays, allPreviousAlignersCount) = getPreviousData(startDates, changeIntervals, indexOfCurrentPeriod)
    val currentTotalDaysInUse = msInUse / SEC_IN_DAY
    val totalDaysInUse = allPreviousDays + currentTotalDaysInUse

    val changeInterval = changeIntervals[indexOfCurrentPeriod].toInt()

    val currentAlignerIndex = currentTotalDaysInUse / changeInterval + 1 + allPreviousAlignersCount
    val totalDays = allPreviousDays + (totalAligners - allPreviousAlignersCount) * changeInterval
    val activeDays = currentTotalDaysInUse % changeInterval
    val replaceInDays = changeInterval - activeDays

    val lastReplaceTime = currentTime - (msInUse % (SEC_IN_DAY * changeInterval))
    val replaceTime = lastReplaceTime + SEC_IN_DAY * changeInterval

    val d = Instant.ofEpochSecond(replaceTime)
    val replaceDate = LocalDate.from(ZonedDateTime.parse(d.toString()))

    val percent = round(totalDaysInUse.toFloat() / (totalDays.toFloat() / 100) * 10) / 10

    val completed = totalDaysInUse >= totalDays

    params.percent = if (percent > 100) "100" else percent.toString()
    params.daysPassed = totalDaysInUse.toString()
    params.daysTotal = totalDays.toString()

    if (completed) {
        params.showMainBlock = false
        params.showCompletedBlock = true
        return params
    }

    if (activeDays == 0L) {
        params.showReplaceWarning = true
    }

    params.current = currentAlignerIndex.toString()
    params.total = totalAligners.toString()

    params.replaceInDays = replaceInDays.toString()
    val day = replaceDate.dayOfMonth.toString().padStart(2, '0')
    val month = replaceDate.monthValue.toString().padStart(2, '0')
    val year = replaceDate.year
    params.replaceDate = "$day.$month.$year"

    return params
}

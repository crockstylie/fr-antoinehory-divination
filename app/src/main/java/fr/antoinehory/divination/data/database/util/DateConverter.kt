package fr.antoinehory.divination.data.database.util

import androidx.room.TypeConverter
import java.util.Date

/**
 * [TypeConverter] for Room to allow storing and retrieving [java.util.Date] objects.
 * Dates are stored as [Long] timestamps (milliseconds since epoch) in the database.
 */
class DateConverter {
    /**
     * Converts a [Date] object to its [Long] timestamp representation.
     *
     * @param date The [Date] to convert. Can be null.
     * @return The timestamp as a [Long] (milliseconds since epoch), or null if the input date was null.
     */
    @TypeConverter
    fun toTimestamp(date: Date?): Long? {
        return date?.time
    }

    /**
     * Converts a [Long] timestamp back to a [Date] object.
     *
     * @param timestamp The [Long] timestamp (milliseconds since epoch) to convert. Can be null.
     * @return The corresponding [Date] object, or null if the input timestamp was null.
     */
    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }
}
package com.asinosoft.cdm.api

import android.content.ContentResolver
import android.database.CharArrayBuffer
import android.database.ContentObserver
import android.database.Cursor
import android.database.DataSetObserver
import android.net.Uri
import android.os.Bundle

class ArrayCursor(
    private val projection: List<String>,
    private val data: List<List<Any?>>
) : Cursor {
    private var position = 0

    override fun close() {
    }

    override fun getCount(): Int {
        return data.size
    }

    override fun getPosition(): Int {
        throw NotImplementedError()
    }

    override fun move(offset: Int): Boolean {
        throw NotImplementedError()
    }

    override fun moveToPosition(position: Int): Boolean {
        throw NotImplementedError()
    }

    override fun moveToFirst(): Boolean {
        throw NotImplementedError()
    }

    override fun moveToLast(): Boolean {
        throw NotImplementedError()
    }

    override fun moveToNext(): Boolean {
        position++
        return position < data.size
    }

    override fun moveToPrevious(): Boolean {
        throw NotImplementedError()
    }

    override fun isFirst(): Boolean {
        throw NotImplementedError()
    }

    override fun isLast(): Boolean {
        throw NotImplementedError()
    }

    override fun isBeforeFirst(): Boolean {
        throw NotImplementedError()
    }

    override fun isAfterLast(): Boolean {
        throw NotImplementedError()
    }

    override fun getColumnIndex(columnName: String?): Int {
        return projection.indexOf(columnName)
    }

    override fun getColumnIndexOrThrow(columnName: String?): Int {
        throw NotImplementedError()
    }

    override fun getColumnName(columnIndex: Int): String {
        throw NotImplementedError()
    }

    override fun getColumnNames(): Array<String> {
        throw NotImplementedError()
    }

    override fun getColumnCount(): Int {
        throw NotImplementedError()
    }

    override fun getBlob(columnIndex: Int): ByteArray {
        throw NotImplementedError()
    }

    override fun getString(columnIndex: Int): String {
        return getCell(position, columnIndex).toString()
    }

    override fun copyStringToBuffer(columnIndex: Int, buffer: CharArrayBuffer?) {
        throw NotImplementedError()
    }

    override fun getShort(columnIndex: Int): Short {
        return getCell(position, columnIndex) as Short
    }

    override fun getInt(columnIndex: Int): Int {
        return getCell(position, columnIndex) as Int
    }

    override fun getLong(columnIndex: Int): Long {
        return getCell(position, columnIndex) as Long
    }

    override fun getFloat(columnIndex: Int): Float {
        return getCell(position, columnIndex) as Float
    }

    override fun getDouble(columnIndex: Int): Double {
        return getCell(position, columnIndex) as Double
    }

    override fun getType(columnIndex: Int): Int {
        throw NotImplementedError()
    }

    override fun isNull(columnIndex: Int): Boolean {
        return null == getCell(position, columnIndex)
    }

    override fun deactivate() {
        throw NotImplementedError()
    }

    override fun requery(): Boolean {
        throw NotImplementedError()
    }

    override fun isClosed(): Boolean {
        throw NotImplementedError()
    }

    override fun registerContentObserver(observer: ContentObserver?) {
        throw NotImplementedError()
    }

    override fun unregisterContentObserver(observer: ContentObserver?) {
        throw NotImplementedError()
    }

    override fun registerDataSetObserver(observer: DataSetObserver?) {
        throw NotImplementedError()
    }

    override fun unregisterDataSetObserver(observer: DataSetObserver?) {
        throw NotImplementedError()
    }

    override fun setNotificationUri(cr: ContentResolver?, uri: Uri?) {
        throw NotImplementedError()
    }

    override fun setNotificationUris(cr: ContentResolver, uris: MutableList<Uri>) {
        throw NotImplementedError()
    }

    override fun getNotificationUri(): Uri {
        throw NotImplementedError()
    }

    override fun getNotificationUris(): MutableList<Uri> {
        throw NotImplementedError()
    }

    override fun getWantsAllOnMoveCalls(): Boolean {
        throw NotImplementedError()
    }

    override fun setExtras(extras: Bundle?) {
        throw NotImplementedError()
    }

    override fun getExtras(): Bundle {
        throw NotImplementedError()
    }

    override fun respond(extras: Bundle?): Bundle {
        throw NotImplementedError()
    }

    private fun getCell(row: Int, col: Int): Any? {
        if (row < 0 || row > data.size) return null
        if (col < 0 || col > data[row].size) return null
        return data[row][col]
    }
}

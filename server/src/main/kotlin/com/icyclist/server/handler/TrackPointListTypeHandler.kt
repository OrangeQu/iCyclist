package com.icyclist.server.handler

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.icyclist.server.model.TrackPoint
import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import org.apache.ibatis.type.MappedTypes
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet

@MappedTypes(List::class)
class TrackPointListTypeHandler : BaseTypeHandler<List<TrackPoint>>() {

    private val objectMapper = ObjectMapper()

    override fun setNonNullParameter(
        ps: PreparedStatement,
        i: Int,
        parameter: List<TrackPoint>,
        jdbcType: JdbcType?
    ) {
        ps.setString(i, objectMapper.writeValueAsString(parameter))
    }

    override fun getNullableResult(rs: ResultSet, columnName: String): List<TrackPoint>? {
        return rs.getString(columnName)?.let {
            objectMapper.readValue(it, object : TypeReference<List<TrackPoint>>() {})
        }
    }

    override fun getNullableResult(rs: ResultSet, columnIndex: Int): List<TrackPoint>? {
        return rs.getString(columnIndex)?.let {
            objectMapper.readValue(it, object : TypeReference<List<TrackPoint>>() {})
        }
    }

    override fun getNullableResult(cs: CallableStatement, columnIndex: Int): List<TrackPoint>? {
        return cs.getString(columnIndex)?.let {
            objectMapper.readValue(it, object : TypeReference<List<TrackPoint>>() {})
        }
    }
}























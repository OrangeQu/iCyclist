package com.icyclist.server.handler

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet

class StringListTypeHandler : BaseTypeHandler<List<String>>() {

    private val objectMapper = ObjectMapper()

    override fun setNonNullParameter(ps: PreparedStatement, i: Int, parameter: List<String>, jdbcType: JdbcType?) {
        ps.setString(i, objectMapper.writeValueAsString(parameter))
    }

    override fun getNullableResult(rs: ResultSet, columnName: String): List<String>? {
        return rs.getString(columnName)?.let {
            objectMapper.readValue(it, object : TypeReference<List<String>>() {})
        }
    }

    override fun getNullableResult(rs: ResultSet, columnIndex: Int): List<String>? {
        return rs.getString(columnIndex)?.let {
            objectMapper.readValue(it, object : TypeReference<List<String>>() {})
        }
    }

    override fun getNullableResult(cs: CallableStatement, columnIndex: Int): List<String>? {
        return cs.getString(columnIndex)?.let {
            objectMapper.readValue(it, object : TypeReference<List<String>>() {})
        }
    }
}





















package com.icyclist.server.mapper

import com.icyclist.server.model.RideRecord
import org.apache.ibatis.annotations.Mapper

@Mapper
interface RideRecordMapper {
    fun insert(rideRecord: RideRecord)
    fun findById(id: Long): RideRecord?
    fun findByUserId(userId: Long): List<RideRecord>
}























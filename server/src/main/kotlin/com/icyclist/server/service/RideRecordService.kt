package com.icyclist.server.service

import com.icyclist.server.dto.LoginRequest
import com.icyclist.server.dto.UserRegistrationRequest
import com.icyclist.server.mapper.RideRecordMapper
import com.icyclist.server.mapper.UserMapper
import com.icyclist.server.model.RideRecord
import com.icyclist.server.model.User
import com.icyclist.server.util.JwtTokenProvider
import org.springframework.stereotype.Service

@Service
class RideRecordService(
    private val rideRecordMapper: RideRecordMapper
) {
    fun createRideRecord(rideRecord: RideRecord, userId: Long): RideRecord {
        rideRecord.userId = userId
        rideRecordMapper.insert(rideRecord)
        return rideRecord
    }

    fun getRideRecordById(id: Long): RideRecord? {
        return rideRecordMapper.findById(id)
    }

    fun getRideRecordsByUserId(userId: Long): List<RideRecord> {
        return rideRecordMapper.findByUserId(userId)
    }
}





















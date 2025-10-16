package com.icyclist.server.controller

import com.icyclist.server.model.RideRecord
import com.icyclist.server.service.RideRecordService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/rides")
class RideRecordController(private val rideRecordService: RideRecordService) {

    @PostMapping
    fun createRide(@AuthenticationPrincipal userId: Long, @RequestBody rideRecord: RideRecord): ResponseEntity<RideRecord> {
        val createdRide = rideRecordService.createRideRecord(rideRecord, userId)
        return ResponseEntity.ok(createdRide)
    }

    @GetMapping
    fun getUserRides(@AuthenticationPrincipal userId: Long): ResponseEntity<List<RideRecord>> {
        val rides = rideRecordService.getRideRecordsByUserId(userId)
        return ResponseEntity.ok(rides)
    }

    @GetMapping("/{id}")
    fun getRideById(@PathVariable id: Long, @AuthenticationPrincipal userId: Long): ResponseEntity<RideRecord> {
        val ride = rideRecordService.getRideRecordById(id)
        return if (ride != null && ride.userId == userId) {
            ResponseEntity.ok(ride)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}





















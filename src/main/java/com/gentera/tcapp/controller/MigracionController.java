package com.gentera.tcapp.controller;

import com.gentera.tcapp.model.ScheduleRequestDTO;
import com.gentera.tcapp.service.TemporalScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/migracion")
public class MigracionController {

    private final TemporalScheduleService temporalScheduleService;

    @PostMapping("/automatic")
    public ResponseEntity<String> lanzarProcesoCronJob(@RequestBody ScheduleRequestDTO requestDTO) {
        temporalScheduleService.createOrUpdateCronSchedule(requestDTO);
        return ResponseEntity.accepted().body("Proceso de importación CSV encolado automatizado");
    }
}
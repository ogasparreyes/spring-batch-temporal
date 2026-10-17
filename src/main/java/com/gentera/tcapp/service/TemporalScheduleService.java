package com.gentera.tcapp.service;

import com.gentera.tcapp.model.ScheduleRequestDTO;

public interface TemporalScheduleService {
    void createOrUpdateCronSchedule(ScheduleRequestDTO request);
}

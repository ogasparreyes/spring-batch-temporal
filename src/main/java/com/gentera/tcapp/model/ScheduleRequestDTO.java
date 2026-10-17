package com.gentera.tcapp.model;

/**
 * String scheduleId: Ej: "customer-job-daily-schedule"
 * String cronExpression: Ej: "0 0 2 * * *" (Todos los días a las 2:00 AM)
 * String filePath: Parámetros opcionales para el Job
 *
 */
public record ScheduleRequestDTO(
        String scheduleId,
        String cronExpression,
        String filePath
) {
}
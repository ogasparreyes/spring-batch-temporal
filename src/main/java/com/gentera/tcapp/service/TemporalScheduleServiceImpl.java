package com.gentera.tcapp.service;

import com.gentera.tcapp.config.TemporalConstants;
import com.gentera.tcapp.model.ScheduleRequestDTO;
import com.gentera.tcapp.workflow.MigracionWorkflow;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.schedules.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TemporalScheduleServiceImpl implements TemporalScheduleService {

    private final ScheduleClient scheduleClient;

    @Override
    public void createOrUpdateCronSchedule(ScheduleRequestDTO request) {

        // 1. Acción: qué Workflow iniciará el Schedule
        ScheduleActionStartWorkflow action = ScheduleActionStartWorkflow.newBuilder()
                .setWorkflowType(MigracionWorkflow.class)
                .setArguments(request.filePath())
                .setOptions(WorkflowOptions.newBuilder()
                        .setWorkflowId(request.scheduleId() + "-workflow")
                        .setTaskQueue(TemporalConstants.CSV_TASK_QUEUE)
                        .build())
                .build();


        // 2. Especificación del cron
        ScheduleSpec spec = ScheduleSpec.newBuilder()
                .setCronExpressions(List.of(request.cronExpression()))
                .build();

        // 3. Schedule completo
        Schedule schedule = Schedule.newBuilder()
                .setAction(action)
                .setSpec(spec)
                .build();

        // 4. Registro en Temporal
        scheduleClient.createSchedule(
                request.scheduleId(),
                schedule,
                ScheduleOptions.newBuilder().build()
        );
    }
}

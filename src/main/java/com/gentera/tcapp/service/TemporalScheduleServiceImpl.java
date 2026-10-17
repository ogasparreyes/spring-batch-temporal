package com.gentera.tcapp.service;

import com.gentera.tcapp.model.ScheduleRequestDTO;
import com.gentera.tcapp.workflow.MigracionWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.schedules.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TemporalScheduleServiceImpl implements TemporalScheduleService{

    private final WorkflowClient workflowClient;

    @Override
    public void createOrUpdateCronSchedule(ScheduleRequestDTO request) {
        // 1. Obtener el cliente de Schedules desde el WorkflowClient
        ScheduleClient scheduleClient = ScheduleClient.newInstance(workflowClient.getWorkflowServiceStubs());

        // 2. Definir la acción (qué Workflow iniciará este Cron)
        ScheduleActionStartWorkflow action = ScheduleActionStartWorkflow.newBuilder()
                .setWorkflowType(MigracionWorkflow.class)
                .setArguments(request.filePath()) // Parámetros pasados al Workflow
                .setOptions(WorkflowOptions.newBuilder()
                        .setWorkflowId(request.scheduleId() + "-workflow")
                        .setTaskQueue("CSV_TASK_QUEUE")
                        .build())
                .build();

        // 3. Configurar la especificación del Cron a partir de la cadena recibida
        ScheduleSpec spec = ScheduleSpec.newBuilder()
                .setCronExpressions(List.of(request.cronExpression()))
                .build();

        // 4. Crear la estructura del Schedule
        Schedule schedule = Schedule.newBuilder()
                .setAction(action)
                .setSpec(spec)
                .build();

        // 5. Registrar el Schedule en el clúster de Temporal
        scheduleClient.createSchedule(
                request.scheduleId(),
                schedule,
                ScheduleOptions.newBuilder().build()
        );
    }
}

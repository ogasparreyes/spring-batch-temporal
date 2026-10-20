package com.gentera.tcapp.workflow;

import com.gentera.tcapp.activity.BatchActivity;
import com.gentera.tcapp.config.TemporalConstants;
import io.temporal.activity.ActivityOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@Slf4j
@WorkflowImpl(taskQueues = TemporalConstants.CSV_TASK_QUEUE)
public class MigracionWorkflowImpl implements MigracionWorkflow {

    // Stub para invocar la actividad con tiempos de espera definidos[cite: 2, 3]
    private final BatchActivity activity = Workflow.newActivityStub(
            BatchActivity.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofMinutes(15)) // Tiempo suficiente para leer el CSV
                    .build()
    );

    @Override
    public String iniciarMigracionCsv(String idProceso) {
        // Ejecución determinista gestionada por Temporal
        log.info("PROCESO: {}", idProceso);
        activity.ejecutarMigracionCsv(idProceso);
        return "Migración del archivo CSV completada exitosamente.";
    }
}
package com.gentera.tcapp.activity;

import io.temporal.spring.boot.ActivityImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ActivityImpl(taskQueues = "CSV_TASK_QUEUE")
public class BatchActivityImpl implements BatchActivity {

    // El operador se encarga de la comunicación directa con el Job y el JobRepository[cite: 1]
    @Autowired
    private JobOperator jobOperator;

    @Autowired
    @Qualifier("importacionCsvJob")
    private Job importacionCsvJob;

    @Override
    public void ejecutarMigracionCsv(String idProceso) {
        try {
            log.info("ejecutarMigracionCsv : {}", idProceso);
            JobParameters params = new JobParametersBuilder()
                    .addString("idProceso", idProceso)
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            // En Spring Batch 6, JobOperator hereda 'run' para aceptar directamente la instancia del Job
            JobExecution execution = jobOperator.start(importacionCsvJob, params);

            if (execution.getStatus().isUnsuccessful()) {
                throw new RuntimeException("El procesamiento del CSV falló: " + execution.getStatus());
            }
        } catch (Exception e) {
            // Se lanza la excepción para que Temporal aplique sus políticas de reintento
            throw new RuntimeException("Error en la Activity de lectura de CSV", e);
        }
    }
}
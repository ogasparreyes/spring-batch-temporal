package com.gentera.tcapp.config;

import com.gentera.tcapp.activity.BatchActivity;
import com.gentera.tcapp.workflow.MigracionWorkflowImpl;
import io.grpc.Deadline;
import io.temporal.api.workflowservice.v1.GetSystemInfoRequest;
import io.temporal.api.workflowservice.v1.GetSystemInfoResponse;
import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.RpcRetryOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerFactoryOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class TemporalConfig {
    private static final long HEALTH_CHECK_TIMEOUT_SECONDS = 3;

    @Bean
    public WorkflowServiceStubs workflowServiceStubs(TemporalConnectionProperties temporalProperties) throws SSLException {

        WorkflowServiceStubs service;
        log.info("Valor enableHttps FALSE: {}", temporalProperties.isEnableHttps());

        if (!temporalProperties.isDefaultConfiguration()) {
            WorkflowServiceStubsOptions options = mapWorkflowServiceStubsOptions(temporalProperties);
            service = WorkflowServiceStubs.newServiceStubs(options);
            log.info("Creando Conexión con Temporal Server");
        } else {
            log.info("Creando Conexión por defecto con Temporal Server");
            service = WorkflowServiceStubs.newLocalServiceStubs();
        }
        checkTemporalConnection(service);
        return service;

    }

    // El método mapWorkflowServiceStubsOptions configura
    //  la infraestructura de comunicación de bajo nivel (gRPC)
    //  entre tu aplicación cliente en Java y el servidor de Temporal.io
    private WorkflowServiceStubsOptions mapWorkflowServiceStubsOptions(TemporalConnectionProperties temporalProperties) throws SSLException {


        WorkflowServiceStubsOptions.Builder builder = WorkflowServiceStubsOptions.newBuilder();

        builder.setEnableKeepAlive(true);
        builder.setKeepAliveTimeout(Duration.ofMinutes(5));
        builder.setKeepAliveTime(Duration.ofSeconds(60));
        builder.setGrpcReconnectFrequency(Duration.ofSeconds(30));

        builder.setEnableHttps(temporalProperties.isEnableHttps());

        RpcRetryOptions rpcRetryOptions = RpcRetryOptions.newBuilder()
                .setMaximumAttempts(3) // Número máximo de intentos de reintentos
                .setInitialInterval(Duration.ofSeconds(1)) // Intervalo inicial entre intentos
                .setMaximumInterval(Duration.ofSeconds(10)) // Intervalo máximo entre intentos
                .setExpiration(Duration.ofMinutes(5)) // Tiempo de expiración total para la operación
                .build();
        builder.setRpcRetryOptions(rpcRetryOptions);

        return builder.build();
    }

    private static boolean checkTemporalConnection(WorkflowServiceStubs workflowServiceStubs) {
        if (workflowServiceStubs == null) {
            log.warn("WorkflowServiceStubs es nulo. No se puede verificar la conexión con Temporal.");
            return false;
        }

        try {
            // Asignamos el retorno a 'response' para eliminar la advertencia 'Result is ignored'
            GetSystemInfoResponse response = workflowServiceStubs.blockingStub()
                    .withDeadline(Deadline.after(HEALTH_CHECK_TIMEOUT_SECONDS, TimeUnit.SECONDS))
                    .getSystemInfo(GetSystemInfoRequest.getDefaultInstance());

            log.info("Conexión con Temporal Server establecida correctamente. Versión del servidor: {}",
                    response.getServerVersion());
            return true;

        } catch (Exception e) {
            log.error("No se pudo establecer la conexión con Temporal Server en {}s. Causa: {}",
                    HEALTH_CHECK_TIMEOUT_SECONDS, e.getMessage());
            return false;
        }
    }

    @Bean
    public WorkflowClient workflowClient(WorkflowServiceStubs serviceStubs) {
        return WorkflowClient.newInstance(serviceStubs);
    }


    @Bean
    public WorkerFactory workerFactory(
            WorkflowClient workflowClient,
            BatchActivity migracionActivities) { // Inyectamos la Activity gestionada por Spring

        // 1. Crear las opciones y la fábrica
        WorkerFactoryOptions workerFactoryOptions = WorkerFactoryOptions.getDefaultInstance();
        WorkerFactory factory = WorkerFactory.newInstance(workflowClient, workerFactoryOptions);

        // 2. Crear el Worker en la MISMA TaskQueue configurada en el Schedule ("CSV_TASK_QUEUE")
        Worker worker = factory.newWorker("CSV_TASK_QUEUE");

        // 3. Registrar la CLASE de la implementación del Workflow
        // (Temporal crea una instancia aislada por cada ejecución del Workflow)
        worker.registerWorkflowImplementationTypes(MigracionWorkflowImpl.class);

        // 4. Registrar la INSTANCIA del Bean de la Activity (gestionada por Spring)
        // (Permite que la Activity inyecte el JobLauncher, JobRepository, etc.)
        worker.registerActivitiesImplementations(migracionActivities);

        // 5. ¡CRUCIAL! Iniciar el polling de eventos en Temporal
        factory.start();
        log.info("WorkerFactory de Temporal iniciado escuchando en la TaskQueue 'CSV_TASK_QUEUE'");

        return factory;
    }


}
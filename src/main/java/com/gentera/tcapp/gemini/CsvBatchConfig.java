package com.gentera.tcapp.gemini;

import com.gentera.tcapp.model.TransaccionCsv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.mapping.RecordFieldSetMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class CsvBatchConfig {

    // Extrae los datos del archivo CSV
    @Bean
    public ItemReader<TransaccionCsv> csvItemReader() {
        return new FlatFileItemReaderBuilder<TransaccionCsv>()
                .name("transaccionItemReader")
                .resource(new ClassPathResource("transacciones.csv"))
                .linesToSkip(1) // <--- Agrega esta línea para omitir los encabezados
                .delimited()
                .names("id", "cuenta", "monto")
                .fieldSetMapper(new RecordFieldSetMapper<>(TransaccionCsv.class))
                .build();
    }

    // Escribe o procesa los datos leídos[cite: 1]
    @Bean
    public ItemWriter<TransaccionCsv> consolaItemWriter() {
        return chunk -> chunk.getItems().forEach(t ->

                log.info("Procesando transacción: {} ${}", t.cuenta(), t.monto())
        );
    }

    // Define el paso aislando el bloque de procesamiento y la gestión transaccional[cite: 1]
    @Bean
    public Step procesarCsvStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("procesarCsvStep", jobRepository)
                .<TransaccionCsv, TransaccionCsv>chunk(10) // Configuración de chunk separada en SB6
                .transactionManager(transactionManager)
                .reader(csvItemReader())
                .writer(consolaItemWriter())
                .build();
    }

    // Define el trabajo principal coordinado por el repositorio[cite: 1]
    @Bean
    public Job importacionCsvJob(JobRepository jobRepository, Step procesarCsvStep) {
        return new JobBuilder("importacionCsvJob", jobRepository)
                .start(procesarCsvStep)
                .build();
    }
}
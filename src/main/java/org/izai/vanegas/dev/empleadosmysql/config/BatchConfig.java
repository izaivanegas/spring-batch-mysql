package org.izai.vanegas.dev.empleadosmysql.config;
import org.izai.vanegas.dev.empleadosmysql.domain.Empleado;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;


@Configuration
public class BatchConfig {

    // 1. Configurar Reader (igual que antes)
    @Bean
    public FlatFileItemReader<Empleado> reader() {
        return new FlatFileItemReaderBuilder<Empleado>()
                .name("empleadoItemReader")
                .resource(new ClassPathResource("data/empleados.csv"))
                .delimited()
                .names("id", "nombre", "apellido", "salario")
                .linesToSkip(1)
                .fieldSetMapper(new BeanWrapperFieldSetMapper<Empleado>() {{
                    setTargetType(Empleado.class);
                }})
                .build();
    }

    // 2. Configurar Processor (igual que antes)
    @Bean
    public ItemProcessor<Empleado, Empleado> processor() {
        return empleado -> {
            empleado.setNombre(empleado.getNombre().toUpperCase());
            empleado.setApellido(empleado.getApellido().toUpperCase());
            return empleado;
        };
    }

    // 3. Configurar Writer (igual que antes)
    @Bean
    public JdbcBatchItemWriter<Empleado> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Empleado>()
                .itemSqlParameterSourceProvider(
                        new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO empleados (id, nombre, apellido, salario) VALUES (:id, :nombre, :apellido, :salario)")
                .dataSource(dataSource)
                .build();
    }

    // 4. Configurar Step (CAMBIO IMPORTANTE)
    @Bean
    public Step step(JobRepository jobRepository,
                     PlatformTransactionManager transactionManager,
                     ItemReader<Empleado> reader,
                     ItemProcessor<Empleado, Empleado> processor,
                     ItemWriter<Empleado> writer) {

        return new StepBuilder("step1", jobRepository)
                .<Empleado, Empleado>chunk(10, transactionManager) // Nuevo parámetro transactionManager
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    // 5. Configurar Job (CAMBIO IMPORTANTE)
    @Bean
    public Job importEmpleadosJob(JobRepository jobRepository, Step step) {
        return new JobBuilder("importEmpleadosJob", jobRepository)
                .start(step)
                .build();
    }
}

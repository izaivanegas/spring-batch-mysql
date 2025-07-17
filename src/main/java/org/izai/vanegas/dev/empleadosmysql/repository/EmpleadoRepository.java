package org.izai.vanegas.dev.empleadosmysql.repository;

import org.izai.vanegas.dev.empleadosmysql.domain.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado,Long> {
}

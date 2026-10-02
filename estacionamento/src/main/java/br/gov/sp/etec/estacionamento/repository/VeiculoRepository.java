package br.gov.sp.etec.estacionamento.repository;

import br.gov.sp.etec.estacionamento.entity.VeiculoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface VeiculoRepository extends JpaRepository<VeiculoEntity, Long> {

    List<VeiculoEntity> findByDataEntradaBetween(
            LocalDateTime inicio,
            LocalDateTime fim
    );

    long countByEstacionado(boolean estacionado);

    @Query(value = """
            SELECT
                UPPER(REPLACE(placa, '-', '')),
                UPPER(modelo),
                COUNT(*)
            FROM tb_veiculo
            GROUP BY
                UPPER(REPLACE(placa, '-', '')),
                UPPER(modelo)
            ORDER BY COUNT(*) DESC
            """, nativeQuery = true)
    List<Object[]> findVeiculosFrequentes();

    @Query(value = """
            SELECT
                UPPER(REPLACE(placa, '-', '')),
                UPPER(modelo),
                COUNT(*),
                AVG(DATEDIFF('MINUTE', data_entrada, data_saida)),
                SUM(DATEDIFF('MINUTE', data_entrada, data_saida))
            FROM tb_veiculo
            WHERE data_saida IS NOT NULL
            GROUP BY
                UPPER(REPLACE(placa, '-', '')),
                UPPER(modelo)
            ORDER BY COUNT(*) DESC
            """, nativeQuery = true)
    List<Object[]> findVeiculosFrequentesComTempos();
}

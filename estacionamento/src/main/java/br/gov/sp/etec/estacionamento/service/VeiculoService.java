package br.gov.sp.etec.estacionamento.service;

import br.gov.sp.etec.estacionamento.model.Veiculo;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface VeiculoService {
    void cadastrarVeiculo(Veiculo veiculo);
    List<Veiculo> listaVeiculos();
    List<Veiculo> listarTodasMovimentacoes(LocalDate dataFiltro);

    List<Veiculo> listarTodasMovimentacoesIntervalo(LocalDateTime inicio, LocalDateTime fim);

    boolean deletarVeiculo(Long id);
    Veiculo atualizarVeiculo(Veiculo v);
    void registrarSaida(Long id);
    long contarVagasOcupadas();
    long contarVagasLivres();
}
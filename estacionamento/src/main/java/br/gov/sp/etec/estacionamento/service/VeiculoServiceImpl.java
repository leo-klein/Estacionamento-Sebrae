package br.gov.sp.etec.estacionamento.service;

import br.gov.sp.etec.estacionamento.entity.VeiculoEntity;
import br.gov.sp.etec.estacionamento.model.Veiculo;
import br.gov.sp.etec.estacionamento.repository.VeiculoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class VeiculoServiceImpl implements VeiculoService {

    @Autowired
    private VeiculoRepository repository;

    private static final long TOTAL_VAGAS_ESTACIONAMENTO = 30;

    @Override
    public void cadastrarVeiculo(Veiculo veiculo) {
        repository.save(toEntity(veiculo));
    }

    @Override
    public List<Veiculo> listaVeiculos() {
        List<VeiculoEntity> entities = repository.findAll();
        List<Veiculo> veiculos = new ArrayList<>();

        for (VeiculoEntity entity : entities) {
            if (entity.isEstacionado()) {
                veiculos.add(toModel(entity));
            }
        }

        return veiculos;
    }

    @Override
    public List<Veiculo> listarTodasMovimentacoes(LocalDate dataFiltro) {
        List<VeiculoEntity> entities;

        if (dataFiltro != null) {
            LocalDateTime inicioDia = dataFiltro.atStartOfDay();
            LocalDateTime fimDia = dataFiltro.atTime(LocalTime.MAX);
            entities = repository.findByDataEntradaBetween(inicioDia, fimDia);
        } else {
            entities = repository.findAll();
        }

        List<Veiculo> veiculos = new ArrayList<>();

        for (VeiculoEntity entity : entities) {
            veiculos.add(toModel(entity));
        }

        return veiculos;
    }

    @Override
    public List<Veiculo> listarTodasMovimentacoesIntervalo(LocalDateTime inicio, LocalDateTime fim) {
        List<VeiculoEntity> entities = repository.findByDataEntradaBetween(inicio, fim);
        List<Veiculo> veiculos = new ArrayList<>();

        for (VeiculoEntity entity : entities) {
            veiculos.add(toModel(entity));
        }

        return veiculos;
    }

    @Override
    public boolean deletarVeiculo(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }

        return false;
    }

    @Override
    public Veiculo atualizarVeiculo(Veiculo v) {
        if (v.getId() == null) {
            return null;
        }

        VeiculoEntity entity = repository.findById(v.getId()).orElse(null);

        if (entity == null) {
            return null;
        }

        entity.setPlaca(v.getPlaca());
        entity.setModelo(v.getModelo());
        entity.setCor(v.getCor());
        entity.setObservacoes(v.getObservacoes());

        return toModel(repository.save(entity));
    }

    @Override
    public void registrarSaida(Long id) {
        VeiculoEntity entity = repository.findById(id).orElse(null);

        if (entity != null) {
            entity.setEstacionado(false);
            entity.setDataSaida(LocalDateTime.now());
            repository.save(entity);
        }
    }

    @Override
    public long contarVagasOcupadas() {
        return repository.countByEstacionado(true);
    }

    @Override
    public long contarVagasLivres() {
        long ocupadas = contarVagasOcupadas();
        long livres = TOTAL_VAGAS_ESTACIONAMENTO - ocupadas;

        return Math.max(livres, 0);
    }

    private VeiculoEntity toEntity(Veiculo veiculo) {
        VeiculoEntity entity = new VeiculoEntity();

        entity.setPlaca(veiculo.getPlaca());
        entity.setModelo(veiculo.getModelo());
        entity.setCor(veiculo.getCor());
        entity.setObservacoes(veiculo.getObservacoes());
        entity.setDataEntrada(LocalDateTime.now());
        entity.setEstacionado(true);

        return entity;
    }

    private Veiculo toModel(VeiculoEntity entity) {
        Veiculo veiculo = new Veiculo();

        veiculo.setId(entity.getId());
        veiculo.setPlaca(entity.getPlaca());
        veiculo.setModelo(entity.getModelo());
        veiculo.setCor(entity.getCor());
        veiculo.setObservacoes(entity.getObservacoes());
        veiculo.setDataEntrada(entity.getDataEntrada());
        veiculo.setDataSaida(entity.getDataSaida());
        veiculo.setEstacionado(entity.isEstacionado());

        return veiculo;
    }
}

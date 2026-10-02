package br.gov.sp.etec.estacionamento.controller;

import br.gov.sp.etec.estacionamento.entity.VeiculoEntity;
import br.gov.sp.etec.estacionamento.repository.VeiculoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class RelatorioController {

    @Autowired
    private VeiculoRepository veiculoRepository;

    @GetMapping("/relatorios")
    public String relatorios(
            @RequestParam(defaultValue = "semanal") String periodo,
            @RequestParam(defaultValue = "veiculos") String tipoDado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            Model model) {

        LocalDate hoje = LocalDate.now();

        LocalDate inicioData = dataInicio != null
                ? dataInicio
                : ("mensal".equals(periodo)
                ? hoje.withDayOfMonth(1)
                : hoje.minusDays(6));

        LocalDate fimData = dataFim != null
                ? dataFim
                : ("mensal".equals(periodo)
                ? hoje.with(TemporalAdjusters.lastDayOfMonth())
                : hoje);

        LocalDateTime inicio = inicioData.atStartOfDay();
        LocalDateTime fim = fimData.atTime(23, 59, 59);

        List<VeiculoEntity> veiculos =
                veiculoRepository.findByDataEntradaBetween(inicio, fim);

        long totalVisitas = veiculos.size();
        long dias = Math.max(1,
                java.time.temporal.ChronoUnit.DAYS.between(inicioData, fimData) + 1);

        model.addAttribute("totalPeriodo", totalVisitas);
        model.addAttribute("tempoMedio", calcularTempoMedio(veiculos));
        model.addAttribute("mediaDiaria",
                String.format(Locale.US, "%.1f", (double) totalVisitas / dias));

        Map<String, Number> dados = criarDadosGrafico(veiculos, periodo, tipoDado);

        model.addAttribute("labelsGrafico", new ArrayList<>(dados.keySet()));
        model.addAttribute("valoresGrafico", new ArrayList<>(dados.values()));

        model.addAttribute("veiculosFrequentes",
                formatarVeiculosFrequentes());

        model.addAttribute("periodoAtual", periodo);
        model.addAttribute("tipoDadoAtual", tipoDado);
        model.addAttribute("dataInicio", dataInicio != null ? dataInicio.toString() : "");
        model.addAttribute("dataFim", dataFim != null ? dataFim.toString() : "");

        return "relatorios";
    }

    private String calcularTempoMedio(List<VeiculoEntity> veiculos) {
        List<VeiculoEntity> comSaida = veiculos.stream()
                .filter(v -> v.getDataEntrada() != null && v.getDataSaida() != null)
                .toList();

        if (comSaida.isEmpty()) {
            return "0h 0m";
        }

        long totalMinutos = comSaida.stream()
                .mapToLong(v -> Duration.between(
                        v.getDataEntrada(),
                        v.getDataSaida()
                ).toMinutes())
                .sum();

        return formatarTempo(totalMinutos / comSaida.size());
    }

    private Map<String, Number> criarDadosGrafico(
            List<VeiculoEntity> veiculos,
            String periodo,
            String tipoDado) {

        Map<String, Number> dados = new LinkedHashMap<>();

        if ("mensal".equals(periodo)) {
            for (int i = 1; i <= 4; i++) {
                String semana = "Semana " + i;

                int finalI = i;
                List<VeiculoEntity> lista = veiculos.stream()
                        .filter(v -> v.getDataEntrada() != null)
                        .filter(v -> {
                            int semanaVeiculo =
                                    (v.getDataEntrada().getDayOfMonth() - 1) / 7 + 1;
                            return Math.min(semanaVeiculo, 4) == finalI;
                        })
                        .toList();

                dados.put(semana, calcularMetrica(lista, tipoDado));
            }
        } else {
            String[] dias = {"DOM", "SEG", "TER", "QUA", "QUI", "SEX", "SAB"};

            for (String dia : dias) {
                List<VeiculoEntity> lista = veiculos.stream()
                        .filter(v -> v.getDataEntrada() != null)
                        .filter(v -> converterDia(v.getDataEntrada().getDayOfWeek()).equals(dia))
                        .toList();

                dados.put(dia, calcularMetrica(lista, tipoDado));
            }
        }

        return dados;
    }

    private Number calcularMetrica(
            List<VeiculoEntity> veiculos,
            String tipoDado) {

        if (!"tempo".equals(tipoDado)) {
            return (long) veiculos.size();
        }

        List<VeiculoEntity> comSaida = veiculos.stream()
                .filter(v -> v.getDataEntrada() != null && v.getDataSaida() != null)
                .toList();

        if (comSaida.isEmpty()) {
            return 0.0;
        }

        long totalMinutos = comSaida.stream()
                .mapToLong(v -> Duration.between(
                        v.getDataEntrada(),
                        v.getDataSaida()
                ).toMinutes())
                .sum();

        return (double) totalMinutos / comSaida.size() / 60.0;
    }

    private List<Object[]> formatarVeiculosFrequentes() {
        List<Object[]> resultados =
                veiculoRepository.findVeiculosFrequentesComTempos();

        List<Object[]> lista = new ArrayList<>();

        for (int i = 0; i < Math.min(resultados.size(), 10); i++) {
            Object[] r = resultados.get(i);

            String placa = (String) r[0];
            String modelo = capitalizar((String) r[1]);
            Long visitas = ((Number) r[2]).longValue();

            Number media = (Number) r[3];
            Number total = (Number) r[4];

            String tempoMedio = media != null
                    ? formatarTempo(Math.round(media.doubleValue()))
                    : "0h 0m";

            String tempoTotal = total != null
                    ? formatarTempo(total.longValue())
                    : "0h 0m";

            lista.add(new Object[]{
                    placa,
                    modelo,
                    visitas,
                    tempoMedio,
                    tempoTotal
            });
        }

        return lista;
    }

    private String converterDia(java.time.DayOfWeek dia) {
        return switch (dia) {
            case SUNDAY -> "DOM";
            case MONDAY -> "SEG";
            case TUESDAY -> "TER";
            case WEDNESDAY -> "QUA";
            case THURSDAY -> "QUI";
            case FRIDAY -> "SEX";
            case SATURDAY -> "SAB";
        };
    }

    private String formatarTempo(long minutos) {
        return (minutos / 60) + "h " + (minutos % 60) + "m";
    }

    private String capitalizar(String texto) {
        if (texto == null || texto.isBlank()) {
            return texto;
        }

        return Arrays.stream(texto.toLowerCase().split("\\s+"))
                .map(p -> Character.toUpperCase(p.charAt(0)) + p.substring(1))
                .collect(Collectors.joining(" "));
    }
}
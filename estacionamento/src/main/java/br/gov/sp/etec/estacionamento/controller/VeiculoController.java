package br.gov.sp.etec.estacionamento.controller;

import br.gov.sp.etec.estacionamento.model.Veiculo;
import br.gov.sp.etec.estacionamento.service.VeiculoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

@Controller
public class VeiculoController {

    @Autowired
    private VeiculoService veiculoService;

    @GetMapping("/painel")
    public String painelPage(Model model) {
        model.addAttribute("veiculos", veiculoService.listaVeiculos());
        model.addAttribute("vagasOcupadas", veiculoService.contarVagasOcupadas());
        model.addAttribute("vagasDisponiveis", veiculoService.contarVagasLivres());
        model.addAttribute("totalVagas", 30);
        return "painel";
    }

    @GetMapping("/veiculos/entrada")
    public String entradaPage(Model model) {
        model.addAttribute("veiculo", new Veiculo());
        return "entrada";
    }

    @PostMapping("/veiculos/salvar")
    public String salvarVeiculo(@ModelAttribute Veiculo veiculo) {
        veiculoService.cadastrarVeiculo(veiculo);
        return "redirect:/painel";
    }

    @GetMapping("/veiculos/saida")
    public String saidaPage(Model model) {
        model.addAttribute("veiculosEstacionados", veiculoService.listaVeiculos());
        return "saida";
    }

    @PostMapping("/veiculos/saida/{id}")
    public String registrarSaidaPost(@PathVariable("id") Long id) {
        veiculoService.registrarSaida(id);
        return "redirect:/painel";
    }

    @GetMapping("/registrar-saida")
    public String registrarSaidaGet(@RequestParam("id") Long id) {
        veiculoService.registrarSaida(id);
        return "redirect:/painel";
    }

    @GetMapping("/deletar-veiculo")
    public String deletarVeiculo(@RequestParam("id") Long id) {
        veiculoService.deletarVeiculo(id);
        return "redirect:/painel";
    }

    @GetMapping("/movimentacoes")
    public String movimentacoesPage(
            @RequestParam(value = "periodo", required = false) String periodo,
            @RequestParam(value = "data", required = false) String dataStr,
            Model model) {

        LocalDate hoje = LocalDate.now();
        LocalDateTime inicio;
        LocalDateTime fim;
        String filtroAtivo = "hoje";

        if (periodo != null && !periodo.isEmpty()) {
            filtroAtivo = periodo;
            switch (periodo) {
                case "semana":
                    LocalDate inicioSemana = hoje.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
                    LocalDate fimSemana = hoje.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
                    inicio = inicioSemana.atStartOfDay();
                    fim = fimSemana.atTime(LocalTime.MAX);
                    break;
                case "mes":
                    LocalDate inicioMes = hoje.with(TemporalAdjusters.firstDayOfMonth());
                    LocalDate fimMes = hoje.with(TemporalAdjusters.lastDayOfMonth());
                    inicio = inicioMes.atStartOfDay();
                    fim = fimMes.atTime(LocalTime.MAX);
                    break;
                case "hoje":
                default:
                    inicio = hoje.atStartOfDay();
                    fim = hoje.atTime(LocalTime.MAX);
                    filtroAtivo = "hoje";
                    break;
            }
        } else if (dataStr != null && !dataStr.trim().isEmpty()) {
            filtroAtivo = "data";
            LocalDate dataSelecionada = LocalDate.parse(dataStr);
            inicio = dataSelecionada.atStartOfDay();
            fim = dataSelecionada.atTime(LocalTime.MAX);
            model.addAttribute("dataSelecionada", dataStr);
        } else {
            inicio = hoje.atStartOfDay();
            fim = hoje.atTime(LocalTime.MAX);
            filtroAtivo = "hoje";
        }

        model.addAttribute("movimentacoes", veiculoService.listarTodasMovimentacoesIntervalo(inicio, fim));
        model.addAttribute("filtroAtivo", filtroAtivo);
        return "movimentacoes";
    }
}
package br.gov.sp.etec.estacionamento.controller;

import br.gov.sp.etec.estacionamento.model.Usuario;
import br.gov.sp.etec.estacionamento.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/cadastro")
    public String cadastroPage() {
        return "cadastro";
    }

    @PostMapping("/efetuar-cadastro")
    public String efetuarCadastro(@ModelAttribute Usuario usuario, Model model) {
        try {
            String resultado = usuarioService.cadastrarUsuario(usuario);

            if (resultado != null && !resultado.toLowerCase().contains("sucesso") && !resultado.toLowerCase().contains("cadastrado")) {
                model.addAttribute("erro", resultado);
                return "cadastro";
            }
        } catch (Exception e) {
            model.addAttribute("erro", "Ocorreu um erro ao realizar o cadastro. Verifique os dados.");
            return "cadastro";
        }

        model.addAttribute("sucesso", "Cadastro realizado com sucesso!");
        return "cadastro";
    }

    @PostMapping("/autenticar")
    public String autenticar(@RequestParam("email") String email,
                             @RequestParam("senha") String senha,
                             Model model) {

        boolean autenticado = usuarioService.autenticar(email, senha);

        if (autenticado) {
            return "redirect:/painel";
        } else {
            model.addAttribute("erro", "E-mail ou senha inválidos!");
            return "login";
        }
    }
}
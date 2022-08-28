package com.marco.cartorios.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marco.cartorios.domain.Cartorio;
import com.marco.cartorios.domain.CartorioCertidao;
import com.marco.cartorios.repositories.CartorioCertidaoRepository;
import com.marco.cartorios.repositories.CartorioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Controller
public class CartorioController {

    private final CartorioRepository cartorioRepository;
    private final CartorioCertidaoRepository cartorioCertidaoRepository;

    public CartorioController(CartorioRepository cartorioRepository, CartorioCertidaoRepository cartorioCertidaoRepository) {
        this.cartorioRepository = cartorioRepository;
        this.cartorioCertidaoRepository = cartorioCertidaoRepository;
    }

    @GetMapping("/")
    public ModelAndView home(Model model) {
        ModelAndView mv = new ModelAndView("home");
        List<Cartorio> cartorios = cartorioRepository.findAll();
        mv.addObject("cartorios", cartorios);
        return mv;
    }

    @GetMapping("/form")
    public ModelAndView cartorioForm(Model model) throws JsonProcessingException {
        ModelAndView mv = new ModelAndView("addCartorioForm");
        List<HashMap<String, Object>> cartorioCertidaos = getCertidoesAPI();

        List<HashMap<String, Object>> list = new ArrayList<>();
        for (HashMap<String, Object> cartorioCertidao : cartorioCertidaos) {
            HashMap<String, Object> cartorioCertidaosMap = new HashMap<>();
            cartorioCertidaosMap.put("id", cartorioCertidao.get("id"));
            cartorioCertidaosMap.put("nome", cartorioCertidao.get("nome"));
            cartorioCertidaosMap.put("checked", false);
            list.add(cartorioCertidaosMap);
        }
        mv.addObject("cartorio", new Cartorio());
        mv.addObject("certidoes", list);

        return mv;
    }

    private List<HashMap<String, Object>> getCertidoesAPI() throws JsonProcessingException {
        String uri = "https://docketdesafiobackend.herokuapp.com/api/v1/certidoes";
        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject(uri, String.class);


        ObjectMapper objectMapper = new ObjectMapper();

        List<HashMap<String, Object>> cartorioCertidaos = objectMapper.readValue(result, new TypeReference<>() {
        });
        return cartorioCertidaos;
    }

    @PostMapping("/add")
    public ModelAndView novo(@Validated Cartorio cartorio, BindingResult result, @RequestParam(value = "cers", required = false) int[] cers, RedirectAttributes redirectAttributes) {

        if (!validarDadosCartorio(cartorio, cers)) {
            return new ModelAndView("addCartorioForm");
        }

        salvarDados(cartorio, cers);

        redirectAttributes.addFlashAttribute("mensagem", "Cartorio salvo com sucesso!");

        return new ModelAndView("redirect:/");
    }

    private void salvarDados(Cartorio cartorio, int[] cers) {
        try {
            Cartorio cartorio1 = cartorioRepository.save(cartorio);

            if (cers != null) {
                CartorioCertidao cartorioCertidao = null;
                for (int i = 0; i < cers.length; i++) {
                    cartorioCertidao = new CartorioCertidao();
                    cartorioCertidao.setIdCertidao(cers[i]);
                    cartorioCertidao.setCartorio(cartorio1);
                    cartorioCertidaoRepository.save(cartorioCertidao);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Boolean validarDadosCartorio(Cartorio cartorio, int[] cers) {
        if (cartorio.getNome().isEmpty() || cartorio.getNome().isBlank()) {
            //redirectAttributes.addFlashAttribute("error", "Nome inválido");
            return false;
        }

        if (cartorio.getEndereco().isEmpty() || cartorio.getEndereco().isBlank()) {
            // redirectAttributes.addFlashAttribute("error", "Endereço inválido");
            return false;
        }

        return cers != null && cers.length != 0;
    }

    @GetMapping("view/{id}")
    public String viewForm(Model model, @PathVariable(name = "id") int id) throws JsonProcessingException {

        List<HashMap<String, Object>> cartorioCertidaos = getCertidoesAPI();


        Cartorio cartorio = cartorioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        List<HashMap<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < cartorioCertidaos.size(); i++) {
            HashMap<String, Object> cartorioCertidaosMap = new HashMap<>();
            cartorioCertidaosMap.put("id", cartorioCertidaos.get(i).get("id"));
            cartorioCertidaosMap.put("nome", cartorioCertidaos.get(i).get("nome"));
            for (CartorioCertidao cartorioCertidao : cartorio.getCertidoes()) {
                if (cartorioCertidao.getIdCertidao() == (Integer) cartorioCertidaosMap.get("id")) {
                    cartorio.getCertidoes().remove(cartorioCertidao);
                    cartorioCertidaosMap.put("checked", true);
                    list.add(cartorioCertidaosMap);
                    break;
                }
            }

        }
        model.addAttribute("cartorio", cartorio);
        model.addAttribute("certidoes", list);

        return "viewForm";
    }

    @GetMapping("form/{id}")
    public String updateForm(Model model, @PathVariable(name = "id") int id) throws JsonProcessingException {

        List<HashMap<String, Object>> cartorioCertidaos = getCertidoesAPI();


        Cartorio cartorio = cartorioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        List<HashMap<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < cartorioCertidaos.size(); i++) {
            HashMap<String, Object> cartorioCertidaosMap = new HashMap<>();
            cartorioCertidaosMap.put("id", cartorioCertidaos.get(i).get("id"));
            cartorioCertidaosMap.put("nome", cartorioCertidaos.get(i).get("nome"));
            cartorioCertidaosMap.put("checked", false);
            for (CartorioCertidao cartorioCertidao : cartorio.getCertidoes()) {
                if (cartorioCertidao.getIdCertidao() == (Integer) cartorioCertidaosMap.get("id")) {
                    cartorioCertidaosMap.put("checked", true);
                    cartorio.getCertidoes().remove(cartorioCertidao);
                    break;
                }
            }
            list.add(cartorioCertidaosMap);
        }
        model.addAttribute("cartorio", cartorio);
        model.addAttribute("certidoes", list);

        return "atualizaForm";
    }

    @PostMapping("update/{id}")
    public String alterarCartorios(@Valid Cartorio cartorio, BindingResult result, @PathVariable int id, @RequestParam(value = "cers", required = false) int[] cers) {

        if (!validarDadosCartorio(cartorio, cers)) {
            return "redirect:/form/" + id;
        }

        salvarDados(cartorio, cers);

        return "redirect:/";
    }



    @GetMapping(value = "/delete/{id}")
    @Transactional
    public ModelAndView excluir(@PathVariable("id") int id) {
        cartorioCertidaoRepository.deleteCartorioCertidaoByCartorio_Id(id);
        cartorioRepository.deleteById(id);
        return new ModelAndView("redirect:/");
    }


}

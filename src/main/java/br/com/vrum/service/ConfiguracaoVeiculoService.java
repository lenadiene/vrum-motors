package br.com.vrum.service;

import br.com.vrum.dao.ConfiguracaoVeiculoDAO;
import br.com.vrum.model.ConfiguracaoVeiculo;
import br.com.vrum.model.TipoConfiguracaoVeiculo;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ConfiguracaoVeiculoService {

    private final ConfiguracaoVeiculoDAO dao = new ConfiguracaoVeiculoDAO();

    public List<String> listarValores(TipoConfiguracaoVeiculo tipo) {
        return dao.listarPorTipo(tipo).stream()
                .map(ConfiguracaoVeiculo::getValor)
                .collect(Collectors.toList());
    }

    public void salvarTipo(TipoConfiguracaoVeiculo tipo, List<String> valores) {
        dao.excluirPorTipo(tipo);
        int ordem = 0;
        for (String v : valores) {
            if (v != null && !v.trim().isEmpty()) {
                ConfiguracaoVeiculo cfg = new ConfiguracaoVeiculo();
                cfg.setTipo(tipo);
                cfg.setValor(v.trim());
                cfg.setOrdem(ordem++);
                dao.salvar(cfg);
            }
        }
    }

    public boolean existeAlgum() {
        return dao.existeAlgum();
    }

    public void popularInicial() {
        salvarTipo(TipoConfiguracaoVeiculo.ANO, Arrays.asList(
                "2025", "2026", "2027"));
        salvarTipo(TipoConfiguracaoVeiculo.MARCA, Arrays.asList(
                "Vrum"));
        salvarTipo(TipoConfiguracaoVeiculo.MOTOR, Arrays.asList(
                "1.0", "1.0 Turbo", "1.3 Turbo", "1.4 Turbo", "1.5",
                "1.6", "2.0", "2.0 Turbo", "3.0", "4.0", "Elétrico"));
        salvarTipo(TipoConfiguracaoVeiculo.COMBUSTIVEL, Arrays.asList(
                "Flex (Gasolina/Álcool)", "Gasolina", "Etanol", "Diesel", "Híbrido", "Elétrico"));
        salvarTipo(TipoConfiguracaoVeiculo.TRANSMISSAO, Arrays.asList(
                "Manual", "Automático", "Automatizado", "CVT"));
        salvarTipo(TipoConfiguracaoVeiculo.TRACAO, Arrays.asList(
                "4x2 (Dianteira)", "4x2 (Traseira)", "4x4", "AWD (Integral)"));
    }
}

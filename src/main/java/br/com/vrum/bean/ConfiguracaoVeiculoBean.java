package br.com.vrum.bean;

import br.com.vrum.model.TipoConfiguracaoVeiculo;
import br.com.vrum.service.ConfiguracaoVeiculoService;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named("configuracaoVeiculoBean")
@ViewScoped
public class ConfiguracaoVeiculoBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<String> opcoesMotor       = new ArrayList<>();
    private List<String> opcoesCombustivel = new ArrayList<>();
    private List<String> opcoesTransmissao = new ArrayList<>();
    private List<String> opcoesTracao      = new ArrayList<>();

    private String novoMotor       = "";
    private String novoCombustivel = "";
    private String novaTransmissao = "";
    private String novaTracao      = "";

    private final ConfiguracaoVeiculoService service = new ConfiguracaoVeiculoService();

    @PostConstruct
    public void init() {
        opcoesMotor       = new ArrayList<>(service.listarValores(TipoConfiguracaoVeiculo.MOTOR));
        opcoesCombustivel = new ArrayList<>(service.listarValores(TipoConfiguracaoVeiculo.COMBUSTIVEL));
        opcoesTransmissao = new ArrayList<>(service.listarValores(TipoConfiguracaoVeiculo.TRANSMISSAO));
        opcoesTracao      = new ArrayList<>(service.listarValores(TipoConfiguracaoVeiculo.TRACAO));
    }

    // ---- Adicionar ----
    public void adicionarMotor() {
        adicionar(opcoesMotor, novoMotor, "Motor");
        novoMotor = "";
    }

    public void adicionarCombustivel() {
        adicionar(opcoesCombustivel, novoCombustivel, "Combustível");
        novoCombustivel = "";
    }

    public void adicionarTransmissao() {
        adicionar(opcoesTransmissao, novaTransmissao, "Transmissão");
        novaTransmissao = "";
    }

    public void adicionarTracao() {
        adicionar(opcoesTracao, novaTracao, "Tração");
        novaTracao = "";
    }

    private void adicionar(List<String> lista, String valor, String categoria) {
        if (valor == null || valor.trim().isEmpty()) {
            addErro("Informe um valor para " + categoria + ".");
            return;
        }
        String v = valor.trim();
        if (lista.contains(v)) {
            addErro("\"" + v + "\" já existe em " + categoria + ".");
            return;
        }
        lista.add(v);
    }

    // ---- Remover ----
    public void removerMotor(String v)       { opcoesMotor.remove(v); }
    public void removerCombustivel(String v) { opcoesCombustivel.remove(v); }
    public void removerTransmissao(String v) { opcoesTransmissao.remove(v); }
    public void removerTracao(String v)      { opcoesTracao.remove(v); }

    // ---- Salvar e Voltar ----
    public String salvarEVoltar() {
        service.salvarTipo(TipoConfiguracaoVeiculo.MOTOR,       opcoesMotor);
        service.salvarTipo(TipoConfiguracaoVeiculo.COMBUSTIVEL, opcoesCombustivel);
        service.salvarTipo(TipoConfiguracaoVeiculo.TRANSMISSAO, opcoesTransmissao);
        service.salvarTipo(TipoConfiguracaoVeiculo.TRACAO,      opcoesTracao);
        return "/pages/admin/veiculos.xhtml?faces-redirect=true";
    }

    public String cancelar() {
        return "/pages/admin/veiculos.xhtml?faces-redirect=true";
    }

    private void addErro(String msg) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
    }

    // ---- Getters / Setters ----
    public List<String> getOpcoesMotor()              { return opcoesMotor; }
    public List<String> getOpcoesCombustivel()        { return opcoesCombustivel; }
    public List<String> getOpcoesTransmissao()        { return opcoesTransmissao; }
    public List<String> getOpcoesTracao()             { return opcoesTracao; }

    public String getNovoMotor()                      { return novoMotor; }
    public void   setNovoMotor(String v)              { this.novoMotor = v; }

    public String getNovoCombustivel()                { return novoCombustivel; }
    public void   setNovoCombustivel(String v)        { this.novoCombustivel = v; }

    public String getNovaTransmissao()                { return novaTransmissao; }
    public void   setNovaTransmissao(String v)        { this.novaTransmissao = v; }

    public String getNovaTracao()                     { return novaTracao; }
    public void   setNovaTracao(String v)             { this.novaTracao = v; }
}

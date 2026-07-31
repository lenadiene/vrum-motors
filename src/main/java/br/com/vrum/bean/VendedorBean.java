package br.com.vrum.bean;

import br.com.vrum.model.*;
import br.com.vrum.service.PedidoService;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Named("vendedorBean")
@ViewScoped
public class VendedorBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Vendedor vendedor;
    private List<Pedido> pedidosDisponiveis;
    private List<Pedido> meusPedidos;
    private Pedido pedidoSelecionado;
    private List<Anexo> anexosPedidoSelecionado = Collections.emptyList();

    // Variaveis de tela
    private Integer prazoEmDias; 
    private LocalDate prazoEntrega;
    private LocalDate dataRetirada;
    private String formaPagamento;
    private String observacao;
    private Part arquivoAnexo;


    private final PedidoService service = new PedidoService();

    @PostConstruct
    public void init() {
        Object obj = FacesContext.getCurrentInstance()
                .getExternalContext().getSessionMap().get("usuarioLogado");
        if (obj instanceof Vendedor) {
            vendedor = (Vendedor) obj;
            carregarPedidos();
        }
    }

    public void carregarPedidos() {
        pedidosDisponiveis = service.listarDisponiveisPorConcessionaria(vendedor.getConcessionaria());
        meusPedidos = service.listarPorVendedor(vendedor);
    }

    public void cancelarSelecao() {
        this.pedidoSelecionado = null;
        this.anexosPedidoSelecionado = Collections.emptyList();
    }

    private List<Pedido> filtrarMeus(StatusPedido s) {
        if (meusPedidos == null) return Collections.emptyList();
        return meusPedidos.stream().filter(p -> p.getStatus() == s).collect(Collectors.toList());
    }

    public boolean isMostrarModal()        { return pedidoSelecionado != null; }
    public void   setMostrarModal(boolean v) { /* derivado de pedidoSelecionado */ }

    public List<Pedido> getMeusEmNegociacao()         { return filtrarMeus(StatusPedido.EM_NEGOCIACAO); }
    public List<Pedido> getMeusAguardandoFabricacao() { return filtrarMeus(StatusPedido.AGUARDANDO_FABRICACAO); }
    public List<Pedido> getMeusEmFabricacao()         { return filtrarMeus(StatusPedido.EM_FABRICACAO); }
    public List<Pedido> getMeusFabricados()           { return filtrarMeus(StatusPedido.FABRICADO); }
    public List<Pedido> getMeusEnviados()             { return filtrarMeus(StatusPedido.ENVIADO_CIDADE); }
    public List<Pedido> getMeusProntos()              { return filtrarMeus(StatusPedido.PRONTO_ENTREGA); }
    public List<Pedido> getMeusFinalizados()          { return filtrarMeus(StatusPedido.FINALIZADO); }

    public void assumirPedido(Pedido pedido) {
        try {
            service.assumirPedido(pedido, vendedor);
            addSucesso("Pedido assumido com sucesso! Entre em contato com o cliente.");
            carregarPedidos();
        } catch (Exception e) {
            addErro(e.getMessage());
        }
    }

    public void selecionarPedido(Pedido pedido) {
        this.pedidoSelecionado = service.buscarPorId(pedido.getId());
        this.anexosPedidoSelecionado = service.listarAnexos(pedidoSelecionado);
        this.formaPagamento = pedidoSelecionado.getFormaPagamento();
        
        
        this.prazoEmDias = null;
        this.arquivoAnexo = null;
    }

    public void enviarParaFabricacao() {
        try {
            LocalDate dataCalculada = null;
            if (this.prazoEmDias != null) {
                dataCalculada = LocalDate.now().plusDays(this.prazoEmDias);
            }

            service.enviarParaFabricacao(pedidoSelecionado, dataCalculada, formaPagamento);
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            addErro(e.getMessage());
            return;
        } catch (Exception e) {
            addErro("Ocorreu um erro inesperado: " + e.getMessage());
            return;
        }

        if (arquivoAnexo != null && arquivoAnexo.getSize() > 0) {
            try {
                processarUpload();
                addSucesso("Pedido enviado para fabricação com contrato anexado!");
            } catch (Exception e) {
                addSucesso("Pedido enviado para fabricação.");
                addErro("Não foi possível salvar o contrato: " +
                        (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
            }
        } else {
            addSucesso("Pedido enviado para fabricação!");
        }

        carregarPedidos();
        pedidoSelecionado = null;
        anexosPedidoSelecionado = Collections.emptyList();
        arquivoAnexo = null;
        formaPagamento = null;
        prazoEmDias = null;
    }

    public void marcarProntoEntrega() {
        try {
            service.marcarProntoEntrega(pedidoSelecionado);
            addSucesso("Veículo marcado como pronto para entrega!");
            carregarPedidos();
            pedidoSelecionado = null;
            anexosPedidoSelecionado = Collections.emptyList();
        } catch (Exception e) {
            addErro(e.getMessage());
        }
    }

    public void finalizarPedido() {
        try {
            service.finalizarPedido(pedidoSelecionado, dataRetirada);
            addSucesso("Pedido finalizado com sucesso!");
            carregarPedidos();
            pedidoSelecionado = null;
            anexosPedidoSelecionado = Collections.emptyList();
        } catch (Exception e) {
            addErro(e.getMessage());
        }
    }

    private void processarUpload() throws Exception {
        String nomeArq = Paths.get(arquivoAnexo.getSubmittedFileName()).getFileName().toString();
        String uploadDir = FacesContext.getCurrentInstance()
                .getExternalContext().getRealPath("/uploads/contratos/");
        if (uploadDir == null) {
            uploadDir = System.getProperty("java.io.tmpdir") + File.separator + "vrum-contratos";
        }
        new File(uploadDir).mkdirs();
        String caminho = uploadDir + File.separator + System.currentTimeMillis() + "_" + nomeArq;
        try (InputStream is = arquivoAnexo.getInputStream()) {
            Files.copy(is, Paths.get(caminho));
        }
        service.adicionarAnexo(pedidoSelecionado.getId(), nomeArq, caminho,
                arquivoAnexo.getContentType(), arquivoAnexo.getSize());
    }

    public void downloadAnexo(Anexo anexo) {
        FacesContext fc = FacesContext.getCurrentInstance();
        try {
            File file = new File(anexo.getCaminhoArquivo());
            if (!file.exists()) {
                addErro("Arquivo não encontrado no servidor: " + anexo.getNomeArquivo());
                return;
            }
            String contentType = (anexo.getTipoArquivo() != null && !anexo.getTipoArquivo().isBlank())
                    ? anexo.getTipoArquivo() : "application/octet-stream";
            jakarta.faces.context.ExternalContext ec = fc.getExternalContext();
            ec.responseReset();
            ec.setResponseContentType(contentType);
            ec.setResponseContentLength((int) file.length());
            ec.setResponseHeader("Content-Disposition",
                    "attachment; filename=\"" + anexo.getNomeArquivo() + "\"");
            try (FileInputStream in = new FileInputStream(file);
                 OutputStream out = ec.getResponseOutputStream()) {
                byte[] buf = new byte[8192];
                int read;
                while ((read = in.read(buf)) != -1) {
                    out.write(buf, 0, read);
                }
            }
            fc.responseComplete();
        } catch (Exception e) {
            addErro("Erro ao baixar arquivo: " +
                    (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }

  public String gerarLinkWhatsapp(Pedido pedido) {
        String tel = pedido.getCliente().getTelefone();
        if (tel == null) return "#";
        tel = tel.replaceAll("[^0-9]", "");
        
        // Saudação padrão inicial com os nomes
        String saudacao = "Olá, " + pedido.getCliente().getNome() + "! Sou " + vendedor.getNome() + " da Vrum Motors. ";
        String msgStatus = "";

        // Define o texto de acordo com o status atual daquele pedido
        if (pedido.getStatus() != null) {
            switch (pedido.getStatus()) {
                case AGUARDANDO_ATENDIMENTO:
                    msgStatus = "Recebemos o seu pedido para o " + pedido.getVeiculo().getNome() + " e serei o responsável por te atender!";
                    break;
                case EM_NEGOCIACAO:
                    msgStatus = "Estou entrando em contato para definirmos os detalhes de pagamento e prazos do seu " + pedido.getVeiculo().getNome() + ".";
                    break;
                case AGUARDANDO_FABRICACAO:
                    msgStatus = "O seu contrato foi aprovado! O pedido do seu " + pedido.getVeiculo().getNome() + " já foi enviado para a fábrica e entrará na fila de montagem.";
                    break;
                case EM_FABRICACAO:
                    msgStatus = "Ótima notícia: seu " + pedido.getVeiculo().getNome() + " já está na linha de produção sendo montado. Falta pouco!";
                    break;
                case FABRICADO:
                    msgStatus = "O seu " + pedido.getVeiculo().getNome() + " acabou de sair da linha de produção e está prontinho no pátio da fábrica!";
                    break;
                case ENVIADO_CIDADE:
                    msgStatus = "O caminhão cegonha já está na estrada! Seu " + pedido.getVeiculo().getNome() + " está a caminho da nossa concessionária.";
                    break;
                case PRONTO_ENTREGA:
                    msgStatus = "Tudo pronto! Seu " + pedido.getVeiculo().getNome() + " já está aqui na loja, lavado e preparado. Podemos agendar a retirada?";
                    break;
                case FINALIZADO:
                    msgStatus = "Parabéns novamente pela conquista do seu " + pedido.getVeiculo().getNome() + "! Caso necessários, estamos a disposição.";
                    break;
                case CANCELADO:
                    msgStatus = "Vi que o seu pedido (Nº " + pedido.getNumeroPedido() + ") foi cancelado. Posso ajudar em algo ou tirar alguma dúvida?";
                    break;
                default:
                    msgStatus = "Estou em contato sobre o seu pedido do " + pedido.getVeiculo().getNome() + " (Pedido: " + pedido.getNumeroPedido() + ").";
                    break;
            }
        } else {
            msgStatus = "Estou em contato sobre o seu pedido do " + pedido.getVeiculo().getNome() + " (Pedido: " + pedido.getNumeroPedido() + ").";
        }

        // Concatena a saudação com a mensagem específica
        String msgFinal = saudacao + msgStatus;

   
        try {
            return "https://wa.me/55" + tel + "?text=" +
                    java.net.URLEncoder.encode(msgFinal, "UTF-8");
        } catch (Exception e) {
            return "https://wa.me/55" + tel;
        }
    }

    private void addErro(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", msg));
    }

    private void addSucesso(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", msg));
    }

    // Getters e Setters
    public List<Pedido> getPedidosDisponiveis() { return pedidosDisponiveis; }
    public List<Pedido> getMeusPedidos() { return meusPedidos; }
    public Pedido getPedidoSelecionado() { return pedidoSelecionado; }
    public void setPedidoSelecionado(Pedido p) { this.pedidoSelecionado = p; }
    public List<Anexo> getAnexosPedidoSelecionado() { return anexosPedidoSelecionado; }
    
    
    public Integer getPrazoEmDias() { return prazoEmDias; }
    public void setPrazoEmDias(Integer d) { this.prazoEmDias = d; }
    
    public LocalDate getPrazoEntrega() { return prazoEntrega; }
    public void setPrazoEntrega(LocalDate d) { this.prazoEntrega = d; }
    public LocalDate getDataRetirada() { return dataRetirada; }
    public void setDataRetirada(LocalDate d) { this.dataRetirada = d; }
    public String getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(String f) { this.formaPagamento = f; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String o) { this.observacao = o; }
    public Part getArquivoAnexo() { return arquivoAnexo; }
    public void setArquivoAnexo(Part p) { this.arquivoAnexo = p; }
    public Vendedor getVendedor() { return vendedor; }
}

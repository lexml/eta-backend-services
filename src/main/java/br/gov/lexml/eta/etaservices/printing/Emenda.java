package br.gov.lexml.eta.etaservices.printing;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="emenda")
public record Emenda(

        @XmlAttribute Instant dataUltimaModificacao,
        @XmlAttribute String aplicacao,
        @XmlAttribute String versaoAplicacao,
        @XmlAttribute ModoEdicaoEmenda modoEdicao,
        @XmlElement Map<String, Object> metadados,
        @XmlElement RefProposicaoEmendada proposicao,
        @XmlElement ColegiadoApreciador colegiado,
        @XmlElement Epigrafe epigrafe,
        @XmlElement List<ComponenteEmendado> componentes,
        @XmlElement ComandoEmenda comandoEmenda,
        @XmlElement String justificativa,
        @XmlElement String local,
        @XmlAttribute Optional<String> data,
        @XmlElement Autoria autoria,
        @XmlElement OpcoesImpressao opcoesImpressao
) {
}

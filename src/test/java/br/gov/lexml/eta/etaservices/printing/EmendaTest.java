package br.gov.lexml.eta.etaservices.printing;


import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static br.gov.lexml.eta.etaservices.printing.SiglaCasaLegislativa.CD;
import static br.gov.lexml.eta.etaservices.printing.TipoAutoria.PARLAMENTAR;
import static br.gov.lexml.eta.etaservices.printing.TipoColegiado.PLENARIO;
import static org.assertj.core.api.Assertions.assertThat;

class EmendaTest {

    @Test
    void test() throws JAXBException {

        final var emenda = new Emenda(
                "2022-06-01",
                "aplicacao",
                "versaoAplicacao",
                ModoEdicaoEmenda.EMENDA,
                Map.of(),
                new RefProposicaoEmendada("1", "MPV", "200", "2022", "aaa", "bcd"),
                new ColegiadoApreciador(CD, PLENARIO, null),
                new Epigrafe("abc",""),
                List.of(),
                new ComandoEmenda(Optional.empty(), List.of()),
                "justificativa emenda",
                "Brasilia",
                "2019-01-01",
                new Autoria(PARLAMENTAR, true, 0, 0, List.of(), Optional.empty()),
                new OpcoesImpressao(true, "", false));

        JAXBContext.newInstance(Emenda.class)
                .createMarshaller().marshal(emenda, System.out);

        assertThat(true).isTrue();
    }

}
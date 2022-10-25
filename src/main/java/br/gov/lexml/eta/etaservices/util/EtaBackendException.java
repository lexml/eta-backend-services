package br.gov.lexml.eta.etaservices.util;

public class EtaBackendException extends RuntimeException {

	public EtaBackendException(String message) {
		super(message);
	}

	public EtaBackendException(String message, Throwable cause) {
		super(message, cause);
	}

}

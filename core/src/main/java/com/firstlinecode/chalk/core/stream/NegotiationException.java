package com.firstlinecode.chalk.core.stream;


public class NegotiationException extends RuntimeException {

	private static final long serialVersionUID = 5834897402216642798L;
	
	private IStreamNegotiant source;
	private Object additionalErrorInfo;
	
	public NegotiationException(IStreamNegotiant source) {
		this(null, (IStreamNegotiant)source);
	}
	
	public NegotiationException(IStreamNegotiant source, Object additionalErrorInfo) {
		this(null, source, additionalErrorInfo);
	}

	public NegotiationException(String message, IStreamNegotiant source, Object additionalErrorInfo) {
		super(message);
		
		this.source = source;
		this.additionalErrorInfo = additionalErrorInfo;
	}
	
	public Object getAdditionalErrorInfo() {
		return additionalErrorInfo;
	}
	
	public IStreamNegotiant getSource() {
		return source;
	}
	
}

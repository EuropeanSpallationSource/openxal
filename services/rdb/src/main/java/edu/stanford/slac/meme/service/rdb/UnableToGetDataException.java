package edu.stanford.slac.meme.service.rdb;

public class UnableToGetDataException extends Exception {
	
	private static final long serialVersionUID = -6542023913705709994L;

	public UnableToGetDataException() {
        super();
    }

    public UnableToGetDataException(String _message) {
        super(_message);
    }

    public UnableToGetDataException(String _message, Throwable _cause) {
        super(_message, _cause);
    }

}

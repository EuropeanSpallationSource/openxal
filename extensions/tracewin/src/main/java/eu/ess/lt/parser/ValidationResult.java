package eu.ess.lt.parser;

public class ValidationResult {

	private boolean successful;
	private String message;
	private String fieldName;

	public ValidationResult(boolean successful, String message, String fieldName) {
		this.successful = successful;
		this.message = message;
		this.fieldName = fieldName;
	}

	public boolean isSuccessful() {
		return successful;
	}

	public String getMessage() {
		return message;
	}

	public String getFieldName() {
		return fieldName;
	}
}

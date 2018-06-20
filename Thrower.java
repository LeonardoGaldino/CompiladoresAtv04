package br.ufpe.cin.if688.minijava.visitor;

public final class Thrower {

    public static void throwExceptionWrapper(String message, Object... params) {
		String errorMessage = String.format(
			message,
			params
		);
		Exception e = new Exception(errorMessage);

		// Wraps Exception on runtimeException,
		// so no need to explicit 'throws Exception... modifier'
		throw new RuntimeException(e);
	}

}
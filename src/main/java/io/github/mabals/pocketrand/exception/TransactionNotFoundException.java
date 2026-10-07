package io.github.mabals.pocketrand.exception;

public class TransactionNotFoundException extends RuntimeException {

    public TransactionNotFoundException(Long id) {
        super("Transaction " + id + " not found");
    }
}

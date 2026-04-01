package ch.ubique.backend.payment;

public enum TransactionStatus {
    CREATE,
    PENDING,
    CONFIRMED,
    PROCESSING,
    FAILED,
    AUTHORIZED,
    VOIDED,
    COMPLETED,
    FULFILL,
    DECLINE
}

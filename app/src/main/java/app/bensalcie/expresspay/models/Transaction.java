package app.bensalcie.expresspay.models;

public class Transaction {
    String checkout_request_id,confirmation_code,status;
    Long transactiondate,msisdn;
    int amount;

    public String getCheckout_request_id() {
        return checkout_request_id;
    }

    public void setCheckout_request_id(String checkout_request_id) {
        this.checkout_request_id = checkout_request_id;
    }

    public String getConfirmation_code() {
        return confirmation_code;
    }

    public void setConfirmation_code(String confirmation_code) {
        this.confirmation_code = confirmation_code;
    }

    public Long getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(Long msisdn) {
        this.msisdn = msisdn;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getTransactiondate() {
        return transactiondate;
    }

    public void setTransactiondate(Long transactiondate) {
        this.transactiondate = transactiondate;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Transaction() { }


}

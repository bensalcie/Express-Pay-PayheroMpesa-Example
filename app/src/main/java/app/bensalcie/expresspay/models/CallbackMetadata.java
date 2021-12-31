package app.bensalcie.expresspay.models;

import java.util.List;

public class CallbackMetadata {
    private List<Item> Item;

    public List<app.bensalcie.expresspay.models.Item> getItem() {
        return Item;
    }

    public void setItem(List<app.bensalcie.expresspay.models.Item> item) {
        Item = item;
    }

    public CallbackMetadata(List<app.bensalcie.expresspay.models.Item> item) {
        Item = item;
    }
}

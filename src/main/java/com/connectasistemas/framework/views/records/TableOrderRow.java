package com.connectasistemas.framework.views.records;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import com.connectasistemas.framework.utils.StringUtils;

/**
 * Representa uma linha da tabela de pedidos demonstrada em TableEventsDemo.
 */
public class TableOrderRow {

    private final StringProperty code = new SimpleStringProperty();
    private final StringProperty customer = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();

    public TableOrderRow(String code, String customer, String status) {
        this.code.set(code);
        this.customer.set(customer);
        this.status.set(status);
    }

    public String getCode() {
        return code.get();
    }

    public void setCode(String value) {
        code.set(value);
    }

    public StringProperty codeProperty() {
        return code;
    }

    public String getCustomer() {
        return customer.get();
    }

    public void setCustomer(String value) {
        customer.set(value);
    }

    public StringProperty customerProperty() {
        return customer;
    }

    public String getStatus() {
        return status.get();
    }

    public void setStatus(String value) {
        status.set(value);
    }

    public StringProperty statusProperty() {
        return status;
    }

    public String describe() {
        return StringUtils.concat(getCode(), " - ", getCustomer());
    }
}

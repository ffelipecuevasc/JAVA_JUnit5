package org.fcuevas.junit;

import org.fcuevas.junit.excepciones.DineroInsuficienteException;

import java.math.BigDecimal;

public class Cuenta {
    private String cliente;
    private BigDecimal saldo;//Objeto inmutable
    private Banco banco;

    public Cuenta(String cliente, BigDecimal saldo) {
        this.cliente = cliente;
        this.saldo = saldo;
    }

    public void debito(BigDecimal monto){
        BigDecimal saldoAuxiliar = this.saldo.subtract(monto);
        if(saldoAuxiliar.compareTo(BigDecimal.ZERO) < 0) throw new DineroInsuficienteException("Dinero Insuficiente");
        this.saldo = saldoAuxiliar;
    }

    public Banco getBanco() {
        return banco;
    }

    public void setBanco(Banco banco) {
        this.banco = banco;
    }

    public void credito(BigDecimal monto){
        this.saldo = this.saldo.add(monto);
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null && !(obj instanceof Cuenta)) return false;
        if(this.cliente == null || this.saldo == null) return false;
        Cuenta c = (Cuenta) obj;
        return this.cliente.equals(c.getCliente()) && this.saldo.equals(c.getSaldo());

    }
}

package org.fcuevas.junit.excepciones;

public class DineroInsuficienteException extends RuntimeException{

    public DineroInsuficienteException(String mensaje){
        super(mensaje);
    }
}

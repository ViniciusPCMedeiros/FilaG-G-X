package com.example.demo;

public class Evento implements Comparable<Evento> {
    public static final int CHEGADA = 1;
    public static final int SAIDA = 2;

    public int tipo;
    public double tempo;
    public Fila fila;
    public boolean externalArrival; // true se veio do exterior (chegada externa), false se é passagem interna

    public Evento(int tipo, double tempo, Fila fila, boolean externalArrival) {
        this.tipo = tipo;
        this.tempo = tempo;
        this.fila = fila;
        this.externalArrival = externalArrival;
    }

    @Override
    public int compareTo(Evento outro) {
        return Double.compare(this.tempo, outro.tempo);
    }
}

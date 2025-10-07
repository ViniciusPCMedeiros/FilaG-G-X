package com.example.demo;

public class Evento implements Comparable<Evento> {
    public static final int CHEGADA = 1;
    public static final int SAIDA = 2;

    public int tipo;
    public double tempo;
    public Fila fila;

    public Evento(int tipo, double tempo, Fila fila) {
        this.tipo = tipo;
        this.tempo = tempo;
        this.fila = fila;
    }

    @Override
    public int compareTo(Evento outro) {
        return Double.compare(this.tempo, outro.tempo);
    }
}


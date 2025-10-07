package com.example.demo;

public class Transicao {
    public Fila destino;   // null se for saída do sistema
    public boolean saida;  // true se é saída do sistema
    public double prob;

    public Transicao(Fila destino, double prob) {
        this.destino = destino;
        this.prob = prob;
        this.saida = false;
    }

    public Transicao(double prob) {
        this.destino = null;
        this.prob = prob;
        this.saida = true;
    }
}

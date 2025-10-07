package com.example.demo;

import java.util.ArrayList;
import java.util.List;

public class Fila {
    private int servidores;
    private int capacidade;
    private int clientes;
    private int perdas;
    private double[] tempos;
    private double ultimoUpdate;

    private double minChegada, maxChegada;
    private double minServico, maxServico;

    public List<Transicao> transicoes = new ArrayList<>();

    public Fila(int servidores, int capacidade,
                double minChegada, double maxChegada,
                double minServico, double maxServico) {
        this.servidores = servidores;
        this.capacidade = capacidade == -1 ? Integer.MAX_VALUE : capacidade;
        this.minChegada = minChegada;
        this.maxChegada = maxChegada;
        this.minServico = minServico;
        this.maxServico = maxServico;
        this.clientes = 0;
        this.perdas = 0;
        this.tempos = new double[this.capacidade == Integer.MAX_VALUE ? 100 : capacidade + 1];
        this.ultimoUpdate = 0.0;
    }

    public void acumulaTempo(double agora) {
        if (clientes < tempos.length) {
            tempos[clientes] += (agora - ultimoUpdate);
        }
        ultimoUpdate = agora;
    }

    public boolean entra() {
        if (clientes < capacidade) {
            clientes++;
            return true;
        } else {
            perdas++;
            return false;
        }
    }

    public void sai() {
        if (clientes > 0) clientes--;
    }

    public double getServico(double rnd) {
        return minServico + (maxServico - minServico) * rnd;
    }

    public double getInterChegada(double rnd) {
        return minChegada + (maxChegada - minChegada) * rnd;
    }

    public int getClientes() { return clientes; }
    public int getServidores() { return servidores; }
    public int getCapacidade() { return capacidade; }
    public int getPerdas() { return perdas; }
    public double[] getTempos() { return tempos; }
}

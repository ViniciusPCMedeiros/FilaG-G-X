package com.example.demo;

import java.util.ArrayList;
import java.util.List;

public class Fila {
    private final int servidores;
    private final int capacidade; // Integer.MAX_VALUE representa infinito
    private int clientes;
    private int perdas;
    private final double[] tempos; // acumulação por estado (0..capacidade) - se capacidade infinita, usamos tamanho grande
    private double ultimoUpdate;

    private final double minChegada, maxChegada;
    private final double minServico, maxServico;

    // métricas adicionais
    public double busyTime = 0.0;      // soma dos tempos de serviço iniciados
    public long departures = 0;        // total de saídas (para qualquer destino) do nó
    public long departuresToExit = 0;  // saídas que foram para "EXIT"
    public long externalAccepted = 0;  // número de chegadas externas aceitas

    public final List<Transicao> transicoes = new ArrayList<>();

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
        // if capacity infinite, allocate a reasonable array (e.g., 200 states). adjust if expected larger.
        int arraySize = (this.capacidade == Integer.MAX_VALUE) ? 200 : (this.capacidade + 1);
        this.tempos = new double[arraySize];
        this.ultimoUpdate = 0.0;
    }

    // atualiza vetor tempos para o estado atual até "agora"
    public void acumulaTempo(double agora) {
        int idx = Math.min(clientes, tempos.length - 1);
        tempos[idx] += (agora - ultimoUpdate);
        ultimoUpdate = agora;
    }

    // tentativa de entrar na fila (externa ou interna)
    // retorna true se entrou (aceito), false se perdido
    public boolean entra(boolean external) {
        if (clientes < capacidade) {
            clientes++;
            if (external) externalAccepted++;
            return true;
        } else {
            perdas++;
            return false;
        }
    }

    // saída de um cliente (quando termina serviço)
    public void sai() {
        if (clientes > 0) clientes--;
    }

    // gera tempo de serviço usando rnd (0..1)
    public double sampleService(double rnd) {
        return minServico + (maxServico - minServico) * rnd;
    }

    // gera intervalo de chegada (para filas que têm chegadas externas)
    public double sampleInterArrival(double rnd) {
        return minChegada + (maxChegada - minChegada) * rnd;
    }

    // getters
    public int getClientes() { return clientes; }
    public int getServidores() { return servidores; }
    public int getCapacidade() { return capacidade; }
    public int getPerdas() { return perdas; }
    public double[] getTempos() { return tempos; }
    public double getBusyTime() { return busyTime; }
    public long getDepartures() { return departures; }
    public long getDeparturesToExit() { return departuresToExit; }
    public long getExternalAccepted() { return externalAccepted; }
}

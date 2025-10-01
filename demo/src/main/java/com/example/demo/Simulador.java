package com.example.demo;

import java.io.FileInputStream;
import java.util.*;
import org.yaml.snakeyaml.Yaml;

public class Simulador {
    private static final int NUM_ALEATORIOS = 100000;
    private static Random random = new Random(42);

    private static double tempoGlobal = 0.0;
    private static int usados = 0;

    public static void main(String[] args) throws Exception {
        // === Carregar modelo do arquivo YAML ===
        Yaml yaml = new Yaml();
        Map<String, Object> dados = yaml.load(new FileInputStream("input.yml"));

        int qtdAleatorios = (int) dados.get("qtd_numeros_aleatorios");
        List<Map<String, Object>> filasCfg = (List<Map<String, Object>>) dados.get("filas");
        List<Map<String, Object>> transicoesCfg = (List<Map<String, Object>>) dados.get("transicoes");

        List<Fila> filas = new ArrayList<>();
        for (Map<String, Object> f : filasCfg) {
            int servidores = (int) f.get("servidores");
            int capacidade = (int) f.get("capacidade");
            double chegadaMin = ((Number) f.get("chegada_min")).doubleValue();
            double chegadaMax = ((Number) f.get("chegada_max")).doubleValue();
            double servicoMin = ((Number) f.get("servico_min")).doubleValue();
            double servicoMax = ((Number) f.get("servico_max")).doubleValue();
            filas.add(new Fila(servidores, capacidade, chegadaMin, chegadaMax, servicoMin, servicoMax));
        }

        // Configurar transições
        for (Map<String, Object> t : transicoesCfg) {
            int origem = (int) t.get("origem") - 1;
            String destinoStr = t.get("destino").toString();
            double prob = ((Number) t.get("probabilidade")).doubleValue();

            if (destinoStr.equals("saida")) {
                filas.get(origem).transicoes.add(new Transicao(prob));
            } else {
                int destino = Integer.parseInt(destinoStr) - 1;
                filas.get(origem).transicoes.add(new Transicao(filas.get(destino), prob));
            }
        }

        // === Simulação ===
        PriorityQueue<Evento> agenda = new PriorityQueue<>();
        agenda.add(new Evento(Evento.CHEGADA, 2.0, filas.get(0))); // chegada inicial

        while (!agenda.isEmpty() && usados < qtdAleatorios) {
            Evento ev = agenda.poll();
            double agora = ev.tempo;
            tempoGlobal = agora;

            for (Fila f : filas) f.acumulaTempo(agora);

            switch (ev.tipo) {
                case Evento.CHEGADA:
                    usados++;
                    if (ev.fila.entra()) {
                        if (ev.fila.getClientes() <= ev.fila.getServidores()) {
                            double servico = ev.fila.getServico(random.nextDouble());
                            agenda.add(new Evento(Evento.SAIDA, agora + servico, ev.fila));
                        }
                    }
                    if (ev.fila.getCapacidade() < Integer.MAX_VALUE) {
                        double interChegada = ev.fila.getInterChegada(random.nextDouble());
                        if (interChegada > 0) {
                            agenda.add(new Evento(Evento.CHEGADA, agora + interChegada, ev.fila));
                        }
                    }
                    break;

                case Evento.SAIDA:
                    usados++;
                    ev.fila.sai();
                    if (ev.fila.getClientes() >= ev.fila.getServidores()) {
                        double servico = ev.fila.getServico(random.nextDouble());
                        agenda.add(new Evento(Evento.SAIDA, agora + servico, ev.fila));
                    }

                    // Decidir transição probabilística
                    double sorteio = random.nextDouble();
                    double acumulado = 0;
                    for (Transicao t : ev.fila.transicoes) {
                        acumulado += t.prob;
                        if (sorteio <= acumulado) {
                            if (!t.saida) {
                                agenda.add(new Evento(Evento.CHEGADA, agora, t.destino));
                            }
                            break;
                        }
                    }
                    break;
            }
        }

        // === Resultados ===
        System.out.println("=== RESULTADOS ===");
        System.out.printf("Tempo global: %.4f%n", tempoGlobal);

        int i = 1;
        for (Fila fila : filas) {
            double soma = Arrays.stream(fila.getTempos()).sum();
            System.out.println("\nFila " + i + ":");
            for (int e = 0; e < fila.getTempos().length; e++) {
                double prob = soma > 0 ? fila.getTempos()[e] / soma : 0;
                System.out.printf("Estado %d -> Tempo = %.4f, Prob = %.6f%n", e, fila.getTempos()[e], prob);
            }
            System.out.println("Perdas = " + fila.getPerdas());
            i++;
        }
    }
}

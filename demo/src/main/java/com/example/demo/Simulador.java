package com.example.demo;

import org.yaml.snakeyaml.Yaml;
import java.io.FileInputStream;
import java.util.*;

public class Simulador {
    public static void main(String[] args) throws Exception {
        // CONFIG
        String yamlFile = args.length >= 1 ? args[0] : "input.yml";
        long maxRandoms = args.length >= 2 ? Long.parseLong(args[1]) : 100000L;
        long seed = args.length >= 3 ? Long.parseLong(args[2]) : 42L;
        double firstArrival = args.length >= 4 ? Double.parseDouble(args[3]) : 2.0; // por enunciado

        // carrega YAML
        Yaml yaml = new Yaml();
        Map<String, Object> dados = yaml.load(new FileInputStream(yamlFile));

        int qtdAleatorios = (int) ((Number) dados.getOrDefault("qtd_numeros_aleatorios", maxRandoms)).intValue();
        List<Map<String, Object>> filasCfg = (List<Map<String, Object>>) dados.get("filas");
        List<Map<String, Object>> transicoesCfg = (List<Map<String, Object>>) dados.get("transicoes");

        // criar filas
        List<Fila> filas = new ArrayList<>();
        for (Map<String, Object> f : filasCfg) {
            int servidores = ((Number) f.get("servidores")).intValue();
            int capacidade = ((Number) f.get("capacidade")).intValue();
            double chegadaMin = ((Number) f.get("chegada_min")).doubleValue();
            double chegadaMax = ((Number) f.get("chegada_max")).doubleValue();
            double servicoMin = ((Number) f.get("servico_min")).doubleValue();
            double servicoMax = ((Number) f.get("servico_max")).doubleValue();
            filas.add(new Fila(servidores, capacidade, chegadaMin, chegadaMax, servicoMin, servicoMax));
        }

        // configurar transicoes
        for (Map<String, Object> t : transicoesCfg) {
            int origem = ((Number) t.get("origem")).intValue() - 1;
            String destinoStr = t.get("destino").toString();
            double prob = ((Number) t.get("probabilidade")).doubleValue();
            if ("saida".equalsIgnoreCase(destinoStr) || "exit".equalsIgnoreCase(destinoStr)) {
                filas.get(origem).transicoes.add(new Transicao(prob));
            } else {
                int destino = Integer.parseInt(destinoStr) - 1;
                filas.get(origem).transicoes.add(new Transicao(filas.get(destino), prob));
            }
        }

        // Random com seed controlada
        Random rnd = new Random(seed);
        long usedRandoms = 0;

        // agenda
        PriorityQueue<Evento> agenda = new PriorityQueue<>();

        // agendar primeira chegada externa na fila 1 (index 0) no tempo firstArrival
        agenda.add(new Evento(Evento.CHEGADA, firstArrival, filas.get(0), true));

        double agora = 0.0;
        // loop de eventos
        while (!agenda.isEmpty() && usedRandoms < qtdAleatorios) {
            Evento ev = agenda.poll();
            agora = ev.tempo;

            // acumula tempos em todas as filas até 'agora'
            for (Fila q : filas) q.acumulaTempo(agora);

            if (ev.tipo == Evento.CHEGADA) {
                // chegada (externa se ev.externalArrival==true; interna se false)
                Fila q = ev.fila;
                boolean entered = q.entra(ev.externalArrival);
                // se entrou e entrou em servidor (clientes <= servidores): inicia serviço
                if (entered) {
                    if (q.getClientes() <= q.getServidores()) {
                        // sorteia serviço
                        double u = rnd.nextDouble(); usedRandoms++;
                        double service = q.sampleService(u);
                        q.busyTime += service; // contabiliza busy time quando iniciar o serviço
                        // agenda saída do cliente daqui
                        agenda.add(new Evento(Evento.SAIDA, agora + service, q, false));
                    }
                }
                // se esta fila tem chegadas externas definidas (chegada_min < chegada_max ou >0),
                // e esse evento era EXTERNO (somente para filas com chegada), agendamos próxima chegada externa
                // **Note**: para simplicidade, assumimos que somente fila 1 tem chegadas externas (como no enunciado).
                if (ev.externalArrival) {
                    // agenda próxima chegada externa para a mesma fila (usamos u)
                    double u2 = rnd.nextDouble(); usedRandoms++;
                    double ia = q.sampleInterArrival(u2);
                    if (ia > 0) {
                        agenda.add(new Evento(Evento.CHEGADA, agora + ia, q, true));
                    }
                }

            } else if (ev.tipo == Evento.SAIDA) {
                // saída do serviço na fila ev.fila
                Fila q = ev.fila;
                q.sai();
                q.departures++; // saiu da fila (contabilizamos saída do nó)
                // se ainda houver clientes ≥ servidores, agendar próxima saída (inicia serviço de próximo na fila)
                if (q.getClientes() >= q.getServidores()) {
                    double u = rnd.nextDouble(); usedRandoms++;
                    double service = q.sampleService(u);
                    q.busyTime += service;
                    agenda.add(new Evento(Evento.SAIDA, agora + service, q, false));
                }

                // rotear conforme transições da fila
                double r = rnd.nextDouble(); usedRandoms++;
                double cumul = 0.0;
                boolean routed = false;
                for (Transicao tr : q.transicoes) {
                    cumul += tr.prob;
                    if (r <= cumul) {
                        if (tr.saida) {
                            q.departuresToExit++;
                            // cliente sai do sistema (não agenda nada)
                        } else {
                            // passagem imediata para destino: agendar CHEGADA no mesmo tempo 'agora'
                            agenda.add(new Evento(Evento.CHEGADA, agora, tr.destino, false));
                        }
                        routed = true;
                        break;
                    }
                }
                // se não roteou (probabilidades somam <1), tratamos como saída do sistema por padrão
                if (!routed) {
                    q.departuresToExit++;
                }
            }
        } // fim loop

        double T = agora; // tempo global observado

        // antes de reportar, acumular tempo final para os estados atuais até T (já feito a cada evento, mas garantir)
        for (Fila q : filas) {
            q.acumulaTempo(T);
        }

        // RELATÓRIO
        System.out.println("=== RELATÓRIO FINAL ===");
        System.out.printf("Tempo global da simulação (T): %.6f%n", T);
        System.out.printf("Aleatórios usados: %d (limite %d)%n%n", usedRandoms, qtdAleatorios);

        int idx = 1;
        for (Fila q : filas) {
            double[] tempos = q.getTempos();
            double totalTempo = 0.0;
            for (double tt : tempos) totalTempo += tt;
            // se totalTempo==0, usar T (evita divisão por zero)
            double denom = totalTempo > 0 ? totalTempo : T;

            // População média L = sum_i i * (tempo_i / denom)
            double L = 0.0;
            for (int i = 0; i < tempos.length; i++) {
                double prob = denom > 0 ? tempos[i] / denom : 0.0;
                L += i * prob;
            }

            // Throughput lambda = departures / T
            double lambda = q.getDepartures() / (T > 0 ? T : 1.0);

            // utilization rho = busyTime / (servers * T)
            double rho = q.getBusyTime() / ( (double) q.getServidores() * (T > 0 ? T : 1.0) );

            // response time W = L / lambda (se lambda>0)
            double W = lambda > 0 ? (L / lambda) : Double.NaN;

            System.out.println("Fila " + idx + ":");
            System.out.printf("  Capacidade = %s, Servidores = %d%n",
                    (q.getCapacidade() == Integer.MAX_VALUE ? "infinito" : q.getCapacidade()), q.getServidores());
            System.out.printf("  Perdas = %d%n", q.getPerdas());
            System.out.printf("  Departures (total nodal) = %d; Departures->Exit = %d; ExternalAccepted = %d%n",
                    q.getDepartures(), q.getDeparturesToExit(), q.getExternalAccepted());
            System.out.printf("  BusyTime = %.6f%n", q.getBusyTime());

            System.out.println("  --- estados (tempo / prob) ---");
            for (int s = 0; s < tempos.length; s++) {
                double prob = denom > 0 ? tempos[s] / denom : 0.0;
                System.out.printf("    estado %d: tempo = %.6f, prob = %.6f%n", s, tempos[s], prob);
            }

            System.out.printf("  L (população média) = %.6f%n", L);
            System.out.printf("  λ (throughput nodal) = %.6f (clientes/unidade tempo)%n", lambda);
            System.out.printf("  ρ (utilização) = %.6f%n", rho);
            if (!Double.isNaN(W)) System.out.printf("  W (tempo de resposta médio) = %.6f%n", W);
            else System.out.println("  W (tempo de resposta) = indefinido (λ=0)");
            System.out.println();
            idx++;
        }

        // resumo global
        long totalDeparturesToExit = filas.stream().mapToLong(Fila::getDeparturesToExit).sum();
        System.out.printf("Total de clientes que saíram do sistema: %d%n", totalDeparturesToExit);
    }
}

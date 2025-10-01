import java.util.PriorityQueue;
import java.util.Random;

public class Simulador {
    private static final int NUM_ALEATORIOS = 100000;

    private static Random random = new Random(42); // semente fixa

    private static double uniforme(double a, double b) {
        return a + (b - a) * random.nextDouble();
    }

    public static void main(String[] args) {
        // Fila 1: G/G/2/3
        Fila fila1 = new Fila(2, 3);

        // Fila 2: G/G/1/5
        Fila fila2 = new Fila(1, 5);

        PriorityQueue<Evento> agenda = new PriorityQueue<>();

        // tempo inicial
        double tempo = 0.0;
        int usados = 0;

        // primeira chegada em 1.5
        agenda.add(new Evento(Evento.CHEGADA, 1.5));

        while (!agenda.isEmpty() && usados < NUM_ALEATORIOS) {
            Evento ev = agenda.poll();
            double agora = ev.tempo;

            // acumula tempo para ambas as filas
            fila1.acumulaTempo(agora);
            fila2.acumulaTempo(agora);

            switch (ev.tipo) {
                case Evento.CHEGADA:
                    usados++;
                    // chegada na fila1
                    if (fila1.entra()) {
                        if (fila1.getClientes() <= fila1.getServidores()) {
                            double servico = uniforme(3.0, 4.0);
                            agenda.add(new Evento(Evento.SAIDA1, agora + servico));
                            usados++;
                        }
                    }
                    // agenda próxima chegada externa
                    double interChegada = uniforme(1.0, 4.0);
                    agenda.add(new Evento(Evento.CHEGADA, agora + interChegada));
                    usados++;
                    break;

                case Evento.SAIDA1:
                    usados++;
                    fila1.sai();
                    if (fila1.getClientes() >= fila1.getServidores()) {
                        double servico1 = uniforme(3.0, 4.0);
                        agenda.add(new Evento(Evento.SAIDA1, agora + servico1));
                        usados++;
                    }
                    // passagem imediata para fila2
                    agenda.add(new Evento(Evento.PASSAGEM, agora));
                    break;

                case Evento.PASSAGEM:
                    usados++;
                    if (fila2.entra()) {
                        if (fila2.getClientes() <= fila2.getServidores()) {
                            double servico2 = uniforme(2.0, 3.0);
                            agenda.add(new Evento(Evento.SAIDA2, agora + servico2));
                            usados++;
                        }
                    }
                    break;

                case Evento.SAIDA2:
                    usados++;
                    fila2.sai();
                    if (fila2.getClientes() >= fila2.getServidores()) {
                        double servico3 = uniforme(2.0, 3.0);
                        agenda.add(new Evento(Evento.SAIDA2, agora + servico3));
                        usados++;
                    }
                    break;
            }

            tempo = agora;
        }

        // Resultados
        System.out.println("=== RESULTADOS ===");
        System.out.printf("Tempo global: %.4f%n", tempo);

        System.out.println("\nFila 1 (G/G/2/3):");
        double soma1 = 0;
        for (double t : fila1.getTempos()) soma1 += t;
        for (int i = 0; i < fila1.getTempos().length; i++) {
            System.out.printf("Estado %d -> Tempo = %.4f, Prob = %.6f%n",
                i, fila1.getTempos()[i], fila1.getTempos()[i] / soma1);
        }
        System.out.println("Perdas Fila 1 = " + fila1.getPerdas());

        System.out.println("\nFila 2 (G/G/1/5):");
        double soma2 = 0;
        for (double t : fila2.getTempos()) soma2 += t;
        for (int i = 0; i < fila2.getTempos().length; i++) {
            System.out.printf("Estado %d -> Tempo = %.4f, Prob = %.6f%n",
                i, fila2.getTempos()[i], fila2.getTempos()[i] / soma2);
        }
        System.out.println("Perdas Fila 2 = " + fila2.getPerdas());
    }
}

public class Evento implements Comparable<Evento> {
    public static final int CHEGADA = 1;
    public static final int SAIDA1 = 2;
    public static final int SAIDA2 = 3;
    public static final int PASSAGEM = 4;

    public int tipo;
    public double tempo;

    public Evento(int tipo, double tempo) {
        this.tipo = tipo;
        this.tempo = tempo;
    }

    @Override
    public int compareTo(Evento outro) {
        return Double.compare(this.tempo, outro.tempo);
    }
}

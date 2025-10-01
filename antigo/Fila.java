public class Fila {
    private int servidores;
    private int capacidade;
    private int clientes;
    private int perdas;
    private double[] tempos;
    private double ultimoUpdate;

    public Fila(int servidores, int capacidade) {
        this.servidores = servidores;
        this.capacidade = capacidade;
        this.clientes = 0;
        this.perdas = 0;
        this.tempos = new double[capacidade + 1];
        this.ultimoUpdate = 0.0;
    }

    public void acumulaTempo(double agora) {
        tempos[clientes] += (agora - ultimoUpdate);
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

    public int getClientes() { return clientes; }
    public int getServidores() { return servidores; }
    public int getCapacidade() { return capacidade; }
    public int getPerdas() { return perdas; }
    public double[] getTempos() { return tempos; }
}

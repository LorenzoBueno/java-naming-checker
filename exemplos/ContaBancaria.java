public class ContaBancaria {
    private static final int LIMITE_SAQUE = 1000;
    private double saldo;

    public void depositar(double valorDeposito) {
        double novoSaldo = saldo + valorDeposito;
        saldo = novoSaldo;
    }
}
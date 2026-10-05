public class conta_bancaria {
    private double saldo;

    public void Depositar_Valor(double valor) {
        saldo += valor;
    }

    public double ObterSaldo() {
        return saldo;
    }
}
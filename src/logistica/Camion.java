package logistica;

import java.util.Locale;

/** Datos del vehículo que espera una asignación en el centro de distribución. */
public class Camion {
    private final String placa;
    private final String conductor;
    private final double capacidadTon;

    public Camion(String placa, String conductor, double capacidadTon) {
        if (placa == null || placa.trim().isEmpty()) {
            throw new IllegalArgumentException("Ingrese la placa del camión.");
        }
        if (conductor == null || conductor.trim().isEmpty()) {
            throw new IllegalArgumentException("Ingrese el nombre del conductor.");
        }
        if (!Double.isFinite(capacidadTon) || capacidadTon <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser un número mayor que cero.");
        }
        this.placa = placa.trim().toUpperCase(Locale.ROOT);
        this.conductor = conductor.trim();
        this.capacidadTon = capacidadTon;
    }

    public String getPlaca() { return placa; }
    public String getConductor() { return conductor; }
    public double getCapacidadTon() { return capacidadTon; }

    @Override
    public String toString() {
        return placa + " | " + conductor + " | " + capacidadTon + " t";
    }
}

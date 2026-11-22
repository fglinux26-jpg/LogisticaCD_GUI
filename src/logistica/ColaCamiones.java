package logistica;

import java.util.Locale;

/** Cola FIFO. Frente y fin permiten retirar o añadir un vehículo en tiempo constante. */
public class ColaCamiones {
    private NodoCola frente;
    private NodoCola fin;

    public void encolar(Camion camion) {
        if (camion == null) {
            throw new IllegalArgumentException("Ingrese un camión válido.");
        }
        NodoCola nuevo = new NodoCola(camion);
        if (fin == null) {
            frente = nuevo;
        } else {
            fin.siguiente = nuevo;
        }
        fin = nuevo;
    }

    public Camion desencolar() {
        if (estaVacia()) {
            return null;
        }
        Camion primero = frente.camion;
        frente = frente.siguiente;
        if (frente == null) {
            fin = null;
        }
        return primero;
    }

    public boolean estaVacia() {
        return frente == null;
    }

    /** El recorrido para mostrar los turnos conserva el frente y el fin. */
    public String mostrar() {
        if (estaVacia()) {
            return "La cola de camiones está vacía.";
        }
        StringBuilder texto = new StringBuilder(
                String.format("%-7s %-11s %-23s %s%n%n", "Turno", "Placa", "Conductor", "Capacidad"));
        int turno = 1;
        for (NodoCola nodo = frente; nodo != null; nodo = nodo.siguiente) {
            Camion c = nodo.camion;
            texto.append(String.format(Locale.ROOT, "%-7d %-11s %-23s %s t%n",
                    turno++, c.getPlaca(), c.getConductor(), c.getCapacidadTon()));
        }
        return texto.toString();
    }
}

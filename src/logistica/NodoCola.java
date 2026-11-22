package logistica;

/** Eslabón de la cola manual de camiones. */
public class NodoCola {
    final Camion camion;
    NodoCola siguiente;

    public NodoCola(Camion camion) {
        this.camion = camion;
    }
}

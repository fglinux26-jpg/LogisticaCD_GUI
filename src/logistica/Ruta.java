package logistica;

/** Una conexión de la lista de adyacencia y su longitud en kilómetros. */
public class Ruta {
    private final String destino;
    private final int distanciaKm;

    public Ruta(String destino, int distanciaKm) {
        if (destino == null || destino.trim().isEmpty() || distanciaKm <= 0) {
            throw new IllegalArgumentException("La ruta requiere destino y distancia positiva.");
        }
        this.destino = destino;
        this.distanciaKm = distanciaKm;
    }

    public String getDestino() { return destino; }
    public int getDistanciaKm() { return distanciaKm; }

    @Override
    public String toString() {
        return destino + " (" + distanciaKm + " km)";
    }
}

package logistica;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;

/** Red de carreteras de doble sentido. Cada ruta se guarda en las dos listas vecinas. */
public class GrafoRutas {
    private final HashMap<String, LinkedList<Ruta>> adyacencia = new HashMap<>();

    private String clave(String ciudad) {
        return ciudad == null ? "" : ciudad.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
    }

    public void agregarCiudad(String ciudad) {
        String nombre = clave(ciudad);
        if (nombre.isEmpty()) {
            throw new IllegalArgumentException("Ingrese el nombre de la ciudad.");
        }
        if (adyacencia.containsKey(nombre)) {
            throw new IllegalArgumentException("La ciudad ya está registrada.");
        }
        adyacencia.put(nombre, new LinkedList<Ruta>());
    }

    private Ruta buscarTramo(String origen, String destino) {
        for (Ruta ruta : adyacencia.get(origen)) {
            if (ruta.getDestino().equals(destino)) {
                return ruta;
            }
        }
        return null;
    }

    public void agregarRuta(String origen, String destino, int km) {
        String desde = clave(origen);
        String hasta = clave(destino);
        if (!adyacencia.containsKey(desde) || !adyacencia.containsKey(hasta)) {
            throw new IllegalArgumentException("Registre primero las dos ciudades de la ruta.");
        }
        if (desde.equals(hasta) || km <= 0) {
            throw new IllegalArgumentException("La ruta necesita dos ciudades distintas y una distancia positiva.");
        }
        if (buscarTramo(desde, hasta) != null) {
            throw new IllegalArgumentException("La ruta entre esas ciudades ya existe.");
        }
        adyacencia.get(desde).add(new Ruta(hasta, km));
        adyacencia.get(hasta).add(new Ruta(desde, km));
    }

    public String mostrarRutas() {
        if (adyacencia.isEmpty()) {
            return "La red de rutas está vacía.";
        }
        String[] ciudades = adyacencia.keySet().toArray(new String[0]);
        Arrays.sort(ciudades);
        StringBuilder texto = new StringBuilder();
        for (String ciudad : ciudades) {
            texto.append(String.format("%-11s  ", ciudad));
            LinkedList<Ruta> vecinas = adyacencia.get(ciudad);
            if (vecinas.isEmpty()) {
                texto.append("Sin conexiones");
            } else {
                String separador = "";
                for (Ruta ruta : vecinas) {
                    texto.append(separador).append(ruta);
                    separador = "  /  ";
                }
            }
            texto.append('\n');
        }
        return texto.toString();
    }

    /** Devuelve las ciudades alcanzables por niveles. Una lista vacía indica inicio desconocido. */
    public List<String> bfs(String inicio) {
        String ciudad = clave(inicio);
        List<String> orden = new LinkedList<>();
        if (!adyacencia.containsKey(ciudad)) {
            return orden;
        }
        Queue<String> pendientes = new ArrayDeque<>();
        Set<String> visitadas = new HashSet<>();
        pendientes.add(ciudad);
        visitadas.add(ciudad);
        while (!pendientes.isEmpty()) {
            String actual = pendientes.remove();
            orden.add(actual);
            for (Ruta ruta : adyacencia.get(actual)) {
                if (visitadas.add(ruta.getDestino())) {
                    pendientes.add(ruta.getDestino());
                }
            }
        }
        return orden;
    }

    /** Suma los tramos consecutivos de un camino. Reporta el primer tramo que falta. */
    public int distanciaRuta(List<String> camino) {
        if (camino == null) {
            throw new IllegalArgumentException("Ingrese un camino válido.");
        }
        long distancia = 0;
        String anterior = null;
        for (String paso : camino) {
            String actual = clave(paso);
            if (!adyacencia.containsKey(actual)) {
                throw new IllegalArgumentException("Ciudad no registrada: " + actual + ".");
            }
            if (anterior != null) {
                Ruta tramo = buscarTramo(anterior, actual);
                if (tramo == null) {
                    throw new IllegalArgumentException("No hay ruta directa entre " + anterior + " y " + actual + ".");
                }
                distancia += tramo.getDistanciaKm();
                if (distancia > Integer.MAX_VALUE) {
                    throw new IllegalArgumentException("La distancia total supera el máximo admitido.");
                }
            }
            anterior = actual;
        }
        return (int) distancia;
    }
}

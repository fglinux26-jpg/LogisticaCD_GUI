package logistica;

import java.util.List;

/** ABB manual ordenado por código. La raíz es nula mientras el inventario está vacío. */
public class ArbolInventario {
    private NodoArbol raiz;

    public boolean insertar(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("Ingrese un producto válido.");
        }
        if (raiz == null) {
            raiz = new NodoArbol(producto);
            return true;
        }
        NodoArbol actual = raiz;
        while (true) {
            int comparacion = Integer.compare(producto.getCodigo(), actual.producto.getCodigo());
            if (comparacion == 0) {
                return false;
            }
            if (comparacion < 0) {
                if (actual.izquierdo == null) {
                    actual.izquierdo = new NodoArbol(producto);
                    return true;
                }
                actual = actual.izquierdo;
            } else {
                if (actual.derecho == null) {
                    actual.derecho = new NodoArbol(producto);
                    return true;
                }
                actual = actual.derecho;
            }
        }
    }

    public Producto buscar(int codigo) {
        NodoArbol actual = raiz;
        while (actual != null) {
            if (codigo == actual.producto.getCodigo()) {
                return actual.producto;
            }
            actual = codigo < actual.producto.getCodigo() ? actual.izquierdo : actual.derecho;
        }
        return null;
    }

    /** Agrega a la lista de salida los productos en orden ascendente de código. */
    public void inOrden(List<Producto> salida) {
        inOrden(raiz, salida);
    }

    private void inOrden(NodoArbol nodo, List<Producto> salida) {
        if (nodo != null) {
            inOrden(nodo.izquierdo, salida);
            salida.add(nodo.producto);
            inOrden(nodo.derecho, salida);
        }
    }

    /** Altura en niveles: un árbol vacío tiene 0 y una raíz sin hijos tiene 1. */
    public int altura() {
        return altura(raiz);
    }

    private int altura(NodoArbol nodo) {
        return nodo == null ? 0 : 1 + Math.max(altura(nodo.izquierdo), altura(nodo.derecho));
    }

    public boolean descontarStock(int codigo, int cantidad) {
        Producto producto = buscar(codigo);
        if (producto == null || cantidad <= 0 || cantidad > producto.getStock()) {
            return false;
        }
        producto.setStock(producto.getStock() - cantidad);
        return true;
    }
}

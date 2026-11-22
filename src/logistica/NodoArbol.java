package logistica;

/** Un nodo guarda un solo producto y los enlaces de sus dos subárboles. */
public class NodoArbol {
    final Producto producto;
    NodoArbol izquierdo;
    NodoArbol derecho;

    public NodoArbol(Producto producto) {
        this.producto = producto;
        producto.fijarCodigo();
    }
}

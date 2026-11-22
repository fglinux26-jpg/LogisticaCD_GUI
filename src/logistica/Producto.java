package logistica;

/** Producto del inventario. El código identifica su posición en el árbol. */
public class Producto {
    private int codigo;
    private String nombre;
    private int stock;
    private boolean codigoFijo;

    public Producto(int codigo, String nombre, int stock) {
        setCodigo(codigo);
        setNombre(nombre);
        setStock(stock);
    }

    public int getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public int getStock() { return stock; }

    public void setCodigo(int codigo) {
        if (codigo <= 0) {
            throw new IllegalArgumentException("El código debe ser mayor que cero.");
        }
        if (codigoFijo && this.codigo != codigo) {
            throw new IllegalArgumentException("El código de un producto registrado no se puede cambiar.");
        }
        this.codigo = codigo;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("Ingrese el nombre del producto.");
        }
        this.nombre = nombre.trim();
    }

    public void setStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("El stock debe ser cero o mayor.");
        }
        this.stock = stock;
    }

    void fijarCodigo() {
        codigoFijo = true;
    }

    @Override
    public String toString() {
        return codigo + " | " + nombre + " | " + stock + " unidades";
    }
}

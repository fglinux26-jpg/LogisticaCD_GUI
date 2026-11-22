package logistica;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Formulario del centro de distribución. Jheferson Guido, U24201286. */
public class Main extends javax.swing.JFrame {
    private final ArbolInventario inventario = new ArbolInventario();
    private final ColaCamiones cola = new ColaCamiones();
    private final GrafoRutas rutas = new GrafoRutas();
    private final ArrayList<Entrega> historial = new ArrayList<>();

    public Main() {
        initComponents();
        setLocationRelativeTo(null);
        pestanas.addChangeListener(evento -> cambiarPestana());
        cambiarPestana();
    }

    private void cambiarPestana() {
        switch (pestanas.getSelectedIndex()) {
            case 0:
                mostrar("Inventario de productos", "Ingrese código, nombre y stock para registrar un producto.\n"
                        + "Para buscarlo, ingrese solo el código.");
                break;
            case 1:
                mostrar("Cola de camiones", "Ingrese la placa, el conductor y la capacidad en toneladas.\n"
                        + "El primer camión en llegar recibe el primer despacho.");
                break;
            case 2:
                mostrar("Red de rutas", "Registre las ciudades antes de unirlas. Cada ruta funciona en ambos sentidos.\n"
                        + "Para recorrer la red, escriba la ciudad inicial y pulse Ejecutar BFS.");
                break;
            case 3:
                mostrar("Registro de entrega", "Ingrese el producto, la cantidad y la ciudad de destino.");
                break;
            case 4:
                mostrarHistorial();
                break;
            default:
                break;
        }
    }

    private void mostrar(String titulo, String texto) {
        lblResultado.setText(titulo);
        txtResultado.setText(texto);
        txtResultado.setCaretPosition(0);
    }

    /** Todos los botones informan los errores en el área de resultados y en un diálogo. */
    private void ejecutar(Runnable accion) {
        try {
            accion.run();
        } catch (IllegalArgumentException error) {
            mostrar("Operación rechazada", error.getMessage());
            JOptionPane.showMessageDialog(this, error.getMessage(), "Operación rechazada",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private String texto(JTextField campo, String nombre) {
        String valor = campo.getText().trim();
        if (valor.isEmpty()) {
            throw new IllegalArgumentException("Complete el campo " + nombre + ".");
        }
        return valor;
    }

    private int entero(JTextField campo, String nombre, int minimo) {
        String valor = texto(campo, nombre);
        try {
            int numero = Integer.parseInt(valor);
            if (numero >= minimo) {
                return numero;
            }
        } catch (NumberFormatException ignorada) {
            // El mismo aviso sirve para valores no numéricos o fuera del rango entero.
        }
        throw new IllegalArgumentException("El campo " + nombre + " requiere un entero mayor o igual que " + minimo + ".");
    }

    private double capacidad() {
        try {
            return Double.parseDouble(texto(txtCapacidad, "capacidad").replace(',', '.'));
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException("Ingrese una capacidad válida en toneladas.");
        }
    }

    private void insertarProducto() {
        Producto producto = new Producto(entero(txtCodigo, "código", 1), texto(txtNombre, "nombre"),
                entero(txtStock, "stock", 0));
        if (!inventario.insertar(producto)) {
            throw new IllegalArgumentException("El código " + producto.getCodigo() + " ya está registrado.");
        }
        mostrar("Producto registrado", producto.toString());
    }

    private void buscarProducto() {
        int codigo = entero(txtCodigo, "código", 1);
        Producto producto = inventario.buscar(codigo);
        if (producto == null) {
            throw new IllegalArgumentException("No existe un producto con el código " + codigo + ".");
        }
        mostrar("Producto encontrado", "Código      " + codigo + "\nProducto    " + producto.getNombre()
                + "\nStock       " + producto.getStock() + " unidades");
    }

    private void mostrarInventario() {
        // Esta lista temporal recibe el recorrido. El inventario se conserva en sus nodos.
        List<Producto> ordenados = new Vector<>();
        inventario.inOrden(ordenados);
        if (ordenados.isEmpty()) {
            mostrar("Inventario ordenado", "El inventario está vacío.");
            return;
        }
        StringBuilder salida = new StringBuilder(String.format("%-9s %-25s %10s%n%n",
                "Código", "Producto", "Stock (u.)"));
        for (Producto producto : ordenados) {
            salida.append(String.format(Locale.ROOT, "%-9d %-25s %10d%n",
                    producto.getCodigo(), producto.getNombre(), producto.getStock()));
        }
        salida.append("\nProductos: ").append(ordenados.size()).append("     Altura del ABB: ")
                .append(inventario.altura()).append(" niveles");
        mostrar("Inventario ordenado por código", salida.toString());
    }

    private void encolarCamion() {
        Camion camion = new Camion(texto(txtPlaca, "placa"), texto(txtConductor, "conductor"), capacidad());
        cola.encolar(camion);
        mostrar("Camión encolado", camion.toString());
    }

    private void desencolarCamion() {
        Camion camion = cola.desencolar();
        if (camion == null) {
            throw new IllegalArgumentException("La cola de camiones está vacía.");
        }
        mostrar("Camión retirado de la cola", camion.toString());
    }

    private void agregarCiudad() {
        String ciudad = texto(txtCiudad, "ciudad");
        rutas.agregarCiudad(ciudad);
        mostrar("Ciudad registrada", ciudad.toUpperCase(Locale.ROOT));
    }

    private void agregarRuta() {
        String origen = texto(txtOrigen, "origen");
        String destino = texto(txtDestinoRuta, "destino");
        int km = entero(txtKm, "distancia", 1);
        rutas.agregarRuta(origen, destino, km);
        mostrar("Ruta registrada en ambos sentidos", origen + " ↔ " + destino + "  |  " + km + " km");
    }

    private void recorrerRed() {
        String inicio = texto(txtCiudad, "ciudad");
        List<String> orden = rutas.bfs(inicio);
        if (orden.isEmpty()) {
            throw new IllegalArgumentException("La ciudad inicial no está registrada.");
        }
        mostrar("Recorrido en anchura desde " + orden.get(0),
                String.join("  →  ", orden) + "\n\nCiudades visitadas: " + orden.size());
    }

    private void calcularDistancia() {
        String camino = texto(txtCamino, "camino");
        int km = rutas.distanciaRuta(Arrays.asList(camino.split(",", -1)));
        mostrar("Distancia del camino", camino + "\n\nDistancia total: " + km + " km");
    }

    /** El evento se ejecuta en el hilo de Swing. Si falla una validación posterior
     * al descuento, se repone el stock antes de devolver el control al formulario. */
    private void registrarEntrega() {
        int codigo = entero(txtCodigoEntrega, "código de producto", 1);
        int cantidad = entero(txtCantidad, "cantidad", 1);
        String destino = texto(txtDestinoEntrega, "destino");

        // 1. El ABB localiza el producto solicitado.
        Producto producto = inventario.buscar(codigo);
        if (producto == null) {
            throw new IllegalArgumentException("No existe un producto con el código " + codigo + ".");
        }
        // 2. La reserva solo se realiza si alcanza el stock.
        int anterior = producto.getStock();
        if (!inventario.descontarStock(codigo, cantidad)) {
            throw new IllegalArgumentException("Stock insuficiente para " + producto.getNombre()
                    + ".\nSolicitado: " + cantidad + " unidades. Disponible: " + anterior + " unidades.");
        }
        // 3. El BFS devuelve primero el destino cuando está registrado, incluso si está aislado.
        List<String> recorrido = rutas.bfs(destino);
        if (recorrido.isEmpty()) {
            producto.setStock(anterior);
            throw new IllegalArgumentException("El destino " + destino + " no está registrado.");
        }
        // 4. El primer camión en la cola atiende el despacho.
        Camion camion = cola.desencolar();
        if (camion == null) {
            producto.setStock(anterior);
            throw new IllegalArgumentException("No hay camiones disponibles en la cola.");
        }
        // 5. Solo una entrega confirmada recibe un número correlativo.
        Entrega entrega = new Entrega(historial.size() + 1, producto.getNombre(),
                camion.getPlaca(), recorrido.get(0), cantidad);
        historial.add(entrega);
        lblTotal.setText("Entregas registradas: " + historial.size());
        String numero = String.format(Locale.ROOT, "%03d", entrega.getId());
        mostrar("Entrega " + numero + " registrada", "Producto     " + producto.getNombre()
                + "\nCantidad     " + cantidad + " unidades\nDestino      " + entrega.getDestino()
                + "\nCamión       " + camion.getPlaca() + "  |  " + camion.getConductor()
                + "\nStock actual " + producto.getStock() + " unidades");
        JOptionPane.showMessageDialog(this, "Entrega " + numero + " registrada correctamente.\n"
                + cantidad + " unidades de " + producto.getNombre() + " para " + entrega.getDestino()
                + ".\nCamión asignado: " + camion.getPlaca() + ".", "Entrega confirmada",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarHistorial() {
        if (historial.isEmpty()) {
            mostrar("Historial de entregas", "Todavía no se han registrado entregas.");
            return;
        }
        StringBuilder salida = new StringBuilder(String.format("%-3s   %-22s  %7s   %-10s  %s%n%n",
                "ID", "Producto", "Unid.", "Camión", "Destino"));
        for (Entrega entrega : historial) {
            salida.append(entrega).append('\n');
        }
        mostrar("Historial de entregas", salida.toString());
    }

    /** Inicia la interfaz en el hilo de eventos de Swing. */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
                UIManager.put("defaultFont", new Font("SansSerif", Font.PLAIN, 14));
            } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException error) {
                // Si Nimbus no está disponible se mantiene el aspecto estándar de Swing.
            }
            UIManager.put("OptionPane.okButtonText", "Aceptar");
            new Main().setVisible(true);
        });
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cabecera = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblEstudiante = new javax.swing.JLabel();
        cuerpo = new javax.swing.JPanel();
        pestanas = new javax.swing.JTabbedPane();
        panelInventario = new javax.swing.JPanel();
        filasInventario = new javax.swing.JPanel();
        filaProducto = new javax.swing.JPanel();
        lblCodigo = new javax.swing.JLabel();
        txtCodigo = new javax.swing.JTextField();
        lblNombre = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        lblStock = new javax.swing.JLabel();
        txtStock = new javax.swing.JTextField();
        accionesInventario = new javax.swing.JPanel();
        btnInsertar = new javax.swing.JButton();
        btnBuscar = new javax.swing.JButton();
        btnInventario = new javax.swing.JButton();
        panelCamiones = new javax.swing.JPanel();
        filasCamiones = new javax.swing.JPanel();
        filaCamion = new javax.swing.JPanel();
        lblPlaca = new javax.swing.JLabel();
        txtPlaca = new javax.swing.JTextField();
        lblConductor = new javax.swing.JLabel();
        txtConductor = new javax.swing.JTextField();
        lblCapacidad = new javax.swing.JLabel();
        txtCapacidad = new javax.swing.JTextField();
        accionesCamiones = new javax.swing.JPanel();
        btnEncolar = new javax.swing.JButton();
        btnDesencolar = new javax.swing.JButton();
        btnCola = new javax.swing.JButton();
        panelRutas = new javax.swing.JPanel();
        filasRutas = new javax.swing.JPanel();
        filaCiudad = new javax.swing.JPanel();
        lblCiudad = new javax.swing.JLabel();
        txtCiudad = new javax.swing.JTextField();
        btnCiudad = new javax.swing.JButton();
        btnBfs = new javax.swing.JButton();
        btnRutas = new javax.swing.JButton();
        filaTramo = new javax.swing.JPanel();
        lblOrigen = new javax.swing.JLabel();
        txtOrigen = new javax.swing.JTextField();
        lblDestinoRuta = new javax.swing.JLabel();
        txtDestinoRuta = new javax.swing.JTextField();
        lblKm = new javax.swing.JLabel();
        txtKm = new javax.swing.JTextField();
        btnRuta = new javax.swing.JButton();
        filaCamino = new javax.swing.JPanel();
        lblCamino = new javax.swing.JLabel();
        txtCamino = new javax.swing.JTextField();
        btnDistancia = new javax.swing.JButton();
        panelEntregas = new javax.swing.JPanel();
        filasEntregas = new javax.swing.JPanel();
        filaEntrega = new javax.swing.JPanel();
        lblCodigoEntrega = new javax.swing.JLabel();
        txtCodigoEntrega = new javax.swing.JTextField();
        lblCantidad = new javax.swing.JLabel();
        txtCantidad = new javax.swing.JTextField();
        lblDestinoEntrega = new javax.swing.JLabel();
        txtDestinoEntrega = new javax.swing.JTextField();
        accionesEntrega = new javax.swing.JPanel();
        btnEntrega = new javax.swing.JButton();
        panelHistorial = new javax.swing.JPanel();
        filasHistorial = new javax.swing.JPanel();
        accionesHistorial = new javax.swing.JPanel();
        btnHistorial = new javax.swing.JButton();
        panelResultados = new javax.swing.JPanel();
        lblResultado = new javax.swing.JLabel();
        scrollResultado = new javax.swing.JScrollPane();
        txtResultado = new javax.swing.JTextArea();
        pie = new javax.swing.JPanel();
        lblTotal = new javax.swing.JLabel();
        btnSalir = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("LogisticaCD_GUI | Centro de distribución");
        setMinimumSize(new java.awt.Dimension(915, 545));

        cabecera.setBackground(new java.awt.Color(35, 61, 80));
        cabecera.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 20, 12, 20));
        cabecera.setLayout(new java.awt.BorderLayout());

        lblTitulo.setText("Centro de distribución");
        lblTitulo.setFont(new java.awt.Font("SansSerif", 1, 23)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(255, 255, 255));
        cabecera.add(lblTitulo, java.awt.BorderLayout.CENTER);

        lblEstudiante.setText("Jheferson Guido  ·  U24201286");
        lblEstudiante.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        lblEstudiante.setForeground(new java.awt.Color(242, 246, 248));
        cabecera.add(lblEstudiante, java.awt.BorderLayout.EAST);

        getContentPane().add(cabecera, java.awt.BorderLayout.NORTH);

        cuerpo.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 18, 10, 18));
        cuerpo.setLayout(new java.awt.BorderLayout());

        pestanas.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N

        panelInventario.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 7, 6, 7));
        panelInventario.setLayout(new java.awt.BorderLayout());

        filasInventario.setLayout(new java.awt.GridLayout(2, 1));

        filaProducto.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        lblCodigo.setText("Código");
        filaProducto.add(lblCodigo);

        txtCodigo.setColumns(7);
        filaProducto.add(txtCodigo);

        lblNombre.setText("Nombre");
        filaProducto.add(lblNombre);

        txtNombre.setColumns(20);
        filaProducto.add(txtNombre);

        lblStock.setText("Stock");
        filaProducto.add(lblStock);

        txtStock.setColumns(8);
        filaProducto.add(txtStock);

        filasInventario.add(filaProducto);

        accionesInventario.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        btnInsertar.setText("Insertar producto");
        btnInsertar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInsertarActionPerformed(evt);
            }
        });
        accionesInventario.add(btnInsertar);

        btnBuscar.setText("Buscar por código");
        btnBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarActionPerformed(evt);
            }
        });
        accionesInventario.add(btnBuscar);

        btnInventario.setText("Mostrar inventario ordenado");
        btnInventario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInventarioActionPerformed(evt);
            }
        });
        accionesInventario.add(btnInventario);

        filasInventario.add(accionesInventario);

        panelInventario.add(filasInventario, java.awt.BorderLayout.NORTH);

        pestanas.addTab("Inventario", panelInventario);

        panelCamiones.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 7, 6, 7));
        panelCamiones.setLayout(new java.awt.BorderLayout());

        filasCamiones.setLayout(new java.awt.GridLayout(2, 1));

        filaCamion.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        lblPlaca.setText("Placa");
        filaCamion.add(lblPlaca);

        txtPlaca.setColumns(9);
        filaCamion.add(txtPlaca);

        lblConductor.setText("Conductor");
        filaCamion.add(lblConductor);

        txtConductor.setColumns(18);
        filaCamion.add(txtConductor);

        lblCapacidad.setText("Capacidad (t)");
        filaCamion.add(lblCapacidad);

        txtCapacidad.setColumns(6);
        filaCamion.add(txtCapacidad);

        filasCamiones.add(filaCamion);

        accionesCamiones.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        btnEncolar.setText("Encolar camión");
        btnEncolar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEncolarActionPerformed(evt);
            }
        });
        accionesCamiones.add(btnEncolar);

        btnDesencolar.setText("Desencolar camión");
        btnDesencolar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDesencolarActionPerformed(evt);
            }
        });
        accionesCamiones.add(btnDesencolar);

        btnCola.setText("Mostrar cola");
        btnCola.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnColaActionPerformed(evt);
            }
        });
        accionesCamiones.add(btnCola);

        filasCamiones.add(accionesCamiones);

        panelCamiones.add(filasCamiones, java.awt.BorderLayout.NORTH);

        pestanas.addTab("Camiones", panelCamiones);

        panelRutas.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 7, 6, 7));
        panelRutas.setLayout(new java.awt.BorderLayout());

        filasRutas.setLayout(new java.awt.GridLayout(3, 1));

        filaCiudad.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        lblCiudad.setText("Ciudad");
        filaCiudad.add(lblCiudad);

        txtCiudad.setColumns(13);
        filaCiudad.add(txtCiudad);

        btnCiudad.setText("Agregar ciudad");
        btnCiudad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCiudadActionPerformed(evt);
            }
        });
        filaCiudad.add(btnCiudad);

        btnBfs.setText("Ejecutar BFS");
        btnBfs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBfsActionPerformed(evt);
            }
        });
        filaCiudad.add(btnBfs);

        btnRutas.setText("Mostrar rutas");
        btnRutas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRutasActionPerformed(evt);
            }
        });
        filaCiudad.add(btnRutas);

        filasRutas.add(filaCiudad);

        filaTramo.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        lblOrigen.setText("Origen");
        filaTramo.add(lblOrigen);

        txtOrigen.setColumns(12);
        filaTramo.add(txtOrigen);

        lblDestinoRuta.setText("Destino");
        filaTramo.add(lblDestinoRuta);

        txtDestinoRuta.setColumns(12);
        filaTramo.add(txtDestinoRuta);

        lblKm.setText("Distancia (km)");
        filaTramo.add(lblKm);

        txtKm.setColumns(5);
        filaTramo.add(txtKm);

        btnRuta.setText("Agregar ruta");
        btnRuta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRutaActionPerformed(evt);
            }
        });
        filaTramo.add(btnRuta);

        filasRutas.add(filaTramo);

        filaCamino.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        lblCamino.setText("Camino (separar con comas)");
        filaCamino.add(lblCamino);

        txtCamino.setColumns(27);
        txtCamino.setToolTipText("Ejemplo: Lima, Chancay, Huacho");
        filaCamino.add(txtCamino);

        btnDistancia.setText("Calcular distancia");
        btnDistancia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDistanciaActionPerformed(evt);
            }
        });
        filaCamino.add(btnDistancia);

        filasRutas.add(filaCamino);

        panelRutas.add(filasRutas, java.awt.BorderLayout.NORTH);

        pestanas.addTab("Rutas", panelRutas);

        panelEntregas.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 7, 6, 7));
        panelEntregas.setLayout(new java.awt.BorderLayout());

        filasEntregas.setLayout(new java.awt.GridLayout(2, 1));

        filaEntrega.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        lblCodigoEntrega.setText("Código de producto");
        filaEntrega.add(lblCodigoEntrega);

        txtCodigoEntrega.setColumns(8);
        filaEntrega.add(txtCodigoEntrega);

        lblCantidad.setText("Cantidad");
        filaEntrega.add(lblCantidad);

        txtCantidad.setColumns(7);
        filaEntrega.add(txtCantidad);

        lblDestinoEntrega.setText("Destino");
        filaEntrega.add(lblDestinoEntrega);

        txtDestinoEntrega.setColumns(14);
        filaEntrega.add(txtDestinoEntrega);

        filasEntregas.add(filaEntrega);

        accionesEntrega.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        btnEntrega.setText("Registrar entrega");
        btnEntrega.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEntregaActionPerformed(evt);
            }
        });
        accionesEntrega.add(btnEntrega);

        filasEntregas.add(accionesEntrega);

        panelEntregas.add(filasEntregas, java.awt.BorderLayout.NORTH);

        pestanas.addTab("Despachos", panelEntregas);

        panelHistorial.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 7, 6, 7));
        panelHistorial.setLayout(new java.awt.BorderLayout());

        filasHistorial.setLayout(new java.awt.GridLayout(1, 1));

        accionesHistorial.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        btnHistorial.setText("Mostrar historial de entregas");
        btnHistorial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHistorialActionPerformed(evt);
            }
        });
        accionesHistorial.add(btnHistorial);

        filasHistorial.add(accionesHistorial);

        panelHistorial.add(filasHistorial, java.awt.BorderLayout.NORTH);

        pestanas.addTab("Historial", panelHistorial);

        cuerpo.add(pestanas, java.awt.BorderLayout.NORTH);

        panelResultados.setLayout(new java.awt.BorderLayout());

        lblResultado.setText("Resultado de la operación");
        lblResultado.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        lblResultado.setForeground(new java.awt.Color(35, 61, 80));
        panelResultados.add(lblResultado, java.awt.BorderLayout.NORTH);

        txtResultado.setEditable(false);
        txtResultado.setColumns(64);
        txtResultado.setRows(8);
        txtResultado.setFont(new java.awt.Font("Monospaced", 0, 15)); // NOI18N
        txtResultado.setText("Ingrese un producto o consulte el inventario.");
        txtResultado.setForeground(new java.awt.Color(35, 61, 80));
        txtResultado.setMargin(new java.awt.Insets(9, 12, 9, 12));
        scrollResultado.setViewportView(txtResultado);

        panelResultados.add(scrollResultado, java.awt.BorderLayout.CENTER);

        cuerpo.add(panelResultados, java.awt.BorderLayout.CENTER);

        pie.setLayout(new java.awt.BorderLayout());

        lblTotal.setText("Entregas registradas: 0");
        lblTotal.setFont(new java.awt.Font("SansSerif", 0, 13)); // NOI18N
        lblTotal.setForeground(new java.awt.Color(56, 85, 101));
        pie.add(lblTotal, java.awt.BorderLayout.WEST);

        btnSalir.setText("Salir");
        btnSalir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSalirActionPerformed(evt);
            }
        });
        pie.add(btnSalir, java.awt.BorderLayout.EAST);

        cuerpo.add(pie, java.awt.BorderLayout.SOUTH);

        getContentPane().add(cuerpo, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnInsertarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInsertarActionPerformed
        ejecutar(this::insertarProducto);
    }//GEN-LAST:event_btnInsertarActionPerformed

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
        ejecutar(this::buscarProducto);
    }//GEN-LAST:event_btnBuscarActionPerformed

    private void btnInventarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInventarioActionPerformed
        ejecutar(this::mostrarInventario);
    }//GEN-LAST:event_btnInventarioActionPerformed

    private void btnEncolarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEncolarActionPerformed
        ejecutar(this::encolarCamion);
    }//GEN-LAST:event_btnEncolarActionPerformed

    private void btnDesencolarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDesencolarActionPerformed
        ejecutar(this::desencolarCamion);
    }//GEN-LAST:event_btnDesencolarActionPerformed

    private void btnColaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnColaActionPerformed
        ejecutar(() -> mostrar("Cola de camiones por orden de llegada", cola.mostrar()));
    }//GEN-LAST:event_btnColaActionPerformed

    private void btnCiudadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCiudadActionPerformed
        ejecutar(this::agregarCiudad);
    }//GEN-LAST:event_btnCiudadActionPerformed

    private void btnBfsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBfsActionPerformed
        ejecutar(this::recorrerRed);
    }//GEN-LAST:event_btnBfsActionPerformed

    private void btnRutasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRutasActionPerformed
        ejecutar(() -> mostrar("Red de rutas de entrega", rutas.mostrarRutas()));
    }//GEN-LAST:event_btnRutasActionPerformed

    private void btnRutaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRutaActionPerformed
        ejecutar(this::agregarRuta);
    }//GEN-LAST:event_btnRutaActionPerformed

    private void btnDistanciaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDistanciaActionPerformed
        ejecutar(this::calcularDistancia);
    }//GEN-LAST:event_btnDistanciaActionPerformed

    private void btnEntregaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEntregaActionPerformed
        ejecutar(this::registrarEntrega);
    }//GEN-LAST:event_btnEntregaActionPerformed

    private void btnHistorialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHistorialActionPerformed
        ejecutar(this::mostrarHistorial);
    }//GEN-LAST:event_btnHistorialActionPerformed

    private void btnSalirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalirActionPerformed
        ejecutar(this::dispose);
    }//GEN-LAST:event_btnSalirActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel accionesCamiones;
    private javax.swing.JPanel accionesEntrega;
    private javax.swing.JPanel accionesHistorial;
    private javax.swing.JPanel accionesInventario;
    private javax.swing.JButton btnBfs;
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnCiudad;
    private javax.swing.JButton btnCola;
    private javax.swing.JButton btnDesencolar;
    private javax.swing.JButton btnDistancia;
    private javax.swing.JButton btnEncolar;
    private javax.swing.JButton btnEntrega;
    private javax.swing.JButton btnHistorial;
    private javax.swing.JButton btnInsertar;
    private javax.swing.JButton btnInventario;
    private javax.swing.JButton btnRuta;
    private javax.swing.JButton btnRutas;
    private javax.swing.JButton btnSalir;
    private javax.swing.JPanel cabecera;
    private javax.swing.JPanel cuerpo;
    private javax.swing.JPanel filaCamino;
    private javax.swing.JPanel filaCamion;
    private javax.swing.JPanel filaCiudad;
    private javax.swing.JPanel filaEntrega;
    private javax.swing.JPanel filaProducto;
    private javax.swing.JPanel filaTramo;
    private javax.swing.JPanel filasCamiones;
    private javax.swing.JPanel filasEntregas;
    private javax.swing.JPanel filasHistorial;
    private javax.swing.JPanel filasInventario;
    private javax.swing.JPanel filasRutas;
    private javax.swing.JLabel lblCamino;
    private javax.swing.JLabel lblCantidad;
    private javax.swing.JLabel lblCapacidad;
    private javax.swing.JLabel lblCiudad;
    private javax.swing.JLabel lblCodigo;
    private javax.swing.JLabel lblCodigoEntrega;
    private javax.swing.JLabel lblConductor;
    private javax.swing.JLabel lblDestinoEntrega;
    private javax.swing.JLabel lblDestinoRuta;
    private javax.swing.JLabel lblEstudiante;
    private javax.swing.JLabel lblKm;
    private javax.swing.JLabel lblNombre;
    private javax.swing.JLabel lblOrigen;
    private javax.swing.JLabel lblPlaca;
    private javax.swing.JLabel lblResultado;
    private javax.swing.JLabel lblStock;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JPanel panelCamiones;
    private javax.swing.JPanel panelEntregas;
    private javax.swing.JPanel panelHistorial;
    private javax.swing.JPanel panelInventario;
    private javax.swing.JPanel panelResultados;
    private javax.swing.JPanel panelRutas;
    private javax.swing.JTabbedPane pestanas;
    private javax.swing.JPanel pie;
    private javax.swing.JScrollPane scrollResultado;
    private javax.swing.JTextField txtCamino;
    private javax.swing.JTextField txtCantidad;
    private javax.swing.JTextField txtCapacidad;
    private javax.swing.JTextField txtCiudad;
    private javax.swing.JTextField txtCodigo;
    private javax.swing.JTextField txtCodigoEntrega;
    private javax.swing.JTextField txtConductor;
    private javax.swing.JTextField txtDestinoEntrega;
    private javax.swing.JTextField txtDestinoRuta;
    private javax.swing.JTextField txtKm;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtOrigen;
    private javax.swing.JTextField txtPlaca;
    private javax.swing.JTextArea txtResultado;
    private javax.swing.JTextField txtStock;
    // End of variables declaration//GEN-END:variables
}

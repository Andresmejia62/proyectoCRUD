package com.appregistros.proyectocrud.controller;

import connection.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import com.appregistros.proyectocrud.model.Producto;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProductoController {

    @FXML
    private TextField txtCodigo;
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtPrecio;
    @FXML
    private TextField txtStock;
    @FXML
    private ComboBox<String> cmbCategoria;
    @FXML
    private ComboBox<String> cmbMarca;
    @FXML
    private DatePicker dpFecha;
    @FXML
    private TextField txtDescripcion;

    @FXML
    private Button btnNuevo;
    @FXML
    private Button btnGuardar;
    @FXML
    private Button btnActualizar;
    @FXML
    private Button btnEliminar;

    @FXML
    private TextField txtBuscar;
    @FXML
    private ComboBox<String> cmbFiltroCategoria;
    @FXML
    private DatePicker dpDesde;
    @FXML
    private DatePicker dpHasta;
    @FXML
    private TextField txtPrecioMin;
    @FXML
    private TextField txtPrecioMax;
    @FXML
    private CheckBox chkSoloConStock;
    @FXML
    private ComboBox<String> cmbOrdenar;
    @FXML
    private Button btnBuscar;
    @FXML
    private Button btnLimpiar;

    @FXML
    private TableView<Producto> tblProductos;
    @FXML
    private TableColumn<Producto, String> colCodigo;
    @FXML
    private TableColumn<Producto, String> colNombre;
    @FXML
    private TableColumn<Producto, String> colCategoria;
    @FXML
    private TableColumn<Producto, String> colMarca;
    @FXML
    private TableColumn<Producto, Double> colPrecio;
    @FXML
    private TableColumn<Producto, Integer> colStock;
    @FXML
    private TableColumn<Producto, java.time.LocalDate> colFecha;

    private final ObservableList<Producto> productos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colMarca.setCellValueFactory(new PropertyValueFactory<>("marca"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaCreacion"));

        tblProductos.setItems(productos);

        cmbOrdenar.setItems(FXCollections.observableArrayList(
                "Nombre", "Precio", "Stock", "Fecha de creación"));

        cargarCombos();
        CargarProductos();

        tblProductos.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, seleccionado) -> {
                    if (seleccionado != null) mostrarEnFormulario(seleccionado);
                });
    }

    private void cargarCombos() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            cargarCombo(connection, "SELECT nombre FROM catalogos.categorias WHERE estado = 1 ORDER BY nombre", cmbCategoria);
            cargarCombo(connection, "SELECT nombre FROM catalogos.marcas WHERE estado = 1 ORDER BY nombre", cmbMarca);
            cargarCombo(connection, "SELECT nombre FROM catalogos.categorias WHERE estado = 1 ORDER BY nombre", cmbFiltroCategoria);
        } catch (SQLException ex) {
            System.out.println("error al cargar combos: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void cargarCombo(Connection connection, String sql, ComboBox<String> comboBox) {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            comboBox.getItems().clear();
            while (resultSet.next()) {
                comboBox.getItems().add(resultSet.getString(1));
            }
        } catch (SQLException ex) {
            System.out.println("error al consultar combo: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void mostrarEnFormulario(Producto p) {
        txtCodigo.setText(p.getCodigo());
        txtNombre.setText(p.getNombre());
        txtPrecio.setText(String.valueOf(p.getPrecio()));
        txtStock.setText(String.valueOf(p.getStock()));
        cmbCategoria.setValue(p.getCategoria());
        cmbMarca.setValue(p.getMarca());
        dpFecha.setValue(p.getFechaCreacion());
    }

    @FXML
    private void onNuevo() {
        limpiarFormulario();
        tblProductos.getSelectionModel().clearSelection();
    }

    @FXML
    private void onGuardar() {
        if (!validarFormulario()) return;

        if (dpFecha.getValue() == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "La fecha es obligatoria.");
            return;
        }

        String sql = "INSERT INTO catalogos.productos (codigo, nombre, descripcion, precio, stock, fecha_creacion, id_categoria, id_marca, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?, (SELECT id_categoria FROM catalogos.categorias WHERE nombre = ?), (SELECT id_marca FROM catalogos.marcas WHERE nombre = ?), 1)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, txtCodigo.getText().trim());
            statement.setString(2, txtNombre.getText().trim());
            statement.setString(3, txtDescripcion.getText().trim());
            statement.setDouble(4, Double.parseDouble(txtPrecio.getText().trim()));
            statement.setInt(5, Integer.parseInt(txtStock.getText().trim()));
            statement.setDate(6, Date.valueOf(dpFecha.getValue()));
            statement.setString(7, cmbCategoria.getValue());
            statement.setString(8, cmbMarca.getValue());

            int filasAfectadas = statement.executeUpdate();
            if (filasAfectadas > 0) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Producto guardado.");
                limpiarFormulario();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo guardar el producto.");
            }
        } catch (SQLException ex) {
            System.out.println("error al guardar producto: " + ex.getMessage());
            ex.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "No se pudo guardar el producto. Revisa los datos e intenta nuevamente.");
        }
    }

    @FXML
    private void onActualizar() {
        if (tblProductos.getSelectionModel().getSelectedItem() == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selecciona un producto de la tabla.");
            return;
        }
        if (!validarFormulario()) return;
    }

    @FXML
    private void onEliminar() {
        Producto seleccionado = tblProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selecciona un producto de la tabla.");
            return;
        }
        limpiarFormulario();
    }

    //  Acciones de filtros


    @FXML
    private void onLimpiarFiltros() {
        txtBuscar.clear();
        cmbFiltroCategoria.setValue(null);
        dpDesde.setValue(null);
        dpHasta.setValue(null);
        txtPrecioMin.clear();
        txtPrecioMax.clear();
        chkSoloConStock.setSelected(false);
        cmbOrdenar.setValue(null);
    }

    // Utilidades
    private boolean validarFormulario() {
        if (txtCodigo.getText().isBlank() || txtNombre.getText().isBlank()
                || cmbCategoria.getValue() == null || cmbMarca.getValue() == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Código, nombre, categoría y marca son obligatorios.");
            return false;
        }
        try {
            double precio = Double.parseDouble(txtPrecio.getText().trim());
            int stock = Integer.parseInt(txtStock.getText().trim());
            if (precio <= 0) {
                mostrarAlerta(Alert.AlertType.WARNING, "El precio debe ser mayor que 0.");
                return false;
            }
            if (stock < 0) {
                mostrarAlerta(Alert.AlertType.WARNING, "El stock no puede ser negativo.");
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Precio y stock deben ser numéricos.");
            return false;
        }
        return true;
    }

    private void limpiarFormulario() {
        txtCodigo.clear();
        txtNombre.clear();
        txtPrecio.clear();
        txtStock.clear();
        txtDescripcion.clear();
        cmbCategoria.setValue(null);
        cmbMarca.setValue(null);
        dpFecha.setValue(null);
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void CargarProductos() {
        System.out.println("CargarProductos invoked");
        productos.clear();

        String sql = "SELECT p.id_producto, p.codigo, p.nombre, p.descripcion, p.precio, p.stock, p.fecha_creacion, " +
               "c.nombre AS categoria, m.nombre AS marca " +
               "FROM catalogos.productos p " +
               "INNER JOIN catalogos.categorias c ON p.id_categoria = c.id_categoria " +
               "INNER JOIN catalogos.marcas m ON p.id_marca = m.id_marca " +
               "WHERE p.estado = 1 " +
               "ORDER BY p.id_producto";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
               int id = resultSet.getInt("id_producto");
               String codigo = resultSet.getString("codigo");
               String nombre = resultSet.getString("nombre");
               String categoria = resultSet.getString("categoria");
               String marca = resultSet.getString("marca");
               Double precio = resultSet.getDouble("precio");
               int stock = resultSet.getInt("stock");
               java.time.LocalDate fechaCreacion = resultSet.getDate("fecha_creacion").toLocalDate();
               Producto producto = new Producto(id, codigo, resultSet.getString("descripcion"), nombre, precio, stock, fechaCreacion, 0, categoria, 0, marca, 1);
               productos.add(producto);
            }

            tblProductos.setItems(productos);

        } catch (SQLException ex) {
            System.out.println("error al cargar Productos: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

/* Este método está diseñado únicamente para cargar todos los datos del resultset utilizado */

    private Producto crearProductoDesdeResultSet(
            ResultSet rs
    ) throws SQLException {

        return new Producto(

                rs.getInt(
                        "id_producto"
                ),

                rs.getString(
                        "codigo"
                ),

                rs.getString(
                        "descripcion"
                ),

                rs.getString(
                        "nombre"
                ),

                rs.getDouble(
                        "precio"
                ),

                rs.getInt(
                        "stock"
                ),

                rs.getDate(
                        "fecha_creacion"
                ).toLocalDate(),

                rs.getInt(
                        "id_categoria"
                ),

                rs.getString(
                        "categoria"
                ),

                rs.getInt(
                        "id_marca"
                ),

                rs.getString(
                        "marca"
                ),

                rs.getInt(
                        "estado"
                )
        );
    }
    /* Buscar Productos */


    @FXML
    private void buscarProductos() {

        productos.clear();

        /*
         * La consulta inicia con los INNER JOIN necesarios
         * y con una condición permanente:
         *
         * p.estado = 1
         *
         * De esta forma nunca mostramos productos
         * eliminados lógicamente.
         */
        StringBuilder sql = new StringBuilder("""
                SELECT
                    p.id_producto,
                    p.codigo,
                    p.nombre,
                    p.descripcion,
                    p.precio,
                    p.stock,
                    p.fecha_creacion,
                    c.id_categoria,
                    c.nombre AS categoria,
                    m.id_marca,
                    m.nombre AS marca,
                    p.estado
                FROM catalogos.productos p
                INNER JOIN catalogos.categorias c
                    ON p.id_categoria = c.id_categoria
                INNER JOIN catalogos.marcas m
                    ON p.id_marca = m.id_marca
                WHERE p.estado = 1
                """);
        /*
         * Aquí almacenaremos los valores que posteriormente
         * serán enviados al PreparedStatement.
         */
        List<Object> parametros =
                new ArrayList<>();

        // Filtro por código o por nombre
        String texto =
                txtBuscar.getText().trim();

        if (!texto.isBlank()) {

            /*
             * OR permite que el texto coincida con código
             * o nombre.
             *
             * ILIKE permite búsquedas parciales sin importar
             * mayúsculas y minúsculas.
             */
            sql.append("""            
                    AND (
                        p.codigo ILIKE ?
                        OR p.nombre ILIKE ?
                    )
                    """);

            parametros.add(
                    "%" + texto + "%"
            );

            parametros.add(
                    "%" + texto + "%"
            );
        }

        // Filtro por fecha desde
        LocalDate fechaDesde =
                dpDesde.getValue();

        if (fechaDesde != null) {
            sql.append("""
                    AND p.fecha_creacion >= ?
                    """);
            parametros.add(fechaDesde);
        }

        // Filtro por fechahasta
        LocalDate fechaHasta =
                dpHasta.getValue();

        if (fechaHasta != null) {

            sql.append("""
                    
                    AND p.fecha_creacion <= ?
                    """);

            parametros.add(fechaHasta);
        }

        /*
         * Verificamos que las fechas tengan sentido.
         *
         * "Desde" no debería ser posterior a "Hasta".
         */
        if (
                fechaDesde != null
                        &&
                        fechaHasta != null
                        &&
                        fechaDesde.isAfter(fechaHasta)
        ) {

            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Rango de fechas incorrecto"
            );

            return;
        }

        // Aplicación del ORDER BY

        /*
         * ORDER BY no utiliza un parámetro ? para nombres
         * de columnas.
         *
         * Por esta razón utilizamos solamente opciones
         * previamente definidas en el ComboBox.
         *
         * Esto funciona como una whitelist.
         */
        String orden =
                cmbOrdenar.getValue();

        if (orden == null) {
            orden = "Nombre";
        }

        switch (orden) {

            case "Precio menor a mayor" ->
                    sql.append("""
                            
                            ORDER BY p.precio ASC
                            """);

            case "Precio mayor a menor" ->
                    sql.append("""
                            
                            ORDER BY p.precio DESC
                            """);

            case "Fecha más reciente" ->
                    sql.append("""
                            
                            ORDER BY p.fecha_creacion DESC
                            """);

            case "Fecha más antigua" ->
                    sql.append("""
                            
                            ORDER BY p.fecha_creacion ASC
                            """);

            case "Stock mayor a menor" ->
                    sql.append("""
                            
                            ORDER BY p.stock DESC
                            """);

            case "Stock menor a mayor" ->
                    sql.append("""
                            
                            ORDER BY p.stock ASC
                            """);

            default ->
                    sql.append("""
                            
                            ORDER BY p.nombre ASC
                            """);
        }

        //Ejecución de la consulta

        try (
                Connection conexion =
                        DatabaseConnection.getConnection();

                PreparedStatement ps =
                        conexion.prepareStatement(
                                sql.toString()
                        )
        ) {

            /*
             * Recorremos la lista de parámetros para
             * asignarlos dinámicamente al PreparedStatement.
             */
            for (
                    int i = 0;
                    i < parametros.size();
                    i++
            ) {
                Object valor =
                        parametros.get(i);

                if (valor instanceof LocalDate fecha) {

                    ps.setDate(
                            i + 1,
                            Date.valueOf(fecha)
                    );

                } else if (valor instanceof BigDecimal numero) {

                    ps.setBigDecimal(
                            i + 1,
                            numero
                    );

                } else {
                    ps.setObject(
                            i + 1,
                            valor
                    );
                }
            }

            try (
                    ResultSet rs =
                            ps.executeQuery()
            ) {

                while (rs.next()) {

                    productos.add(
                            crearProductoDesdeResultSet(rs)
                    );
                }
            }

            tblProductos.setItems(
                    productos
            );

            /*
             * Podemos informar al estudiante/usuario cuando
             * ningún registro satisface las condiciones.
             */
            if (productos.isEmpty()) {
                mostrarAlerta(
                        Alert.AlertType.INFORMATION,
                        "Sin resultados"
                );
            }
        } catch (SQLException e) {
            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "No se pudo realizar la búsqueda."
            );
        }
    }
    @FXML
    private void ActualizarTabla() {
        CargarProductos();
    }
}
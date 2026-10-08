package com.appregistros.proyectocrud.controller;

import connection.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import com.appregistros.proyectocrud.model.Producto;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ProductoController {

    // ===== Formulario =====
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

    // Filtros
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

    //  Tabla
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

        tablaProductos.setItems(productos);

        cmbOrdenar.setItems(FXCollections.observableArrayList(
                "Nombre", "Precio", "Stock", "Fecha de creación"));

        cargarCombos();

        tablaProductos.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, seleccionado) -> {
                    if (seleccionado != null) mostrarEnFormulario(seleccionado);
                });
    }

    private void cargarCombos() {

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

    //  Acciones del formulario
    @FXML
    private void onNuevo() {
        limpiarFormulario();
        tablaProductos.getSelectionModel().clearSelection();
    }

    @FXML
    private void onGuardar() {
        if (!validarFormulario()) return;
        mostrarAlerta(Alert.AlertType.INFORMATION, "Producto guardado.");
        onBuscar();
        limpiarFormulario();
    }

    @FXML
    private void onActualizar() {
        if (tablaProductos.getSelectionModel().getSelectedItem() == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selecciona un producto de la tabla.");
            return;
        }
        if (!validarFormulario()) return;
        onBuscar();
    }

    @FXML
    private void onEliminar() {
        Producto seleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selecciona un producto de la tabla.");
            return;
        }
        onBuscar();
        limpiarFormulario();
    }

    //  Acciones de filtros
    @FXML
    private void onBuscar() {

    }

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
        onBuscar();
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

    private void CargarProductos(){
        System.out.println("CargarProductos invoked");
        productos.clear();

        String sql = "SELECT * FROM producto ";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String Titulo = resultSet.getString("titulo");
                String Autor = resultSet.getString("autor");
                String categoria = resultSet.getString("categoria");
                Double Precio = resultSet.getDouble("Precio");
                int Stock = resultSet.getInt("stock");
                Producto producto = new Producto();
                productos.add(producto);

            }

            tblProductos.setItems(productos);

        } catch (SQLException ex) {
            System.out.println("error al cargar Productos: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    }

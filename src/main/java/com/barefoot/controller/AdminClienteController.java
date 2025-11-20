// src/main/java/com/barefoot/controller/AdminClienteController.java
package com.barefoot.controller;

import com.barefoot.model.Pedido;
import com.barefoot.model.Usuario;
import com.barefoot.repository.UsuarioRepository; // Importamos el repo
import com.barefoot.repository.PedidoRepository;   // Importamos el repo
import com.barefoot.service.DashboardService; // Importamos el servicio
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/clientes") // La URL base será /admin/clientes
public class AdminClienteController {

    @Autowired
    private UsuarioRepository usuarioRepository; // Usamos el repo para buscar

    @Autowired
    private PedidoRepository pedidoRepository; // Usamos el repo para el historial

    @Autowired
    private DashboardService dashboardService; // Usamos el servicio para las stats

    /**
     * Requerimiento: Lista de clientes
     */
    @GetMapping
    public String mostrarListaClientes(Model model) {
        // Usamos el nuevo método del repositorio para traer solo USUARIOS
        List<Usuario> listaClientes = usuarioRepository.findAllByRol(Usuario.Rol.USUARIO);

        model.addAttribute("clientes", listaClientes);

        // Apuntará a "templates/admin/clientes/lista.html"
        return "admin/clientes/lista";
    }

    /**
     * Requerimiento: Historial de compras y Estadísticas por cliente
     */
    @GetMapping("/{id:\\d+}")
    public String mostrarDetalleCliente(@PathVariable("id") Long clienteId, Model model) {

        Usuario cliente = usuarioRepository.findById(clienteId).orElse(null);

        if (cliente == null || !cliente.getRol().equals(Usuario.Rol.USUARIO)) {
            return "redirect:/admin/clientes";
        }

        List<Pedido> historial = pedidoRepository.findByUsuarioId(clienteId);
        Map<String, Object> estadisticas = dashboardService.calcularEstadisticasCliente(clienteId);

        model.addAttribute("cliente", cliente);
        model.addAttribute("historial", historial);
        model.addAttribute("stats", estadisticas);

        return "admin/clientes/detalle";
    }
}
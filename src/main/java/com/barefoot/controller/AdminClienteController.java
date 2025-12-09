// src/main/java/com/barefoot/controller/AdminClienteController.java
package com.barefoot.controller;

import com.barefoot.model.Pedido;
import com.barefoot.model.Usuario;
import com.barefoot.repository.UsuarioRepository; // Importamos el repo
import com.barefoot.repository.PedidoRepository;   // Importamos el repo
import com.barefoot.service.DashboardService; // Importamos el servicio
import com.barefoot.security.RoleValidator; // Importamos la clase utility para validar roles
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
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
     * Requerimiento: Lista de clientes, administradores y encargados
     */
    @GetMapping
    public String mostrarListaUsuarios(Model model, HttpSession session) {
        // Solo un ADMIN puede ver esta página
        if (!RoleValidator.esAdmin(session)) {
            return "redirect:/"; // O a una página de acceso denegado
        }

        // Traemos todos los usuarios y los separamos por rol
        List<Usuario> todos = usuarioRepository.findAll();

        model.addAttribute("clientes", todos.stream()
                .filter(u -> u.getRol().equals(Usuario.Rol.USUARIO))
                .collect(Collectors.toList()));

        model.addAttribute("admins", todos.stream()
                .filter(u -> u.getRol().equals(Usuario.Rol.ADMIN))
                .collect(Collectors.toList()));

        model.addAttribute("encargados", todos.stream()
                .filter(u -> u.getRol().equals(Usuario.Rol.ENCARGADO))
                .collect(Collectors.toList()));

        // Apuntará a "templates/admin/clientes/lista.html"
        return "admin/clientes/lista";
    }


    /**
     * Requerimiento: Historial de compras y Estadísticas por cliente
     */
    @GetMapping("/{id:\\d+}")
    public String mostrarDetalleCliente(@PathVariable("id") Long clienteId, Model model, HttpSession session) {
        // Solo un ADMIN puede ver los detalles
        if (!RoleValidator.esAdmin(session)) {
            return "redirect:/";
        }

        Usuario cliente = usuarioRepository.findById(clienteId).orElse(null);

        if (cliente == null) {
            return "redirect:/admin/clientes";
        }

        // Si no es un cliente regular, no mostramos historial de compras
        if (cliente.getRol().equals(Usuario.Rol.USUARIO)) {
            List<Pedido> historial = pedidoRepository.findByUsuarioId(clienteId);
            Map<String, Object> estadisticas = dashboardService.calcularEstadisticasCliente(clienteId);
            model.addAttribute("historial", historial);
            model.addAttribute("stats", estadisticas);
        }

        model.addAttribute("cliente", cliente);

        return "admin/clientes/detalle";
    }

    /**
     * Cambiar el rol de un usuario.
     * Solo el ADMIN puede hacerlo.
     * Un ADMIN no puede cambiarse el rol a sí mismo.
     * Se puede cambiar de ADMIN a ENCARGADO/USUARIO, y viceversa.
     */
    @PostMapping("/{id}/cambiar-rol")
    public String cambiarRolUsuario(
            @PathVariable("id") Long usuarioId,
            @RequestParam String nuevoRol,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // 1. Solo ADMIN puede cambiar roles
        if (!RoleValidator.esAdmin(session)) {
            redirectAttributes.addFlashAttribute("mensaje", "No tienes permisos para esta acción.");
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
            return "redirect:/admin/clientes";
        }

        // 2. Un admin no puede cambiarse el rol a sí mismo para evitar auto-bloqueo
        Long adminId = (Long) session.getAttribute("usuarioId");
        if (adminId.equals(usuarioId)) {
            redirectAttributes.addFlashAttribute("mensaje", "No puedes cambiar tu propio rol.");
            redirectAttributes.addFlashAttribute("tipoMensaje", "warning");
            return "redirect:/admin/clientes";
        }

        try {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Usuario.Rol rolActual = usuario.getRol();
            Usuario.Rol rolNuevo = Usuario.Rol.valueOf(nuevoRol.toUpperCase());

            // Si el rol no ha cambiado, no hacemos nada
            if (rolActual.equals(rolNuevo)) {
                redirectAttributes.addFlashAttribute("mensaje", "El usuario ya tiene el rol " + rolNuevo.name());
                redirectAttributes.addFlashAttribute("tipoMensaje", "info");
                return "redirect:/admin/clientes";
            }

            usuario.setRol(rolNuevo);
            usuarioRepository.save(usuario);

            log.info("Rol del usuario {} ({}) cambiado de {} a {}", usuario.getId(), usuario.getEmail(), rolActual, rolNuevo);

            redirectAttributes.addFlashAttribute("mensaje",
                    String.format("Rol de %s cambiado de %s a %s.", usuario.getNombre(), rolActual.name(), rolNuevo.name()));
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensaje", "Rol inválido: " + nuevoRol);
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        } catch (Exception e) {
            log.error("Error al cambiar rol del usuario: ", e);
            redirectAttributes.addFlashAttribute("mensaje", "Error inesperado: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        }

        return "redirect:/admin/clientes";
    }
}

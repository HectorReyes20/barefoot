package com.barefoot.security;

import jakarta.servlet.http.HttpSession;

/**
 * Utility class para validar roles de usuario desde la sesión HTTP
 */
public class RoleValidator {

    /**
     * Verifica si el usuario en sesión es ADMIN
     */
    public static boolean esAdmin(HttpSession session) {
        String rol = (String) session.getAttribute("usuarioRol");
        return "ADMIN".equals(rol);
    }

    /**
     * Verifica si el usuario en sesión es ENCARGADO
     */
    public static boolean esEncargado(HttpSession session) {
        String rol = (String) session.getAttribute("usuarioRol");
        return "ENCARGADO".equals(rol);
    }

    /**
     * Verifica si el usuario en sesión es ADMIN o ENCARGADO
     */
    public static boolean esAdminOEncargado(HttpSession session) {
        String rol = (String) session.getAttribute("usuarioRol");
        return "ADMIN".equals(rol) || "ENCARGADO".equals(rol);
    }

    /**
     * Verifica si el usuario en sesión tiene un rol específico
     */
    public static boolean tieneRol(HttpSession session, String rolBuscado) {
        String rol = (String) session.getAttribute("usuarioRol");
        return rolBuscado.equals(rol);
    }

    /**
     * Verifica si el usuario está autenticado
     */
    public static boolean estaAutenticado(HttpSession session) {
        return session.getAttribute("usuarioId") != null;
    }

    /**
     * Obtiene el rol actual del usuario
     */
    public static String obtenerRol(HttpSession session) {
        return (String) session.getAttribute("usuarioRol");
    }
}


package mx.edu.unpa.actividadesapi.service.storage.impl;

import mx.edu.unpa.actividadesapi.exception.BusinessException;
import mx.edu.unpa.actividadesapi.service.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
public class LocalStorageServiceImpl implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalStorageServiceImpl.class);

    // Se configura en application.properties
    @Value("${storage.local.base-path:uploads}")
    private String basePath;

    @Value("${storage.local.base-url:http://localhost:8181/uploads}")
    private String baseUrl;

    private static final List<String> TIPOS_PERMITIDOS =
            List.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_TAMANO = 5 * 1024 * 1024; // 5 MB

    @Override
    public String guardar(MultipartFile archivo, String subcarpeta) {
        validarArchivo(archivo);

        try {
            // Crear carpeta si no existe
            Path directorio = Paths.get(basePath, subcarpeta);
            Files.createDirectories(directorio);

            // Generar nombre único para evitar colisiones
            String extension = obtenerExtension(archivo.getOriginalFilename());
            String nombreUnico = UUID.randomUUID() + extension;

            Path destino = directorio.resolve(nombreUnico);
            Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            String url = baseUrl + "/" + subcarpeta + "/" + nombreUnico;
            log.info("Imagen guardada: {}", url);
            return url;

        } catch (IOException e) {
            log.error("Error al guardar imagen: {}", e.getMessage());
            throw new BusinessException("No se pudo guardar la imagen: " + e.getMessage());
        }
    }

    @Override
    public void eliminar(String url) {
        try {
            String rutaRelativa = url.replace(baseUrl + "/", "");
            Path archivo = Paths.get(basePath, rutaRelativa);
            Files.deleteIfExists(archivo);
            log.info("Imagen eliminada: {}", url);
        } catch (IOException e) {
            log.warn("No se pudo eliminar la imagen {}: {}", url, e.getMessage());
        }
    }

    private void validarArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new BusinessException("El archivo está vacío");
        }
        if (!TIPOS_PERMITIDOS.contains(archivo.getContentType())) {
            throw new BusinessException("Tipo de archivo no permitido. Use JPG, PNG o WEBP");
        }
        if (archivo.getSize() > MAX_TAMANO) {
            throw new BusinessException("El archivo supera el límite de 5 MB");
        }
    }

    private String obtenerExtension(String nombreArchivo) {
        if (nombreArchivo == null || !nombreArchivo.contains(".")) return ".jpg";
        return nombreArchivo.substring(nombreArchivo.lastIndexOf("."));
    }
}

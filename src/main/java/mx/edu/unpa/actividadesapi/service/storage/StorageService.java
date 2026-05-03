package mx.edu.unpa.actividadesapi.service.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    // Guarda el archivo y retorna la URL/ruta para guardar en BD
    String guardar(MultipartFile archivo, String subcarpeta);

    // Elimina un archivo dado su URL
    void eliminar(String url);
}

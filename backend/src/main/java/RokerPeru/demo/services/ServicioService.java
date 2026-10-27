package RokerPeru.demo.services;

import RokerPeru.demo.dto.ServicioClienteResponseDTO;
import RokerPeru.demo.dto.ServicioRequestDTO;
import RokerPeru.demo.dto.ServicioResponseDTO;
import RokerPeru.demo.entities.Servicio;
import RokerPeru.demo.repository.ServicioRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicioService {
    private final ServicioRepository servicioRepository;

    @Transactional
    public ServicioResponseDTO crearServicio(ServicioRequestDTO dto){
        Servicio s = new Servicio ();
        copiarDatos(s, dto);
        return aDto(servicioRepository.save(s));
    }

    @Transactional(readOnly = true)
    public List<ServicioResponseDTO> listarServicio() {
        return servicioRepository.findAll(Sort.by("nombreServicio"))
                .stream()
                .map(this::aDto)
                .toList();
    }
    @Transactional(readOnly = true)
    public ServicioResponseDTO buscarPorIdServicio(Long id) {
        return aDto(obtenerServicio(id));
    }

    @Transactional
    public ServicioResponseDTO actualizarServicio(Long id, ServicioRequestDTO dto) {
        Servicio s = obtenerServicio(id);
        copiarDatos(s, dto);
        return aDto(s);
    }

    @Transactional
    public void eliminarServicio(Long id) {
        Servicio s = obtenerServicio(id);
        servicioRepository.delete(s);
    }

    @Transactional(readOnly = true)
    public List<ServicioClienteResponseDTO> listarServiciosCliente() {
        return servicioRepository.findAll(Sort.by("nombreServicio"))
                .stream()
                .map(this::aClienteDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServicioClienteResponseDTO> buscarServiciosCliente(String texto) {
        return servicioRepository.buscarServicio(texto.trim())
                .stream()
                .map(this::aClienteDto)
                .toList();
    }

    private void copiarDatos(Servicio s, ServicioRequestDTO dto) {
        s.setCategoriaServicio(normalizar(dto.getCategoriaServicio()));
        s.setNombreServicio(normalizar(dto.getNombreServicio()));
        s.setTipoServicio(normalizar(dto.getTipoServicio()));
        s.setDescripcionServicio(normalizar(dto.getDescripcionServicio()));
        s.setPrecioServicio(dto.getPrecioServicio());
        s.setDuracionEstimadaServicio(normalizar(dto.getDuracionEstimadaServicio()));
        s.setCantidadPersonalServicio(normalizar(dto.getCantidadPersonalServicio()));
    }

    private String normalizar(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }

    private Servicio obtenerServicio(Long id) {
        return servicioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado: " + id));
    }



    private ServicioResponseDTO aDto(Servicio s) {
        return new ServicioResponseDTO(
                s.getIdServicio(),
                s.getCategoriaServicio(),
                s.getNombreServicio(),
                s.getTipoServicio(),
                s.getDescripcionServicio(),
                s.getPrecioServicio(),
                s.getDuracionEstimadaServicio(),
                s.getCantidadPersonalServicio()
        );
    }

    private ServicioClienteResponseDTO aClienteDto(Servicio s) {
        return new ServicioClienteResponseDTO(
                s.getCategoriaServicio(),
                s.getNombreServicio(),
                s.getTipoServicio(),
                s.getDescripcionServicio(),
                s.getPrecioServicio(),
                s.getDuracionEstimadaServicio(),
                s.getCantidadPersonalServicio()
        );
    }
}
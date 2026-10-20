package RokerPeru.demo.services;

import RokerPeru.demo.dto.ServicioRequestDTO;
import RokerPeru.demo.dto.ServicioResponseDTO;
import RokerPeru.demo.entities.Servicio;
import RokerPeru.demo.repository.ServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicioService {

    private final ServicioRepository servicioRepository;

    public ServicioResponseDTO crear(ServicioRequestDTO dto) {
        Servicio s = new Servicio();
        s.setCategoriaServicio(dto.getCategoriaServicio());
        s.setNombreServicio(dto.getNombreServicio());
        s.setTipoServicio(dto.getTipoServicio());
        s.setDescripcionServicio(dto.getDescripcionServicio());
        s.setPrecioServicio(dto.getPrecioServicio());
        s.setDuracionEstimadaServicio(dto.getDuracionEstimadaServicio());
        s.setCantidadPersonalServicio(dto.getCantidadPersonalServicio());
        return aDto(servicioRepository.save(s));
    }

    public List<ServicioResponseDTO> listar() {
        return servicioRepository.findAll().stream().map(this::aDto).toList();
    }

    public ServicioResponseDTO buscarPorId(Long id) {
        return aDto(servicioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado: " + id)));
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
}
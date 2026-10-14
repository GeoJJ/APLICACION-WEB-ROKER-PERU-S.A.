package RokerPeru.demo.services;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import RokerPeru.demo.entities.Rol;
import RokerPeru.demo.repository.RolRepository;

@Service
public class RolServiceImpl implements RolService {
    @Autowired
    private RolRepository rolRepository;

    @Override
    public List<Rol> Listar() {
        return rolRepository.findAll();
    }
}

package services;
import entities.Rol;
import java.util.List;
import repository.RolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RolServiceImpl implements RolService {
    @Autowired
    private RolRepository rolRepository;

    @Override
    public List<Rol> Listar() {
        return rolRepository.findAll();
    }
}

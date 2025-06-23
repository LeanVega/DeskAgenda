package persistencia;

import logica.Tarea;
import java.util.List;

public interface IRepositorioTareas {
    void guardarTareas(List<Tarea> tareas);
    List<Tarea> cargarTareas();
    void exportarTareas(List<Tarea> tareas, String archivo);
    List<Tarea> importarTareas(String archivo);
}

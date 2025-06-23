package logica;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;

public class GestorSonido {

    private Clip clip;

    public void reproducirSonido() { // Renombrado.
        try {
            // Asegurarse de encontrar el archivo de sonido en el path
            InputStream audioSrc = getClass().getResourceAsStream("/notification/notification_sound.wav");
            
            if (audioSrc == null) {
                System.err.println("Archivo de sonido no encontrado: /notification/notification_sound.wav. Por favor, asegúrese de que el archivo .wav exista en la carpeta de recursos.");
                return;
            }

            InputStream bufferedIn = new BufferedInputStream(audioSrc);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);
            
            clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start(); // Reproducir una vez

            // Opcional: si quieres que el sonido se detenga después de un tiempo o al cerrar
            // clip.addLineListener(event -> {
            //     if (event.getType() == LineEvent.Type.STOP) {
            //         clip.close();
            //     }
            // });

        } catch (UnsupportedAudioFileException e) {
            System.err.println("Formato de audio no soportado: " + e.getMessage());
            // Considerar usar un formato como WAV si MP3 da problemas sin librerías adicionales
        } catch (Exception e) {
            System.err.println("Error al reproducir sonido: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void detenerSonido() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close();
        }
    }
    
    // Método de prueba para verificar la reproducción de sonido directamente
    /*
    public static void main(String[] args) {
        GestorSonido gs = new GestorSonido();
        gs.reproducirSonido(); // Updated call for testing
        // Mantener el programa corriendo un poco para que el sonido se reproduzca
        try {
            Thread.sleep(5000); // Espera 5 segundos
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    */
}
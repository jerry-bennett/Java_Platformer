import javax.sound.sampled.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    private static final Map<String, byte[]> soundCache = new HashMap<>();
    private static final Map<String, AudioFormat> formatCache = new HashMap<>();

    public static void loadSound(String name, String path) {
        try {
            InputStream is = SoundManager.class.getResourceAsStream(path);
            AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
            
            // Convert to a standard format Java likes (16-bit PCM)
            AudioFormat baseFormat = ais.getFormat();
            AudioFormat decodedFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
                baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false
            );
            
            AudioInputStream convertedAis = AudioSystem.getAudioInputStream(decodedFormat, ais);
            byte[] data = convertedAis.readAllBytes();
            
            soundCache.put(name, data);
            formatCache.put(name, decodedFormat);
            
            convertedAis.close();
        } catch (Exception e) {
            System.err.println("Could not cache: " + path);
        }

        try {
            Clip warmUp = AudioSystem.getClip();
            AudioFormat decodedFormat = null;
            warmUp.open(decodedFormat, new byte[0], 0, 0); 
            warmUp.close();
        } catch (Exception e) {}
    }

    public static void playSound(String name) {
        byte[] data = soundCache.get(name);
        AudioFormat format = formatCache.get(name);
        
        if (data == null) {
            System.err.println("Sound not found in cache: " + name); // Tells you if the name is wrong
            return;
        }

        new Thread(() -> {
            try {
                Clip clip = AudioSystem.getClip();
                clip.open(format, data, 0, data.length);
                clip.start();
                
                // This listener ensures the clip is disposed of ONLY when done playing
                clip.addLineListener(e -> {
                    if (e.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
            } catch (Exception e) { 
                e.printStackTrace(); 
            }
        }).start();
    }
}
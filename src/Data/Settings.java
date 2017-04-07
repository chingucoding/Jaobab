package Data;

/**
 * Class containing all Settings
 * Created by Marcel on 07.04.2017.
 */
public final class Settings {

    public static boolean multiThreading=true;

    public static int threadCountLimit;

    static{
        threadCountLimit=Runtime.getRuntime().availableProcessors();
    }

    private Settings(){}
}
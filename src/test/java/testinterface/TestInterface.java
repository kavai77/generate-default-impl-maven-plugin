package testinterface;

import java.util.List;
import java.util.Map;

/**
 * @author Csaba Kávai
 */
public interface TestInterface
{
    byte getByte(int[] param1, int param2);

    char getChar(String param);

    double getDouble(Void aVoid);

    float getFloat();

    int getInt(List<String> param);

    long getLong(Map<String, String> map);

    boolean isBoolean();

    String getName(String firstName);

    short getShort(Object param1, int... param2);

    <T> T getValue(T t);

    void doSomething();
}

generate-default-impl-maven-plugin
==================================
This plugin is intended to generate an empty implementation of an arbitrary Java interface.
Let's look an example. Provided we have an interface like:

```java
public interface TestInterface
{
    char getChar(String param);
    float getFloat();
    int getInt(List<String> param);
    boolean isBoolean();
    String getName(String firstName);
    void doSomething();
}
```

This tool helps you generate a code like this:

```java
public class TestInterfaceDefaultImpl implements com.company.TestInterface {
     @Override public char getChar(java.lang.String p1) {return 0;}
     @Override public float getFloat() {return 0;}
     @Override public int getInt(java.util.List p1) {return 0;}
     @Override public boolean isBoolean() {return false;}
     @Override public java.lang.String getName(java.lang.String p1) {return null;}
     @Override public void doSomething() {}
}
```

This means that for every primitive type we it returns a 0 or false, and for every object ot returns null;
After you activate this plugin in your generate-sources maven phase, you can override the generated class.

You can activate the plugin with the following snippet:
```xml
<plugin>
    <groupId>com.github.kavaicsaba</groupId>
    <artifactId>generate-default-impl-plugin</artifactId>
    <version>1.0</version>
    <configuration>
        <interfaces>
            <interface>com.company.TestInterface</interface>
            <interface>com.company.MyInterface2</interface>
        </interfaces>
        <targetPackage>com.company.generatedstuff</targetPackage>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>generate-default-impl</goal>
            </goals>                        
        </execution>
    </executions> 
    <dependencies>                    
        <dependency>
            <groupId>${project.groupId}</groupId>
            <artifactId>${project.artifactId}</artifactId>
            <version>${project.version}</version>
        </dependency>                    
    </dependencies>
</plugin>
```
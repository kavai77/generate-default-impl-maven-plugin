package com.github.himadri77;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import testinterface.TestEmptyInterface;
import testinterface.TestInterface;

import java.io.File;

import java.lang.reflect.Method;

import java.net.URL;
import java.net.URLClassLoader;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * @author Csaba Kávai
 */
public class GenerateDefaultImplTest
{
    GenerateDefaultImpl generateDefault;

    @Before public void init()
    {
        generateDefault = new GenerateDefaultImpl();
        generateDefault.classNamePostfix = "Impl";
        generateDefault.outputDirectory = new File("./target/generated-test-sources");
        generateDefault.targetPackage = "testinterface";
    }

    @Test public void testExecute() throws Exception
    {
        generateDefault.interfaces = new String[] { TestInterface.class.getName() };
        generateDefault.execute();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        File interfaceFile = new File("./src/test/java/testinterface/TestInterface.java");

        assertTrue(interfaceFile.exists());
        File generatedImplFile = new File(generateDefault.outputDirectory, "testinterface/TestInterfaceImpl.java");

        assertTrue(generatedImplFile.exists());
        File classOutputDir = new File("./target/test-runtime-compilation-classes");

        classOutputDir.mkdirs();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> compUnits = fileManager.getJavaFileObjects(interfaceFile, generatedImplFile);
        Boolean compilationResult = compiler.getTask(null, fileManager, null, Arrays.asList("-d", classOutputDir.getAbsolutePath()), null,
                compUnits).call();

        assertEquals(Boolean.TRUE, compilationResult);
        URLClassLoader urlClassLoader = (URLClassLoader) getClass().getClassLoader();
        Method addURLMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);

        addURLMethod.setAccessible(true);
        addURLMethod.invoke(urlClassLoader, classOutputDir.toURI().toURL());
        TestInterface instance = (TestInterface) Class.forName("testinterface.TestInterfaceImpl").newInstance();

        assertEquals(0, instance.getByte(null, 1));
        assertEquals(0.0, instance.getDouble(null), 0.01);
        assertEquals(0.0, instance.getFloat(), 0.01);
        assertEquals(0, instance.getShort(new Object(), 1, 2, 3));
        assertEquals(0, instance.getChar("dsdsd"));
        assertEquals(0, instance.getInt(Collections.<String>emptyList()));
        assertEquals(0, instance.getLong(new HashMap<String, String>()));
        assertFalse(instance.isBoolean());
        assertNull(instance.getName("Kavai"));
        assertNull(instance.getValue("Test"));
    }

    @Test public void testExecuteOnEmptyInterface() throws Exception
    {
        generateDefault.classNamePostfix = "OtherPostFix";
        generateDefault.targetPackage = "";
        generateDefault.interfaces = new String[] { TestEmptyInterface.class.getName() };

        generateDefault.execute();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        File interfaceFile = new File("./src/test/java/testinterface/TestEmptyInterface.java");

        assertTrue(interfaceFile.exists());
        File generatedImplFile = new File(generateDefault.outputDirectory, "TestEmptyInterfaceOtherPostFix.java");

        assertTrue(generatedImplFile.exists());
        int compilationResult = compiler.run(null, null, null, interfaceFile.getAbsolutePath(), generatedImplFile.getAbsolutePath());

        assertEquals(0, compilationResult);
    }
}

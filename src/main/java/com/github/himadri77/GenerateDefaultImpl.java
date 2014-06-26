/*
* Copyright 2001-2005 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.github.himadri77;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.lang.reflect.Method;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Mojo(
    name = "generate-default-impl",
    defaultPhase = LifecyclePhase.GENERATE_SOURCES
)
public class GenerateDefaultImpl extends AbstractMojo
{
    public static final Map<Class<?>, String> PRIMITIVE_RETURN_VALUES = getPrimitiveReturnValues();
    @Parameter(
        defaultValue = "DefaultImpl",
        property = "classNamePostfix"
    )
    String classNamePostfix;
    @Parameter(
        required = true,
        property = "interfaces"
    )
    String[] interfaces;
    @Parameter(
        defaultValue = "${project.build.directory}/generated-sources/java",
        property = "outputDirectory"
    )
    File outputDirectory;
    @Parameter(
        defaultValue = "",
        property = "targetPackage"
    )
    String targetPackage;

    public void execute() throws MojoExecutionException
    {
        File javaSrcPackage = new File(outputDirectory, targetPackage.replace('.', '/'));

        javaSrcPackage.mkdirs();
        getLog().info("Generating default implementations into " + outputDirectory.getAbsolutePath());
        getLog().debug("Generating default implementations for interfaces: " + Arrays.toString(interfaces));
        for (String interfaceName : interfaces)
        {
            FileWriter fileWriter = null;

            try
            {
                Class<?> clazz = Class.forName(interfaceName);
                File file = new File(javaSrcPackage, clazz.getSimpleName() + classNamePostfix + ".java");

                fileWriter = new FileWriter(file);

                generateImpl(clazz, new PrintWriter(fileWriter));
                fileWriter.close();
                getLog().debug("Generated default impl: " + file.getAbsolutePath());
            }
            catch (IOException e)
            {
                throw new MojoExecutionException("Cannot generate file for interface " + interfaceName, e);
            }
            catch (ClassNotFoundException e)
            {
                throw new MojoExecutionException("Cannot load interface " + interfaceName, e);
            }
            finally
            {
                if (fileWriter != null)
                {
                    try
                    {
                        fileWriter.close();
                    }
                    catch (IOException e)
                    {
                        // nothing to do
                    }
                }
            }
        }
    }

    private static Map<Class<?>, String> getPrimitiveReturnValues()
    {
        Map<Class<?>, String> ret = new HashMap<Class<?>, String>();

        ret.put(Boolean.TYPE, "return false;");
        ret.put(Character.TYPE, "return 0;");
        ret.put(Byte.TYPE, "return 0;");
        ret.put(Short.TYPE, "return 0;");
        ret.put(Integer.TYPE, "return 0;");
        ret.put(Long.TYPE, "return 0;");
        ret.put(Float.TYPE, "return 0;");
        ret.put(Double.TYPE, "return 0;");
        ret.put(Void.TYPE, "");

        return ret;
    }

    private void generateImpl(Class<?> clazz, PrintWriter printWriter) throws MojoExecutionException
    {
        if (!clazz.isInterface())
        {
            throw new MojoExecutionException("The specified class is not an interface: " + clazz.getName());
        }

        if (targetPackage.length() > 0)
        {
            printWriter.printf("package %s;%n", targetPackage);
        }

        printWriter.printf("public class %s%s implements %s {%n", clazz.getSimpleName(), classNamePostfix, clazz.getCanonicalName());
        for (Method method : clazz.getMethods())
        {
            printWriter.printf("     @Override public %s %s(", method.getReturnType().getCanonicalName(), method.getName());
            for (int i = 0; i < method.getParameterTypes().length; i++)
            {
                printWriter.printf("%s p%d", method.getParameterTypes()[i].getCanonicalName(), i + 1);
                if (i < (method.getParameterTypes().length - 1))
                {
                    printWriter.print(", ");
                }
            }

            printWriter.print(")");
            for (int i = 0; i < method.getExceptionTypes().length; i++)
            {
                if (i == 0)
                {
                    printWriter.print(" throws ");
                }

                printWriter.printf(method.getExceptionTypes()[i].getCanonicalName());
                if (i < (method.getExceptionTypes().length - 1))
                {
                    printWriter.print(", ");
                }
            }

            printWriter.print(" {");
            if (PRIMITIVE_RETURN_VALUES.containsKey(method.getReturnType()))
            {
                printWriter.print(PRIMITIVE_RETURN_VALUES.get(method.getReturnType()));
            }
            else
            {
                printWriter.print("return null;");
            }

            printWriter.println("}");
        }

        printWriter.println("}");
    }
}

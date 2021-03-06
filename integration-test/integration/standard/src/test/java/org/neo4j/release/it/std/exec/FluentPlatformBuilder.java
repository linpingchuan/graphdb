/**
 * Copyright (c) 2002-2011 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.release.it.std.exec;

import org.ops4j.pax.exam.options.ProvisionOption;
import org.ops4j.pax.runner.platform.*;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 *
 */
public class FluentPlatformBuilder implements PlatformBuilder
{

    static
    {
        System.setProperty( "java.protocol.handler.pkgs", "org.ops4j.pax.url" );
    }

    private String[] jvmOpts;
    private String mainClassName;
    private String[] arguments;
    private String m_version = "0.1";
    private ProvisionOption[] platformProvisions;
    private Properties m_configuration = new Properties();
    private Set<String> classpaths = new HashSet<String>();

    public void prepare( PlatformContext context ) throws PlatformException
    {
        context.getWorkingDirectory();
    }

    public String getMainClassName()
    {
        return mainClassName;
    }


    public void setMainClassName( String mainClassName )
    {
        this.mainClassName = mainClassName;
    }

    public String[] getArguments( PlatformContext context )
    {
        return arguments;
    }

    public void setArguments( String[] arguments )
    {
        this.arguments = arguments;
    }

    public String[] getVMOptions( PlatformContext context )
    {
        return jvmOpts;
    }

    private void setVMOptions( String[] jvmOpts )
    {
        this.jvmOpts = jvmOpts;
    }


    public InputStream getDefinition( Configuration configuration ) throws IOException
    {
        StringBuilder definitionBuilder = new StringBuilder()
                .append( "<platform>" )
                .append( " <name>POJA - " ).append( m_version ).append( "</name>" )
                .append( " <system>mvn:org.neo4j.release.integration/poja/0.1-SNAPSHOT</system>" )

                .append( " <packages>" )
                .append( " </packages>" )

                .append( " <profile name=\"minimal\" default=\"true\"/>" )

                .append( "</platform>" );

        return new ByteArrayInputStream( definitionBuilder.toString().getBytes( "UTF-8" ) );
    }

    public String getRequiredProfile( PlatformContext context )
    {
        return null;
    }

    public String getProviderName()
    {
        return "fluent";
    }

    public String getProviderVersion()
    {
        return m_version;
    }

    public List<SystemFileReference> getSystemFileReferences()
    {
        if ( platformProvisions != null )
        {
            List<SystemFileReference> systemReferences = new ArrayList<SystemFileReference>();
            for ( ProvisionOption provision : platformProvisions )
            {
                try
                {
                    SystemFileReferenceBean systemBean = new SystemFileReferenceBean( new URL( provision.getURL() ) );
                    systemReferences.add( systemBean );
                } catch ( MalformedURLException e )
                {
                    System.err.println( "Skipping provision with bad URL: " + provision.getURL() );
                }
            }
            return systemReferences;
        }
        else
        {
            return Collections.emptyList();
        }
    }

    public Dictionary getConfiguration()
    {
        StringBuilder assembledClasspath = new StringBuilder();
        for ( String classpath : classpaths )
        {
            assembledClasspath.append( classpath );
            assembledClasspath.append( File.pathSeparator );
        }
        if ( assembledClasspath.length() > 0 )
        {
            assembledClasspath.deleteCharAt( assembledClasspath.length() - 1 );
        }
        m_configuration.put( ServiceConstants.CONFIG_CLASSPATH, assembledClasspath.toString() );
        return this.m_configuration;
    }

    private void addClasspath( String classpath )
    {
        classpaths.add( classpath );
    }


    private void setProvisions( ProvisionOption[] provisions )
    {
        this.platformProvisions = provisions;
    }

    public static interface BuilderModifier
    {
        FluentPlatformBuilder modify( FluentPlatformBuilder builder );
    }

    public static ObservablePlatform buildPlatform( BuilderModifier... modifiers )
    {
        FluentPlatformBuilder builder = new FluentPlatformBuilder();
        for ( BuilderModifier modifier : modifiers )
        {
            modifier.modify( builder );
        }
        return new ObservablePlatform( builder );
    }

    static class MainClassModifier implements BuilderModifier
    {

        String mainClassName;
        String[] mainArgs;

        public MainClassModifier( String nameOfMainClass, String[] args )
        {
            this.mainClassName = nameOfMainClass;
            this.mainArgs = args;
        }

        public FluentPlatformBuilder modify( FluentPlatformBuilder builder )
        {
            builder.setMainClassName( mainClassName );
            builder.setArguments( mainArgs );
            return builder;
        }
    }

    public static MainClassModifier forMainClass( String nameOfMainClass, String... args )
    {
        return new MainClassModifier( nameOfMainClass, args );
    }

    public static String named( String name )
    {
        return name;
    }

    public static String[] usingArgs( String... args )
    {
        return args;
    }

    public static class JvmOptsModifier implements BuilderModifier
    {
        private String[] jvmOpts;

        public JvmOptsModifier( String[] jvmOpts )
        {
            this.jvmOpts = jvmOpts;
        }

        public FluentPlatformBuilder modify( FluentPlatformBuilder builder )
        {
            builder.setVMOptions( jvmOpts );
            return builder;
        }
    }

    public static JvmOptsModifier configuringJvmWith( String... jvmOpts )
    {
        return new JvmOptsModifier( jvmOpts );
    }

    public static ClasspathModifier includingTargetTestClasses()
    {
        String currentClasspath = System.getProperty( "java.class.path" );
        String[] currentParts = currentClasspath.split( ":" );
        String targetClassesDir = ".";
        for ( String part : currentParts )
        {
            if ( part.contains( "target" + File.separator + "test-classes" ) )
            {
                targetClassesDir = part;
                break;
            }
        }
        return new ClasspathModifier( targetClassesDir );
    }

    public static ClasspathModifier includingTargetClasses()
    {
        String currentClasspath = System.getProperty( "java.class.path" );
        String[] currentParts = currentClasspath.split( ":" );
        String targetClassesDir = ".";
        for ( String part : currentParts )
        {
            if ( part.contains( "target" + File.separator + "classes" ) )
            {
                targetClassesDir = part;
                break;
            }
        }
        return new ClasspathModifier( targetClassesDir );
    }

    public static class ClasspathModifier implements BuilderModifier
    {
        private String classpath;

        public ClasspathModifier( String classpath )
        {
            this.classpath = classpath;
        }

        public FluentPlatformBuilder modify( FluentPlatformBuilder builder )
        {
            builder.addClasspath( classpath );

            return builder;
        }
    }

    public static ProvisionModifier provisioning( ProvisionOption... provisions )
    {
        return new ProvisionModifier( provisions );
    }

    public static class ProvisionModifier implements BuilderModifier
    {
        private ProvisionOption[] provisions;

        public ProvisionModifier( ProvisionOption[] provisions )
        {
            this.provisions = provisions;
        }

        public FluentPlatformBuilder modify( FluentPlatformBuilder builder )
        {
            builder.setProvisions( provisions );
            return builder;
        }
    }

}

package fr.pilato.maven.elasticsearch;

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

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.classworlds.ClassRealm;
import org.codehaus.classworlds.ClassWorld;
import org.codehaus.classworlds.DuplicateRealmException;
import org.elasticsearch.osem.annotations.Searchable;
import org.elasticsearch.osem.annotations.impl.AttributeSourceImpl;
import org.elasticsearch.osem.core.ObjectContextMapper;
import org.elasticsearch.osem.core.impl.ObjectContextMapperImpl;
import org.elasticsearch.osem.property.impl.PropertySignatureSourceImpl;

/**
 * Goal generation JSON mapping files from java class
 * 
 * @goal create
 * @phase package
 */
public class ElasticSearchMojo extends AbstractMojo {
	private ObjectContextMapper mapper;

	/**
	 * Indicates all classes that you need to create mapping
	 * @parameter
	 */
	private String[] classes;

    /**
     * Output Directory for generating mapping files
     * @parameter default-value="${project.build.directory}/elasticsearch"
     */
    private String outputDirectory;

    /**
     * Maven project
     * @parameter expression="${project}"
     * @readonly
     * @required
     */
    private MavenProject project;
    
    /**
     * Dépendances du projet Maven qui utilise le plugin
     * @parameter expression="${project.compileArtifacts}"
     */
    private List<Artifact> projectArtifacts;
    
    /**
     * Dépendances du plugin lui-même
     * @parameter expression="${pluginArtifacts}"
     */
    private List<Artifact> pluginArtifacts;
    
    /**
     * Input Directory for classes files (.class)
     * @parameter default-value="${project.build.outputDirectory}"
     */
    private String inputDirectory;

    protected ClassLoader createClassLoader()
    	throws MalformedURLException, DuplicateRealmException {
    	ClassWorld world = new ClassWorld();
    	ClassRealm realm = world.newRealm( "elasticsearch-plugin" );
    	
    	// Ajout de tous les éléments du classpath "compile" du projet
		getLog().debug( "Adding project compile dependencies to classpath :");
    	for (Artifact artifact : projectArtifacts ) {
    		getLog().debug( "  - " + artifact.toString());
    		realm.addConstituent( artifact.getFile().toURL() );
    	}

    	// Ajout des dépendances du plugin
		getLog().debug( "Adding plugin dependencies to classpath :");
    	for (Artifact artifact : pluginArtifacts ) {
    		getLog().debug( "  - " + artifact.toString());
    		realm.addConstituent( artifact.getFile().toURL() );
    	}

    	// Ajout des répertoires de compilation
		getLog().debug( "Adding build path project to classpath :");
		realm.addConstituent( new File(inputDirectory).toURL() );
		getLog().debug( "  - " + inputDirectory);
    	return realm.getClassLoader();
    }
    
    @Override
	public void execute() throws MojoExecutionException, MojoFailureException {
        File JSONFile = null;

		getLog().info("Starting ElasticSearch Plugin " + outputDirectory);
		if (classes == null || classes.length == 0) {
			String message = "You did not set any classes. You should set it up like this :" + "\n" +
			"<configuration>" + "\n" +
			"  <classes>" + "\n" +
			"    <classe>mypackage.MyClass</classe>" + "\n" +
			"  </classes>" + "\n" +
			"</configuration>";
			getLog().warn(message);
		} else {
			// Creating target dir if needed 
			boolean success = (new File(outputDirectory)).mkdir();
			if (success) {
				getLog().debug("Output directory " + outputDirectory + " created");
			}

			try {
				// Creating mapper for OSEM
		        mapper = new ObjectContextMapperImpl( 
		        		new AttributeSourceImpl(), 
		        		new PropertySignatureSourceImpl());

				// Getting the right classloader
				ClassLoader cl = createClassLoader();
				
//		        String _classname = "fr.pilato.maven.elasticsearch.pojo.Tweet";
//		        
//		        // cl.loadClass(_classname);
//				Class<?> __class = Class.forName(_classname);
//		        
////				Class<?> __class = Class.forName(_classname, false, cl);
//		        mapper.add(__class);
//				String _result = new String(mapper.getMapping(__class).copiedBytes());
//				getLog().warn(_result);
				
				// Step 1 : setup the OSEM context by loading all needed classes
				for (String classname : classes) {
					getLog().debug("Adding " + classname + " to OSEM context");
//					Class<?> _class = Class.forName(classname);
					Class<?> _class = Class.forName(classname, true, cl);
					
					Annotation annos[] = _class.getAnnotations();
					Annotation anno = _class.getAnnotation(Searchable.class);
					
					for (int i = 0; i < annos.length; i++) {
						Class<?> __class = annos[i].annotationType();
						if (__class.equals(Searchable.class)) {
							getLog().debug("   ***c** BINGO ********");
						}
					}
					
					getLog().debug("DONE");
			        mapper.add(_class);
				}
			
				// Step 2 : call OSEM mapping generation for all classes
				for (String classname : classes) {
					String mappingFileName = classname + ".json";
					getLog().debug("Generating mapping : " + mappingFileName);
//					Class<?> _class = Class.forName(classname);
					Class<?> _class = Class.forName(classname, true, cl);
					
					String result = new String(mapper.getMapping(_class).copiedBytes());
					
					getLog().info(result);
				}
			} catch (ClassNotFoundException e) {
				getLog().error("Can not generate mapping - ClassNotFoundException : " + e.getMessage());
			} catch (MalformedURLException e) {
				getLog().error("Can not generate mapping - MalformedURLException : " + e.getMessage());
			} catch (DuplicateRealmException e) {
				getLog().error("Can not generate mapping - DuplicateRealmException : " + e.getMessage());
			} catch (IOException e) {
				getLog().error("Can not generate mapping - IOException : " + e.getMessage());
			} finally {

			}
		}
	}

}
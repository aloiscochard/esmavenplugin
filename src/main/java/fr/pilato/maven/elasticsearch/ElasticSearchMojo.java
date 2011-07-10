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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.elasticsearch.osem.core.ObjectContext;
import org.elasticsearch.osem.core.ObjectContextFactory;
import org.elasticsearch.osem.core.ObjectContextMapper;
import org.elasticsearch.osem.core.ObjectContextMappingException;

/**
 * Goal generation JSON mapping files from java class
 * 
 * @goal create
 * @phase package
 * @requiresDependencyResolution compile
 */
public class ElasticSearchMojo extends AbstractMojo {

	private ObjectContext context;
	private ObjectContextMapper mapper;
	// private AttributeSource as ;
	// private PropertySignatureSource ass;

	/**
	 * Indicates all classes that you need to create mapping
	 * @parameter
	 */
	private String[] classes;

    /**
     * The maven project
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;
    
    /**
     * Output Directory for generating mapping files
     * @parameter default-value="${project.build.directory}/elasticsearch"
     */
    private String outputDirectory;

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

			for (String classname : classes) {
				try {
					getLog().debug("Generating mapping for " + classname);
					String mappingFileName = classname + ".json";
					
					getLog().info(project.toString());
					//project.ge
					Class<?> _class = Class.forName(classname);
					
					getLog().info("" + mappingFileName);
//					getLog().info("" + mappingFileName + " content : " + translate(_class));
					
//					JSONFile = new File("./ES_JsonMapping/" + "es/" + classname
//							+ ".json");
//					JSONFile.createNewFile();
//					JSONFile.setWritable(true);
//					
//					FileWriter fw = new FileWriter("./ES/Mapping/" + classname
//							+ ".json");
//					fw.write(translate(Class.forName(classname)));
//					fw.close();

//				} catch (IOException e) {
//					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					getLog().error("Can not generate mapping - ClassNotFoundException : " + e.getMessage());
				} finally {

				}
			}
			
		}
	}

	public String translate(Class<?> c) {

		context = ObjectContextFactory.create();
		mapper = context.add(c);
		mapper.add(c);

		// public ObjectContextMapperImpl(AttributeSource attributes,
		// PropertySignatureSource signatures) {
		// mapper = new ObjectContextMapperImpl(as.getProperties(c),
		// ass.get(c));

		String JSONformat = "";

		try {
			// mapper = new ObjectContextMapperImpl(new AttributeSourceImpl(),
			// new PropertySignatureSourceImpl());
			// mapper.add(c.getClass());

			// mapper = new ObjectContextMapperImpl(new AttributeSourceImpl(),
			// new PropertySignatureSourceImpl());
			// JSONformat = new
			// String(mapper.getMapping(c.getClass()).copiedBytes());

			JSONformat = new String(mapper.getMapping(c).copiedBytes());
			// mapper.add(Dossier.class);
			// System.out.println("Classe dossier mappee ? : " +
			// mapper.isRegistred(Dossier.class));
			// System.out.println("chaine JSONString : " + JSONString);
			// JSONString = new
			// String(mapper.getMapping(Dossier.class).copiedBytes());
		} catch (ObjectContextMappingException e) {
			System.err.println("Exception : " + e.getMessage());
		} catch (IOException e) {
			System.err.println("Exception : " + e.getMessage());
		} finally {

		}
		return JSONformat;
	}

}
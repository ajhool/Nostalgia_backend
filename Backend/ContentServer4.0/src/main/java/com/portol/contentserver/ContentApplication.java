package com.portol.contentserver;

import java.math.BigInteger;
import java.util.EnumSet;
import java.util.logging.Level;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration.Dynamic;
import javax.ws.rs.client.Client;
import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.Mongo;
import com.portol.contentserver.config.ContentServConfig;
import com.portol.contentserver.healthchecks.MongoHealthCheck;
import com.portol.contentserver.manager.MongoManaged;
import com.portol.contentserver.repository.CategoryRepository;
import com.portol.contentserver.repository.ContentRepository;
import com.portol.contentserver.repository.MetadataRepository;
import com.portol.contentserver.resource.CategoryResource;
import com.portol.contentserver.resource.ContentResource;
import com.portol.contentserver.service.MasterAddressGetter;

import io.dropwizard.Application;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.jersey.jackson.JacksonMessageBodyProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class ContentApplication extends Application<ContentServConfig> {

	public static final String NAME = "Content_Server";
	final static Logger logger = LoggerFactory
			.getLogger(ContentApplication.class);

	public static void main(String[] args) throws Exception {
		new ContentApplication().run(args);
	}

	@Override
	public String getName() {
		return NAME;
	}

	private void configureCors(Environment environment) {
		Dynamic filter = environment.servlets().addFilter("CORS",
				CrossOriginFilter.class);
		filter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class),
				true, "/*");
		filter.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM,
				"GET,PUT,POST,DELETE,OPTIONS");
		filter.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_METHODS_HEADER, "GET,PUT,POST,DELETE,OPTIONS");
		filter.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
		filter.setInitParameter(
				CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
		filter.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_HEADERS_HEADER,
				"Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin,Cookies");
		filter.setInitParameter("allowCredentials", "true");
		filter.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true");
	}
	
	
	@Override
	public void initialize(Bootstrap<ContentServConfig> bootstrap) {
	}
	

	
	public static interface MixIn {
        @JsonIgnore
        public void setYear(BigInteger year);
    }

	
	
	@Override
	public void run(ContentServConfig config, Environment environment)
			throws Exception {
		configureCors( environment);
		
		
		environment.jersey().register(MultiPartFeature.class);
		ObjectMapper mapper = new ObjectMapper();
		mapper.addMixInAnnotations(XMLGregorianCalendar.class, MixIn.class);
		environment.jersey().register(new JacksonMessageBodyProvider(mapper, environment.getValidator()));
		
		// init content DB
		MongoManaged contentMongoManaged = new MongoManaged(config.mongoContent);
		environment.lifecycle().manage(contentMongoManaged);
		environment.healthChecks().register("MongoContentDBHealthCheck",
				new MongoHealthCheck(contentMongoManaged));
		
		// init splash screen contents DB
		MongoManaged splashMongoManaged = new MongoManaged(config.mongoSplash);
		environment.lifecycle().manage(splashMongoManaged);
		environment.healthChecks().register("MongoSplashDBHealthCheck",
				new MongoHealthCheck(splashMongoManaged));
		
		
		
		MongoManaged catMongoManaged = this.addCatMongo(config, environment);
		
		MetadataRepository metaRepo = new MetadataRepository(splashMongoManaged);
		CategoryRepository catRepo = new CategoryRepository(catMongoManaged);
		ContentRepository contentRepo = new ContentRepository(contentMongoManaged);
		
		environment.jersey().register(
				new ContentResource(contentRepo, metaRepo, config.getDataMongoConfiguration()));
		
		environment.jersey().register(new CategoryResource(metaRepo, catRepo));
		

	}

	public MongoManaged addCatMongo(ContentServConfig config, Environment environment) throws Exception{
			// init category DB
			MongoManaged catMongoManaged = new MongoManaged(config.getCategoryMongoConfiguration());
			environment.lifecycle().manage(catMongoManaged);
			environment.healthChecks().register("MongoCatDBHealthCheck",
					new MongoHealthCheck(catMongoManaged));
	
			logger.info("Category Database Initialized");
			return catMongoManaged;
		}



}

/*
 * Copyright 2016-2017 the original author or authors.
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
package org.lib.apinative;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.WebApplicationType;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

/**
 * @author Dave Syer
 *
 */
public class FunctionalSpringApplication extends org.springframework.boot.SpringApplication {

	/**
	 * Name of default property source.
	 */
	private static final String DEFAULT_PROPERTIES = "defaultProperties";

	/**
	 * Flag to say that context is functional beans.
	 */
	public static final String SPRING_FUNCTIONAL_ENABLED = "spring.functional.enabled";

	/**
	 * Enumeration of web application types.
	 */
	public static final String SPRING_WEB_APPLICATION_TYPE = "spring.main.web-application-type";

	public static void main(String[] args) throws Exception {
		FunctionalSpringApplication.run(new Class<?>[0], args);
	}

	public static ConfigurableApplicationContext run(Class<?> primarySource, String... args) {
		return run(new Class<?>[] { primarySource }, args);
	}

	public static ConfigurableApplicationContext run(Class<?>[] primarySources, String[] args) {
		ConfigurableApplicationContext context = new FunctionalSpringApplication(primarySources).run(args);
		return context;
	}

	public FunctionalSpringApplication(Class<?>... primarySources) {
		super(primarySources);
		setApplicationContextClass(GenericApplicationContext.class);
		setWebApplicationType(WebApplicationType.REACTIVE);
	}

	@Override
	protected void postProcessApplicationContext(ConfigurableApplicationContext context) {
		super.postProcessApplicationContext(context);
		defaultProperties(context);
	}

	@Override
	protected void load(ApplicationContext context, Object[] sources) {
		if (!context.getEnvironment().getProperty(SPRING_FUNCTIONAL_ENABLED, Boolean.class, false)) {
			super.load(context, sources);
		}
	}

	private void defaultProperties(ConfigurableApplicationContext context) {
		MutablePropertySources sources = context.getEnvironment().getPropertySources();
		if (!sources.contains(DEFAULT_PROPERTIES)) {
			sources.addLast(new MapPropertySource(DEFAULT_PROPERTIES, Collections.emptyMap()));
		}
		@SuppressWarnings("unchecked")
		Map<String, Object> source = (Map<String, Object>) sources.get(DEFAULT_PROPERTIES).getSource();
		Map<String, Object> map = new HashMap<>(source);
		map.put(SPRING_FUNCTIONAL_ENABLED, "true");
		map.put(SPRING_WEB_APPLICATION_TYPE, getWebApplicationType());
		sources.replace(DEFAULT_PROPERTIES, new MapPropertySource(DEFAULT_PROPERTIES, map));
	}

}

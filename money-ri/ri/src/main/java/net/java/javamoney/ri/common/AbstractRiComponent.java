/*
 *  Copyright (c) 2012, 2013, Credit Suisse (Anatole Tresch), Werner Keil.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * Contributors:
 *    Anatole Tresch - initial implementation
 *    Wernner Keil - extensions and adaptions.
 */
package net.java.javamoney.ri.common;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.ServiceLoader;

import javax.money.provider.Monetary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a shared implementation that loads a set of provider
 * instances using the {@link ServiceLoader}.
 * 
 * @author Anatole Tresch
 * @author Werner Keil
 */
public abstract class AbstractRiComponent {
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	/**
	 * Singleton constructor.
	 */
	protected <T> List<T> getSPIProviders(Class<T> providerClass, Class<? extends Annotation>... annotations) {
		return Monetary.getLoader().getInstances(providerClass, annotations);
	}

}

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
package net.java.javamoney.ri.ext.provider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import javax.inject.Singleton;
import javax.money.CurrencyUnit;
import javax.money.ext.Region;
import javax.money.ext.RegionalCurrencyUnitProvider;
import javax.money.ext.spi.RegionalCurrencyUnitProviderSpi;

import net.java.javamoney.ri.common.AbstractRiComponent;


/**
 * This class models the singleton defined by JSR 354 that provides accessors
 * for {@link CurrencyUnit}.
 * 
 * @author Anatole Tresch
 */
@Singleton
public final class RegionalCurrencyUnitProviderImpl extends AbstractRiComponent
		implements RegionalCurrencyUnitProvider {

	/** Loaded region providers. */
	private List<RegionalCurrencyUnitProviderSpi> regionalCurrencyProviders = new ArrayList<RegionalCurrencyUnitProviderSpi>();

	public RegionalCurrencyUnitProviderImpl() {
		reload();
	}
	
	/**
	 * This method defined that this implementation is exposed as
	 * {@link RegionalCurrencyUnitProvider}.
	 * 
	 * @return {@link RegionalCurrencyUnitProvider}.class
	 */
	public Class<RegionalCurrencyUnitProvider> getExposedType() {
		return RegionalCurrencyUnitProvider.class;
	}

	/**
	 * This method reloads the providers available from the
	 * {@link ServiceLoader}. This adds providers that were not yet visible
	 * before.
	 */
	@SuppressWarnings("unchecked")
	public void reload() {
		List<RegionalCurrencyUnitProviderSpi> loadedList = getSPIProviders(RegionalCurrencyUnitProviderSpi.class);
		for (RegionalCurrencyUnitProviderSpi provSPI : loadedList) {
			if (!regionalCurrencyProviders.contains(provSPI)) {
				this.regionalCurrencyProviders.add(provSPI);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.money.CurrencyUnitProvider#get(java.lang.String,
	 * java.lang.String, long)
	 */
	public CurrencyUnit[] getAll(Region region) {
		return getAll(region, -1);
	}

	public CurrencyUnit[] getAll(Region region, long timestamp) {
		Set<CurrencyUnit> result = new HashSet<CurrencyUnit>();
		for (RegionalCurrencyUnitProviderSpi prov : regionalCurrencyProviders) {
			CurrencyUnit[] currencies = prov.getAll(region);
			if (currencies != null && currencies.length > 0) {
				result.addAll(Arrays.asList(currencies));
			}
		}
		return result.toArray(new CurrencyUnit[result.size()]);
	}

	@Override
	public boolean isLegalTender(CurrencyUnit currency, Region region) {
		return isLegalTender(currency, region, null);
	}
	
	@Override
	public boolean isLegalTender(CurrencyUnit currency, Region region,
			Long timestamp) {
		CurrencyUnit[] tenders = getLegalTenders(region, timestamp);
		for (int i = 0; i < tenders.length; i++) {
			if(!tenders[i].getNamespace().equals(currency.getNamespace())){
				continue;
			}
			if(!tenders[i].getCurrencyCode().equals(currency.getCurrencyCode())){
				continue;
			}
			return true;
		}
		return false;
	}

	@Override
	public CurrencyUnit[] getLegalTenders(Region region) {
		return getLegalTenders(region, null);
	}

	@Override
	public CurrencyUnit[] getLegalTenders(Region region, Long timestamp) {
		Set<CurrencyUnit> result = new HashSet<CurrencyUnit>();
		for (RegionalCurrencyUnitProviderSpi prov : regionalCurrencyProviders) {
			CurrencyUnit[] currencies = prov.getLegalTenders(region, timestamp);
			if (currencies != null && currencies.length > 0) {
				result.addAll(Arrays.asList(currencies));
			}
		}
		return result.toArray(new CurrencyUnit[result.size()]);
	}

	
}

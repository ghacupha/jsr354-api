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
package javax.money;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.Test;

public class IntegralMoneyTest {

	private static final BigDecimal TEN = new BigDecimal(10.0d);
	protected static final CurrencyUnit EURO = MoneyCurrency.of("EUR");
	protected static final CurrencyUnit DOLLAR = MoneyCurrency
			.of("USD");

	@Test
	public void testGetInstanceCurrencyBigDecimal() {
		IntegralMoney m = IntegralMoney.of(MoneyCurrency.of("EUR"), TEN);
		assertEquals(TEN.setScale(4, RoundingMode.HALF_EVEN), m.asType(BigDecimal.class).setScale(4, RoundingMode.HALF_EVEN));
		assertEquals(Long.valueOf(10L), m.asType(Long.class));
	}

	@Test
	public void testGetInstanceCurrencyDouble() {
		IntegralMoney m = IntegralMoney.of(MoneyCurrency.of("EUR"), 10.0d);
		assertTrue(TEN.doubleValue() == m.doubleValue());
	}

	@Test
	public void testGetCurrency() {
		MonetaryAmount money = IntegralMoney.of(EURO, BigDecimal.TEN);
		assertNotNull(money.getCurrency());
		assertEquals("EUR", money.getCurrency().getCurrencyCode());
	}

	@Test
	public void testAddNumber() {
		IntegralMoney money1 = IntegralMoney.of(EURO, BigDecimal.TEN);
		IntegralMoney money2 = IntegralMoney.of(EURO, BigDecimal.ONE);
		IntegralMoney moneyResult = money1.add(money2);
		assertNotNull(moneyResult);
		assertEquals(11d, moneyResult.doubleValue(), 0d);
	}

	@Test
	public void testSubtractMonetaryAmount() {
		IntegralMoney money1 = IntegralMoney.of(EURO, BigDecimal.TEN);
		IntegralMoney money2 = IntegralMoney.of(EURO, BigDecimal.ONE);
		IntegralMoney moneyResult = money1.subtract(money2);
		assertNotNull(moneyResult);
		assertEquals(9d, moneyResult.doubleValue(), 0d);
	}

	@Test
	public void testDivideAndRemainder() {
		IntegralMoney money1 = IntegralMoney.of(EURO, 1000);
		IntegralMoney[] divideAndRemainder = money1.divideAndRemainder(11);
		assertEquals(90L, divideAndRemainder[0].longValue());
		assertEquals(10L, divideAndRemainder[1].longValue());
	}

	@Test
	public void testDivideToIntegralValue() {
		IntegralMoney money1 = IntegralMoney.of(EURO, 1000);
		IntegralMoney result = money1.divideToIntegralValue(5);
		assertEquals(200L, result.longValue());
	}
	
	@Test
	public void testFrom() {
		IntegralMoney m = IntegralMoney.of("CHF",  10.56);
		IntegralMoney m2 = IntegralMoney.from(m);
		assertTrue(m==m2);
		IntegralMoney m3 = IntegralMoney.from(Money.of("CHF", 10.56));
		assertEquals(m.asNumber(), m3.asNumber());
	}
}

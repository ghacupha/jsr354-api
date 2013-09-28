/*
 * Copyright (c) 2012, 2013, Credit Suisse (Anatole Tresch), Werner Keil.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package javax.money;

import static javax.money.Money.Checker.checkAmountParameter;
import static javax.money.Money.Checker.checkNumber;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Platform RI: Default immutable implementation of {@link MonetaryAmount} based on
 * {@link BigDecimal} for the numeric representation.
 * <p>
 * As required by {@link MonetaryAmount} this class is final, thread-safe,
 * immutable and serializable.
 * 
 * @version 0.6
 * @author Anatole Tresch
 * @author Werner Keil
 */
public final class Money implements MonetaryAmount, Comparable<MonetaryAmount>,
		Serializable {

	private static final MathContext DEFAULT_MATH_CONTEXT = initDefaultMathContext();

	/** The numeric part of this amount. */
	private BigDecimal number;

	/** The currency of this amount. */
	private CurrencyUnit currency;

	/** tHE DEFAULT {@link MathContext} used by this instance, e.g. on division. */
	private MathContext mathContext;

	/**
	 * Creates a new instance os {@link Money}.
	 * 
	 * @param currency
	 *            the currency, not null.
	 * @param number
	 *            the amount, not null.
	 */
	private Money(CurrencyUnit currency, Number number) {
		this(currency, number, getDefaultMathContext());
	}

	private static MathContext initDefaultMathContext() {
		// TODO Initialize default, e.g. by system properties, or better:
		// classpath properties!
		return MathContext.DECIMAL64;
	}

	/**
	 * Creates a new instance os {@link Money}.
	 * 
	 * @param currency
	 *            the currency, not null.
	 * @param number
	 *            the amount, not null.
	 */
	private Money(CurrencyUnit currency, Number number, MathContext mathContext) {
		if (currency == null) {
			throw new IllegalArgumentException("Currency is required.");
		}
		if (number == null) {
			throw new IllegalArgumentException("Number is required.");
		}
		if (mathContext == null) {
			throw new IllegalArgumentException("MathContext is required.");
		}
		checkNumber(number);
		this.currency = currency;
		this.mathContext = mathContext;
		this.number = getBigDecimal(number, mathContext);
	}

	/**
	 * Access the default {@link MathContext} used by instances of this class,
	 * if not configured on creation.
	 * 
	 * @return the default {@link MathContext}, never null.
	 */
	public static final MathContext getDefaultMathContext() {
		// TODO the accessor to a public static final constant member variable
		// seems of little use. Why not use constant directly?
		return DEFAULT_MATH_CONTEXT;
	}

	// Static Factory Methods
	/**
	 * Translates a {@code BigDecimal} value and a {@code CurrencyUnit} currency
	 * into a {@code Money}.
	 * 
	 * @param number
	 *            numeric value of the {@code Money}.
	 * @param currency
	 *            currency unit of the {@code Money}.
	 * @return a {@code Money} combining the numeric value and currency unit.
	 */
	public static Money of(CurrencyUnit currency, BigDecimal number) {
		return new Money(currency, number, getDefaultMathContext());
	}

	/**
	 * Translates a {@code BigDecimal} value and a {@code CurrencyUnit} currency
	 * into a {@code Money}.
	 * 
	 * @param number
	 *            numeric value of the {@code Money}.
	 * @param currency
	 *            currency unit of the {@code Money}.
	 * @param mathContext
	 *            the {@link MathContext} to be used.
	 * @return a {@code Money} combining the numeric value and currency unit.
	 */
	public static Money of(CurrencyUnit currency, BigDecimal number,
			MathContext mathContext) {
		return new Money(currency, number, mathContext);
	}

	/**
	 * Static factory method for creating a new instance of {@link Money}.
	 * 
	 * @param currency
	 *            The target currency, not null.
	 * @param number
	 *            The numeric part, not null.
	 * @return A new instance of {@link Money}.
	 */
	public static Money of(CurrencyUnit currency, Number number) {
		// TODO caching
		return new Money(currency, number, getDefaultMathContext());
	}

	/**
	 * Static factory method for creating a new instance of {@link Money}.
	 * 
	 * @param currency
	 *            The target currency, not null.
	 * @param number
	 *            The numeric part, not null.
	 * @return A new instance of {@link Money}.
	 */
	public static Money of(CurrencyUnit currency, Number number,
			MathContext mathContext) {
		// TODO caching
		return new Money(currency, number, mathContext);
	}

	/**
	 * Static factory method for creating a new instance of {@link Money}.
	 * 
	 * @param isoCurrencyCode
	 *            The target currency as ISO currency code.
	 * @param number
	 *            The numeric part, not null.
	 * @return A new instance of {@link Money}.
	 */
	public static Money of(String currencyCode, Number number) {
		return new Money(MoneyCurrency.of(currencyCode), number,
				getDefaultMathContext());
	}

	/**
	 * Static factory method for creating a new instance of {@link Money}.
	 * 
	 * @param isoCurrencyCode
	 *            The target currency as ISO currency code.
	 * @param number
	 *            The numeric part, not null.
	 * @return A new instance of {@link Money}.
	 */
	public static Money of(String currencyCode, Number number,
			MathContext mathContext) {
		return new Money(MoneyCurrency.of(currencyCode), number, mathContext);
	}

/**
	 * Factory method creating a zero instance with the given {@code currency);
	 * @param currency the target currency of the amount being created.
	 * @return
	 */
	public static Money ofZero(CurrencyUnit currency) {
		return new Money(currency, BigDecimal.ZERO, getDefaultMathContext());
	}

/**
	 * Factory method creating a zero instance with the given {@code currency);
	 * @param currency the target currency of the amount being created.
	 * @return
	 */
	public static Money ofZero(String currency) {
		return ofZero(MoneyCurrency.of(currency));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((currency == null) ? 0 : currency.hashCode());
		result = prime * result + ((number == null) ? 0 : number.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Money other = (Money) obj;
		if (currency == null) {
			if (other.currency != null)
				return false;
		} else if (!currency.equals(other.currency))
			return false;
		if (number == null) {
			if (other.number != null)
				return false;
		} else if (!number.equals(other.number))
			return false;
		return true;
	}

	/*
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(MonetaryAmount o) {
		checkAmountParameter(this.currency, o);
		int compare = -1;
		if (this.currency.equals(o.getCurrency())) {
			compare = this.number.compareTo(Money.from(o).number);
		} else {
			compare = this.currency.getCurrencyCode().compareTo(
					o.getCurrency().getCurrencyCode());
		}
		return compare;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.money.MonetaryAmount#getCurrency()
	 */
	public CurrencyUnit getCurrency() {
		return currency;
	}

	/**
	 * Access the {@link MathContext} used by this instance.
	 * 
	 * @return the {@link MathContext} used, never null.
	 */
	public MathContext getMathContext() {
		return this.mathContext;
	}

	/**
	 * Allows to change the {@link MathContext}. The context will used, on
	 * subsequent operation, where feasible and also propagated to child results
	 * of arithmetic calculations.
	 * 
	 * @param mathContext
	 *            The new {@link MathContext}, not null.
	 * @return a new {@link Money} instance, with the new {@link MathContext}.
	 */
	public Money withMathContext(MathContext mathContext) {
		if (mathContext == null) {
			throw new IllegalArgumentException("MathContext required.");
		}
		return new Money(this.currency, this.number, mathContext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.money.MonetaryAmount#abs()
	 */
	public Money abs() {
		if (this.isPositiveOrZero()) {
			return this;
		}
		return this.negate();
	}

	// Arithmetic Operations

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.money.MonetaryAmount#add(javax.money.MonetaryAmount)
	 */
	public Money add(MonetaryAmount amount) {
		checkAmountParameter(this.currency, amount);
		return new Money(this.currency, this.number.add(
				Money.from(amount).number, this.mathContext),
				this.mathContext);
	}



	private BigDecimal getBigDecimal(Number num) {
		if (num instanceof BigDecimal) {
			return (BigDecimal) num;
		}
		if (num instanceof Long || num instanceof Integer) {
			return BigDecimal.valueOf(num.longValue());
		}
		if (num instanceof Float || num instanceof Double) {
			return new BigDecimal(num.toString());
		}
		if (num instanceof Byte || num instanceof AtomicLong) {
			return BigDecimal.valueOf(num.longValue());
		}
		try {
			// Avoid imprecise conversion to double value if at all possible
			return new BigDecimal(num.toString());
		} catch (NumberFormatException e) {
		}
		return BigDecimal.valueOf(num.doubleValue());
	}

	private BigDecimal getBigDecimal(Number number, MathContext mathContext) {
		if (number instanceof BigDecimal) {
			return (BigDecimal) number;
		} else {
			return new BigDecimal(number.doubleValue(), mathContext);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.money.MonetaryAmount#divide(javax.money.MonetaryAmount)
	 */
	public Money divide(MonetaryAmount divisor) {
		checkAmountParameter(this.currency, divisor);
		BigDecimal dec = this.number.divide(Money.from(divisor).number,
				this.mathContext);
		return new Money(this.currency, dec, this.mathContext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.money.MonetaryAmount#divide(javax.money.MonetaryAmount)
	 */
	public Money divide(Number divisor) {
		BigDecimal dec = this.number.divide(getBigDecimal(divisor),
				this.mathContext);
		return new Money(this.currency, dec, this.mathContext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.money.MonetaryAmount#divideAndRemainder(javax.money.MonetaryAmount)
	 */
	public Money[] divideAndRemainder(MonetaryAmount divisor) {
		checkAmountParameter(this.currency, divisor);
		BigDecimal[] dec = this.number.divideAndRemainder(
				Money.from(divisor).number, this.mathContext);
		return new Money[] {
				new Money(this.currency, dec[0], this.mathContext),
				new Money(this.currency, dec[1], this.mathContext) };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.money.MonetaryAmount#divideAndRemainder(javax.money.MonetaryAmount)
	 */
	public Money[] divideAndRemainder(Number divisor) {
		BigDecimal[] dec = this.number.divideAndRemainder(
				getBigDecimal(divisor), this.mathContext);
		return new Money[] {
				new Money(this.currency, dec[0], this.mathContext),
				new Money(this.currency, dec[1], this.mathContext) };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.money.MonetaryAmount#divideToIntegralValue(javax.money.MonetaryAmount
	 * )
	 */
	public Money divideToIntegralValue(MonetaryAmount divisor) {
		checkAmountParameter(this.currency, divisor);
		BigDecimal dec = this.number.divideToIntegralValue(
				Money.from(divisor).number, this.mathContext);
		return new Money(this.currency, dec, this.mathContext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.money.MonetaryAmount#divideToIntegralValue(Number) )D
	 */
	public Money divideToIntegralValue(Number divisor) {
		BigDecimal dec = this.number.divideToIntegralValue(
				getBigDecimal(divisor), this.mathContext);
		return new Money(this.currency, dec, this.mathContext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.money.MonetaryAmount#multiply(javax.money.MonetaryAmount)
	 */
	public Money multiply(MonetaryAmount multiplicand) {
		checkAmountParameter(this.currency, multiplicand);
		BigDecimal dec = this.number.multiply(
				Money.from(multiplicand).number, this.mathContext);
		return new Money(this.currency, dec, this.mathContext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.money.MonetaryAmount#multiply(Number)
	 */
	public Money multiply(Number multiplicand) {
		BigDecimal dec = this.number.multiply(getBigDecimal(multiplicand),
				this.mathContext);
		return new Money(this.currency, dec, this.mathContext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.money.MonetaryAmount#negate()
	 */
	public Money negate() {
		return new Money(this.currency, this.number.negate(this.mathContext),
				this.mathContext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.money.MonetaryAmount#plus()
	 */
	public Money plus() {
		return new Money(this.currency, this.number.plus(this.mathContext),
				this.mathContext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.money.MonetaryAmount#subtract(javax.money.MonetaryAmount)
	 */
	public Money subtract(MonetaryAmount subtrahend) {
		checkAmountParameter(this.currency, subtrahend);
		return new Money(this.currency, this.number.subtract(
				Money.from(subtrahend).number, this.mathContext),
				this.mathContext);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.money.MonetaryAmount#pow(int)
	 */
	public Money pow(int n) {
		return new Money(this.currency, this.number.pow(n, this.mathContext),
				this.mathContext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.money.MonetaryAmount#ulp()
	 */
	public Money ulp() {
		return new Money(this.currency, this.number.ulp());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.money.MonetaryAmount#remainder(javax.money.MonetaryAmount)
	 */
	public Money remainder(MonetaryAmount divisor) {
		checkAmountParameter(this.currency, divisor);
		return new Money(this.currency, this.number.remainder(
				Money.from(divisor).number, this.mathContext),
				this.mathContext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.money.MonetaryAmount#remainder(Number)
	 */
	public Money remainder(Number divisor) {
		return new Money(this.currency, this.number.remainder(
				getBigDecimal(divisor), this.mathContext),
				this.mathContext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.money.MonetaryAmount#scaleByPowerOfTen(int)
	 */
	public Money scaleByPowerOfTen(int n) {
		return new Money(this.currency, this.number.scaleByPowerOfTen(n),
				this.mathContext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.money.MonetaryAmount#isZero()
	 */
	public boolean isZero() {
		return this.number.signum() == 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.money.MonetaryAmount#isPositive()
	 */
	public boolean isPositive() {
		return signum() == 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.money.MonetaryAmount#isPositiveOrZero()
	 */
	public boolean isPositiveOrZero() {
		return signum() >= 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.money.MonetaryAmount#isNegative()
	 */
	public boolean isNegative() {
		return signum() == -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.money.MonetaryAmount#isNegativeOrZero()
	 */
	public boolean isNegativeOrZero() {
		return signum() <= 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.money.MonetaryAmount#with(java.lang.Number)
	 */
	public Money with(Number amount) {
		checkNumber(amount);
		return new Money(this.currency, getBigDecimal(amount), this.mathContext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.money.MonetaryAmount#with(CurrencyUnit, java.lang.Number)
	 */
	public Money with(CurrencyUnit currency, Number amount) {
		checkNumber(amount);
		return new Money(currency, getBigDecimal(amount), this.mathContext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.money.MonetaryAmount#getScale()
	 */
	public int getScale() {
		return this.number.scale();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.money.MonetaryAmount#getPrecision()
	 */
	public int getPrecision() {
		return this.number.precision();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.money.MonetaryAmount#longValue()
	 */
	public long longValue() {
		return this.number.longValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.money.MonetaryAmount#longValueExact()
	 */
	public long longValueExact() {
		return this.number.longValueExact();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.money.MonetaryAmount#doubleValue()
	 */
	public double doubleValue() {
		return this.number.doubleValue();
	}



	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.money.MonetaryAmount#signum()
	 */

	public int signum() {
		return this.number.signum();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.money.MonetaryAmount#toEngineeringString()
	 */
	public String toEngineeringString() {
		return this.currency.getCurrencyCode() + ' '
				+ this.number.toEngineeringString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.money.MonetaryAmount#toPlainString()
	 */
	public String toPlainString() {
		return this.currency.getCurrencyCode() + ' '
				+ this.number.toPlainString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.money.MonetaryAmount#lessThan(javax.money.MonetaryAmount)
	 */
	public boolean isLessThan(MonetaryAmount amount) {
		checkAmountParameter(this.currency, amount);
		return number.compareTo(Money.from(amount).number) < 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.money.MonetaryAmount#lessThanOrEqualTo(javax.money.MonetaryAmount)
	 */
	public boolean isLessThanOrEqualTo(MonetaryAmount amount) {
		checkAmountParameter(this.currency, amount);
		return number.compareTo(Money.from(amount).number) <= 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.money.MonetaryAmount#greaterThan(javax.money.MonetaryAmount)
	 */
	public boolean isGreaterThan(MonetaryAmount amount) {
		checkAmountParameter(this.currency, amount);
		return number.compareTo(Money.from(amount).number) > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.money.MonetaryAmount#greaterThanOrEqualTo(javax.money.MonetaryAmount
	 * ) #see
	 */
	public boolean isGreaterThanOrEqualTo(MonetaryAmount amount) {
		checkAmountParameter(this.currency, amount);
		return number.compareTo(Money.from(amount).number) >= 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.money.MonetaryAmount#isEqualTo(javax.money.MonetaryAmount)
	 */
	public boolean isEqualTo(MonetaryAmount amount) {
		checkAmountParameter(this.currency, amount);
		return number.compareTo(Money.from(amount).number) == 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.money.MonetaryAmount#isNotEqualTo(javax.money.MonetaryAmount)
	 */
	public boolean isNotEqualTo(MonetaryAmount amount) {
		checkAmountParameter(this.currency, amount);
		return number.compareTo(Money.from(amount).number) != 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.money.MonetaryAmount#getNumberType()
	 */
	public Class<?> getNumberType() {
		return BigDecimal.class;
	}

	/*
	 * }(non-Javadoc)
	 * 
	 * @see javax.money.MonetaryAmount#adjust(javax.money.AmountAdjuster)
	 */
	@Override
	public <T> T query(MonetaryQuery<T> query) {
		return query.queryFrom(this);
	}

	/*
	 * @see javax.money.MonetaryAmount#asType(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public <T> T asType(Class<T> type) {
		if (BigDecimal.class.equals(type)) {
			return (T) this.number;
		}
		if (Number.class.equals(type)) {
			final T asType = (T) this.number;
			return asType;
		}
		if (Double.class.equals(type)) {
			return (T) Double.valueOf(this.number.doubleValue());
		}
		if (Float.class.equals(type)) {
			return (T) Float.valueOf(this.number.floatValue());
		}
		if (Long.class.equals(type)) {
			return (T) Long.valueOf(this.number.longValue());
		}
		if (Integer.class.equals(type)) {
			return (T) Integer.valueOf(this.number.intValue());
		}
		if (Short.class.equals(type)) {
			return (T) Short.valueOf(this.number.shortValue());
		}
		if (Byte.class.equals(type)) {
			return (T) Byte.valueOf(this.number.byteValue());
		}
		if (BigInteger.class.equals(type)) {
			return (T) this.number.toBigInteger();
		}
		throw new IllegalArgumentException("Unsupported representation type: "
				+ type);
	}

	
	/**
	 * Gets the number representation of the numeric value of this item.
	 * 
	 * @return The {@link Number} represention matching best.
	 */
	public Number asNumber() {
		return this.number;
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.writeObject(this.number);
		oos.writeObject(this.mathContext);
		oos.writeObject(this.currency);
	}

	private void readObject(ObjectInputStream ois) throws IOException,
			ClassNotFoundException {
		this.number = (BigDecimal) ois.readObject();
		this.mathContext = (MathContext) ois.readObject();
		this.currency = (CurrencyUnit) ois.readObject();
	}

	private void readObjectNoData()
			throws ObjectStreamException {
		if (this.number == null) {
			this.number = BigDecimal.ZERO;
		}
		if (this.mathContext == null) {
			this.mathContext = DEFAULT_MATH_CONTEXT;
		}
		if (this.currency == null) {
			this.currency = MoneyCurrency.of(
					"XXX"); // no currency
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return currency.getCurrencyCode() + ' ' + number;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.money.MonetaryAmount#getAmountWhole()
	 */
	@Override
	public long getAmountWhole() {
		return this.number.setScale(0,
				RoundingMode.DOWN).longValueExact();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.money.MonetaryAmount#getAmountFractionNumerator()
	 */
	@Override
	public long getAmountFractionNumerator() {
		BigDecimal bd = this.number.remainder(BigDecimal.ONE);
		return bd.movePointRight(getScale()).longValueExact();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.money.MonetaryAmount#getAmountFractionDenominator()
	 */
	@Override
	public long getAmountFractionDenominator() {
		return BigDecimal.valueOf(10).pow(getScale()-1).longValueExact();
	}

	@Override
	public Money with(MonetaryAdjuster adjuster) {
		MonetaryAmount amt = adjuster.adjustInto(this);
		return Money.from(amt);
	}

	public static Money from(MonetaryAmount amt) {
		if (amt.getClass() == Money.class) {
			return (Money) amt;
		}
		BigDecimal whole = BigDecimal.valueOf(amt.getAmountWhole());
		BigDecimal fraction = BigDecimal.valueOf(amt
				.getAmountFractionNumerator());
		BigDecimal number = whole.add(fraction);
		fraction = fraction.scaleByPowerOfTen(-fraction.precision());
		return Money.of(amt.getCurrency(), number, getDefaultMathContext());
	}

	/**
	 * Platform RI: This is an inner checker class for aspects of
	 * {@link MonetaryAmount}. It may be used by multiple implementations
	 * (inside the same package) to avoid code duplication.
	 * 
	 * This class is for internal use only.
	 * 
	 * @author Werner Keil
	 */
	static final class Checker {
		private Checker() {
		}

		/**
		 * Internal method to check for correct number parameter.
		 * 
		 * @param number
		 * @throws IllegalArgumentException
		 *             If the number is null
		 */
		static final void checkNumber(Number number) {
			if (number == null) {
				throw new IllegalArgumentException("Number is required.");
			}
		}

		/**
		 * Method to check if a currency is compatible with this amount
		 * instance.
		 * 
		 * @param amount
		 *            The monetary amount to be compared to, never null.
		 * @throws IllegalArgumentException
		 *             If the amount is null, or the amount's currency is not
		 *             compatible (same {@link CurrencyUnit#getNamespace()} and
		 *             same {@link CurrencyUnit#getCurrencyCode()}).
		 */
		static final void checkAmountParameter(CurrencyUnit currency,
				MonetaryAmount amount) {
			if (amount == null) {
				throw new IllegalArgumentException("Amount must not be null.");
			}
			final CurrencyUnit amountCurrency = amount.getCurrency();
			if (!(currency.getCurrencyCode().equals(amountCurrency
					.getCurrencyCode()))) {
				throw new CurrencyMismatchException(currency, amountCurrency);
			}
		}
	}

}

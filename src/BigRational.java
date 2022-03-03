import java.math.BigInteger;




public class BigRational

{

	private BigInteger m_n;
	private BigInteger m_q;
	public final static int DEFAULT_RADIX = 10;

	private final static BigInteger BIG_INTEGER_ZERO = BigInteger.valueOf(0);
	private final static BigInteger BIG_INTEGER_ONE = BigInteger.valueOf(1);
	private final static BigInteger BIG_INTEGER_MINUS_ONE = BigInteger.valueOf(-1);
	private final static BigInteger BIG_INTEGER_TWO = BigInteger.valueOf(2);
	private final static BigInteger BIG_INTEGER_MINUS_TWO = BigInteger.valueOf(-2);
	private final static BigInteger BIG_INTEGER_TEN = BigInteger.valueOf(10);
	private final static BigInteger BIG_INTEGER_SIXTEEN = BigInteger.valueOf(16);

	public BigRational(BigInteger n, BigInteger q)

	{

		if (bigIntegerZerop(q)) {

			throw new NumberFormatException("quotient zero");

		}



		m_n = n;

		m_q = q;



		normalize();

	}



	/** Construct a BigRational from a big number integer,

	* denominator is 1.

	*/

	public BigRational(BigInteger n)

	{

		this(n, BIG_INTEGER_ONE);

	}



	/** Construct a BigRational from long fix number integers

	* representing numerator and denominator.

	*/

	public BigRational(long n, long q)

	{

		this(bigIntegerValueOf(n), bigIntegerValueOf(q));

	}



	/** Construct a BigRational from a long fix number integer.

	*/

	public BigRational(long n)

	{

		this(bigIntegerValueOf(n), BIG_INTEGER_ONE);

	}



	// note: byte/short/int implicitly upgraded to long,

	// so we don't implement BigRational(int,int) et al.



	// note: "[+-]i.fEe" not supported.

	// we wouldn't want to distinguish exponent-'e' from large-base-digit-'e' anyway

	// (at least not at this place).



	/** Construct a BigRational from a string representation,

	* the supported formats are "[+-]d/[+-]q", "[+-]i.f", "[+-]i".

	*/

	public BigRational(String s, int radix)

	{

		if (s == null) {

			throw new NumberFormatException("null");

		}



		int slash = s.indexOf('/');

		int dot = s.indexOf('.');



		if (slash != -1 && dot != -1) {

			throw new NumberFormatException("can't have both slash and dot");

		}



		if (slash != -1) {



			// "[+-]d/[+-]q"

			String d = s.substring(0, slash);

			String q = s.substring(slash + 1);



			// check for multiple signs or embedded signs

			checkNumberFormat(d);



			// skip '+'

			if (d.length() > 0 && d.charAt(0) == '+') {

				d = d.substring(1);

			}



			// handle "/x" as "1/x"

			// note: "1" and "-1" are treated special in newBigInteger().

			if (d.equals("")) {

				d = "1";

			} else if (d.equals("-")) {

				d = "-1";

			}



			// note: here comes some code duplicated from numerator handling.

			// it seems however clearer not to invent a unobvious abstraction

			// to handle these two cases.



			// check for multiple signs or embedded signs

			checkNumberFormat(q);



			// skip '+'

			if (q.length() > 0 && q.charAt(0) == '+') {

				q = q.substring(1);

			}



			// handle "x/" as "x"

			// note: "1" and "-1" are treated special in newBigInteger().

			if (q.equals("")) {

				q = "1";

			} else if (q.equals("-")) {

				q = "-1";

			}



			m_n = newBigInteger(d, radix);

			m_q = newBigInteger(q, radix);



		} else if (dot != -1) {



			// "[+-]i.f"

			String i = s.substring(0, dot);

			String f = s.substring(dot + 1);



			// check for multiple signs or embedded signs

			checkNumberFormat(i);



			// skip '+'

			if (i.length() > 0 && i.charAt(0) == '+') {

				i = i.substring(1);

			}



			// handle '-'

			boolean negt = false;

			if (i.length() > 0 && i.charAt(0) == '-') {

				negt = true;

				i = i.substring(1);

			}



			// handle ".x" as "0.x" ("." as "0.0")

			// note: "0" is treated special in newBigInteger().

			if (i.equals("")) {

				i = "0";

			}



			// check for signs

			checkFractionFormat(f);



			// handle "x." as "x.0" ("." as "0.0")

			// note: "0" is treated special in newBigInteger().

			if (f.equals("")) {

				f = "0";

			}



			BigInteger iValue = newBigInteger(i, radix);

			BigInteger fValue = newBigInteger(f, radix);



			int scale = f.length();

			checkRadix(radix);

			BigInteger fq = bigIntegerPow(bigIntegerValueOf(radix), scale);



			m_n = bigIntegerMultiply(iValue, fq).add(fValue);



			if (negt) {

				m_n = m_n.negate();

			}



			m_q = fq;



		} else {



			// "[+-]i".  [not just delegating to BigInteger.]



			String i = s;



			// check for multiple signs or embedded signs

			checkNumberFormat(i);



			// skip '+'.  BigInteger doesn't handle these.

			if (i.length() > 0 && i.charAt(0) == '+') {

				i = i.substring(1);

			}



			// handle "" as "0"

			// note: "0" is treated special in newBigInteger().

			if (i.equals("") || i.equals("-")) {

				i = "0";

			}



			m_n = newBigInteger(i, radix);

			m_q = BIG_INTEGER_ONE;

		}



		normalize();

	}



	/** Construct a BigRational from a string representation, with default radix,

	* the supported formats are "[+-]d/[+-]q", "[+-]i.f", "[+-]i".

	*/

	public BigRational(String s)

	{

		this(s, DEFAULT_RADIX);

	}



	/** Construct a BigRational from an unscaled value by scaling.

	*/

	public BigRational(BigInteger unscaledValue, int scale, int radix)

	{

		boolean negt = (scale < 0);

		if (negt) {

			scale = -scale;

		}



		checkRadix(radix);

		BigInteger scaleValue = bigIntegerPow(bigIntegerValueOf(radix), scale);



		if (!negt) {

			m_n = unscaledValue;

			m_q = scaleValue;

		} else {

			m_n = bigIntegerMultiply(unscaledValue, scaleValue);

			m_q = BIG_INTEGER_ONE;

		}



		normalize();

	}



	/** Construct a BigRational from an unscaled value by scaling, default radix.

	*/

	public BigRational(BigInteger unscaledValue, int scale)

	{

		this(unscaledValue, scale, DEFAULT_RADIX);

	}



	/** Construct a BigRational from an unscaled fix number value by scaling.

	*/

	public BigRational(long unscaledValue, int scale, int radix)

	{

		this(bigIntegerValueOf(unscaledValue), scale, radix);

	}



	// can't have public BigRational(long unscaledValue, int scale)

	// as alias for BigRational(unscaledValue, scale, DEFAULT_RADIX);

	// it's too ambigous with public BigRational(long d, long q).



	/** Normalize BigRational.

	* Denominator will be positive, nummerator and denominator will have

	* no common divisor.

	* BigIntegers -1, 0, 1 will be set to constants for later comparision speed.

	*/

	private void normalize()

	{

		// note: don't call anything that depends on a normalized this.

		// i.e.: don't call most (or all) of the BigRational methods.



		if (m_n == null || m_q == null) {

			throw new NumberFormatException("null");

		}



		// [these are typically cheap.]

		int ns = m_n.signum();

		int qs = m_q.signum();



		// note: we don't throw on qs==0.  that'll be done elsewhere.

		// if (qs == 0) {

		//	throw new NumberFormatException("quotient zero");

		// }



		if (ns == 0 && qs == 0) {

			// [both for speed]

			m_n = BIG_INTEGER_ZERO;

			m_q = BIG_INTEGER_ZERO;

			return;

		}



		if (ns == 0) {

			m_q = BIG_INTEGER_ONE;

			// [for speed]

			m_n = BIG_INTEGER_ZERO;

			return;

		}



		if (qs == 0) {

			m_n = BIG_INTEGER_ONE;

			// [for speed]

			m_q = BIG_INTEGER_ZERO;

			return;

		}



		// check the frequent case of q==1, for speed.

		// note: this only covers the normalized-for-speed 1-case.

		if (m_q == BIG_INTEGER_ONE) {

			// [for speed]

			m_n = proxyBigInteger(m_n);

			return;

		}



		// check the symmetric case too, for speed.

		// note: this only covers the normalized-for-speed 1-case.

		if ((m_n == BIG_INTEGER_ONE || m_n == BIG_INTEGER_MINUS_ONE) && qs > 0) {

			// [for speed]

			m_q = proxyBigInteger(m_q);

			return;

		}



		// setup torn apart for speed

		BigInteger na = m_n;

		BigInteger qa = m_q;



		if (qs < 0) {

			m_n = m_n.negate();

			m_q = m_q.negate();

			ns = -ns;

			qs = -qs;



			qa = m_q;

			if (ns > 0) {

				na = m_n;

			}



		} else {

			if (ns < 0) {

				na = m_n.negate();

			}

		}



		BigInteger g = na.gcd(qa);



		if (!bigIntegerOnep(g)) {

			m_n = m_n.divide(g);

			m_q = m_q.divide(g);

		}



		// for [later] speed

		m_n = proxyBigInteger(m_n);

		m_q = proxyBigInteger(m_q);

	}



	/** Check constraints on radixes.

	* Radix may not be negative or less than two.

	*/

	private static void checkRadix(int radix)

	{

		if (radix < 0) {

			throw new NumberFormatException("radix negative");

		}



		if (radix < 2) {

			throw new NumberFormatException("radix too small");

		}

	}



	/** Check some of the integer format constraints.

	*/

	private static void checkNumberFormat(String s)

	{

		// "x", "-x", "+x", "", "-", "+"



		if (s == null) {

			throw new NumberFormatException("null");

		}



		// note: 'embedded sign' catches both-signs cases too.



		int p = s.indexOf('+');

		int m = s.indexOf('-');



		int pp = (p == -1 ? -1 : s.indexOf('+', p + 1));

		int mm = (m == -1 ? -1 : s.indexOf('-', m + 1));



		if ((p != -1 && p != 0) || (m != -1 && m != 0) || pp != -1 || mm != -1) {

			// embedded sign.  this covers the both-signs case.

			throw new NumberFormatException("embedded sign");

		}

	}



	/** Check number format for fraction part.

	*/

	private static void checkFractionFormat(String s)

	{

		if (s == null) {

			throw new NumberFormatException("null");

		}



		if (s.indexOf('+') != -1 || s.indexOf('-') != -1) {

			throw new NumberFormatException("sign in fraction");

		}

	}



	/** Proxy to BigInteger.ValueOf().

	* Speeds up comparisions by using constants.

	*/

	private static BigInteger bigIntegerValueOf(long n)

	{

		// return the internal constants used for checks if possible.



		// check whether it's outside int range.

		// actually check a much narrower range, fitting the switch below.

		if (n < -16 || n > 16) {

			return BigInteger.valueOf(n);

		}



		// jump table, for speed

		switch ((int)n) {



		case 0 :

			return BIG_INTEGER_ZERO;



		case 1 :

			return BIG_INTEGER_ONE;



		case -1 :

			return BIG_INTEGER_MINUS_ONE;



		case 2 :

			return BIG_INTEGER_TWO;



		case -2 :

			return BIG_INTEGER_MINUS_TWO;



		case 10 :

			return BIG_INTEGER_TEN;



		case 16 :

			return BIG_INTEGER_SIXTEEN;



		}



		return BigInteger.valueOf(n);

	}



	/** Convert BigInteger to its proxy.

	* Speeds up comparisions by using constants.

	*/

	private static BigInteger proxyBigInteger(BigInteger n)

	{

		// note: these tests are quite expensive,

		// so they should be minimized to a reasonable amount.



		// there is a priority order in the tests:

		// 1, 0, -1.



		// two layer testing.

		// cheap tests first.



		if (n == BIG_INTEGER_ONE) {

			return n;

		}



		if (n == BIG_INTEGER_ZERO) {

			return n;

		}



		if (n == BIG_INTEGER_MINUS_ONE) {

			return n;

		}



		// more expensive tests later.



		if (n.equals(BIG_INTEGER_ONE)) {

			return BIG_INTEGER_ONE;

		}



		if (n.equals(BIG_INTEGER_ZERO)) {

			// [typically not reached from normalize().]

			return BIG_INTEGER_ZERO;

		}



		if (n.equals(BIG_INTEGER_MINUS_ONE)) {

			return BIG_INTEGER_MINUS_ONE;

		}



		// note: BIG_INTEGER_TWO et al. _not_ used for checks

		// and therefore not proxied _here_.  this speeds up tests.



		// not proxy-able

		return n;

	}



	/** Proxy to new BigInteger().

	* Speeds up comparisions by using constants.

	*/

	private static BigInteger newBigInteger(String s, int radix)

	{

		// note: mind the radix.

		// however, 0/1/-1 are not a problem.



		// _often_ used strings (e.g. 0 for empty fraction and

		// 1 for empty denominator), for speed.



		if (s.equals("1")) {

			return BIG_INTEGER_ONE;

		}



		if (s.equals("0")) {

			return BIG_INTEGER_ZERO;

		}



		if (s.equals("-1")) {

			return BIG_INTEGER_MINUS_ONE;

		}



		// note: BIG_INTEGER_TWO et al. _not_ used for checks

		// and therefore not proxied _here_.  this speeds up tests.



		return new BigInteger(s, radix);

	}



	/** BigInteger equality proxy.

	* For speed.

	*/

	private static boolean bigIntegerEquals(BigInteger n, BigInteger m)

	{

		// first test is for speed.

		if (n == m) {

			return true;

		}



		return n.equals(m);

	}



	/** Zero (0) value predicate.

	* For convenience and speed.

	*/

	private static boolean bigIntegerZerop(BigInteger n)

	{

		// first test is for speed.

		if (n == BIG_INTEGER_ZERO) {

			return true;

		}



		// well, this is also optimized for speed a bit.

		return (n.signum() == 0);

	}



	/** One (1) value predicate.

	* For convenience and speed.

	*/

	private static boolean bigIntegerOnep(BigInteger n)

	{

		// first test is for speed.

		if (n == BIG_INTEGER_ONE) {

			return true;

		}



		return bigIntegerEquals(n, BIG_INTEGER_ONE);

	}



	/** BigInteger multiply proxy.

	* For speed.

	* The more common cases of integers (q == 1) are optimized.

	*/

	private static BigInteger bigIntegerMultiply(BigInteger n, BigInteger m)

	{

		// optimization: one or both operands are zero.

		if (bigIntegerZerop(n) || bigIntegerZerop(m)) {

			return BIG_INTEGER_ZERO;

		}



		// optimization: second operand is one (i.e. neutral element).

		if (bigIntegerOnep(m)) {

			return n;

		}



		// optimization: first operand is one (i.e. neutral element).

		if (bigIntegerOnep(n)) {

			return m;

		}



		// default case.  [this would handle all cases.]

		return n.multiply(m);

	}



	/** Proxy to new BigInteger.pow().

	* For speed.

	*/

	private static BigInteger bigIntegerPow(BigInteger n, int exponent)

	{

		// jump table, for speed.

		switch (exponent) {



		case 0 :

			if (bigIntegerZerop(n)) {

				throw new ArithmeticException("zero exp zero");

			}

			return BIG_INTEGER_ONE;



		case 1 :

			return n;



		}



		return n.pow(exponent);

	}



	/** The constant zero (0).

	* [Constant name: see class BigInteger.]

	*/

	public final static BigRational ZERO = new BigRational(0);



	/** The constant one (1).

	* [Name: see class BigInteger.]

	*/

	public final static BigRational ONE = new BigRational(1);



	/** The constant minus-one (-1).

	*/

	public final static BigRational MINUS_ONE = new BigRational(-1);



	/** Positive predicate.

	* For convenience.

	*/

	private boolean positivep()

	{

		return (signum() > 0);

	}



	/** Negative predicate.

	* For convenience.

	*/

	private boolean negativep()

	{

		return (signum() < 0);

	}



	/** Zero predicate.

	* For convenience and speed.

	*/

	private boolean zerop()

	{

		// first test is for speed.

		if (this == ZERO) {

			return true;

		}



		// well, this is also optimized for speed a bit.

		return (signum() == 0);

	}



	/** One predicate.

	* For convenience and speed.

	*/

	private boolean onep()

	{

		// first test is for speed.

		if (this == ONE) {

			return true;

		}



		return equals(ONE);

	}



	/** Integer predicate (quotient is one).

	* For convenience.

	*/

	private boolean integerp()

	{

		return bigIntegerOnep(m_q);

	}



	/** BigRational string representation, format "[-]d[/q]".

	*/

	public String toString(int radix)

	{

		String s = m_n.toString(radix);



		if (integerp()) {

			return s;

		}



		String t = m_q.toString(radix);



		return s + "/" + t;

	}



	/** BigRational string representation, format "[-]d[/q]", default radix.

	* Default string representation.

	* Overwrites Object.toString().

	*/

	public String toString()

	{

		return toString(DEFAULT_RADIX);

	}



	/** Dot-format "[-]i.f" string representation, with a precision.

	* Precision may be negative.

	*/

	public String toStringDot(int precision, int radix)

	{

		checkRadix(radix);

		BigRational scaleValue = new BigRational(

			bigIntegerPow(bigIntegerValueOf(radix),

				(precision < 0 ? -precision : precision)));

		if (precision < 0) {

			scaleValue = scaleValue.invert();

		}



		// default round mode.

		BigRational n = multiply(scaleValue).round();

		boolean negt = n.negativep();

		if (negt) {

			n = n.negate();

		}



		String s = n.toString(radix);



		if (precision >= 0) {

			// left-pad with '0'

			while (s.length() <= precision) {

				s = "0" + s;

			}



			int dot = s.length() - precision;

			String i = s.substring(0, dot);

			String f = s.substring(dot);



			s = i;

			if (f.length() > 0) {

				s = s + "." + f;

			}



		} else {

			if (!s.equals("0")) {

				// right-pad with '0'

				for (int i = -precision; i > 0; i--) {

					s = s + "0";

				}

			}

		}



		// add sign

		if (negt) {

			s = "-" + s;

		}



		return s;

	}



	/** Dot-format "[-]i.f" string representation, with a precision, default radix

	* Precision may be negative.

	*/

	public String toStringDot(int precision)

	{

		return toStringDot(precision, DEFAULT_RADIX);

	}



	// note: there is no 'default' precision.



	/** Add two BigRationals and return a new BigRational.

	* [Name: see class BigInteger.]

	*/

	public BigRational add(BigRational that)

	{

		// optimization: second operand is zero (i.e. neutral element).

		if (that.zerop()) {

			return this;

		}



		// optimization: first operand is zero (i.e. neutral element).

		if (zerop()) {

			return that;

		}



		// note: the calculated n/q may be denormalized,

		// implicit normalize() is needed.



		// optimization: same denominator.

		if (bigIntegerEquals(m_q, that.m_q)) {

			return new BigRational(

				m_n.add(that.m_n),

				m_q);

		}



		// optimization: second operand is an integer.

		if (that.integerp()) {

			return new BigRational(

				m_n.add(that.m_n.multiply(m_q)),

				m_q);

		}



		// optimization: first operand is an integer.

		if (integerp()) {

			return new BigRational(

				m_n.multiply(that.m_q).add(that.m_n),

				that.m_q);

		}



		// default case.  [this would handle all cases.]

		return new BigRational(

			m_n.multiply(that.m_q).add(that.m_n.multiply(m_q)),

			m_q.multiply(that.m_q));

	}



	/** Add a BigRational and a long fix number integer and return a new BigRational.

	*/

	public BigRational add(long that)

	{

		return add(new BigRational(that));

	}



	/** Subtract a BigRational from another (this) and return a new BigRational.

	* [Name: see class BigInteger.]

	*/

	public BigRational subtract(BigRational that)

	{

		// optimization: second operand is zero.

		if (that.zerop()) {

			return this;

		}



		// [not optimizing first operand being zero.]



		// note: the calculated n/q may be denormalized,

		// implicit normalize() is needed.



		// optimization: same denominator.

		if (bigIntegerEquals(m_q, that.m_q)) {

			return new BigRational(

				m_n.subtract(that.m_n),

				m_q);

		}



		// optimization: second operand is an integer.

		if (that.integerp()) {

			return new BigRational(

				m_n.subtract(that.m_n.multiply(m_q)),

				m_q);

		}



		// optimization: first operand is an integer.

		if (integerp()) {

			return new BigRational(

				m_n.multiply(that.m_q).subtract(that.m_n),

				that.m_q);

		}



		// default case.  [this would handle all cases.]

		return new BigRational(

			m_n.multiply(that.m_q).subtract(that.m_n.multiply(m_q)),

			m_q.multiply(that.m_q));

	}



	/** Subtract a long fix number integer from this and return a new BigRational.

	*/

	public BigRational subtract(long that)

	{

		return subtract(new BigRational(that));

	}



	/** An alias to subtract().

	*/

	public BigRational sub(BigRational that)

	{

		return subtract(that);

	}



	/** An alias to subtract().

	*/

	public BigRational sub(long that)

	{

		return subtract(that);

	}



	/** Multiply two BigRationals and return a new BigRational.

	* [Name: see class BigInteger.]

	*/

	public BigRational multiply(BigRational that)

	{

		BigInteger n = bigIntegerMultiply(m_n, that.m_n);



		// optimization: one of the operands was zero.

		if (bigIntegerZerop(n)) {

			return ZERO;

		}



		BigInteger q = bigIntegerMultiply(m_q, that.m_q);



		// note: the calculated n/q may be denormalized,

		// implicit normalize() is needed.



		return new BigRational(n, q);

	}



	/** Multiply a long fix number integer to this and return a new BigRational.

	*/

	public BigRational multiply(long that)

	{

		return multiply(new BigRational(that));

	}



	/** An alias to multiply().

	*/

	public BigRational mul(BigRational that)

	{

		return multiply(that);

	}



	/** An alias to multiply().

	*/

	public BigRational mul(long that)

	{

		return multiply(that);

	}



	/** Divide a BigRational (this) through another and return a new BigRational.

	* [Name: see class BigInteger.]

	*/

	public BigRational divide(BigRational that)

	{

		if (that.zerop()) {

			throw new ArithmeticException("division by zero");

		}



		// note: the calculated n/q may be denormalized,

		// implicit normalize() is needed.



		return new BigRational(

			bigIntegerMultiply(m_n, that.m_q),

			bigIntegerMultiply(m_q, that.m_n));

	}



	/** Divide a BigRational (this) through a long fix number integer

	* and return a new BigRational.

	*/

	public BigRational divide(long that)

	{

		return divide(new BigRational(that));

	}



	/** An alias to divide().

	*/

	public BigRational div(BigRational that)

	{

		return divide(that);

	}



	/** An alias to divide().

	*/

	public BigRational div(long that)

	{

		return divide(that);

	}



	/** Calculate a BigRational's integer power and return a new BigRational.

	* The integer exponent may be negative.

	*/

	public BigRational power(int exponent)

	{

		boolean z = zerop();



		if (z && exponent == 0) {

			throw new ArithmeticException("zero exp zero");

		}



		if (exponent == 0) {

			return ONE;

		}



		// optimization

		if (z && exponent > 0) {

			return ZERO;

		}



		// optimization

		if (exponent == 1) {

			return this;

		}



		boolean negt = (exponent < 0);

		if (negt) {

			exponent = -exponent;

		}



		BigInteger d = bigIntegerPow(m_n, exponent);

		BigInteger q = bigIntegerPow(m_q, exponent);



		// note: the calculated n/q are not denormalized,

		// implicit normalize() would not be needed.



		if (!negt) {

			return new BigRational(d, q);

		} else {

			return new BigRational(q, d);

		}

	}



	/** An alias to power().

	* [Name: see classes Math, BigInteger.]

	*/

	public BigRational pow(int exponent)

	{

		return power(exponent);

	}



	/** Calculate the remainder of two BigRationals and return a new BigRational.

	* [Name: see class BigInteger.]

	* The remainder result may be negative.

	* The remainder is based on round down (towards zero) / truncate.

	* 5/3 == 1 + 2/3 (remainder 2), 5/-3 == -1 + 2/-3 (remainder 2),

	* -5/3 == -1 + -2/3 (remainder -2), -5/-3 == 1 + -2/-3 (remainder -2).

	*/

	public BigRational remainder(BigRational that)

	{

		int s = signum();

		int ts = that.signum();



		if (ts == 0) {

			throw new ArithmeticException("division by zero");

		}



		BigRational a = this;

		if (s < 0) {

			a = a.negate();

		}



		// divisor's sign doesn't matter, as stated above.

		// this is also BigInteger's behavior, but don't let us be

		// dependent of a change in that.

		BigRational b = that;

		if (ts < 0) {

			b = b.negate();

		}



		BigRational r = a.remainderOrModulusAbsolute(b);



		if (s < 0) {

			r = r.negate();

		}



		return r;

	}



	/** Calculate the remainder of a BigRational and a long fix number integer

	* and return a new BigRational.

	*/

	public BigRational remainder(long that)

	{

		return remainder(new BigRational(that));

	}



	/** An alias to remainder().

	*/

	public BigRational rem(BigRational that)

	{

		return remainder(that);

	}



	/** An alias to remainder().

	*/

	public BigRational rem(long that)

	{

		return remainder(that);

	}



	/** Calculate the modulus of two BigRationals and return a new BigRational.

	* The modulus result may be negative.

	* Modulus is based on round floor (towards negative).

	* 5/3 == 1 + 2/3 (modulus 2), 5/-3 == -2 + -1/-3 (modulus -1),

	* -5/3 == -2 + 1/3 (modulus 1), -5/-3 == 1 + -2/-3 (modulus -2).

	*/

	public BigRational modulus(BigRational that)

	{

		int s = signum();

		int ts = that.signum();



		if (ts == 0) {

			throw new ArithmeticException("division by zero");

		}



		BigRational a = this;

		if (s < 0) {

			a = a.negate();

		}



		BigRational b = that;

		if (ts < 0) {

			b = b.negate();

		}



		BigRational r = a.remainderOrModulusAbsolute(b);



		if (s < 0 && ts < 0) {

			r = r.negate();

		} else if (ts < 0) {

			r = r.subtract(b);

		} else if (s < 0) {

			r = b.subtract(r);

		}



		return r;

	}



	/** Calculate the modulus of a BigRational and a long fix number integer

	* and return a new BigRational.

	*/

	public BigRational modulus(long that)

	{

		return modulus(new BigRational(that));

	}



	/** An alias to modulus().

	* [Name: see class BigInteger.]

	*/

	public BigRational mod(BigRational that)

	{

		return modulus(that);

	}



	/** An alias to modulus().

	*/

	public BigRational mod(long that)

	{

		return modulus(that);

	}



	/** Remainder or modulus of non-negative values.

	* Helper function to remainder() and modulus().

	*/

	private BigRational remainderOrModulusAbsolute(BigRational that)

	{

		int s = signum();

		int ts = that.signum();



		if (s < 0 || ts < 0) {

			throw new IllegalArgumentException("negative values(s)");

		}



		if (ts == 0) {

			throw new ArithmeticException("division by zero");

		}



		// optimization

		if (s == 0) {

			return ZERO;

		}



		BigInteger n = bigIntegerMultiply(m_n, that.m_q);

		BigInteger q = bigIntegerMultiply(m_q, that.m_n);



		BigInteger r = n.remainder(q);

		BigInteger rq = bigIntegerMultiply(m_q, that.m_q);



		return new BigRational(r, rq);

	}



	/** Signum, -1, 0, or 1.

	* [Name: see class BigInteger.]

	*/

	public int signum()

	{

		// note: m_q is positive.

		return m_n.signum();

	}



	/** An alias to signum().

	*/

	public int sign()

	{

		return signum();

	}



	/** Return a new BigRational with the absolute value of this.

	*/

	public BigRational absolute()

	{

		// note: m_q is positive.



		if (m_n.signum() >= 0) {

			return this;

		}



		// optimization

		if (this == MINUS_ONE) {

			return ONE;

		}



		// note: the calculated n/q are not denormalized,

		// implicit normalize() would not be needed.



		return new BigRational(m_n.negate(), m_q);

	}



	/** An alias to absolute().

	* [Name: see classes Math, BigInteger.]

	*/

	public BigRational abs()

	{

		return absolute();

	}



	/** Return a new BigRational with the negative value of this.

	* [Name: see class BigInteger.]

	*/

	public BigRational negate()

	{

		// optimization

		if (this == ZERO) {

			return this;

		}



		// optimization

		if (this == ONE) {

			return MINUS_ONE;

		}



		// optimization

		if (this == MINUS_ONE) {

			return ONE;

		}



		// note: the calculated n/q are not denormalized,

		// implicit normalize() would not be needed.



		return new BigRational(m_n.negate(), m_q);

	}



	/** An alias to negate().

	*/

	public BigRational neg()

	{

		return negate();

	}



	/** Return a new BigRational with the inverted (reciprocal) value of this.

	*/

	public BigRational invert()

	{

		if (zerop()) {

			throw new ArithmeticException("division by zero");

		}



		// optimization

		if (this == ONE || this == MINUS_ONE) {

			return this;

		}



		// note: the calculated n/q are not denormalized,

		// implicit normalize() would not be needed.



		return new BigRational(m_q, m_n);

	}



	/** An alias to invert().

	*/

	public BigRational inv()

	{

		return invert();

	}



	/** Return the minimal value of two BigRationals.

	*/

	public BigRational minimum(BigRational that)

	{

		return (compareTo(that) <= 0 ? this : that);

	}



	/** Return the minimal value of a BigRational and a long fix number integer.

	*/

	public BigRational minimum(long that)

	{

		return minimum(new BigRational(that));

	}



	/** An alias to minimum().

	* [Name: see classes Math, BigInteger.]

	*/

	public BigRational min(BigRational that)

	{

		return minimum(that);

	}



	/** An alias to minimum().

	*/

	public BigRational min(long that)

	{

		return minimum(that);

	}



	/** Return the maximal value of two BigRationals.

	*/

	public BigRational maximum(BigRational that)

	{

		return (compareTo(that) >= 0 ? this : that);

	}



	/** Return the maximum value of a BigRational and a long fix number integer.

	*/

	public BigRational maximum(long that)

	{

		return maximum(new BigRational(that));

	}



	/** An alias to maximum().

	* [Name: see classes Math, BigInteger.]

	*/

	public BigRational max(BigRational that)

	{

		return maximum(that);

	}



	/** An alias to maximum().

	*/

	public BigRational max(long that)

	{

		return maximum(that);

	}



	/** Compare object for equality.

	* Overwrites Object.equals().

	* Only some object types are allowed (see compareTo).

	* Never throws.

	*/

	public boolean equals(Object object)

	{

		if (object == this) {

			return true;

		}



		// delegate to compareTo(Object)

		try {

			return (compareTo(object) == 0);

		} catch (ClassCastException e) {

			return false;

		}

	}



	/** Hash code.

	* Overwrites Object.hashCode().

	*/

	public int hashCode()

	{

		int dh = m_n.hashCode(), qh = m_q.hashCode();

		// dh * qh;

		int h = ((dh + 1) * (qh + 2));

		return h;

	}



	/** Compare two BigRationals.

	*/

	public int compareTo(BigRational that)

	{

		int s = signum();

		int t = that.signum();



		if (s != t) {

			return (s < t ? -1 : 1);

		}



		// optimization: both zero.

		if (s == 0) {

			return 0;

		}



		// note: both m_q are positive.

		return bigIntegerMultiply(m_n, that.m_q).compareTo(

			bigIntegerMultiply(that.m_n, m_q));

	}



	/** Compare to BigInteger.

	*/

	public int compareTo(BigInteger that)

	{

		return compareTo(new BigRational(that));

	}



	/** Compare to long.

	*/

	public int compareTo(long that)

	{

		return compareTo(new BigRational(that));

	}



	/** Compare object (BigRational/BigInteger/Long/Integer).

	* Implements Comparable.compareTo(Object) (jdk-1.2 and later).

	* Only BigRational/BigInteger/Long/Integer objects allowed, will throw otherwise.

	*/

	public int compareTo(Object object)

	{

		if (object instanceof Integer) {

			return compareTo(((Integer)object).longValue());

		}



		if (object instanceof Long) {

			return compareTo(((Long)object).longValue());

		}



		if (object instanceof BigInteger) {

			return compareTo((BigInteger)object);

		}



		// now assuming that it's either 'instanceof BigRational'

		// or it'll throw a ClassCastException.



		return compareTo((BigRational)object);

	}



	/** Convert to BigInteger, by rounding.

	*/

	public BigInteger bigIntegerValue()

	{

		return round().m_n;

	}



	/** Convert to long, by rounding and delegating to BigInteger.

	* Implements Number.longValue().

	*/

	public long longValue()

	{

		// delegate to BigInteger.

		return bigIntegerValue().longValue();

	}



	/** Convert to int, by rounding and delegating to BigInteger.

	* Implements Number.intValue().

	*/

	public int intValue()

	{

		// delegate to BigInteger.

		return bigIntegerValue().intValue();

	}



	/** Manifactor a BigRational from a BigInteger.

	*/

	public static BigRational valueOf(BigInteger value)

	{

		return new BigRational(value);

	}



	/** Manifactor a BigRational from a long fix number integer.

	*/

	public static BigRational valueOf(long value)

	{

		return new BigRational(value);

	}



	// note: byte/short/int implicitly upgraded to long,

	// so we don't implement valueOf(int) et al.



	/** Rounding mode to round away from zero.

	*/

	public final static int ROUND_UP = 0;



	/** Rounding mode to round towards zero.

	*/

	public final static int ROUND_DOWN = 1;



	/** Rounding mode to round towards positive infinity.

	*/

	public final static int ROUND_CEILING = 2;



	/** Rounding mode to round towards negative infinity.

	*/

	public final static int ROUND_FLOOR = 3;



	/** Rounding mode to round towards nearest neighbor unless both

	* neighbors are equidistant, in which case to round up.

	*/

	public final static int ROUND_HALF_UP = 4;



	/** Rounding mode to round towards nearest neighbor unless both

	* neighbors are equidistant, in which case to round down.

	*/

	public final static int ROUND_HALF_DOWN = 5;



	/** Rounding mode to round towards the nearest neighbor unless both

	* neighbors are equidistant, in which case to round towards the even neighbor.

	*/

	public final static int ROUND_HALF_EVEN = 6;



	/** Rounding mode to assert that the requested operation has an exact

	* result, hence no rounding is necessary.

	* If this rounding mode is specified on an operation that yields an inexact result,

	* an ArithmeticException is thrown.

	*/

	public final static int ROUND_UNNECESSARY = 7;



	/** Rounding mode to round towards nearest neighbor unless both

	* neighbors are equidistant, in which case to round ceiling.

	*/

	public final static int ROUND_HALF_CEILING = 8;



	/** Rounding mode to round towards nearest neighbor unless both

	* neighbors are equidistant, in which case to round floor.

	*/

	public final static int ROUND_HALF_FLOOR = 9;



	/** Rounding mode to round towards the nearest neighbor unless both

	* neighbors are equidistant, in which case to round towards the odd neighbor.

	*/

	public final static int ROUND_HALF_ODD = 10;



	/** Default round mode, ROUND_HALF_UP.

	*/

	public final static int DEFAULT_ROUND_MODE = ROUND_HALF_UP;



	/** Round.

	*/

	public BigRational round(int roundMode)

	{

		// return self if we don't need to round, independant of rounding mode

		if (integerp()) {

			return this;

		}



		return new BigRational(roundToBigInteger(roundMode));

	}



	/** Round to BigInteger helper function.

	* Internally used.

	*/

	private BigInteger roundToBigInteger(int roundMode)

	{

		// note: remainder and its duplicate are calculated for all cases.



		BigInteger d = m_n;

		BigInteger q = m_q;



		int sgn = d.signum();

		if (sgn == 0) {

			return d;

		}



		// keep info on the sign

		boolean pos = (sgn > 0);



		// operate on positive values

		if (!pos) {

			d = d.negate();

		}



		BigInteger[] divrem = d.divideAndRemainder(q);

		BigInteger dv = divrem[0];

		BigInteger r = divrem[1];



		// return if we don't need to round, independant of rounding mode

		if (bigIntegerZerop(r)) {

			if (!pos) {

				dv = dv.negate();

			}



			return dv;

		}



		boolean up = false;

		int comp = r.multiply(BIG_INTEGER_TWO).compareTo(q);



		switch (roundMode) {



		// Rounding mode to round away from zero.

		case ROUND_UP :

			up = true;

			break;



		// Rounding mode to round towards zero.

		case ROUND_DOWN :

			up = false;

			break;



		// Rounding mode to round towards positive infinity.

		case ROUND_CEILING :

			up = pos;

			break;



		// Rounding mode to round towards negative infinity.

		case ROUND_FLOOR :

			up = !pos;

			break;



		// Rounding mode to round towards "nearest neighbor" unless both

		// neighbors are equidistant, in which case round up.

		case ROUND_HALF_UP :

			up = (comp >= 0);

			break;



		// Rounding mode to round towards "nearest neighbor" unless both

		// neighbors are equidistant, in which case round down.

		case ROUND_HALF_DOWN :

			up = (comp > 0);

			break;



		case ROUND_HALF_CEILING :

			up = (comp != 0 ? comp > 0 : pos);

			break;



		case ROUND_HALF_FLOOR :

			up = (comp != 0 ? comp > 0 : !pos);

			break;



		// Rounding mode to round towards the "nearest neighbor" unless both

		// neighbors are equidistant, in which case, round towards the even neighbor.

		case ROUND_HALF_EVEN :

			up = (comp != 0 ?

				comp > 0 :

				!bigIntegerZerop(dv.remainder(BIG_INTEGER_TWO)));

			break;



		case ROUND_HALF_ODD :

			up = (comp != 0 ?

				comp > 0 :

				bigIntegerZerop(dv.remainder(BIG_INTEGER_TWO)));

			break;



		// Rounding mode to assert that the requested operation has an exact

		// result, hence no rounding is necessary.  If this rounding mode is

		// specified on an operation that yields an inexact result, an

		// ArithmeticException is thrown.

		case ROUND_UNNECESSARY :

			if (!bigIntegerZerop(r)) {

				throw new ArithmeticException("rounding necessary");

			}

			up = false;

			break;



		default :

			throw new IllegalArgumentException("unsupported rounding mode");

		}



		if (up) {

			dv = dv.add(BIG_INTEGER_ONE);

		}



		if (!pos) {

			dv = dv.negate();

		}



		return dv;

	}



	/** Round by default mode.

	*/

	public BigRational round()

	{

		return round(DEFAULT_ROUND_MODE);

	}



	/** Floor, round towards negative infinity.

	*/

	public BigRational floor()

	{

		return round(ROUND_FLOOR);

	}



	/** Ceiling, round towards positive infinity.

	*/

	public BigRational ceiling()

	{

		return round(ROUND_CEILING);

	}



	/** An alias to ceiling().

	* [Name: see class Math.]

	*/

	public BigRational ceil()

	{

		return ceiling();

	}



	/** Truncate, round towards zero.

	*/

	public BigRational truncate()

	{

		return round(ROUND_DOWN);

	}



	/** An alias to truncate().

	*/

	public BigRational trunc()

	{

		return truncate();

	}



	/** Integer part.

	*/

	public BigRational integerPart()

	{

		return round(ROUND_DOWN);

	}



	/** Fractional part.

	*/

	public BigRational fractionalPart()

	{

		BigRational ip = integerPart();

		BigRational fp = subtract(ip);



		// this==ip+fp; sign(fp)==sign(this)

		return fp;

	}



	/** Return an array of BigRationals with both integer and fractional part.

	*/

	public BigRational[] integerAndFractionalPart()

	{

		// note: this duplicates fractionalPart() code, for speed.



		BigRational ip = integerPart();

		BigRational fp = subtract(ip);



		BigRational[] pp = new BigRational[2];

		pp[0] = ip;

		pp[1] = fp;



		return pp;

	}




}


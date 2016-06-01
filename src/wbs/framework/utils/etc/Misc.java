package wbs.framework.utils.etc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.NonNull;
import lombok.SneakyThrows;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.ReadableInstant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import fj.data.Either;

// TODO lots to deprecate here
public
class Misc {

	public final static
	SimpleDateFormat timestampFormatSeconds =
		new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");

	private
	Misc () {
		// never instantiated
	}

	@SafeVarargs
	public static
	<Type> Type ifNull (
			@NonNull Type... values) {

		for (Type value : values) {

			if (value != null)
				return value;

		}

		return null;

	}

	public static
	<Type> Type ifNull (
			Type input,
			Type ifNull) {

		return input == null
			? ifNull
			: input;

	}

	public static
	<Type> Type nullIf (
			Type input,
			Type nullIf) {

		if (input == null)
			return null;

		if (nullIf != null && input.equals (nullIf))
			return null;

		return input;

	}

	public static
	String emptyStringIfNull (
			String string) {

		return ifNull (
			string,
			"");

	}

	public static
	String nullIfEmptyString (
			String string) {

		return nullIf (
			string,
			"");

	}

	public static
	<Type> Type ifEq (
			Type value,
			Type lookFor,
			Type replaceWith) {

		return equal (value, lookFor)
			? replaceWith
			: value;

	}

	private final static
	Pattern intPattern =
		Pattern.compile ("[0-9]+");

	public static
	boolean isInt (
			String string) {

		if (string == null)
			return false;

		return intPattern
			.matcher (string)
			.matches ();

	}

	public static
	Integer toInteger (
			String string) {

		if (string == null)
			return null;

		if (isInt (string))
			return Integer.parseInt (string);

		return null;

	}

	public static
	Long toLong (
			String string) {

		if (string == null)
			return null;

		if (isInt (string))
			return Long.parseLong (string);

		return null;

	}

	public static <Type extends Enum<Type>>
	Type toEnum (
			Class<Type> enumType,
			String name) {

		if (name == null || name.length () == 0)
			return null;

		if (equal (name, "null"))
			return null;

		return Enum.valueOf (enumType, name);

	}

	public static
	Enum<?> toEnumGeneric (
			@NonNull Class<?> enumType,
			@NonNull String name) {

		return (Enum<?>)
			Arrays.stream (enumType.getEnumConstants ())
			.filter (value -> ((Enum<?>) value).name ().equals (name))
			.findFirst ()
			.orElseThrow (() -> new IllegalArgumentException ());

	}

	public static
	Boolean toBoolean (
			String string) {

		if (string == null)
			return null;

		if (string.equals ("true"))
			return true;

		if (string.equals ("false"))
			return false;

		return null;

	}

	public static
	String toStringNull (
			Object object) {

		if (object == null)
			return null;

		return object.toString ();

	}

	public static
	String objectToString (
			@NonNull Object object) {

		return object.toString ();

	}

	public static
	String urlEncode (
			String string) {

		try {

			return URLEncoder.encode (
				string,
				"utf8");

		} catch (UnsupportedEncodingException exception) {

			throw new RuntimeException (
				exception);

		}

	}

	public static
	byte[] stringToUnicodeBytes (
			@NonNull String string) {

		return stringToBytes (
			string,
			"utf-8");

	}


	public static
	byte[] stringToBytes (
			@NonNull String string,
			@NonNull String charset) {

		try {

			return string.getBytes (
				charset);

		} catch (UnsupportedEncodingException exception) {

			throw new RuntimeException (exception);

		}

	}

	public static
	String bytesToString (
			byte[] bytes,
			String charset) {

		try {

			return new String (
				bytes,
				charset);

		} catch (UnsupportedEncodingException exception) {

			throw new RuntimeException (exception);

		}

	}

	private final static
	Pattern nonAlphanumericWordsPattern =
		Pattern.compile ("[^a-z0-9]+");

	public static
	String simplify (
			@NonNull String s) {

		return nonAlphanumericWordsPattern.matcher (
			s.toLowerCase()).replaceAll(
				" ").trim();

	}

	public static
	RuntimeException rethrow (
			Throwable throwable) {

		if (throwable instanceof Error) {
			throw (Error) throwable;
		}

		if (throwable instanceof RuntimeException) {
			throw (RuntimeException) throwable;
		}

		throw new RuntimeException (
			throwable);

	}

	public static
	String implode (
			String glue,
			Collection<? extends Object> pieces) {

		boolean first = true;

		StringBuffer sb = new StringBuffer();

		for (Object piece : pieces) {
			if (first)
				first = false;
			else
				sb.append(glue);
			sb.append(piece.toString());
		}
		return sb.toString();
	}

	@Deprecated
	public static
	String implode (
			String glue,
			Object... pieces) {

		return implode (
			glue,
			Arrays.asList (
				pieces));

	}

	@Deprecated
	public static
	String implode (
			String glue,
			String... pieces) {

		return implode (
			glue,
			Arrays.asList (
				pieces));

	}

	static DateTimeFormatter isoDateFormat =
		DateTimeFormat.forPattern (
				"yyyy-MM-dd'T'HH:mm:ss'Z'")
			.withZoneUTC ();

	public static
	String isoDate (
			@NonNull Instant instant) {

		return instant.toString (
			isoDateFormat);

	}

	/**
	 * Does a null-safe equals.
	 */
	public static
	boolean equal (
			Object object1,
			Object object2) {

		if (object1 == object2)
			return true;

		if (object1 == null || object2 == null)
			return false;

		return object1.equals (object2);

	}

	public static <Type>
	boolean optionalEquals (
			@NonNull Optional<Type> left,
			@NonNull Type right) {

		if (left.isPresent ()) {

			return left.get ().equals (
				right);

		} else {

			return false;

		}

	}

	@SafeVarargs
	public static <Type>
	boolean optionalIn (
			@NonNull Optional<Type> left,
			@NonNull Type... rights) {

		if (left.isPresent ()) {

			return in (
				left.get (),
				rights);

		} else {

			return false;

		}

	}

	public static
	boolean notEqual (
			Object left,
			Object right) {

		if (left == right)
			return false;

		if (left == null || right == null)
			return true;

		return ! left.equals (right);

	}

	/**
	 * Does a equalsIgnoreCase between any number of strings.
	 */
	public static
	boolean equalIgnoreCase (
			String... strings) {

		if (strings.length == 0)
			return true;

		String firstString =
			strings [0];

		for (String string
				: strings) {

			if (string == firstString)
				continue;

			if (! firstString.equalsIgnoreCase (string))
				return false;

		}

		return true;

	}

	public static
	boolean referenceEqual (
			@NonNull Object object1,
			@NonNull Object object2) {

		return object1 == object2;

	}

	public static
	boolean referenceNotEqual (
			@NonNull Object object1,
			@NonNull Object object2) {

		return object1 != object2;

	}

	public static
	String naivePluralise (
			@NonNull String singular) {

		if (
			endsWith (
				singular,
				"s")
		) {

			return singular + "es";

		} else {

			return singular + "s";

		}

	}

	public static
	boolean startsWith (
			@NonNull String subject,
			@NonNull String suffix) {

		return subject.startsWith (
			suffix);

	}

	public static
	boolean endsWith (
			@NonNull String subject,
			@NonNull String suffix) {

		return subject.endsWith (
			suffix);

	}

	public static
	String pluralise (
			long quantity,
			String singularNoun,
			String pluralNoun) {

		return quantity == 1L
			? "" + quantity + " " + singularNoun
			: "" + quantity + " " + pluralNoun;

	}

	public static
	String pluralise (
			long quantity,
			String singularNoun) {

		return quantity == 1L

			? stringFormat (
				"%s %s",
				quantity,
				singularNoun)

			: stringFormat (
				"%s %s",
				quantity,
				naivePluralise (
					singularNoun));

	}

	@SafeVarargs
	public static <Type>
	boolean in (
			@NonNull Type left,
			@NonNull Type... rights) {

		for (
			Type right
				: rights
		) {

			if (
				equal (
					left,
					right)
			) {
				return true;
			}

		}

		return false;

	}

	public static <Type>
	boolean in (
			Type left,
			Collection<Type> rights) {

		for (
			Type right
				: rights
		) {

			if (
				equal (
					left,
					right)
			) {
				return true;
			}

		}

		return false;

	}

	@SafeVarargs
	public static <Type>
	boolean notIn (
			Type left,
			Type... rights) {

		return ! in (
			left,
			rights);

	}

	public static
	Date dateAddMs (
			Date date,
			int milliseconds) {

		Calendar calendar =
			Calendar.getInstance ();

		calendar.setTime (
			date);

		calendar.add (
			Calendar.MILLISECOND,
			milliseconds);

		return calendar.getTime ();

	}

	// TODO use LocalDate instead
	public static
	int age (
			@NonNull TimeZone timeZone,
			long birth,
			long when) {

		Calendar calendar =
			new GregorianCalendar (timeZone);

		calendar.setTime (
			new Date (birth));

		int birthYear =
			calendar.get (Calendar.YEAR);

		int birthMonth =
			calendar.get (Calendar.MONTH);

		int birthDay =
			calendar.get (Calendar.DAY_OF_MONTH);

		calendar.setTime (
			new Date (when));

		int whenYear =
			calendar.get (Calendar.YEAR);

		int whenMonth =
			calendar.get (Calendar.MONTH);

		int whenDay =
			calendar.get (Calendar.DAY_OF_MONTH);

		int age =
			whenYear - birthYear;

		if (whenMonth < birthMonth)
			age --;

		if (whenMonth == birthMonth
				&& whenDay < birthDay)
			age --;

		return age;

	}

	public static
	int min (
			int... params) {

		int ret =
			params [0];

		for (int param : params) {

			if (param < ret)
				ret = param;

		}

		return ret;

	}

	public static
	long min (
			long... params) {

		long ret =
			params [0];

		for (
			long param
				: params
		) {

			if (param < ret)
				ret = param;

		}

		return ret;

	}

	public static
	long max (
			long... params) {

		long ret =
			params [0];

		for (
			long param
				: params
		) {

			if (param > ret)
				ret = param;

		}

		return ret;

	}

	public static
	String lowercase (
			@NonNull String string) {

		return string.toLowerCase ();

	}

	public static
	String capitalise (
			@NonNull String string) {

		if (string.length () == 0)
			return "";

		return (
			Character.toUpperCase (string.charAt (0))
			+ string.substring (1)
		);

	}

	public static
	String uncapitalise (
			@NonNull String string) {

		if (string.length () == 0)
			return "";

		return Character.toLowerCase (string.charAt (0))
			+ string.substring (1);

	}

	public static
	String prettyHour (
			long hour) {

		if (hour < 0 || hour > 23)
			throw new IllegalArgumentException ();

		if (hour == 0)
			return "12am";

		if (hour < 12)
			return "" + hour + "am";

		if (hour == 12)
			return "12pm";

		return "" + (hour - 12) + "pm";

	}

	public static
	String fixNewlines (
			@NonNull String input) {

		return input
			.replaceAll ("\r\n", "\n")
			.replaceAll ("\r", "\n");

	}

	public static <T>
	Iterable<T> iterable (
			final Iterator<T> iterator) {

		return new Iterable<T> () {

			@Override
			public
			Iterator<T> iterator () {
				return iterator;
			}

		};

	}

	public static
	String stringFormat (
			Object... arguments) {

		return StringFormatter.standardArray (
			arguments);

	}

	public static
	String stringFormatArray (
			Object[] args) {

		return stringFormat (
			args);

	}

	@Deprecated
	public static
	void autoClose (
			Object object) {

		try {

			if (object == null)
				return;

			if (object instanceof File) {

				((File) object).delete ();

			} else if (object instanceof OutputStream) {

				((OutputStream) object).close ();

			} else if (object instanceof InputStream) {

				((InputStream) object).close ();

			} else {

				throw new RuntimeException (
					"Can't auto close " + object.getClass ());

			}

		} catch (Exception exception) {

			throw new RuntimeException (
				exception);

		}

	}

	public static
	void autoClose  (
			Object... objects) {

		for (Object object : objects)
			autoClose (object);

	}

	public static
	File createTempFile (
			byte[] data,
			String extension)
		throws IOException {

		File file =
			File.createTempFile (
				"wbs-temp-",
				extension);

		boolean success = false;

		try {

			OutputStream outputStream =
				new FileOutputStream (file);

			outputStream.write (data);
			outputStream.close ();

			success = true;

		} finally {

			if (! success)
				file.delete ();

		}

		return file;

	}

	public static
	int runCommand (
			Logger logger,
			String... command)
		throws IOException {

		logger.info (
			stringFormat (
				"Executing %s",
				implode (" ", command)));

		Process process =
			Runtime.getRuntime ().exec (command);

		InputStream inputStream = null;

		try {

			// copy the error output to our standard error

			inputStream =
				process.getErrorStream ();

			BufferedReader bufferedReader =
				new BufferedReader (
					new InputStreamReader (
						inputStream,
						"utf-8"));

			String line;

			while ((line = bufferedReader.readLine ()) != null)
				logger.debug (line);

			bufferedReader.close ();

			return process.waitFor ();

		} catch (InterruptedException exception) {

			throw new RuntimeException (exception);

		} finally {

			autoClose (inputStream);

			try {
				process.waitFor ();
			} catch (Exception exception) { }

		}

	}

	public static
	byte[] runFilter (
			Logger logger,
			byte[] data,
			String inExt,
			String outExt,
			String... command) {

		return runFilterAdvanced (
			logger,
			data,
			inExt,
			outExt,
			ImmutableList.<List<String>>of (
				ImmutableList.<String>copyOf (
					command)));
	}

	public static
	byte[] runFilterAdvanced (
			Logger logger,
			byte[] data,
			String inExt,
			String outExt,
			List<List<String>> commands) {

		File inFile = null;
		File outFile = null;

		try {

			// create the input and output files

			inFile =
				createTempFile (
					data,
					inExt);

			outFile =
				File.createTempFile (
					"wbs-",
					outExt);

			// stick the filenames into the command

			for (
				List<String> command
					: commands
			) {

				String[] newCommand =
					new String [command.size ()];

				for (
					int index = 0;
					index < command.size ();
					index ++
				) {

					if (
						equal (
							command.get (index),
							"<in>")
					) {

						newCommand [index] =
							inFile.getPath ();

					} else if (
						equal (
							command.get (index),
							"<out>")
					) {

						newCommand [index] =
							outFile.getPath ();

					} else {

						newCommand [index] =
							command.get (index);

					}

				}

				// run the command

				int status =
					runCommand (
						logger,
						newCommand);

				if (status != 0)
					throw new RuntimeException ("Command returned " + status);

			}

			// read the output file

			return FileUtils.readFileToByteArray (outFile);

		} catch (Exception exception) {

			throw new RuntimeException (exception);

		} finally {

			autoClose (
				inFile,
				outFile);

		}

	}

	public static
	byte[] fromHex (
			@NonNull String hex) {

		byte[] bytes =
			new byte [
				hex.length () / 2];

		for (
			int index = 0;
			index < bytes.length;
			index ++
		) {

			bytes [index] = (byte)
				Integer.parseInt (
					hex.substring (
						2 * index,
						2 * index + 2),
					16);

		}

		return bytes;

	}

	static final
	byte[] HEX_CHAR_TABLE = {
		(byte) '0', (byte) '1', (byte) '2', (byte) '3',
		(byte) '4', (byte) '5', (byte) '6', (byte) '7',
		(byte) '8', (byte) '9', (byte) 'a', (byte) 'b',
		(byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f'
	};

	public static
	String toHex (
			@NonNull byte[] byteValues) {

		byte[] hex =
			new byte [2 * byteValues.length];

		int index = 0;

		for (byte byteValue : byteValues) {

			int intValue =
				byteValue & 0xFF;

			hex [index ++] =
				HEX_CHAR_TABLE [intValue >>> 4];

			hex [index ++] =
				HEX_CHAR_TABLE [intValue & 0xf];

		}

		return bytesToString (
			hex,
			"ASCII");

	}

	public static
	String substring (
			@NonNull Object object,
			int start,
			int end) {

		String string =
			object.toString ();

		if (start < 0) start = 0;

		if (end > string.length ())
			end = string.length ();

		return string.substring (start, end);

	}

	public static
	String hyphenToCamel (
			@NonNull String string) {

		return delimitedToCamel (
			string,
			"-");

	}

	public static
	String underscoreToCamel (
			@NonNull String string) {

		return delimitedToCamel (
			string,
			"_");

	}

	public static
	String replaceAll (
			@NonNull String source,
			@NonNull String find,
			@NonNull String replaceWith) {

		return source.replaceAll (
			Pattern.quote (
				find),
			replaceWith);

	}

	public static
	String underscoreToHyphen (
			@NonNull String string) {

		return replaceAll (
			string,
			"_",
			"-");

	}

	public static
	String hyphenToUnderscore (
			@NonNull String string) {

		return replaceAll (
			string,
			"-",
			"_");

	}

	public static
	String delimitedToCamel (
			@NonNull String string,
			@NonNull String delimiter) {

		String[] parts =
			string.split (delimiter);

		StringBuilder ret =
			new StringBuilder (parts [0]);

		for (
			int index = 1;
			index < parts.length;
			index ++
		) {

			ret.append (
				Character.toUpperCase (
					parts [index].charAt (0)));

			ret.append (
				parts [index].substring (1));

		}

		return ret.toString ();

	}

	public static
	String camelToUnderscore (
			String string) {

		return camelToDelimited (
			string,
			"_");

	}

	public static
	String camelToHyphen (
			String string) {

		return camelToDelimited (
			string,
			"-");

	}

	public static
	String camelToSpaces (
			String string) {

		return camelToDelimited (
			string,
			" ");

	}

	public static
	String camelToDelimited (
			String string,
			String delimiter) {

		if (string == null)
			return null;

		StringBuilder stringBuilder =
			new StringBuilder (
				string.length () * 2);

		stringBuilder.append (
			Character.toLowerCase (
				string.charAt (0)));

		for (
			int pos = 1;
			pos < string.length ();
			pos ++
		) {

			char ch =
				string.charAt (pos);

			if (Character.isUpperCase (ch)) {

				stringBuilder.append (
					delimiter);

				stringBuilder.append (
					Character.toLowerCase (ch));

			} else {

				stringBuilder.append (ch);

			}

		}

		return stringBuilder.toString ();

	}

	private static final
	char[] lowercaseLetters =
		new char [26];

	private static final
	char[] digits =
		new char [10];

	static {

		for (
			int index = 0;
			index < 26;
			index ++
		) {

			lowercaseLetters [index] =
				(char) ('a' + index);

		}

		for (
			int index = 0;
			index < 10;
			index ++
		) {

			digits [index] =
				(char) ('0' + index);

		}

	}

	public static
	String joinWithSeparator (
			@NonNull String separator,
			@NonNull String prefix,
			@NonNull Iterable<String> parts,
			@NonNull String suffix) {

		StringBuilder stringBuilder =
			new StringBuilder ();

		boolean first = true;

		for (
			String part
				: parts
		) {

			if (first) {

				first = false;

			} else {

				stringBuilder.append (
					separator);

			}

			stringBuilder.append (
				prefix);

			stringBuilder.append (
				part);

			stringBuilder.append (
				suffix);

		}

		return stringBuilder.toString ();

	}

	public static
	String joinWithoutSeparator (
			Iterable<String> parts) {

		return joinWithSeparator (
			"",
			"",
			parts,
			"");

	}

	public static
	String joinWithSeparator (
			String separator,
			Iterable<String> parts) {

		return joinWithSeparator (
			separator,
			"",
			parts,
			"");

	}

	public static
	String joinWithSeparator (
			@NonNull String separator,
			@NonNull String... parts) {

		return joinWithSeparator (
			separator,
			"",
			Arrays.asList (parts),
			"");

	}

	public static
	String joinWithoutSeparator (
			@NonNull String... parts) {

		return joinWithSeparator (
			"",
			"",
			Arrays.asList (parts),
			"");

	}

	public static
	String joinWithSpace (
			@NonNull Iterable<String> parts) {

		return joinWithSeparator (
			" ",
			"",
			parts,
			"");

	}

	public static
	String joinWithSpace (
			String... parts) {

		return joinWithSeparator (
			" ",
			"",
			Arrays.asList (
				parts),
			"");

	}

	public static
	String joinWithFullStop (
			@NonNull Iterable<String> parts) {

		return joinWithSeparator (
			".",
			"",
			parts,
			"");

	}

	public static
	String joinWithFullStop (
			@NonNull String... parts) {

		return joinWithSeparator (
			".",
			"",
			Arrays.asList (
				parts),
			"");

	}

	public static
	String joinWithSlash (
			@NonNull Iterable<String> parts) {

		return joinWithSeparator (
			"/",
			"",
			parts,
			"");

	}

	public static
	String joinWithSlash (
			@NonNull String... parts) {

		return joinWithSeparator (
			"/",
			"",
			Arrays.asList (
				parts),
			"");

	}

	public static
	String joinWithPipe (
			@NonNull List<String> parts) {

		return joinWithSeparator (
			"|",
			"",
			parts,
			"");

	}

	public static
	String joinWithPipe (
			@NonNull String... parts) {

		return joinWithSeparator (
			"|",
			"",
			Arrays.asList (
				parts),
			"");

	}

	public static
	String prettySize (
			int bytes) {

		int limit = 2;

		if (bytes < limit * 1024)
			return "" + bytes + " bytes";

		if (bytes < limit * 1024 * 1024)
			return "" + (bytes / 1024) + " kilobytes";

		if (bytes < limit * 1024 * 1024 * 1024)
			return "" + (bytes / 1024 / 1024) + " megabytes";

		return "" + (bytes / 1024 / 1024 / 1024) + " gigabytes";

	}

	public static
	RuntimeException todo () {
		return new RuntimeException ("TODO");
	}

	@SneakyThrows (NoSuchAlgorithmException.class)
	public static
	String hashSha1 (
			String string) {

		MessageDigest messageDigest =
			MessageDigest.getInstance ("SHA-1");

		messageDigest.update (
			string.getBytes ());

		return Base64.encodeBase64String (
			messageDigest.digest ());

	}

	public static
	String spacify (
			String input) {

		return spacify (
			input,
			32);

	}

	private final static
	Pattern nonWhitespaceWordsPattern =
		Pattern.compile ("\\S+");

	/**
	 * Returns a transformed version of a string, with all whitespace replaced
	 * by a single space and all words longer than wordLength split into
	 * wordLength or less.
	 *
	 * @param input
	 *            the input string
	 * @param wordLength
	 *            the maximum word length in the output string
	 * @return the transformed string
	 */
	public static
	String spacify (
			String input,
			int wordLength) {

		StringBuilder stringBuilder =
			new StringBuilder ();

		Matcher matcher =
			nonWhitespaceWordsPattern.matcher (input);

		while (matcher.find ()) {

			String string =
				matcher.group (0);

			int position = 0;

			int length =
				string.length ();

			while (true) {

				if (stringBuilder.length () > 0)
					stringBuilder.append (' ');

				if (position + wordLength > length) {

					stringBuilder.append (
						string,
						position,
						length);

					break;

				}

				stringBuilder.append (
					string,
					position,
					position + wordLength);

				position += wordLength;

			}

		}

		return stringBuilder.toString ();

	}

	public static
	Instant toInstant (
			@NonNull ReadableInstant readableInstant) {

		return readableInstant.toInstant ();

	}

	public static
	Instant toInstantNullSafe (
			ReadableInstant readableInstant) {

		return readableInstant != null
			? readableInstant.toInstant ()
			: null;

	}

	public static
	Instant dateToInstantNullSafe (
			Date date) {

		if (date == null)
			return null;

		return new Instant (
			date);

	}

	public static
	Date instantToDateNullSafe (
			ReadableInstant instant) {

		if (instant == null)
			return null;

		return instant.toInstant ().toDate ();

	}

	public static
	Instant millisToInstant (
			long millis) {

		return new Instant (
			millis);

	}

	public static
	Instant secondsToInstant (
			long seconds) {

		return new Instant (
			seconds * 1000);

	}

	public static
	Timestamp toSqlTimestamp (
			@NonNull ReadableInstant instant) {

		return new Timestamp (
			instant.getMillis ());

	}

	public static
	Instant toInstant (
			@NonNull Timestamp timestamp) {

		return millisToInstant (
			timestamp.getTime ());

	}

	public static
	String booleanToString (
			Boolean value,
			String yesString,
			String noString,
			String nullString) {

		if (value == null)
			return nullString;

		if (value == true)
			return yesString;

		if (value == false)
			return noString;

		throw new RuntimeException ();

	}

	public static
	Boolean stringToBoolean (
			@NonNull String string,
			@NonNull String yesString,
			@NonNull String noString,
			@NonNull String nullString) {

		if (equal (
				string,
				yesString))
			return true;

		if (equal (
				string,
				noString))
			return false;

		if (
			equal (
				string,
				nullString)
		) {
			return null;
		}

		throw new RuntimeException (
			stringFormat (
				"Invalid boolean string: \"%s\"",
				string));

	}

	public static
	List<String> split (
			String source,
			String regex) {

		return Arrays.asList (
			source.split (regex));

	}

	public static
	void doNothing () {

	}

	public static <Type>
	List<Type> maybeList (
			List<Type> value) {

		return value != null
			? value
			: Collections.<Type>emptyList ();

	}

	public static <Type>
	List<Type> maybeList (
			boolean condition,
			Type value) {

		return condition

			? Collections.<Type>singletonList (
				value)

			: Collections.<Type>emptyList ();

	}

	public static
	boolean isNull (
			Object object) {

		return object == null;

	}

	public static
	boolean isNotNull (
			Object object) {

		return object != null;

	}

	public static
	boolean lessThan (
			int left,
			int right) {

		return left < right;

	}

	public static
	boolean lessThan (
			long left,
			long right) {

		return left < right;

	}

	public static
	boolean notLessThan (
			int left,
			int right) {

		return left >= right;

	}

	public static
	boolean notLessThan (
			long left,
			long right) {

		return left >= right;

	}

	public static
	boolean moreThan (
			int left,
			int right) {

		return left > right;

	}

	public static
	boolean moreThan (
			long left,
			long right) {

		return left > right;

	}

	public static
	boolean isZero (
			int value) {

		return value == 0;

	}

	public static
	boolean notLessThanZero (
			int value) {

		return value >= 0;

	}

	public static
	boolean doesNotStartWith (
			String string,
			String prefix) {

		return ! string.startsWith (prefix);

	}

	public static
	int sum (
			int value0,
			int value1) {

		return value0 + value1;

	}

	public static
	long sum (
			long value0,
			long value1) {

		return value0 + value1;

	}

	public static
	boolean allOf (
			boolean... values) {

		for (boolean value : values) {

			if (! value)
				return false;

		}

		return true;

	}

	public static
	boolean anyOf (
			boolean... values) {

		for (boolean value : values) {

			if (value)
				return true;

		}

		return false;

	}

	public static
	boolean not (
			boolean value) {

		return ! value;

	}

	public static
	boolean allFalse (
			boolean... values) {

		for (boolean value : values) {

			if (value)
				return false;

		}

		return true;

	}

	public static
	boolean isEmpty (
			Collection<?> collection) {

		return collection.isEmpty ();

	}

	public static
	boolean isEmpty (
			String string) {

		return string.isEmpty ();

	}

	public static
	boolean isNotEmpty (
			Collection<?> collection) {

		return ! collection.isEmpty ();

	}

	public static
	boolean isNotEmpty (
			Map<?,?> collection) {

		return ! collection.isEmpty ();

	}

	public static
	boolean isNotEmpty (
			String string) {

		if (string == null)
			return false;

		return ! string.isEmpty ();

	}

	public static
	URL stringToUrl (
			String urlString) {

		try {

			return new URL (
				urlString);

		} catch (MalformedURLException exception) {

			throw new RuntimeException (
				exception);

		}

	}

	public static
	Instant earliest (
			Instant... instants) {

		Instant earliest =
			null;

		for (
			Instant instant
				: instants
		) {

			if (

				earliest == null

				|| instant.isBefore (
					earliest)

			) {

				earliest =
					instant;

			}

		}

		return earliest;

	}

	public static
	boolean isPresent (
			@NonNull Optional<?> optional) {

		return optional.isPresent ();

	}

	public static
	boolean isNotPresent (
			@NonNull Optional<?> optional) {

		return ! optional.isPresent ();

	}

	public static <Type>
	Type optionalRequired (
			@NonNull Optional<Type> optional) {

		return optional.get ();

	}

	public static <Type>
	Type optionalOrNull (
			@NonNull Optional<Type> optional) {

		return optional.orNull ();

	}

	public static <Type>
	Optional<Type> requiredOptional (
			@NonNull Optional<Type> optional) {

		if (! optional.isPresent ()) {
			throw new RuntimeException ();
		}

		return optional;

	}

	public static
	boolean doesNotImplement (
			@NonNull Class<?> subjectClass,
			@NonNull Class<?> implementedClass) {

		return ! implementedClass.isAssignableFrom (
			subjectClass);

	}

	public static <Type>
	boolean contains (
			@NonNull Collection<Type> collection,
			@NonNull Type value) {

		return collection.contains (
			value);

	}

	public static <Type>
	boolean contains (
			@NonNull Map<Type,?> map,
			@NonNull Type value) {

		return map.keySet ().contains (
			value);

	}

	public static <Type>
	boolean doesNotContain (
			@NonNull Collection<Type> collection,
			@NonNull Type value) {

		return ! collection.contains (
			value);

	}

	public static <Type>
	Optional<Integer> indexOf (
			@NonNull List<Type> list,
			@NonNull Type value) {

		int index =
			list.indexOf (
				value);

		if (index < 0) {

			return Optional.<Integer>absent ();

		} else {

			return Optional.of (
				index);

		}

	}

	public static <Type>
	int indexOfRequired (
			@NonNull List<Type> list,
			@NonNull Type value) {

		int index =
			list.indexOf (
				value);

		if (index < 0) {

			throw new IllegalArgumentException ();

		} else {

			return index;

		}

	}

	public static
	boolean earlierThan (
			@NonNull Instant left,
			@NonNull Instant right) {

		return left.isBefore (
			right);

	}

	public static
	boolean laterThan (
			@NonNull Instant left,
			@NonNull Instant right) {

		return left.isAfter (
			right);

	}

	public static
	Class<?> classForNameRequired (
			@NonNull String className) {

		try {

			return Class.forName (
				className);

		} catch (ClassNotFoundException exception) {

			throw new RuntimeException (
				exception);

		}

	}

	public static
	Optional<Class<?>> classForName (
			@NonNull String className) {

		try {

			return Optional.<Class<?>>of (
				Class.forName (
					className));

		} catch (ClassNotFoundException exception) {

			return Optional.<Class<?>>absent ();

		}

	}

	public static <Type>
	Type orNull (
			@NonNull Optional<Type> optional) {

		return optional.orNull ();

	}

	public static
	String fullClassName (
			@NonNull Class<?> theClass) {

		return theClass.getName ();

	}

	public static
	Optional<String> fullClassName (
			@NonNull Optional<Class<?>> theClass) {

		return theClass.isPresent ()
			? Optional.<String>of (
				theClass.get ().getName ())
			: Optional.<String>absent ();

	}

	public static <Type>
	Iterable<Type> presentInstances (
			@NonNull Iterable<Optional<Type>> collection) {

		return Optional.presentInstances (
			collection);

	}

	public static <Type>
	Iterable<Type> presentInstances () {

		return ImmutableList.<Type>of ();

	}

	public static <Type>
	Iterable<Type> presentInstances (
			@NonNull Optional<Type> argument) {

		return presentInstances (
			ImmutableList.<Optional<Type>>of (
				argument));

	}

	public static <Type>
	Iterable<Type> presentInstances (
			@NonNull Optional<Type> argument0,
			@NonNull Optional<Type> argument1) {

		return presentInstances (
			ImmutableList.<Optional<Type>>of (
				argument0,
				argument1));

	}

	public static <Type>
	Iterable<Type> presentInstances (
			@NonNull Optional<Type> argument0,
			@NonNull Optional<Type> argument1,
			@NonNull Optional<Type> argument2) {

		return presentInstances (
			ImmutableList.<Optional<Type>>of (
				argument0,
				argument1,
				argument2));

	}

	public static <Type>
	Iterable<Type> presentInstances (
			@NonNull Optional<Type> argument0,
			@NonNull Optional<Type> argument1,
			@NonNull Optional<Type> argument2,
			@NonNull Optional<Type> argument3) {

		return presentInstances (
			ImmutableList.<Optional<Type>>of (
				argument0,
				argument1,
				argument2,
				argument3));

	}

	@SafeVarargs
	public static <Type>
	Iterable<Type> presentInstances (
			@NonNull Optional<Type>... arguments) {

		return Optional.presentInstances (
			Arrays.asList (
				arguments));

	}

	public static <Type>
	Optional<Type> optionalIf (
			@NonNull Boolean present,
			@NonNull Type value) {

		return present
			? Optional.<Type>of (
				value)
			: Optional.<Type>absent ();

	}

	public static <Type>
	Type requiredValue (
			@NonNull Type value) {

		return value;

	}

	public static
	String trim (
			@NonNull String source) {

		return source.trim ();

	}

	public static
	boolean moreThanZero (
			int value) {

		return value > 0;

	}

	public static
	boolean moreThanZero (
			long value) {

		return value > 0;

	}

	public static <T>
	T optionalOr (
			Optional<T> optional,
			T instead) {

		return optional.or (
			instead);

	}

	public static
	Method getMethodRequired (
			@NonNull Class<?> objectClass,
			@NonNull String name,
			@NonNull List<Class<?>> parameterTypes) {

		try {

			return objectClass.getMethod (
				name,
				parameterTypes.toArray (
					new Class<?> [0]));

		} catch (NoSuchMethodException exception) {

			throw new RuntimeException (
				exception);

		}

	}

	public static
	Method getDeclaredMethodRequired (
			@NonNull Class<?> objectClass,
			@NonNull String name,
			@NonNull List<Class<?>> parameterTypes) {

		try {

			return objectClass.getDeclaredMethod (
				name,
				parameterTypes.toArray (
					new Class<?> [0]));

		} catch (NoSuchMethodException exception) {

			throw new RuntimeException (
				exception);

		}

	}

	public static
	Object methodInvoke (
			@NonNull Method method,
			@NonNull Object target,
			@NonNull List<Object> arguments) {

		try {

			return method.invoke (
				target,
				arguments.toArray ());

		} catch (InvocationTargetException exception) {

			Throwable targetException =
				exception.getTargetException ();

			if (targetException instanceof RuntimeException) {

				throw (RuntimeException)
					targetException;

			} else {

				throw new RuntimeException (
					targetException);

			}

		} catch (IllegalAccessException exception) {

			throw new RuntimeException (
				exception);

		}

	}

	public static <Type>
	Type eitherGetLeft (
			@NonNull Either<Type,?> either) {

		return either.left ().value ();

	}

	public static <Type>
	Type getValue (
			@NonNull Either<Type,?> either) {

		return either.left ().value ();

	}

	public static <Type>
	Type eitherGetRight (
			@NonNull Either<?,Type> either) {

		return either.right ().value ();

	}

	public static <Type>
	Type getError (
			@NonNull Either<?,Type> either) {

		return either.right ().value ();

	}

	public static
	boolean isLeft (
			@NonNull Either<?,?> either) {

		return either.isLeft ();

	}

	public static
	boolean isRight (
			@NonNull Either<?,?> either) {

		return either.isRight ();

	}

	public static <LeftType,RightType>
	Either<LeftType,RightType> successResult (
			@NonNull LeftType left) {

		return Either.<LeftType,RightType>left (
			left);

	}

	public static <LeftType,RightType>
	Either<LeftType,RightType> errorResult (
			@NonNull RightType right) {

		return Either.<LeftType,RightType>right (
			right);

	}

	public static
	boolean isError (
			@NonNull Either<?,?> either) {

		return either.isRight ();

	}

	public static
	boolean isSuccess (
			@NonNull Either<?,?> either) {

		return either.isLeft ();

	}

	public static <Left,Right>
	Left requiredSuccess (
			@NonNull Either<Left,Right> either) {

		return either.left ().value ();

	}

	public static <Left,Right>
	Right requiredError (
			@NonNull Either<Left,Right> either) {

		return either.right ().value ();

	}

	public static
	boolean isInstanceOf (
			@NonNull Class<?> theClass,
			@NonNull Object object) {

		return theClass.isInstance (
			object);

	}

	public static
	boolean isNotInstanceOf (
			@NonNull Class<?> theClass,
			@NonNull Object object) {

		return ! theClass.isInstance (
			object);

	}

	public static
	Integer toIntegerNullSafe (
			Long longValue) {

		return longValue == null
			? null
			: (int) (long) longValue;

	}

	@SafeVarargs
	public static <Type>
	Type ifNotPresent (
			@NonNull Optional<Type>... optionalValues) {

		for (
			Optional<Type> optionalValue
				: optionalValues
		) {

			if (
				isPresent (
					optionalValue)
			) {

				return optionalValue.get ();

			}

		}

		throw new IllegalArgumentException ();

	}

	public static <Type>
	Type ifNotPresent (
			@NonNull Optional<? extends Type> optionalValueOne) {

		if (
			isPresent (
				optionalValueOne)
		) {

			return optionalValueOne.get ();

		}

		throw new IllegalArgumentException ();

	}

	public static <Type>
	Type ifNotPresent (
			@NonNull Optional<? extends Type> optionalValueOne,
			@NonNull Optional<? extends Type> optionalValueTwo) {

		if (
			isPresent (
				optionalValueOne)
		) {

			return optionalValueOne.get ();

		}

		if (
			isPresent (
				optionalValueTwo)
		) {

			return optionalValueTwo.get ();

		}

		throw new IllegalArgumentException ();

	}

	public static <Type>
	Type ifNotPresent (
			@NonNull Optional<? extends Type> optionalValueOne,
			@NonNull Optional<? extends Type> optionalValueTwo,
			@NonNull Optional<? extends Type> optionalValueThree) {

		if (
			isPresent (
				optionalValueOne)
		) {

			return optionalValueOne.get ();

		}

		if (
			isPresent (
				optionalValueTwo)
		) {

			return optionalValueTwo.get ();

		}

		if (
			isPresent (
				optionalValueThree)
		) {

			return optionalValueThree.get ();

		}

		throw new IllegalArgumentException ();

	}

	public static <Type>
	Type cast (
			@NonNull Class<Type> classToCastTo,
			@NonNull Object value) {

		return classToCastTo.cast (
			value);

	}

	public static <Type>
	Optional<Type> optionalCast (
			@NonNull Class<Type> classToCastTo,
			@NonNull Optional<?> optionalValue) {

		if (
			isPresent (
				optionalValue)
		) {

			if (
				isInstanceOf (
					classToCastTo,
					optionalValue.get ())
			) {

				return Optional.of (
					cast (
						classToCastTo,
						optionalValue.get ()));

			} else {

				throw new ClassCastException ();

			}

		} else {

			return Optional.<Type>absent ();

		}

	}

	public static <Type>
	Optional<Type> optionalMap (
			@NonNull Optional<Type> optionalValue,
			@NonNull Function<? super Type,? extends Type> mappingFunction) {

		if (
			isPresent (
				optionalValue)
		) {

			return Optional.of (
				mappingFunction.apply (
					optionalValue.get ()));

		} else {

			return Optional.absent ();

		}

	}

	public static
	boolean isNotStatic (
			@NonNull Method method) {

		return ! Modifier.isStatic (
			method.getModifiers ());

	}

	public static
	Method getStaticMethodRequired (
			@NonNull Class<?> containingClass,
			@NonNull String methodName,
			@NonNull List<Class<?>> parameterTypes) {

		Method method =
			getDeclaredMethodRequired (
				containingClass,
				methodName,
				parameterTypes);

		if (
			isNotStatic (
				method)
		) {
			throw new RuntimeException ();
		}

		return method;

	}

	public static
	LocalDate localDate (
			@NonNull ReadableInstant instant,
			@NonNull DateTimeZone timezone) {

		return instant

			.toInstant ()

			.toDateTime (
				timezone)

			.toLocalDate ();

	}

	public static <Key,Value>
	Map.Entry<Key,Value> mapEntry (
			@NonNull Key key,
			@NonNull Value value) {

		return new AbstractMap.SimpleEntry<Key,Value> (
			key,
			value);

	}

}

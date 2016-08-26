/*
 * Copyright © 2016 <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.jaffirm.core;

import com.io7m.junreachable.UnreachableCodeException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.DoubleFunction;
import java.util.function.DoublePredicate;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.LongFunction;
import java.util.function.LongPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Functions to check postconditions.
 */

public final class Postconditions
{
  private Postconditions()
  {
    throw new UnreachableCodeException();
  }

  /**
   * <p>Evaluate all of the given {@code conditions} using {@code value} as
   * input.</p>
   *
   * <p>All of the conditions are evaluated and the function throws {@link
   * PostconditionViolationException} if any of the conditions are false, or
   * raise an exception that is not of type {@link Error}. Exceptions of type
   * {@link Error} are propagated immediately, without any further contract
   * checking.</p>
   *
   * @param value      The value
   * @param conditions The set of conditions
   * @param <T>        The type of values
   *
   * @return value
   *
   * @throws PostconditionViolationException If any of the conditions are false
   */

  @SafeVarargs
  public static <T> T checkPostconditions(
    final T value,
    final ContractConditionType<T>... conditions)
    throws PostconditionViolationException
  {
    final Violations violations = Postconditions.checkAll(value, conditions);
    if (violations != null) {
      throw Postconditions.postconditionsFailed(value, violations);
    }
    return value;
  }

  /**
   * An {@code int} specialized version of {@link #checkPostconditions(Object,
   * ContractConditionType[])}
   *
   * @param value      The value
   * @param conditions The conditions the value must obey
   *
   * @return value
   *
   * @throws PostconditionViolationException If any of the conditions are false
   */

  public static int checkPostconditionsI(
    final int value,
    final ContractIntConditionType... conditions)
    throws PostconditionViolationException
  {
    final Violations violations =
      Postconditions.checkAllInt(value, conditions);
    if (violations != null) {
      throw Postconditions.postconditionsFailed(
        Integer.valueOf(value),
        violations);
    }
    return value;
  }

  /**
   * A {@code long} specialized version of {@link #checkPostconditions(Object,
   * ContractConditionType[])}
   *
   * @param value      The value
   * @param conditions The conditions the value must obey
   *
   * @return value
   *
   * @throws PostconditionViolationException If any of the conditions are false
   */

  public static long checkPostconditionsL(
    final long value,
    final ContractLongConditionType... conditions)
    throws PostconditionViolationException
  {
    final Violations violations =
      Postconditions.checkAllLong(value, conditions);
    if (violations != null) {
      throw Postconditions.postconditionsFailed(
        Long.valueOf(value),
        violations);
    }
    return value;
  }

  /**
   * A {@code double} specialized version of {@link #checkPostconditions(Object,
   * ContractConditionType[])}
   *
   * @param value      The value
   * @param conditions The conditions the value must obey
   *
   * @return value
   *
   * @throws PostconditionViolationException If any of the conditions are false
   */

  public static double checkPostconditionsD(
    final double value,
    final ContractDoubleConditionType... conditions)
    throws PostconditionViolationException
  {
    final Violations violations =
      Postconditions.checkAllDouble(value, conditions);
    if (violations != null) {
      throw Postconditions.postconditionsFailed(
        Double.valueOf(value),
        violations);
    }
    return value;
  }

  /**
   * <p>Evaluate the given {@code predicate} using {@code value} as input.</p>
   *
   * <p>The function throws {@link PostconditionViolationException} if the
   * predicate is false.</p>
   *
   * @param value     The value
   * @param condition The predicate
   * @param <T>       The type of values
   *
   * @return value
   *
   * @throws PostconditionViolationException If the predicate is false
   */

  public static <T> T checkPostcondition(
    final T value,
    final ContractConditionType<T> condition)
    throws PostconditionViolationException
  {
    return Postconditions.checkPostcondition(
      value, condition.predicate(), condition.describer());
  }

  /**
   * <p>Evaluate the given {@code predicate} using {@code value} as input.</p>
   *
   * <p>The function throws {@link PostconditionViolationException} if the
   * predicate is false.</p>
   *
   * @param value     The value
   * @param predicate The predicate
   * @param describer A describer for the predicate
   * @param <T>       The type of values
   *
   * @return value
   *
   * @throws PostconditionViolationException If the predicate is false
   */

  public static <T> T checkPostcondition(
    final T value,
    final Predicate<T> predicate,
    final Function<T, String> describer)
  {
    final boolean ok;
    try {
      ok = predicate.test(value);
    } catch (final Throwable e) {
      throw Postconditions.postconditionsFailed(
        value, Violations.one(Postconditions.failedPredicate(e)));
    }

    return Postconditions.checkPostcondition(value, ok, describer);
  }

  /**
   * <p>Evaluate the given {@code predicate} using {@code value} as input.</p>
   *
   * <p>The function throws {@link PostconditionViolationException} if the
   * predicate is false.</p>
   *
   * @param value     The value
   * @param condition The predicate
   * @param describer A describer for the predicate
   * @param <T>       The type of values
   *
   * @return value
   *
   * @throws PostconditionViolationException If the predicate is false
   */

  public static <T> T checkPostcondition(
    final T value,
    final boolean condition,
    final Function<T, String> describer)
  {
    if (!condition) {
      throw Postconditions.postconditionsFailed(value, Violations.one(
        Postconditions.applyDescriberChecked(value, describer)));
    }
    return value;
  }

  /**
   * A specialized version of {@link #checkPostcondition(Object, boolean,
   * Function)} that does not mention an input value.
   *
   * @param condition The predicate
   * @param message   The predicate description
   *
   * @throws PostconditionViolationException Iff {@code predicate == false}
   */

  public static void checkPostcondition(
    final boolean condition,
    final String message)
    throws PostconditionViolationException
  {
    if (!condition) {
      throw Postconditions.postconditionsFailed(
        "<unspecified>", Violations.one(message));
    }
  }

  /**
   * A specialized version of {@link #checkPostcondition(Object, boolean,
   * Function)} that does not mention an input value.
   *
   * @param condition The predicate
   * @param message   The predicate description supplier
   *
   * @throws PostconditionViolationException Iff {@code predicate == false}
   */

  public static void checkPostcondition(
    final boolean condition,
    final Supplier<String> message)
    throws PostconditionViolationException
  {
    if (!condition) {
      throw Postconditions.postconditionsFailed(
        "<unspecified>",
        Violations.one(Postconditions.applySupplierChecked(message)));
    }
  }

  /**
   * An {@code int} specialized version of {@link #checkPostcondition(Object,
   * ContractConditionType)}.
   *
   * @param value     The value
   * @param condition The predicate
   *
   * @return value
   *
   * @throws PostconditionViolationException If the predicate is false
   */

  public static int checkPostconditionI(
    final int value,
    final ContractIntConditionType condition)
    throws PostconditionViolationException
  {
    return Postconditions.checkPostconditionI(
      value, condition.predicate(), condition.describer());
  }

  /**
   * An {@code int} specialized version of {@link #checkPostcondition(Object,
   * ContractConditionType)}.
   *
   * @param value     The value
   * @param predicate The predicate
   * @param describer The describer for the predicate
   *
   * @return value
   *
   * @throws PostconditionViolationException If the predicate is false
   */

  public static int checkPostconditionI(
    final int value,
    final IntPredicate predicate,
    final IntFunction<String> describer)
  {
    final boolean ok;
    try {
      ok = predicate.test(value);
    } catch (final Throwable e) {
      throw Postconditions.postconditionsFailed(
        Integer.valueOf(value),
        Violations.one(Postconditions.failedPredicate(e)));
    }

    return Postconditions.checkPostconditionI(value, ok, describer);
  }

  /**
   * An {@code int} specialized version of {@link #checkPostcondition(Object,
   * boolean, Function)}.
   *
   * @param value     The value
   * @param condition The predicate
   * @param describer The describer for the predicate
   *
   * @return value
   *
   * @throws PostconditionViolationException If the predicate is false
   */

  public static int checkPostconditionI(
    final int value,
    final boolean condition,
    final IntFunction<String> describer)
  {
    if (!condition) {
      throw Postconditions.postconditionsFailed(
        Integer.valueOf(value),
        Violations.one(Postconditions.applyDescriberIChecked(
          value,
          describer)));
    }
    return value;
  }

  /**
   * A {@code long} specialized version of {@link #checkPostcondition(Object,
   * ContractConditionType)}.
   *
   * @param value     The value
   * @param condition The predicate
   *
   * @return value
   *
   * @throws PostconditionViolationException If the predicate is false
   */

  public static long checkPostconditionL(
    final long value,
    final ContractLongConditionType condition)
    throws PostconditionViolationException
  {
    return Postconditions.checkPostconditionL(
      value,
      condition.predicate(),
      condition.describer());
  }

  /**
   * A {@code long} specialized version of {@link #checkPostcondition(Object,
   * Predicate, Function)}
   *
   * @param value     The value
   * @param predicate The predicate
   * @param describer The describer of the predicate
   *
   * @return value
   *
   * @throws PostconditionViolationException If the predicate is false
   */

  public static long checkPostconditionL(
    final long value,
    final LongPredicate predicate,
    final LongFunction<String> describer)
  {
    final boolean ok;
    try {
      ok = predicate.test(value);
    } catch (final Throwable e) {
      throw Postconditions.postconditionsFailed(
        Long.valueOf(value),
        Violations.one(Postconditions.failedPredicate(e)));
    }

    return Postconditions.checkPostconditionL(value, ok, describer);
  }

  /**
   * A {@code long} specialized version of {@link #checkPostcondition(Object,
   * Predicate, Function)}
   *
   * @param condition The predicate
   * @param value     The value
   * @param describer The describer of the predicate
   *
   * @return value
   *
   * @throws PostconditionViolationException If the predicate is false
   */

  public static long checkPostconditionL(
    final long value,
    final boolean condition,
    final LongFunction<String> describer)
  {
    if (!condition) {
      throw Postconditions.postconditionsFailed(
        Long.valueOf(value),
        Violations.one(Postconditions.applyDescriberLChecked(
          value,
          describer)));
    }
    return value;
  }

  /**
   * A {@code double} specialized version of {@link #checkPostcondition(Object,
   * ContractConditionType)}.
   *
   * @param value     The value
   * @param condition The predicate
   *
   * @return value
   *
   * @throws PostconditionViolationException If the predicate is false
   */

  public static double checkPostconditionD(
    final double value,
    final ContractDoubleConditionType condition)
    throws PostconditionViolationException
  {
    return Postconditions.checkPostconditionD(
      value, condition.predicate(), condition.describer());
  }

  /**
   * A {@code double} specialized version of {@link #checkPostcondition(Object,
   * Predicate, Function)}
   *
   * @param value     The value
   * @param predicate The predicate
   * @param describer The describer of the predicate
   *
   * @return value
   *
   * @throws PostconditionViolationException If the predicate is false
   */

  public static double checkPostconditionD(
    final double value,
    final DoublePredicate predicate,
    final DoubleFunction<String> describer)
  {
    final boolean ok;
    try {
      ok = predicate.test(value);
    } catch (final Throwable e) {
      throw Postconditions.postconditionsFailed(
        Double.valueOf(value),
        Violations.one(Postconditions.failedPredicate(e)));
    }

    return Postconditions.checkPostconditionD(value, ok, describer);
  }

  /**
   * A {@code double} specialized version of {@link #checkPostcondition(Object,
   * boolean, Function)}
   *
   * @param value     The value
   * @param condition The predicate
   * @param describer The describer of the predicate
   *
   * @return value
   *
   * @throws PostconditionViolationException If the predicate is false
   */

  public static double checkPostconditionD(
    final double value,
    final boolean condition,
    final DoubleFunction<String> describer)
  {
    if (!condition) {
      throw Postconditions.postconditionsFailed(
        Double.valueOf(value),
        Violations.one(Postconditions.applyDescriberDChecked(
          value,
          describer)));
    }
    return value;
  }

  private static <T> Violations checkAll(
    final T value,
    final ContractConditionType<T>[] conditions)
  {
    Violations violations = null;

    for (int index = 0; index < conditions.length; ++index) {
      final ContractConditionType<T> condition = conditions[index];
      final Predicate<T> predicate = condition.predicate();

      final boolean ok;
      final int count = conditions.length;
      try {
        ok = predicate.test(value);
      } catch (final Throwable e) {
        violations = Postconditions.maybeAllocate(violations, count);
        violations.messages[index] = Postconditions.failedPredicate(e);
        ++violations.count;
        continue;
      }

      if (!ok) {
        violations = Postconditions.maybeAllocate(violations, count);
        violations.messages[index] =
          Postconditions.applyDescriberChecked(value, condition.describer());
        ++violations.count;
      }
    }
    return violations;
  }

  private static Violations checkAllInt(
    final int value,
    final ContractIntConditionType[] conditions)
  {
    Violations violations = null;

    for (int index = 0; index < conditions.length; ++index) {
      final ContractIntConditionType condition = conditions[index];
      final IntPredicate predicate = condition.predicate();

      final boolean ok;
      final int count = conditions.length;
      try {
        ok = predicate.test(value);
      } catch (final Throwable e) {
        violations = Postconditions.maybeAllocate(violations, count);
        violations.messages[index] = Postconditions.failedPredicate(e);
        ++violations.count;
        continue;
      }

      if (!ok) {
        violations = Postconditions.maybeAllocate(violations, count);
        violations.messages[index] =
          Postconditions.applyDescriberIChecked(value, condition.describer());
        ++violations.count;
      }
    }
    return violations;
  }

  private static Violations checkAllLong(
    final long value,
    final ContractLongConditionType[] conditions)
  {
    Violations violations = null;

    for (int index = 0; index < conditions.length; ++index) {
      final ContractLongConditionType condition = conditions[index];
      final LongPredicate predicate = condition.predicate();

      final boolean ok;
      final int count = conditions.length;
      try {
        ok = predicate.test(value);
      } catch (final Throwable e) {
        violations = Postconditions.maybeAllocate(violations, count);
        violations.messages[index] = Postconditions.failedPredicate(e);
        ++violations.count;
        continue;
      }

      if (!ok) {
        violations = Postconditions.maybeAllocate(violations, count);
        violations.messages[index] =
          Postconditions.applyDescriberLChecked(value, condition.describer());
        ++violations.count;
      }
    }
    return violations;
  }

  private static Violations checkAllDouble(
    final double value,
    final ContractDoubleConditionType[] conditions)
  {
    Violations violations = null;

    for (int index = 0; index < conditions.length; ++index) {
      final ContractDoubleConditionType condition = conditions[index];
      final DoublePredicate predicate = condition.predicate();

      final boolean ok;
      final int count = conditions.length;
      try {
        ok = predicate.test(value);
      } catch (final Throwable e) {
        violations = Postconditions.maybeAllocate(violations, count);
        violations.messages[index] = Postconditions.failedPredicate(e);
        ++violations.count;
        continue;
      }

      if (!ok) {
        violations = Postconditions.maybeAllocate(violations, count);
        violations.messages[index] =
          Postconditions.applyDescriberDChecked(value, condition.describer());
        ++violations.count;
      }
    }
    return violations;
  }

  private static Violations maybeAllocate(
    final Violations violations,
    final int count)
  {
    return violations == null ? new Violations(count) : violations;
  }

  private static String applySupplierChecked(
    final Supplier<String> message)
  {
    try {
      return message.get();
    } catch (final Throwable e) {
      return Postconditions.failedDescriber(e);
    }
  }

  private static <T> String applyDescriberChecked(
    final T value,
    final Function<T, String> describer)
  {
    try {
      return describer.apply(value);
    } catch (final Throwable e) {
      return Postconditions.failedDescriber(e);
    }
  }

  private static String applyDescriberIChecked(
    final int value,
    final IntFunction<String> describer)
  {
    try {
      return describer.apply(value);
    } catch (final Throwable e) {
      return Postconditions.failedDescriber(e);
    }
  }

  private static String applyDescriberLChecked(
    final long value,
    final LongFunction<String> describer)
  {
    try {
      return describer.apply(value);
    } catch (final Throwable e) {
      return Postconditions.failedDescriber(e);
    }
  }

  private static String applyDescriberDChecked(
    final double value,
    final DoubleFunction<String> describer)
  {
    try {
      return describer.apply(value);
    } catch (final Throwable e) {
      return Postconditions.failedDescriber(e);
    }
  }

  private static <T> String failedPredicate(
    final Throwable exception)
  {
    return Postconditions.failedApply(
      exception, "Exception raised whilst evaluating predicate: ");
  }

  private static String failedDescriber(
    final Throwable exception)
  {
    return Postconditions.failedApply(
      exception, "Exception raised whilst evaluating describer: ");
  }

  private static String failedApply(
    final Throwable exception,
    final String prefix)
  {
    if (exception instanceof Error) {
      throw (Error) exception;
    }

    final StringBuilder sb = new StringBuilder(128);
    sb.append(prefix);
    sb.append(exception.getClass());
    sb.append(": ");
    sb.append(exception.getMessage());
    sb.append(System.lineSeparator());
    sb.append(System.lineSeparator());
    Postconditions.stackTraceToStringBuilder(exception, sb);
    return sb.toString();
  }

  private static void stackTraceToStringBuilder(
    final Throwable exception,
    final StringBuilder sb)
  {
    final StringWriter sw = new StringWriter();
    final PrintWriter pw = new PrintWriter(sw, true);
    exception.printStackTrace(pw);
    sb.append(sw.toString());
  }

  private static <T> PostconditionViolationException postconditionsFailed(
    final T value,
    final Violations violations)
  {
    final StringBuilder sb = new StringBuilder(128);
    sb.append("Postcondition violation.");
    sb.append(System.lineSeparator());
    sb.append("  Received: ");
    sb.append(value);
    sb.append(System.lineSeparator());
    sb.append("  Violated conditions: ");
    sb.append(System.lineSeparator());

    final String[] messages = violations.messages;
    for (int index = 0; index < messages.length; ++index) {
      if (messages[index] != null) {
        sb.append("    [");
        sb.append(index);
        sb.append("]: ");
        sb.append(messages[index]);
        sb.append(System.lineSeparator());
      }
    }

    throw new PostconditionViolationException(sb.toString(), violations.count);
  }

  private static final class Violations
  {
    private final String[] messages;
    private int count;

    private Violations(final int expected)
    {
      this.messages = new String[expected];
      this.count = 0;
    }

    private static Violations one(
      final String message)
    {
      final Violations violations = new Violations(1);
      violations.messages[0] = message;
      violations.count = 1;
      return violations;
    }
  }
}

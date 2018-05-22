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

import com.io7m.immutables.styles.ImmutablesStyleType;
import org.immutables.value.Value;

import java.util.function.LongFunction;
import java.util.function.LongPredicate;

import static org.immutables.value.Value.Immutable;

/**
 * A {@code long} specialized version of the {@link ContractConditionType}
 * type.
 */

@Immutable
@ImmutablesStyleType
public interface ContractLongConditionType
{
  /**
   * @return A predicate that must evaluate to {@code true} for the contract to
   * hold
   */

  @Value.Parameter(order = 0)
  LongPredicate predicate();

  /**
   * @return A function that returns a textual description of the predicate
   */

  @Value.Parameter(order = 1)
  LongFunction<String> describer();
}

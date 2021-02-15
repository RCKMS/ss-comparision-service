package org.cdsframework.rckms.compare;

import java.util.Objects;

import org.xmlunit.util.Predicate;

interface PredicateSupport<T> extends Predicate<T>
{
  default PredicateSupport<T> negate()
  {
    return (t) -> !test(t);
  }

  default PredicateSupport<T> and(PredicateSupport<? super T> other)
  {
    Objects.requireNonNull(other);
    return (t) -> test(t) && other.test(t);
  }

  static <T> PredicateSupport<T> not(PredicateSupport<? super T> target)
  {
    Objects.requireNonNull(target);
    return (PredicateSupport<T>) target.negate();
  }
}

package de.logotakt.logolyze.controller;

import java.util.Iterator;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Answer that always a new iterator of an {@link Iterable}.
 * @param <E> The type iterated over.
 */
public class IteratorAnswer<E> implements Answer<Iterator<E>> {
    private Iterable<E> iterableObj;

    /**
     * Create a new answer that always returns a new {@link Iterator}.
     * @param iterable The {@link Iterable} object.
     */
    public IteratorAnswer(final Iterable<E> iterable) {
        this.iterableObj = iterable;
    }

    @Override
    public Iterator<E> answer(final InvocationOnMock invocation) throws Throwable {
        return iterableObj.iterator();
    }

}

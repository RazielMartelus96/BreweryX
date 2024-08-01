package com.dre.brewery.model;

/**
 * Interface representing the general functionality the Factory Design Pattern. Used to allow ease of swapping out the
 * implementation of interfaces.
 * @param <T> The type parameter of the object to create using this Factory. <i>(Note, it is good practice to ensure
 *           this type parameter is only ever an interface, to ensure ease of implementation modification.)</i>
 */
public interface Factory<T> {
	/**
	 * Creates an instance of the object the factory is responsible for.
	 * @return The created instance.
	 */
	T create();
}

package org.springside.modules.utils;

import org.junit.Test;

public class IdentitiesTest {

	@Test
	public void demo() {
		System.out.println("uuid: " + Identities.uuid());
		System.out.println("uuid2:" + Identities.uuid2());
		System.out.println("randomInt:  " + Identities.randomInt());
		System.out.println("randomLong:  " + Identities.randomLong());
		System.out.println("randomBase62:" + Identities.randomBase62(7));
		System.out.println("randomNumber:" + Identities.randomNumber(7));
		
		for (int i = 0; i < 1000; i++) {
			System.out.println(System.nanoTime());
		}
	}

}

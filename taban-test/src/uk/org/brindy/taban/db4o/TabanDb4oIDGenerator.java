package uk.org.brindy.taban.db4o;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Test;

import com.db4o.ObjectContainer;

import uk.org.brindy.taban.mock.MockLogService;

public class TabanDb4oIDGenerator {

	private TabanPersistence persistence;

	@After
	public void after() throws Exception {
		persistence.deactivate();
		assertTrue(new File(TabanPersistence.class.getName() + ".yap").delete());
	}

	@Test
	public void generateIDs() throws Exception {

		persistence = new TabanPersistence() {

			@Override
			protected ObjectContainer openContainer() {
				return super.openContainer();
			}

		};
		persistence.bindOptional(new MockLogService());
		persistence.activate();

		assertEquals("1", persistence.generateID("/"));

		assertEquals("1", persistence.generateID("/countries/"));

		assertEquals("2", persistence.generateID("/"));
		assertEquals("3", persistence.generateID("/"));

		persistence.deactivate();
		persistence.activate();

		assertEquals("4", persistence.generateID("/"));

		assertEquals("2", persistence.generateID("/countries/"));
	}

}

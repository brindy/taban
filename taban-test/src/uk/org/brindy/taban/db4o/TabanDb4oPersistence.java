package uk.org.brindy.taban.db4o;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.codehaus.jackson.JsonNode;
import org.junit.After;

import uk.org.brindy.taban.Persistence;
import uk.org.brindy.taban.base.PersistenceTests;
import uk.org.brindy.taban.mock.MockLogService;

import com.db4o.ObjectContainer;

public class TabanDb4oPersistence extends PersistenceTests {

	private ObjectContainer container;
	private TabanPersistence persistence;

	@Override
	protected Persistence createPersistence() throws Exception {
		persistence = new TabanPersistence() {

			@Override
			protected ObjectContainer openContainer() {
				return container = super.openContainer();
			}

		};
		persistence.bindOptional(new MockLogService());
		persistence.activate();

		return persistence;
	}

	@After
	public void after() throws Exception {
		if (null != persistence) {
			persistence.deactivate();
		}
		persistence = null;
		container = null;
		assertTrue(new File(TabanPersistence.class.getName() + ".yap").delete());
	}

	@Override
	public void basicReadWriteContent() throws Exception {
		super.basicReadWriteContent();
		persistence.deactivate();
		persistence.activate();
		assertEquals(2, container.query(JsonNode.class).size());
	}

	@Override
	public void basicReadWriteRootDirectory() throws Exception {
		super.basicReadWriteRootDirectory();
		persistence.deactivate();
		persistence.activate();
		assertEquals(4, container.query(JsonNode.class).size());
	}

	@Override
	public void basicReadWriteRootDirectoryWithPrevious() throws Exception {
		super.basicReadWriteRootDirectoryWithPrevious();
		persistence.deactivate();
		persistence.activate();
		assertEquals(2, container.query(JsonNode.class).size());
	}

	@Override
	public void basicReadWriteSubDirectoryWithPrevious() throws Exception {
		super.basicReadWriteSubDirectoryWithPrevious();
		persistence.deactivate();
		persistence.activate();
		assertEquals(2, container.query(JsonNode.class).size());
	}

	@Override
	public void readWriteDeleteSubDirectoryWithPrevious() throws Exception {
		super.readWriteDeleteSubDirectoryWithPrevious();
		persistence.deactivate();
		persistence.activate();
		assertEquals(0, container.query(JsonNode.class).size());
	}

}

package uk.org.brindy.taban.db4o;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.log.LogService;

import uk.org.brindy.taban.IDGenerator;
import uk.org.brindy.taban.Persistence;
import uk.org.brindy.taban.TabanQuery;
import uk.org.brindy.taban.Util;
import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.EmbeddedConfiguration;
import com.db4o.query.Predicate;
import com.db4o.reflect.jdk.JdkLoader;
import com.db4o.reflect.jdk.JdkReflector;

@Component
public class TabanPersistence implements IDGenerator, Persistence {

	private LogService log = Util.nullLogService;

	private ObjectContainer container;

	@Reference(name = "LOG", optional = true)
	public void bindOptional(LogService log) {
		this.log = log;
	}

	public void unbindOptional(LogService log) {
		this.log = Util.nullLogService;
	}

	@Activate
	public void activate() throws Exception {
		container = openContainer();
	}

	@Deactivate
	public void deactivate() throws Exception {
		if (null != container) {
			container.close();
			container = null;
		}
	}

	@Override
	public String generateID(String location) {
		log.log(LogService.LOG_DEBUG, "generateID (" + location + ")");

		if (!location.endsWith("/")) {
			throw new IllegalArgumentException("");
		}

		AutoID example = new AutoID(location);
		long id = 1;
		ObjectSet results = container.queryByExample(example);
		if (results.size() > 1) {
			throw new RuntimeException("Too many auto ids for location : "
					+ location);
		} else if (0 == results.size()) {
			example.set(id);
		} else {
			example = (AutoID) results.get(0);
			id = example.next();
		}

		container.store(example);
		container.commit();

		return String.valueOf(id);
	}

	@Override
	public JsonNode read(String location, int start, int limit,
			TabanQuery... queries) {
		log.log(LogService.LOG_DEBUG, "read(" + location + ", " + start + ", "
				+ limit + "," + Arrays.asList(queries) + ")");
		ObjectSet results = query(location, queries);

		if (location.endsWith("/")) {

			if (results.size() == 0) {
				return null;
			}

			ArrayNode array = new ArrayNode(JsonNodeFactory.instance);

			if (limit == -1) {
				limit = results.size();
			}

			Set<String> children = new HashSet<String>();
			for (int i = start; i < results.size() && array.size() < limit; i++) {
				JsonStorage storage = (JsonStorage) results.get(i);
				if (storage.getParent().equals(location)) {
					array.add(storage.getLocation());
				} else {
					String child = storage.getParent().substring(
							location.length());
					if (!children.contains(child)) {
						array.add(child);
						children.add(child);
					}
				}
			}
			return array;

		} else {

			if (0 == results.size()) {
				return null;
			} else if (results.size() > 1) {
				throw new RuntimeException("Wrong number of results: "
						+ results.size());
			}
			return ((JsonStorage) results.get(0)).getNode();
		}
	}

	@Override
	public JsonNode read(String location, TabanQuery... queries) {
		log.log(LogService.LOG_DEBUG,
				"read(" + location + "," + Arrays.asList(queries) + ")");
		return read(location, 0, -1, queries);
	}

	@Override
	public JsonNode write(String location, JsonNode node) {
		if (location.endsWith("/")) {
			throw new IllegalArgumentException(
					"location must not end with / : " + location);
		}

		if (!location.startsWith("/")) {
			throw new IllegalArgumentException("location must start with / : "
					+ location);
		}

		log.log(LogService.LOG_DEBUG, "write(" + location + ", " + node + ")");

		JsonNode previousNode = read(location);

		// no point deleting unless there's something to delete
		if (null != previousNode) {
			delete(location);
		}

		JsonStorage storage = new JsonStorage(location, node);

		log.log(LogService.LOG_DEBUG, "*write : " + storage.getParent() + " : "
				+ storage.getLocation() + " : " + node);

		container.store(storage);
		container.commit();

		return previousNode;
	}

	@Override
	public JsonNode delete(String location) {
		log.log(LogService.LOG_DEBUG, "delete(" + location + ")");

		JsonStorage storage = new JsonStorage(location, null);
		ObjectSet results = container.queryByExample(storage);
		if (0 == results.size()) {
			return null;
		} else if (results.size() > 1) {
			throw new RuntimeException("Wrong number of results: "
					+ results.size());
		}

		JsonStorage previousStorage = (JsonStorage) results.get(0);
		JsonNode previousNode = previousStorage.getNode();
		container.delete(previousStorage);
		container.commit();

		return previousNode;
	}

	protected ObjectContainer openContainer() {
		EmbeddedConfiguration config = Db4oEmbedded.newConfiguration();
		config.common().reflectWith(
				new JdkReflector(getClass().getClassLoader()));

		// config.common().reflectWith(
		// new JdkReflector(new OSGiLoader(FrameworkUtil
		// .getBundle(getClass()), new ClassLoaderJdkLoader(
		// getClass().getClassLoader()))));

		config.common().objectClass(JsonStorage.class).cascadeOnDelete(true);

		config.common().objectClass(JsonNode.class).cascadeOnDelete(true);
		config.common().objectClass(JsonNode.class).cascadeOnActivate(true);

		config.common().objectClass(AutoID.class).cascadeOnActivate(true);
		config.common().objectClass(AutoID.class).cascadeOnUpdate(true);

		return Db4oEmbedded.openFile(config, TabanPersistence.class.getName()
				+ ".yap");
	}

	private ObjectSet query(final String location, final TabanQuery... queries) {

		if (location.endsWith("/")) {
			return executeDirectoryQuery(location, queries);
		} else {
			JsonStorage storage = new JsonStorage(location, null);
			return container.queryByExample(storage);
		}

	}

	private ObjectSet executeDirectoryQuery(String location,
			TabanQuery... queries) {

		Predicate predicate;

		if (null == queries || queries.length == 0) {
			predicate = new JsonStorageLocationPredicate(location);
		} else {
			predicate = new JsonStorageLocationQueryPredicate(location, queries);
		}

		return container.query(predicate);
	}

	class OSGiLoader implements JdkLoader {

		private final Bundle _bundle;
		private JdkLoader _loader;

		public OSGiLoader(Bundle bundle, JdkLoader loader) {
			_bundle = bundle;
			_loader = loader;
		}

		public Class loadClass(String className) {
			Class clazz = _loader.loadClass(className);
			if (clazz != null) {
				return clazz;
			}
			try {
				return _bundle.loadClass(className);
			} catch (ClassNotFoundException exc) {
				return null;
			}
		}

		public Object deepClone(Object context) {
			return new OSGiLoader(_bundle,
					(JdkLoader) _loader.deepClone(context));
		}

	}
}

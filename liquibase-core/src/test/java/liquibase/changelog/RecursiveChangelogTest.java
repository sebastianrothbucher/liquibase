package liquibase.changelog;

import static org.junit.Assert.assertEquals;
import liquibase.exception.SetupException;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class RecursiveChangelogTest {
	private DatabaseChangeLog changeLog;
	private ResourceAccessor resourceAccessor;
	private Runnable handleCallback;
	private int includeCount;

	@Before
	public void setUp() {
		changeLog = new DatabaseChangeLog() {

			@Override
			protected void handleChildNode(ParsedNode node,
					ResourceAccessor resourceAccessor)
					throws ParsedNodeException, SetupException {
				includeCount++;
				handleCallback.run();
			}

		};
		changeLog.setPhysicalFilePath("somepath");
		resourceAccessor = Mockito.mock(ResourceAccessor.class);
		includeCount = 0;
	}
    
	@Test
	public void testRecursiveHandling() throws SetupException, ParsedNodeException {
		final ParsedNode loadNode = new ParsedNode(null, "changeSet");
		loadNode.addChild(null, "include", "someincludepath");
		handleCallback = new Runnable() {

			@Override
			public void run() {
				try {
					changeLog.load(loadNode, resourceAccessor);
				} catch (Exception exc) {
					throw new RuntimeException(exc);
				}
			}
		};
		// call infinitely w/above - only 1 invocation (2nd prevents)
		changeLog.load(loadNode, resourceAccessor);
		assertEquals(1, includeCount);
		// again a new call - 2 times overall, and similar
		changeLog.load(loadNode, resourceAccessor);
		assertEquals(2, includeCount);
	}
	
}

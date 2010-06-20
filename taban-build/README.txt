Taban is essentially a HTTP, REST based JSON database designed for dropping in to OSGi containers.  Taban has no 'schema' so applications are free to persist any JSON structure they wish while still being able to execute intelligent queries.

Taban - Celtic/Gaelic name meaning 'Genius'.  Genius is not a description of the developers but is an expression of Taban's simplicity and flexibility.

Taban uses a modular architecture to allow alternative services to be provided:
- persistence, the default uses db4o, an OODBMS ( http://www.db4o.com/ )
- authentication, (no default implementation is supplied)
- ID generation, the default is a simple numerical ID that is automatically incremented for a given URI

Taban provides the following simple applications implemented as easily removable separate bundles:
- a generic database browser
- a sample address book application


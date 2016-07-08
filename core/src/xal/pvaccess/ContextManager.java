package xal.pvaccess;

import org.epics.pvdatabase.pva.ContextLocal;

/**
 * Singleton class that holds the pvDatabase LocalContext object and destroys it on garbage collection.
 * 
 * As every context broadcasts the same database we have to make sure that only one such context is running.
 * Otherwise, the same PV is exposed from multiple times.
 * 
 * @author <a href="mailto:blaz.kranjc@cosylab.com">Blaz Kranjc</a>
 */
public class ContextManager {

    private static class LazyContextHolder {
        private static final ContextManager INSTANCE = new ContextManager();
    }

    private static final ContextLocal CONTEXT = new ContextLocal();

    private ContextManager () {
        CONTEXT.start(false);
    }

    public static ContextManager getInstance() {
        return LazyContextHolder.INSTANCE;
    }
    
    public static void destroy() {
        CONTEXT.destroy();
    }

    @Override
    protected void finalize() throws Throwable {
        destroy();
        super.finalize();
    }

}
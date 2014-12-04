package org.apache.jackrabbit.core.query.lucene;

import org.apache.jackrabbit.core.id.NodeId;
import org.apache.jackrabbit.core.query.OnWorkspaceInconsistency;
import org.apache.jackrabbit.core.query.QueryHandlerContext;
import org.apache.jackrabbit.core.query.lucene.directory.DirectoryManager;
import org.apache.jackrabbit.core.state.ItemStateException;
import org.apache.jackrabbit.core.state.NodeState;
import org.apache.jackrabbit.core.state.PropertyState;
import org.apache.jackrabbit.spi.Path;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Similarity;
import org.apache.tika.parser.Parser;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.util.*;

/**
 * Temporary index used for reindexation only
 */
public class JahiaSecondaryIndex extends JahiaSearchIndex {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(JahiaSecondaryIndex.class);

    private JahiaSearchIndex mainIndex;

    private SpellChecker spellChecker;

    private List<DelayedIndexUpdate> delayedUpdates = Collections.synchronizedList(new ArrayList<DelayedIndexUpdate>());
    private String path;

    public JahiaSecondaryIndex(JahiaSearchIndex mainIndex) {
        this.mainIndex = mainIndex;
    }



    /**
     * Initializes this <code>QueryHandler</code>. This implementation requires
     * that a path parameter is set in the configuration. If this condition
     * is not met, a <code>IOException</code> is thrown.
     *
     * @throws IOException if an error occurs while initializing this handler.
     */
    protected void newIndexInit() throws IOException {
        QueryHandlerContext context = getContext();

        Set<NodeId> excludedIDs = new HashSet<NodeId>();
        if (context.getExcludedNodeId() != null) {
            excludedIDs.add(context.getExcludedNodeId());
        } else {
            // Avoid parsing of version storage
            if (mainIndex.isSkipVersionIndex()) {
                excludedIDs.add(new NodeId("deadbeef-face-babe-cafe-babecafebabe"));
            }
        }

        index = new MultiIndex(this, excludedIDs);
        if (index.numDocs() == 0) {
            Path rootPath;
            if (context.getExcludedNodeId() == null) {
                // this is the index for jcr:system
                rootPath = JCR_SYSTEM_PATH;
            } else {
                rootPath = ROOT_PATH;
            }
            index.createInitialIndex(context.getItemStateManager(),
                    context.getRootId(), rootPath);
        }

        log.info("Running consistency check...");
        try {
            ConsistencyCheck check = runConsistencyCheck();
            check.repair(true);
        } catch (Exception e) {
            log.warn("Failed to run consistency check on index: " + e);
        }

        // initialize spell checker
        SpellChecker spCheck = null;
        if (getSpellCheckerClass() != null) {
            try {
                Class<?> clazz = Class.forName(getSpellCheckerClass());
                Class spellCheckerClass;
                if (SpellChecker.class.isAssignableFrom(clazz)) {
                    spellCheckerClass = clazz;
                    spCheck = (SpellChecker) spellCheckerClass.newInstance();
                    spCheck.init(this);
                } else {
                    log.warn("Invalid value for spellCheckerClass, {} "
                                    + "does not implement SpellChecker interface.",
                            getSpellCheckerClass());
                }
            } catch (Exception e) {
                log.warn("Exception initializing spell checker: "
                        + getSpellCheckerClass(), e);
            }
        }

        spellChecker = spCheck;
    }

    public void replayDelayedUpdates(JahiaSearchIndex targetIndex) throws RepositoryException, IOException {
        while (!delayedUpdates.isEmpty()) {
            DelayedIndexUpdate n = delayedUpdates.remove(0);
            targetIndex.updateNodes(n.remove, n.add, true);
        }
    }

    public void addDelayedUpdated(Iterator<NodeId> remove, Iterator<NodeState> add) {
        delayedUpdates.add(new DelayedIndexUpdate(remove,add));
    }

    public List<DelayedIndexUpdate> getDelayedUpdates() {
        return delayedUpdates;
    }

    @Override
    public SpellChecker getSpellChecker() {
        return spellChecker;
    }

    @Override
    public IndexingConfiguration getIndexingConfig() {
        return mainIndex.getIndexingConfig();
    }

    @Override
    public NamespaceMappings getNamespaceMappings() {
        return mainIndex.getNamespaceMappings();
    }

    @Override
    public Analyzer getTextAnalyzer() {
        return mainIndex.getTextAnalyzer();
    }

    @Override
    public Similarity getSimilarity() {
        return mainIndex.getSimilarity();
    }

    @Override
    public DirectoryManager getDirectoryManager() {
        try {
            Class<?> clazz = Class.forName(getDirectoryManagerClass());
            if (!DirectoryManager.class.isAssignableFrom(clazz)) {
                throw new IOException(getDirectoryManagerClass() +
                        " is not a DirectoryManager implementation");
            }
            DirectoryManager df = (DirectoryManager) clazz.newInstance();
            df.init(this);
            return df;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RedoLogFactory getRedoLogFactory() {
        return mainIndex.getRedoLogFactory();
    }

    @Override
    public boolean getUseCompoundFile() {
        return mainIndex.getUseCompoundFile();
    }

    @Override
    public int getMinMergeDocs() {
        return mainIndex.getMinMergeDocs();
    }

    @Override
    public int getMaxExtractLength() {
        return mainIndex.getMaxExtractLength();
    }

    @Override
    public int getMaxFieldLength() {
        return mainIndex.getMaxFieldLength();
    }

    @Override
    public long getMaxHistoryAge() {
        return mainIndex.getMaxHistoryAge();
    }

    @Override
    public long getMaxVolatileIndexSize() {
        return mainIndex.getMaxVolatileIndexSize();
    }

    @Override
    public int getMergeFactor() {
        return mainIndex.getMergeFactor();
    }

    @Override
    public int getMaxMergeDocs() {
        return mainIndex.getMaxMergeDocs();
    }

    @Override
    public Parser getParser() {
        return mainIndex.getParser();
    }

    @Override
    public String getPath() {
        if (path == null) {
            path = mainIndex.getPath() + ".newIndex." + System.currentTimeMillis();
        }

        return path;
    }

    @Override
    public Path getRelativePath(NodeState nodeState, PropertyState propState) throws RepositoryException, ItemStateException {
        return mainIndex.getRelativePath(nodeState, propState);
    }

    @Override
    public String getSpellCheckerClass() {
        return mainIndex.getSpellCheckerClass();
    }

    @Override
    public boolean getSupportHighlighting() {
        return mainIndex.getSupportHighlighting();
    }

    @Override
    public int getTermInfosIndexDivisor() {
        return mainIndex.getTermInfosIndexDivisor();
    }

    @Override
    public int getVolatileIdleTime() {
        return mainIndex.getVolatileIdleTime();
    }

    @Override
    public String getDirectoryManagerClass() {
        return mainIndex.getDirectoryManagerClass();
    }

    @Override
    public int getCacheSize() {
        return mainIndex.getCacheSize();
    }

    @Override
    public int getBufferSize() {
        return mainIndex.getBufferSize();
    }

    @Override
    public QueryHandlerContext getContext() {
        return mainIndex.getContext();
    }

    @Override
    public OnWorkspaceInconsistency getOnWorkspaceInconsistencyHandler() {
        return OnWorkspaceInconsistency.LOG;
    }

    public static class DelayedIndexUpdate {
        Iterator<NodeId> remove;
        Iterator<NodeState> add;

        public DelayedIndexUpdate(Iterator<NodeId> remove, Iterator<NodeState> add) {
            this.remove = remove;
            this.add = add;
        }
    }


}
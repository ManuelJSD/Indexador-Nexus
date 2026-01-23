package org.nexus.indexador.services;

import org.nexus.indexador.gamedata.models.GrhData;

/**
 * Service to handle filtering and matching logic for GrhData.
 */
public class FilterService {

    private static volatile FilterService instance;

    private FilterService() {
    }

    public static FilterService getInstance() {
        if (instance == null) {
            synchronized (FilterService.class) {
                if (instance == null) {
                    instance = new FilterService();
                }
            }
        }
        return instance;
    }

    /**
     * Checks if a GrhData matches the given search query and advanced filters.
     */
    public boolean matches(GrhData grh, String query, FilterCriteria criteria) {
        // 1. Check basic query
        if (query != null && !query.isEmpty()) {
            if (!matchesQuery(grh, query)) {
                return false;
            }
        }

        // 2. Check advanced criteria
        if (criteria != null) {
            if (criteria.onlyAnimations && grh.getNumFrames() <= 1)
                return false;
            if (criteria.onlyStatics && grh.getNumFrames() > 1)
                return false;

            if (criteria.fileNum != null && grh.getFileNum() != criteria.fileNum)
                return false;
            if (criteria.width != null && grh.getTileWidth() != criteria.width)
                return false;
            if (criteria.height != null && grh.getTileHeight() != criteria.height)
                return false;
        }

        return true;
    }

    /**
     * Logic for basic text query (e.g. "123", "f:1", "w:32", "h:32")
     */
    private boolean matchesQuery(GrhData grh, String query) {
        query = query.toLowerCase().trim();

        if (query.startsWith("f:")) {
            return matchesPrefix(query, "f:", grh.getFileNum());
        } else if (query.startsWith("w:")) {
            return matchesPrefix(query, "w:", grh.getTileWidth());
        } else if (query.startsWith("h:")) {
            return matchesPrefix(query, "h:", grh.getTileHeight());
        } else {
            // Default: match by GRH ID
            try {
                return grh.getGrh() == Integer.parseInt(query);
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }

    private boolean matchesPrefix(String query, String prefix, int value) {
        try {
            int target = Integer.parseInt(query.substring(prefix.length()).trim());
            return value == target;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Criteria object for advanced filtering.
     */
    public static class FilterCriteria {
        public boolean onlyAnimations = false;
        public boolean onlyStatics = false;
        public Integer fileNum = null;
        public Integer width = null;
        public Integer height = null;

        public FilterCriteria() {
        }

        public FilterCriteria(boolean onlyAnimations, boolean onlyStatics,
                Integer fileNum, Integer width, Integer height) {
            this.onlyAnimations = onlyAnimations;
            this.onlyStatics = onlyStatics;
            this.fileNum = fileNum;
            this.width = width;
            this.height = height;
        }
    }
}

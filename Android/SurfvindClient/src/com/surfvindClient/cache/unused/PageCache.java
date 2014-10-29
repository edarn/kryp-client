package com.surfvindClient.cache.unused;


public class PageCache {

	private int maxCache;
	private int nbrOfCachedObj;

	/* counter */
	private int atElement;

	private CacheObject first;

	public static final int NEXT = 1;
	public static final int PREVIOUS = -1;

	private CacheObject getObject(int nbr) {
		if (nbr == 0) {
			return first;
		}
		return getObject(--nbr).next;
	}
	
	public PageCache(int maxCache) {
		this.maxCache = maxCache;
		nbrOfCachedObj = 0;
		atElement = 0;
	}

	public PageCache() {
		this(2);
	}

	public void cache(CacheObject co) {
		if (co == null) {
			return;
		}

		if (first == null) {
			first = co;
			nbrOfCachedObj++;
		} else {
			co.next = first;
			first = co;
			nbrOfCachedObj++;
			if (nbrOfCachedObj > maxCache) {
				// need to let the last cache go
				CacheObject rm = getObject(maxCache-1);
				rm.next.clean();
				rm.next = null;
				nbrOfCachedObj--;
			}
		}
		atElement = 0;
	}

	public CacheObject step(int dir) {
		switch (dir) {
		case NEXT: {
			if (hasNext()) {
				atElement--;
				return getObject(atElement);
			}
		}
		case PREVIOUS: {
			if (hasPrevious()) {
				atElement++;
				return getObject(atElement);
			}
		}
		default: {
			return null;
		}
		}

	}

	public boolean hasPrevious() {
		return atElement < nbrOfCachedObj-1;
	}

	public boolean hasNext() {
		return atElement > 0;
	}
}

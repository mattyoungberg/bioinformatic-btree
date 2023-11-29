package cs321.search;



public class GeneBankSearchBTreeArguments
{
	private final boolean useCache;
    private final int degree;
    private final String btreeFileName;
    private final int subsequenceLength;
    private final String queryFileName;
    private final int cacheSize;
    private final int debugLevel;

    public GeneBankSearchBTreeArguments(boolean useCache, int degree, String btreeFileName, int subsequenceLength, String queryFileName, int cacheSize, int debugLevel)
    {
        this.useCache = useCache;
        this.degree = degree;
        this.btreeFileName = btreeFileName;
        this.subsequenceLength = subsequenceLength;
        this.queryFileName = queryFileName;
        this.cacheSize = cacheSize;
        this.debugLevel = debugLevel;
    }

    @Override
    public boolean equals(Object obj)
    {
        //this method was generated using an IDE
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        GeneBankSearchBTreeArguments other = (GeneBankSearchBTreeArguments) obj;
        if (cacheSize != other.cacheSize)
        {
            return false;
        }
        if (getDebugLevel() != other.getDebugLevel())
        {
            return false;
        }
        if (getDegree() != other.getDegree())
        {
            return false;
        }
        if (getBtreeFileName() == null)
        {
            if (other.getBtreeFileName() != null)
            {
                return false;
            }
        }
        else
        {
            if (!getBtreeFileName().equals(other.getBtreeFileName()))
            {
                return false;
            }
        }
        if (subsequenceLength != other.subsequenceLength)
        {
            return false;
        }
        if (getQueryFileName() == null)
        {
            if (other.getQueryFileName() != null)
            {
                return false;
            }
        }
        else
        {
            if (!getQueryFileName().equals(other.getQueryFileName()))
            {
                return false;
            }
        }
        if (useCache != other.useCache)
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        //this method was generated using an IDE
        return "GeneBankSearchBTreeArguments{" +
                "useCache=" + useCache +
                ", degree=" + getDegree() +
                ", btreeFileName='" + getBtreeFileName() + '\'' +
                ", subsequenceLength=" + getSubsequenceLength() +
                ", queryFileName='" + getQueryFileName() + '\'' +
                ", cacheSize=" + cacheSize +
                ", debugLevel=" + getDebugLevel() +
                '}';
    }

	
	/**
	 * @return the subsequenceLength
	 */
	public int getSubsequenceLength() {
		return subsequenceLength;
	}

	/**
	 * @return the degree
	 */
	public int getDegree() {
		return degree;
	}

	/**
	 * @return the debugLevel
	 */
	public int getDebugLevel() {
		return debugLevel;
	}

	/**
	 * @return the btreeFileName
	 */
	public String getBtreeFileName() {
		return btreeFileName;
	}

	/**
	 * @return the queryFileName
	 */
	public String getQueryFileName() {
		return queryFileName;
	}
}

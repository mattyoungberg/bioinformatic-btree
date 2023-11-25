package cs321.create;

public class GeneBankCreateBTreeArguments
{
    private final boolean useCache;
    private final int degree;
    private final String gbkFileName;
    private final int subsequenceLength;
    private final int cacheSize;
    private final int debugLevel;

    public GeneBankCreateBTreeArguments(boolean useCache, int degree, String gbkFileName, int subsequenceLength, int cacheSize, int debugLevel)
    {
        this.useCache = useCache;
        this.degree = degree;
        this.gbkFileName = gbkFileName;
        this.subsequenceLength = subsequenceLength;
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
        GeneBankCreateBTreeArguments other = (GeneBankCreateBTreeArguments) obj;
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
        if (getGbkFileName() == null)
        {
            if (other.getGbkFileName() != null)
            {
                return false;
            }
        }
        else
        {
            if (!getGbkFileName().equals(other.getGbkFileName()))
            {
                return false;
            }
        }
        if (subsequenceLength != other.subsequenceLength)
        {
            return false;
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
        return "GeneBankCreateBTreeArguments{" +
                "useCache=" + useCache +
                ", degree=" + getDegree() +
                ", gbkFileName='" + getGbkFileName() + '\'' +
                ", subsequenceLength=" + subsequenceLength +
                ", cacheSize=" + cacheSize +
                ", debugLevel=" + getDebugLevel() +
                '}';
    }

	/**
	 * @return the gbkFileName
	 */
	public String getGbkFileName() {
		return gbkFileName;
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

	
}

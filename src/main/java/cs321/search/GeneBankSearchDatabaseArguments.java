package cs321.search;

import java.nio.file.Path;

public class GeneBankSearchDatabaseArguments
{
	private final Path databasePath;
	private final String queryFileName;

    public GeneBankSearchDatabaseArguments(Path databasePath, String queryFileName)
    {
        this.databasePath = databasePath;
        this.queryFileName = queryFileName;
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
        GeneBankSearchDatabaseArguments other = (GeneBankSearchDatabaseArguments) obj;
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
        if (getDatabasePath() == null)
        {
            if (other.getDatabasePath() != null)
            {
                return false;
            }
        }
        else
        {
            if (!getDatabasePath().equals(other.getDatabasePath()))
            {
                return false;
            }
        }
        
        return true;
    }

    @Override
    public String toString()
    {
        //this method was generated using an IDE
        return "GeneBankSearchDatabaseArguments{" +
                "dataBasePath=" + getDatabasePath() +
                ", queryFileName='" + getQueryFileName() + '\'' +
                '}';
    }

	/**
	 * @return the databasePath
	 */
	public Path getDatabasePath() {
		return databasePath;
	}

	/**
	 * @return the queryFileName
	 */
	public String getQueryFileName() {
		return queryFileName;
	}


}

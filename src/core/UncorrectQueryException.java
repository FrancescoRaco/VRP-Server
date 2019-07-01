package core;

/**
 * Uncorrect Query Exception
 * @author Francesco Raco
 *
 */
public class UncorrectQueryException extends Exception
{
	public UncorrectQueryException()
	{
		super("Uncorrect Input: points not found or too low number of target points");
	}
}

package stats.colloc;

class Collocation
{
	String base;
	String collocate;
	double score;
	int f1;
	int f2;
	int f;

	public String toString()
	{
		return collocate + "\t" + base + "\t" + f + "\t" + f1 + "\t" + f2 + "\t" + score;
	}
}
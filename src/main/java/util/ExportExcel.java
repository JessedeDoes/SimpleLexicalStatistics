package util;


import java.io.*;
import java.io.IOException;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

import jxl.read.biff.BiffException;

import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class ExportExcel
{
	public static void main(String[] args)
	{
		try
		{
			Workbook workbook = Workbook.getWorkbook(new File(args[0]));
			for (int i=0; i < workbook.getNumberOfSheets(); i++)
			{
				PrintWriter pw = new PrintWriter(new FileWriter(args[0] + ".sheet." + i  + ".txt"));
				Sheet s = workbook.getSheet(i);
				System.err.println("Sheet " + i + " rows = "  + s.getRows()  + " , columns = " + s.getColumns());
				if (false) continue;
				for (int j=0; j < s.getRows(); j++)
				{
					for (int k=0; k < s.getColumns(); k++)
					{
						String cell = "";
						try
						{
							Cell c = s.getCell(k,j);
							if (c != null)
							{
								cell = c.getContents();
								cell = cell.replaceAll("\\s",  " ");
							}
						} catch (Exception e)
						{
							System.err.println("error  at  i= " + i + "  j=" + j + ", k=" + k);
							e.printStackTrace();
						}
						if (k > 0)
							pw.print("\t");
						pw.print(cell);
					}
					pw.print("\n");
				}
			}
		} catch (BiffException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}

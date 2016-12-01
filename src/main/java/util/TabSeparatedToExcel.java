package util;
import java.io.*;
import java.util.*;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.format.CellFormat;
import jxl.read.biff.BiffException;
import jxl.format.Alignment;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFeatures;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/**
 * Add a bunch of tab-separated files as separate sheets to an excel sheet.
 * Command line:
 * <pre>
 *  java TabSeparatedToExcel &lt;excel file to write> args
 * </pre>
 * each arg consists (separated by ":") of
 * <pre>
 * &lt;sheetName>:&lt;fileName>:&lt;column names>
 * </pre>
 * Column names are separated by "/".
 *
 * <p>
 * There is a half-baked attempt to write a column which is completely numerical as excel numbers to the sheet.
 * @author does
 */
public class TabSeparatedToExcel
{
	WritableWorkbook wworkbook;
	String[] columnNames = {};
	private boolean hasColumnNames;
	public static void main(String[] args)
	{
		TabSeparatedToExcel ts2e = new TabSeparatedToExcel();
		ts2e.export(args);
	}
	
	public TabSeparatedToExcel()
	{
		
	}
	

	public void export(String[] args)
	{
		try
		{
			wworkbook = Workbook.createWorkbook(new File(args[0]));
			for (int i=1; i < args.length; i++)
			{
				String a = args[i];
				String sheetName = a;
				sheetName = sheetName.replaceAll(".*[/\\\\]", "");
				String fileName = a;
				String columns =  null;

				hasColumnNames = false;
				if (a.contains(":"))
				{
					String[] z = a.split(":");
					sheetName = z[0]; fileName=z[1];	
					if (z.length > 2)
					{
						columns =z[2];
						 columnNames = columns.split("/");
						 hasColumnNames = true;
					}
				};
				
				WritableSheet wsheet = wworkbook.createSheet(sheetName, i-1);
				List<List<String>> content = new ArrayList<List<String>>();
				BufferedReader b = new BufferedReader(new FileReader(fileName));
				String line;
				int nCols = 0;
				int offset = hasColumnNames?1:0;
				
				if (hasColumnNames)
				{
					for (int x=0; x < columnNames.length; x++)
					{
						Label l = new Label(x, 0, columnNames[x]);
						try
						{
							wsheet.addCell(l);
						} catch (RowsExceededException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (WriteException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				while ((line = b.readLine()) != null)
				{
					String[] cols =  line.split("\t");
					if (cols.length > nCols)
					{
						nCols = cols.length;
					}
					List<String> l = Arrays.asList(cols);
					content.add(l);
				}
				for (int j=0; j < nCols; j++)
				{
					boolean contentMaybeNumber = true;
					boolean contentMaybeInteger = true;
					for (int k=0; k < content.size(); k++)
					{
						try
						{
							Double d = Double.parseDouble(content.get(k).get(j));
							try 
							{ 
								Integer x = Integer.parseInt(content.get(k).get(j));
							} catch (Exception e)
							{
								contentMaybeInteger = false;
							}
						} catch (Exception e)
						{
							//System.err.println("column " + j  + " is not numerical");
							contentMaybeNumber = false;
						}
					}
					for (int k=0; k < content.size(); k++)
					{
						
						try
						{
							String cell = content.get(k).get(j);
							if (contentMaybeNumber && contentMaybeInteger)
							{
								Number number = new Number(j, k+offset, Integer.parseInt(cell ));
								wsheet.addCell(number);
							} else if (contentMaybeNumber)
							{
								Number number = new Number(j, k+offset, Double.parseDouble(cell ));
								wsheet.addCell(number);
							} else
							{
								Label label = new Label(j, k+offset, cell);
								//checkColumnProperties(label,j);
								wsheet.addCell(label); // here we need to retrieve special formats first...
							}
						} catch (Exception e)
						{
							//e.printStackTrace();
						}
					}
				}
			}
			try
			{
				wworkbook.write();
				wworkbook.close();
			} catch (WriteException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void checkColumnProperties(Cell cell, int column, int row)
	{
		// TODO Auto-generated method stub
		//WritableCellFeatures cf = label.getWritableCellFeatures()
		//cf.s
		//label.
		//CellFormat x;
		//x.
		//label.setCellFormat(cf);
		WritableCellFormat newFormat = null;
		
		WritableCellFormat cellFormatObj = new WritableCellFormat();
		CellFormat readFormat = cell.getCellFormat() == null ? cellFormatObj
		                    : cell.getCellFormat();
		newFormat = new WritableCellFormat(readFormat);
		//newFormat.setBackground(Colour.WHITE);
		//newFormat.setBorder(jxl.format.Border.BOTTOM,jxl.format.BorderLineStyle.THIN);
		try
		{
			newFormat.setAlignment(Alignment.CENTRE);
		} catch (WriteException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// en dan:
		//WritableSheet s = workbook.getSheet(0);
		 //column, row , where you wan the new format , note newFormat is passed as parameter.
		//sheet.addCell(new Label(column, row, request.getRuleId(), copyCellFormat(s, column, newFormat))); 
	}
	
	public void setColumnProperty(String name)
	{
	
	}
}

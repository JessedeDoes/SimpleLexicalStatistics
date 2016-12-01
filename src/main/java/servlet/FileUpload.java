package servlet;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class FileUpload
{
	static int maxMemSize = Integer.MAX_VALUE;
	static int maxFileSize = Integer.MAX_VALUE;
	String uploadLocation = "/tmp";
	
	public FileUpload (String uploadLocation)
	{
		this.uploadLocation = uploadLocation;
	}
	
	/**
	 * Should return a map....
	 * @param request
	 * @param parameterMap
	 * @return
	 */
	public Map<String,File> processMultipartFormData(HttpServletRequest request,
			Map<String, String> parameterMap) 
	{
		DiskFileItemFactory factory = new DiskFileItemFactory();
		// maximum size that will be stored in memory
		factory.setSizeThreshold(maxMemSize);
		// Location to save data that is larger than maxMemSize.
		factory.setRepository(new File("/tmp/"));

		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);
		// maximum file size to be uploaded.
		upload.setSizeMax( maxFileSize );

		Map<String,File> uploadedFileList = new HashMap<String,File>();

		upload.setHeaderEncoding("UTF-8");
		
		try
		{ 
			// Parse the request to get file items.
			List fileItems = upload.parseRequest(request);
			//if (fileItems.size() > 1) formatNerredFile = false;
			// Process the uploaded file items
			Iterator i = fileItems.iterator();

			while ( i.hasNext () ) 
			{
				FileItem fi = (FileItem) i.next();
				
				if (fi.isFormField ())
				{
					String fieldname = fi.getFieldName();       
					String fieldvalue = fi.getString(); 
					byte[] bytes = fi.get();
					fieldvalue = new String(bytes,"UTF-8"); // (hm)
					parameterMap.put(fieldname, fieldvalue);
				}
				else 
				{
					// Get the uploaded file parameters
					try
					{
						String fieldName = fi.getFieldName();
						String fileName = fi.getName();
						String contentType = fi.getContentType();
						boolean isInMemory = fi.isInMemory();
						long sizeInBytes = fi.getSize();
						File createdTempFile = 
								File.createTempFile("tomcatUpload",  "couldYouPleaseDeleteMe");
						// Write the file (should we?) 
					
						fi.write(createdTempFile);
						System.err.printf("File upload for field %s : %s\n", fieldName, 
								createdTempFile.getCanonicalPath());
						uploadedFileList.put(fieldName,createdTempFile);
					} catch (Exception e)
					{
						System.err.println("Error processing: " + fi);
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		System.err.println("Map:" + parameterMap);
		return uploadedFileList;
	}

	public static void cleanup(Map<String, File> fileUploadMap)
	{
		for (File f: fileUploadMap.values())
		{
			if (f.exists())
				f.delete();
		}
		
	} 
}

package json;
import org.json.simple.*;
import java.util.*;

public class JSONObjects
{
	public static JSONArray toJSONArray(List l)
	{
		JSONArray ja = new JSONArray();
		int i=0;
		for (Object o: l)
		{
			if (true || o.getClass().isInstance("json.JSONSerializable"))
			{
				try
				{
					JSONSerializable x = (JSONSerializable) o;
			
					JSONObject jo = x.toJSON();
					ja.add(jo);
				} catch (Exception e) // simple object?
				{
					ja.add(o);
				}
				// System.err.println(jo.toJSONString());
			} else
			{
				System.err.println(o.getClass());
			}
		}
		return ja;
	}
}


import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.catalyst.advanced.CatalystAdvancedIOHandler;
import com.zc.component.object.ZCRowObject;
import com.zc.component.zcql.ZCQL;

import org.json.JSONArray;
import org.json.JSONObject;

public class DatastoreFetch implements CatalystAdvancedIOHandler {
	private static final Logger LOGGER = Logger.getLogger(DatastoreFetch.class.getName());

	@Override
	public void runner(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			//Fetches the endpoint that the call was made to
			String url = request.getRequestURI();
			String method = request.getMethod();

			if (url.equals("/fetchData") && method.equals("GET")) {
				String tableName = request.getParameter("tablename");
				//Queries the Data Store to fetch news from a particular table using ZCQL
				String query = "select title,url from " + tableName;
				ArrayList<ZCRowObject> rowList = ZCQL.getInstance().executeQuery(query);
				JSONObject data = new JSONObject();
				JSONArray content = new JSONArray();
				
				//Constructs the data from the Data Store as an JSON response
				for (int i = 0; i < rowList.size(); i++) {
					JSONObject rowData = new JSONObject();
					JSONObject tableData = new JSONObject();
					String urls = (String) rowList.get(i).get(tableName, "url");
					Object title = rowList.get(i).get(tableName, "title");
					rowData.put("title", title);
					rowData.put("url", urls);
					tableData.put(tableName, rowData);
					content.put(tableData);
				}
				data.put("content", content);

				//Sends the JSON response back to the client
				response.setContentType("application/json");
				response.getWriter().write(data.toString());
				response.setStatus(200);
			} else {
//The errors are logged. You can check them from Catalyst Logs.
				LOGGER.log(Level.SEVERE, "Error. Invalid Request");
				response.setStatus(404);
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Exception in DatastoreFetch", e);
			response.setStatus(500);
		}
	}
}

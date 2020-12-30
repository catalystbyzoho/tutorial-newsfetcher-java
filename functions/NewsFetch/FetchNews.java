
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.catalyst.Context;
import com.catalyst.cron.CRON_STATUS;
import com.catalyst.cron.CronRequest;
import com.catalyst.cron.CatalystCronHandler;

import com.zc.common.ZCProject;
import com.zc.component.object.ZCObject;
import com.zc.component.object.ZCRowObject;
import com.zc.component.object.ZCTable;
import com.zc.component.zcql.ZCQL;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FetchNews implements CatalystCronHandler {

	private static final Logger LOGGER = Logger.getLogger(FetchNews.class.getName());
	static String[] TABLENAME = { "HEADLINES", "BUSINESS", "ENTERTAINMENT", "HEALTH", "SCIENCE", "SPORTS",
			"TECHNOLOGY" }; // The names of the tables in the Data Store where the news items need to be stored
	static String COUNTRY = "IN"; // Fetches the news items from India
	static String APIKEY = "ENTER_YOUR_API_KEY_HERE"; // Provide the API key you obtained from NewsAPI inside the quotations

	@Override
	public CRON_STATUS handleCronExecute(CronRequest request, Context arg1) throws Exception {
		try {
			ZCProject.initProject();
			OkHttpClient client = new OkHttpClient();

			// Fetches the breaking news of different categories with their headlines
			for (int i = 0; i < TABLENAME.length; i++) {
				// Builds the URL required to make the API call
				HttpUrl.Builder urlBuilder = HttpUrl.parse("http://newsapi.org/v2/top-headlines").newBuilder();
				urlBuilder.addQueryParameter("country", COUNTRY);
				urlBuilder.addQueryParameter("apiKey", APIKEY);
				if (!TABLENAME[i].equals("HEADLINES")) {
					urlBuilder.addQueryParameter("category", TABLENAME[i]);
				}
				String url = urlBuilder.build().toString();
				Request requests = new Request.Builder().url(url).build();

				// Makes an API call to the News API to fetch the data
				Response response = client.newCall(requests).execute();

				// If the response is 200, the data is moved to the Data Store. If not, the error is logged.
				if (response.code() == 200) {
					JSONObject responseObject = new JSONObject(response.body().string());

					JSONArray responseArray = (JSONArray) responseObject.getJSONArray("articles");
					// This method inserts/updates the news in the Data Store
					pushNewstoDatastore(responseArray, i);
				} else {
					LOGGER.log(Level.SEVERE, "Error fetching data from News API");
				}
//The actions are logged. You can check the logs from Catalyst Logs.
				LOGGER.log(Level.SEVERE, " News Updated");

			}
		} catch (Exception e) {

			LOGGER.log(Level.SEVERE, "Exception in Cron Function", e);
			return CRON_STATUS.FAILURE;
		}
		return CRON_STATUS.SUCCESS;
	}

	private void pushNewstoDatastore(JSONArray responseArray, int length) throws Exception {
		String action = null;
//Defines the ZCQL query that will be used to find out the number of rows in a table
		String query = "select ROWID from " + TABLENAME[length];
		ArrayList<ZCRowObject> rowList = ZCQL.getInstance().executeQuery(query);

		ZCRowObject row = ZCRowObject.getInstance();
		List<ZCRowObject> rows = new ArrayList<>();

		ZCObject object = ZCObject.getInstance();
		ZCTable table = object.getTable(TABLENAME[length]);

		// Inserts the data obtained from the API call into a list
		for (int i = 0; i < 15; i++) {

			JSONObject response = responseArray.getJSONObject(i);

			Object title = response.get("title");
			Object url = response.get("url");
			row.set("title", title);
			row.set("url", url);

			// Obtains the number of rows in the table
			if (rowList.size() > 0) {
				Object RowID = rowList.get(i).get(TABLENAME[length], "ROWID");
				String rowid = RowID.toString();
				Long ID = Long.parseLong(rowid);
//If there are no rows, the data is inserted, else the existing rows are updated
				row.set("ROWID", ID);
				rows.add(row);
				action = "Update";
				table.updateRows(rows);
			} else {
				action = "Insert";
				table.insertRow(row);
			}
		}
//The actions are logged. You can check the logs from Catalyst Logs.
		if (action.equals("Update")) {
			LOGGER.log(Level.SEVERE, TABLENAME[length] + " Table updated with current News");
		} else if (action.equals("Insert")) {
			LOGGER.log(Level.SEVERE, TABLENAME[length] + " Table inserted with current News");
		}
	}
}

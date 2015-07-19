package wesports.com.wesports;

/**
 * Created by Eric on 2015-07-19.
 */

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.util.List;

public class LoadGamesAsyncTask extends AsyncTask<Void, Void, Void> {

    private HttpPost mHttppost;
    private Callback mCallback;
    private Error mError;

    List<Game> mGamesList;

    public LoadGamesAsyncTask(HttpPost httppost, Callback callback) {
        mHttppost = httppost;
        mCallback = callback;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response = httpclient.execute(mHttppost);
            String json = EntityUtils.toString(response.getEntity());

            Gson gson = new Gson();
            TypeToken<List<Game>> token = new TypeToken<List<Game>>(){};
            mGamesList = gson.fromJson(json, token.getType());

        } catch (Exception e) {
            e.printStackTrace();
            mError = new Error(e.getMessage(), e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

        if (mCallback != null) {
            mCallback.onComplete(mGamesList, mError);
        }

    }

    public interface Callback {
        void onComplete(Object o, Error error);
    }
}
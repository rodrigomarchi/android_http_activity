
import android.os.AsyncTask;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;

import br.com.ge.amigoreal.App;

public class GetTask extends AsyncTask<Void, Void, String> {

    private String url;
    private Hashtable<String, String> params;
    private HttpViewHandler viewHandler;
    private int statusCode = 0;

    public GetTask(String url, Hashtable<String, String> params, HttpViewHandler viewHandler){
        this.url = url;
        this.params = params;
        this.viewHandler = viewHandler;
        LogHelper.debug("GetTask url: " + url);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(!App.get().checkInternetAccess()){
            App.get().showAlertSemRede(viewHandler.getCurrentActivity());
            cancel(true);
            LogHelper.debug("Sem internet");
        } else {
            viewHandler.showProgress(true);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        viewHandler.showProgress(false);
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        viewHandler.showProgress(false);
        if(result == null || result.isEmpty() || statusCode != 200){
            App.get().showAlert(viewHandler.getCurrentActivity(), viewHandler.getCurrentActivity().getTitle() + " Ocorreu um erro inesperado, \nTente novamente mais tarde.");
            LogHelper.debug("GetTask result: " + result);
        } else {
            viewHandler.httpResponse(result);
        }

    }

    @Override
    protected String doInBackground(Void... params) {
        if (this.url == null){
            throw new RuntimeException("Url não pode ser nulo.");
        }

        URL URL = null;
        try {
            URL = new URL(this.url);
            HttpURLConnection conn = (HttpURLConnection) URL.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
            writer.write(getPostParamString(this.params));
            writer.flush();
            writer.close();

            statusCode = conn.getResponseCode();
            if(statusCode == 200){
                InputStream in = new BufferedInputStream(conn.getInputStream());
                return IOUtils.toString(in, "UTF-8");
            }
        } catch (IOException e) {
            LogHelper.debug("GetTask", e);
        }
        return "";
    }

    private String getPostParamString(Hashtable<String, String> params) {
        if(params.size() == 0)
            return "";
        StringBuffer buf = new StringBuffer();
        Enumeration<String> keys = params.keys();
        while(keys.hasMoreElements()) {
            buf.append(buf.length() == 0 ? "" : "&");
            String key = keys.nextElement();
            buf.append(key).append("=").append(params.get(key));
        }
        return buf.toString();
    }

}

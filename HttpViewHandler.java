
import android.app.Activity;

public interface HttpViewHandler {

    void showProgress(Boolean show);

    Activity getCurrentActivity();

    void httpResponse(String response);

}

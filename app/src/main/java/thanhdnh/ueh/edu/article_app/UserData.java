package thanhdnh.ueh.edu.article_app;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.GridView;
import android.widget.ProgressBar;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UserData {
    public static UserList data;
    private Context context;
    private GridView gridview;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public UserData(Context context, GridView gridview) {
        this.context = context;
        this.gridview = gridview;
    }

    public static UserProfile getUserFromId(int id) {
        for (int i = 0; i < data.getUsers().size(); i++)
            if (data.getUsers().get(i).getId() == id)
                return data.getUsers().get(i);
        return null;
    }

    public void loadData(String url, Activity activity, ProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setMax(100);
        progressBar.setProgress(0);

        executor.execute(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).build();
                Response response = client.newCall(request).execute();

                if (!response.isSuccessful() || response.body() == null) {
                    activity.runOnUiThread(() -> progressBar.setVisibility(View.GONE));
                    return;
                }

                long totalBytes = response.body().contentLength();
                InputStream inputStream = response.body().byteStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                byte[] buffer = new byte[1024];
                long downloadedBytes = 0;
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                    downloadedBytes += bytesRead;
                    if (totalBytes > 0) {
                        final int progress = (int) ((downloadedBytes * 100) / totalBytes);
                        activity.runOnUiThread(() -> progressBar.setProgress(progress));
                    }
                }

                String jsonString = baos.toString("UTF-8");

                activity.runOnUiThread(() -> {
                    Gson gson = new Gson();
                    data = gson.fromJson(jsonString, (Type) UserList.class);
                    UserAdapter adapter = new UserAdapter(data.getUsers(), context);
                    gridview.setAdapter(adapter);
                    progressBar.setVisibility(View.GONE);
                });

                response.close();
            } catch (Exception e) {
                e.printStackTrace();
                activity.runOnUiThread(() -> progressBar.setVisibility(View.GONE));
            }
        });
    }
}

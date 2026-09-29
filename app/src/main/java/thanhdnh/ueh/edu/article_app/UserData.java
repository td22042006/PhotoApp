package thanhdnh.ueh.edu.article_app;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    if (data != null && data.getUsers() != null) {
      for (int i = 0; i < data.getUsers().size(); i++) {
        if (data.getUsers().get(i).getId() == id) {
          return data.getUsers().get(i);
        }
      }
    }
    return null;
  }

  public void loadData(String url, Activity activity, View progressContainer, ProgressBar progressBar, TextView tvProgress) {
    executor.execute(() -> {
      File file = Downloader.downloadWithProgress(url, context.getCacheDir(), progressBar, tvProgress, activity);
      if (file != null) {
        String jsonText = readText(file);
        activity.runOnUiThread(() -> {
          Gson gson = new Gson();
          data = gson.fromJson(jsonText, (Type) UserList.class);
          if (data != null && data.getUsers() != null) {
            UserAdapter adapter = new UserAdapter(data.getUsers(), context);
            gridview.setAdapter(adapter);
          }
          if (progressContainer != null) {
            progressContainer.postDelayed(() -> progressContainer.setVisibility(View.GONE), 300);
          } else if (progressBar != null) {
            progressBar.postDelayed(() -> progressBar.setVisibility(View.GONE), 300);
          }
        });
      }
    });
  }

  public void loadData(String url, Activity activity, ProgressBar progressBar) {
    loadData(url, activity, null, progressBar, null);
  }

  public String readText(File file) {
    BufferedReader reader = null;
    try {
      InputStream stream = new FileInputStream(file);
      reader = new BufferedReader(new InputStreamReader(stream));
      StringBuilder buffer = new StringBuilder();
      String line = "";
      while ((line = reader.readLine()) != null) {
        buffer.append(line).append("\n");
      }
      String content = buffer.toString();
      if (content.startsWith("\uFEFF")) {
        content = content.substring(1);
      }
      return content;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "";
  }
}

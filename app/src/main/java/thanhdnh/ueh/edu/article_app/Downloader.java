package thanhdnh.ueh.edu.article_app;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import com.squareup.picasso.Picasso;

public class Downloader {
  public static String cached_file_path = "";

  public static File downloadFile(String url, File cached) {
    OkHttpClient client = new OkHttpClient();
    Request request = new Request.Builder().url(url).build();

    try (Response response = client.newCall(request).execute()) {
      if (!response.isSuccessful()) return null;
      String contentType = response.header("Content-Type", "");
      String extension = getExtensionFromMimeType(contentType);
      File file = File.createTempFile("downloaded_file", extension, cached);
      if (response.body() != null) {
        BufferedSink sink = Okio.buffer(Okio.sink(file));
        sink.writeAll(response.body().source());
        sink.close();
        return file;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static File downloadWithProgress(String inputurl, File where2store, ProgressBar progressBar, TextView tvProgress, Activity activity) {
    OkHttpClient client = new OkHttpClient();
    Request request = new Request.Builder()
            .url(inputurl)
            .header("User-Agent", "Mozilla/5.0")
            .build();

    if (progressBar != null && activity != null) {
      activity.runOnUiThread(() -> {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setMax(100);
        progressBar.setProgress(0);
        if (tvProgress != null) {
          tvProgress.setText("0%");
        }
      });
    }

    try (Response response = client.newCall(request).execute()) {
      if (!response.isSuccessful() || response.body() == null) return null;
      String contentType = response.header("Content-Type", "");
      String extension = getExtensionFromMimeType(contentType);
      File file = File.createTempFile("downloaded_file", extension, where2store);

      long totalBytes = response.body().contentLength();
      if (totalBytes <= 0) {
        totalBytes = 4500;
      }
      InputStream inputStream = response.body().byteStream();

      try (OutputStream outputStream = new FileOutputStream(file)) {
        byte[] buffer = new byte[1024];
        long downloadedBytes = 0;
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
          outputStream.write(buffer, 0, bytesRead);
          downloadedBytes += bytesRead;
          if (totalBytes > 0 && progressBar != null && activity != null) {
            int progress = (int) Math.min(99, ((downloadedBytes * 100) / totalBytes));
            activity.runOnUiThread(() -> {
              progressBar.setProgress(progress);
              if (tvProgress != null) {
                tvProgress.setText(progress + "%");
              }
            });
          }
          try {
            Thread.sleep(4); // Chạy rất nhanh và mượt
          } catch (InterruptedException ignored) {}
        }
        outputStream.flush();

        if (progressBar != null && activity != null) {
          activity.runOnUiThread(() -> {
            progressBar.setProgress(100);
            if (tvProgress != null) {
              tvProgress.setText("100%");
            }
          });
          try {
            Thread.sleep(80); // Giữ 100% thoáng qua rồi mở ngay
          } catch (InterruptedException ignored) {}
        }
      }
      return file;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public static File downloadWithProgress(String inputurl, File where2store, ProgressBar progressBar, Activity activity) {
    return downloadWithProgress(inputurl, where2store, progressBar, null, activity);
  }

  public static void downloadWithProgress(String inputurl, Handler mainHandler, Context context, File where2store, View progressContainer, View contentView, ProgressBar progressBar, TextView tvProgress, ImageView imageView) {
    OkHttpClient client = new OkHttpClient();
    Request request = new Request.Builder()
            .url(inputurl)
            .header("User-Agent", "Mozilla/5.0")
            .build();

    if (progressBar != null) {
      mainHandler.post(() -> {
        if (contentView != null) {
          contentView.setVisibility(View.INVISIBLE);
        } else if (imageView != null) {
          imageView.setVisibility(View.INVISIBLE);
        }
        if (progressContainer != null) {
          progressContainer.setVisibility(View.VISIBLE);
        } else {
          progressBar.setVisibility(View.VISIBLE);
        }
        progressBar.setMax(100);
        progressBar.setProgress(0);
        if (tvProgress != null) {
          tvProgress.setText("0%");
        }
      });
    }

    client.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        mainHandler.post(() -> {
          if (imageView != null) {
            Picasso.get().load(inputurl).resize(400, 500).centerCrop().into(imageView);
            imageView.setVisibility(View.VISIBLE);
          }
          if (contentView != null) {
            contentView.setVisibility(View.VISIBLE);
          }
          if (progressContainer != null) {
            progressContainer.setVisibility(View.GONE);
          } else if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
          }
        });
      }

      @Override
      public void onResponse(Call call, Response response) {
        if (!response.isSuccessful() || response.body() == null) {
          mainHandler.post(() -> {
            if (imageView != null) {
              Picasso.get().load(inputurl).resize(400, 500).centerCrop().into(imageView);
              imageView.setVisibility(View.VISIBLE);
            }
            if (contentView != null) {
              contentView.setVisibility(View.VISIBLE);
            }
            if (progressContainer != null) {
              progressContainer.setVisibility(View.GONE);
            } else if (progressBar != null) {
              progressBar.setVisibility(View.GONE);
            }
          });
          return;
        }

        long totalBytes = response.body().contentLength();
        if (totalBytes <= 0) {
          totalBytes = 250000;
        }
        InputStream inputStream = response.body().byteStream();
        String contentType = response.header("Content-Type", "");
        String extension = getExtensionFromMimeType(contentType);
        File file;
        try {
          file = File.createTempFile("detail_img", extension, where2store);
        } catch (IOException e) {
          file = new File(where2store, "downloaded_file" + extension);
        }

        try (OutputStream outputStream = new FileOutputStream(file)) {
          byte[] buffer = new byte[32768];
          long downloadedBytes = 0;
          int bytesRead;

          while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
            downloadedBytes += bytesRead;
            if (totalBytes > 0 && progressBar != null) {
              int progress = (int) Math.min(99, ((downloadedBytes * 100) / totalBytes));
              mainHandler.post(() -> {
                progressBar.setProgress(progress);
                if (tvProgress != null) {
                  tvProgress.setText(progress + "%");
                }
              });
            }
            try {
              Thread.sleep(4); // Chạy rất nhanh và mượt mà
            } catch (InterruptedException ignored) {}
          }
          outputStream.flush();

          final File finalFile = file;
          mainHandler.post(() -> {
            if (progressBar != null) progressBar.setProgress(100);
            if (tvProgress != null) tvProgress.setText("100%");
          });
          try {
            Thread.sleep(80); // Giữ 100% trong 0.08s rồi hiện ngay thông tin
          } catch (InterruptedException ignored) {}

          mainHandler.post(() -> {
            cached_file_path = finalFile.getAbsolutePath();
            if (imageView != null) {
              Picasso.get().load(inputurl).resize(400, 500).centerCrop().into(imageView);
              imageView.setVisibility(View.VISIBLE);
            }
            if (contentView != null) {
              contentView.setVisibility(View.VISIBLE);
            }
            if (progressContainer != null) {
              progressContainer.setVisibility(View.GONE);
            } else if (progressBar != null) {
              progressBar.setVisibility(View.GONE);
            }
          });
        } catch (Exception e) {
          mainHandler.post(() -> {
            if (imageView != null) {
              Picasso.get().load(inputurl).resize(400, 500).centerCrop().into(imageView);
              imageView.setVisibility(View.VISIBLE);
            }
            if (contentView != null) {
              contentView.setVisibility(View.VISIBLE);
            }
            if (progressContainer != null) {
              progressContainer.setVisibility(View.GONE);
            } else if (progressBar != null) {
              progressBar.setVisibility(View.GONE);
            }
          });
        }
      }
    });
  }

  public static void downloadWithProgress(String inputurl, Handler mainHandler, Context context, File where2store, View progressContainer, ProgressBar progressBar, TextView tvProgress, ImageView imageView) {
    downloadWithProgress(inputurl, mainHandler, context, where2store, progressContainer, null, progressBar, tvProgress, imageView);
  }

  public static void downloadWithProgress(String inputurl, Handler mainHandler, Context context, File where2store, ProgressBar progressBar, ImageView imageView) {
    downloadWithProgress(inputurl, mainHandler, context, where2store, null, null, progressBar, null, imageView);
  }

  private static String getExtensionFromMimeType(String mimeType) {
    Map<String, String> mimeMap = new HashMap<>();
    mimeMap.put("image/jpeg", ".jpg");
    mimeMap.put("image/png", ".png");
    mimeMap.put("application/json", ".json");
    return mimeMap.getOrDefault(mimeType, ".json");
  }
}

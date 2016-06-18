/*
 * Copyright (C) 2016 CodeFireUA <edu@codefire.com.ua>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package javathreads;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author CodeFireUA <edu@codefire.com.ua>
 */
public class DownloadFileTask implements Runnable {

    private URL source;
    private File store;
    private int progress;
    private boolean downloading;
    private DownloadFileTaskListener listener;

    public DownloadFileTask(URL source, File store) {
        this.source = source;
        this.store = store;
    }

    public int getProgress() {
        return progress;
    }

    public void setListener(DownloadFileTaskListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        downloading = true;
//        System.out.println("Download: " + source);
        try {

            URLConnection conn = source.openConnection();
            conn.getContentType();
            long totalBytes = conn.getContentLengthLong();

            String filePath = new String(conn.getURL().getFile().getBytes("ISO-8859-1"), "UTF-8");

            String fileName = new File(URLDecoder.decode(filePath, "UTF-8")).getName();

            File targetFile = new File(store, fileName);

            if (listener != null) {
                listener.complete(this);
            }

            int count = 0;
            int lastProgress = 0;

            try (FileOutputStream fos = new FileOutputStream(targetFile);) {
                BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
                byte[] buffer = new byte[4096];

                int read;
                while ((read = bis.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                    fos.flush();

                    count += read;

                    progress = (int) (count * 100 / totalBytes);

                    if (lastProgress != progress) {
                        lastProgress = progress;
                        
//                        System.out.println(source + ": " + progress + "%");

                        if (listener != null) {
                            listener.progress(this);
                        }
                    }
                }
            }

//            Files.copy(source.openStream(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            Logger.getLogger(DownloadFileTask.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String toString() {
        if (downloading) {
            return String.format("[ %3d%% ] %s", progress, source);
        } else {
            return String.format("%s", source);
        }
    }

    public interface DownloadFileTaskListener {

        public void complete(DownloadFileTask downloadFileTask);

        public void progress(DownloadFileTask downloadFileTask);

    }

}

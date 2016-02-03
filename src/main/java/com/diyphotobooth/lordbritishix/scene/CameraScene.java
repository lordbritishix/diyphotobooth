package com.diyphotobooth.lordbritishix.scene;

import com.diyphotobooth.lordbritishix.client.IpCameraException;
import com.diyphotobooth.lordbritishix.client.IpCameraHttpClient;
import com.diyphotobooth.lordbritishix.client.JpegStreamReader;
import com.diyphotobooth.lordbritishix.controller.CameraSceneController;
import com.google.common.collect.Queues;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayDeque;

/**
 * Camera Scene is responsible for:
 * 1. Displaying the view finder
 * 2. Walking the user through the capture image process
 */
public class CameraScene extends BaseScene {
    private static final int MAX_QUEUE_SIZE = 5;

    private final IpCameraHttpClient client;
    private final ImageView imageView;
    private final JpegStreamReader jis;

    private final ArrayDeque<byte[]> deque;

    @Inject
    public CameraScene(CameraSceneController controller, IpCameraHttpClient client) {
        super(new StackPane(), controller);
        this.client = client;
        imageView = new ImageView();
        getRootPane().getChildren().add(imageView);
        deque = Queues.newArrayDeque();

        try {
            jis = new JpegStreamReader(this.client.getStream());
        } catch (IpCameraException e) {
            throw new RuntimeException(e);
        }

        Thread t = new Thread(() -> {
            while (true) {
                try {
                    byte[] b = jis.nextFrame();
                    enqueue(b);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        t.start();

        Thread t2 = new Thread(() -> {
           while (true) {
               byte[] data = dequeue();
               if (data == null) {
                   continue;
               }

               ByteArrayInputStream bais = new ByteArrayInputStream(data);
               Image image = new Image(bais);

               Platform.runLater(() -> {
                   imageView.setImage(image);
               });
           }
        });
        t2.start();
    }

    private synchronized void enqueue(byte[] data) {
        deque.push(data);

        if (deque.size() > MAX_QUEUE_SIZE) {
            deque.removeLast();
        }
    }

    private synchronized byte[] dequeue() {
        if (!deque.isEmpty()) {
            return deque.pop();
        }
        else {
            return null;
        }
    }



}

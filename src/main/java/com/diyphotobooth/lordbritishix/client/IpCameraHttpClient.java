package com.diyphotobooth.lordbritishix.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import java.io.InputStream;

@Singleton
public class IpCameraHttpClient implements IpCameraClient {
    private final String hostName;
    private final int portNumber;

    @Inject
    public IpCameraHttpClient(
            @Named("ipcamera.hostName")
            String hostName,
            @Named("ipcamera.portNumber")
            int portNumber) {
        this.hostName = hostName;
        this.portNumber = portNumber;
    }

    @Override
    public InputStream takePhoto(boolean withFocus) throws IpCameraException {
        Client client = Client.create();

        String action = withFocus ? "/photoaf.jpg" : "/photo.jpg";

        WebResource webResource = client.resource(hostAndPort() + action);

        ClientResponse response = webResource.accept("image/jpeg")
                .get(ClientResponse.class);

        if (response.getStatus() != 200) {
            throw new IpCameraException(response.getStatus(), "Unable to take photo");
        }

        return response.getEntityInputStream();
    }

    @Override
    public InputStream getStream() throws IpCameraException {
        Client client = Client.create();

        WebResource webResource = client.resource(hostAndPort() + "/video");

        ClientResponse response = webResource.accept("image/jpeg")
                .get(ClientResponse.class);

        if (response.getStatus() != 200) {
            throw new IpCameraException(response.getStatus(), "Unable to get the stream");
        }

        return response.getEntityInputStream();
    }

    @Override
    public String hostAndPort() {
        return "http://" + hostName + ":" + portNumber;
    }
}

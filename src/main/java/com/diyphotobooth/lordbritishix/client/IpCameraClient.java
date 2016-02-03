package com.diyphotobooth.lordbritishix.client;

import java.io.InputStream;

public interface IpCameraClient {
    InputStream takePhoto(boolean withFocus) throws IpCameraException;
    InputStream getStream() throws IpCameraException;

    String hostAndPort();
}

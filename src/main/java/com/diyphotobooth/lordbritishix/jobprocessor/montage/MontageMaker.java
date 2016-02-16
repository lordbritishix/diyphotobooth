package com.diyphotobooth.lordbritishix.jobprocessor.montage;

import java.nio.file.Path;
import com.diyphotobooth.lordbritishix.model.Session;

public interface MontageMaker {
    Path apply(Session session, Path sessionDir);
}

package com.myapp.iib.model;

import com.holonplatform.core.property.PropertySet;
import com.holonplatform.core.property.StringProperty;

public interface FilesNodeModel extends AbstractModel, BrokerModel {

    StringProperty LOCAL_DIR = StringProperty.create("Local Dir");
    StringProperty PATTERN = StringProperty.create("Pattern");
    StringProperty FTP = StringProperty.create("FTP");
    StringProperty FTP_TYPE = StringProperty.create("FTPType");
    StringProperty FTP_USER = StringProperty.create("FTPUser");
    StringProperty FTP_SERVER = StringProperty.create("FTPServer");
    StringProperty FTP_DIR = StringProperty.create("FTPDir");
    StringProperty FTP_MODE = StringProperty.create("FTPTranfrMode");
    StringProperty FTP_SCAN_DELAY = StringProperty.create("FTPScanDelay");

    PropertySet<?> PROPERTIES = PropertySet.builderOf(SNO, NAME, NODE_NAME, NODE_TYPE, LOCAL_DIR, PATTERN
            , FTP, FTP_TYPE, FTP_USER,FTP_SERVER, FTP_DIR, FTP_MODE, FTP_SCAN_DELAY, PARENT_NAME, PARENT_TYPE, EG_NAME)
            .withIdentifier(SNO)
            .build();

}

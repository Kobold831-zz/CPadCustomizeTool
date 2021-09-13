package com.saradabar.cpadcustomizetool.ChangeXml;

import android.os.Environment;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jp.co.benesse.dcha.dchaservice.IDchaService;


public class PackagesXml {
    private static final String PATH_SYSTEM_FILE = "/data/system/packages.xml";
    private static final String PATH_TMP_FILE = new File(Environment.getExternalStorageDirectory(), "packages.xml").getPath();

    private final Document document;
    private final NodeList elementsPackages;
    private final NodeList elementsSharedUser;
    private final NodeList elementsUpdated;

    private PackagesXml(Document document) {
        this.document = document;
        Element documentElement = document.getDocumentElement();
        elementsPackages =  documentElement.getElementsByTagName("package");
        elementsSharedUser = documentElement.getElementsByTagName("shared-user");
        elementsUpdated = documentElement.getElementsByTagName("updated-package");
    }

    public void grantPermission(String packageNameOrSharedUserId, String permissionName) {
        if (! isPermissionGranted(packageNameOrSharedUserId, permissionName)) {
            int index = queryOfName(elementsSharedUser, packageNameOrSharedUserId);
            Node node;
            if (index != -1) {
                node = elementsSharedUser.item(index);
            } else {
                int i = queryOfName(elementsUpdated, packageNameOrSharedUserId);
                if (i != -1) {
                    node = elementsUpdated.item(i);
                } else {
                    int queryOfName = queryOfName(elementsPackages, packageNameOrSharedUserId);
                    if (queryOfName != -1) {
                        node = elementsPackages.item(queryOfName);
                    } else {
                        return;
                    }
                }
            }
            grantPermissionNode(node, permissionName);
        }
    }

    private void grantPermissionNode(Node packageNode, String permissionName) {
        int permIndex = queryOfTag(packageNode.getChildNodes());
        Node permNode = packageNode.getChildNodes().item(permIndex);

        Element newElement = document.createElement("item");
        newElement.setAttribute("name", permissionName);

        permNode.appendChild(newElement);
    }

    private int queryOfTag(NodeList nodeList) {
        int length = nodeList.getLength();
        Node node;
        for (int i=0;i<length;i++) {
            node = nodeList.item(i);
            if ("perms".equals(node.getNodeName())) {
                return i;
            }
        }
        return -1;
    }

    private int queryOfName(NodeList nodeList, String name) {
        int length = nodeList.getLength();
        Node node;

        for (int i=0;i<length;i++) {
            node = nodeList.item(i);
            if (getAttribute(node).equals(name)) {
                return i;
            }
        }
        return -1;
    }

    private String getAttribute(Node node) {
        NamedNodeMap map = node.getAttributes();
        if (map == null) {
            return "";
        }

        int length = map.getLength();
        for (int i=0;i<length;i++) {
            if (map.item(i).getNodeName().equals("name")) {
                return map.item(i).getNodeValue();
            }
        }
        return "";
    }

    private boolean isPermissionGranted(String packageNameOrSharedUser, String permission) {
        Node node;
        int index = queryOfName(elementsSharedUser, packageNameOrSharedUser);
        if (index != -1) {
            node = elementsSharedUser.item(index);
        } else {
            int i = queryOfName(elementsUpdated, packageNameOrSharedUser);
            if (i != -1) {
                node = elementsUpdated.item(i);
            } else {
                int queryOfName = queryOfName(elementsPackages, packageNameOrSharedUser);
                if (queryOfName != -1) {
                    node = elementsPackages.item(queryOfName);
                } else {
                    return false;
                }
            }
        }
        NodeList childNodes = node.getChildNodes();
        int permIndex = queryOfTag(childNodes);
        if (permIndex == -1) return false;
        NodeList permItems = childNodes.item(permIndex).getChildNodes();
        return queryOfName(permItems, permission) != -1;
    }

    public void outputToSystem(IDchaService mDchaService) {
        if (mDchaService == null) throw new IllegalArgumentException();

        Transformer transformer;
        try {
            File tmpFile = new File(PATH_TMP_FILE);
            transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty("indent", "yes");
            String FILE_ENCODE = "UTF-8";
            transformer.setOutputProperty("encoding", FILE_ENCODE);
            transformer.transform(new DOMSource(document), new StreamResult(tmpFile));
            mDchaService.copyUpdateImage(PATH_TMP_FILE, PATH_SYSTEM_FILE);
            //noinspection ResultOfMethodCallIgnored
            tmpFile.delete();
        } catch (Exception ignored) {
        }
    }

    public static PackagesXml inputFromSystem(IDchaService mDchaService) {
        if (mDchaService == null) throw new IllegalArgumentException();

        try {
            mDchaService.copyUpdateImage(PATH_SYSTEM_FILE, PATH_TMP_FILE);
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            File tmpFile = new File(PATH_TMP_FILE);
            Document document = builder.parse(tmpFile);
            //noinspection ResultOfMethodCallIgnored
            tmpFile.delete();
            return new PackagesXml(document);
        } catch (Exception e) {
            return null;
        }
    }
}
package com.saradabar.cpadcustomizetool.service;

interface IDeviceOwnerService {
    boolean isDeviceOwnerApp();
    void setUninstallBlocked(String str, boolean bl);
    boolean isUninstallBlocked(String str);
    void installPackages(String str, in List<Uri> uriList);
}
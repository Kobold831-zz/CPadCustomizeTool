package com.saradabar.cpadcustomizetool.service;

interface IDeviceOwnerService {
    void setUninstallBlocked(String str, boolean bl);
    boolean isUninstallBlocked(String str);
}
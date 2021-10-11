package jp.co.benesse.dcha.dchautilservice;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IDchaUtilService extends IInterface {
    void clearDefaultPreferredApp(String str) throws RemoteException;

    boolean copyDirectory(String str, String str2, boolean z) throws RemoteException;

    boolean copyFile(String str, String str2) throws RemoteException;

    boolean deleteFile(String str) throws RemoteException;

    int[] getDisplaySize() throws RemoteException;

    int[] getLcdSize() throws RemoteException;

    int getUserCount() throws RemoteException;

    void hideNavigationBar(boolean z) throws RemoteException;

    boolean makeDir(String str, String str2) throws RemoteException;

    void sdUnmount() throws RemoteException;

    void setDefaultPreferredHomeApp(String str) throws RemoteException;

    boolean setForcedDisplaySize(int i, int i2) throws RemoteException;

    public static abstract class Stub extends Binder implements IDchaUtilService {
        private static final String DESCRIPTOR = "jp.co.benesse.dcha.dchautilservice.IDchaUtilService";
        static final int TRANSACTION_clearDefaultPreferredApp = 12;
        static final int TRANSACTION_copyDirectory = 6;
        static final int TRANSACTION_copyFile = 5;
        static final int TRANSACTION_deleteFile = 7;
        static final int TRANSACTION_getDisplaySize = 2;
        static final int TRANSACTION_getLcdSize = 3;
        static final int TRANSACTION_getUserCount = 10;
        static final int TRANSACTION_hideNavigationBar = 9;
        static final int TRANSACTION_makeDir = 8;
        static final int TRANSACTION_sdUnmount = 4;
        static final int TRANSACTION_setDefaultPreferredHomeApp = 11;
        static final int TRANSACTION_setForcedDisplaySize = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDchaUtilService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDchaUtilService)) {
                return new Proxy(obj);
            }
            return (IDchaUtilService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _arg0;
            boolean _arg2;
            int i = 0;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result = setForcedDisplaySize(data.readInt(), data.readInt());
                    reply.writeNoException();
                    if (_result) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    int[] _result2 = getDisplaySize();
                    reply.writeNoException();
                    reply.writeIntArray(_result2);
                    return true;
                case TRANSACTION_getLcdSize /*{ENCODED_INT: 3}*/:
                    data.enforceInterface(DESCRIPTOR);
                    int[] _result3 = getLcdSize();
                    reply.writeNoException();
                    reply.writeIntArray(_result3);
                    return true;
                case TRANSACTION_sdUnmount /*{ENCODED_INT: 4}*/:
                    data.enforceInterface(DESCRIPTOR);
                    sdUnmount();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_copyFile /*{ENCODED_INT: 5}*/:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result4 = copyFile(data.readString(), data.readString());
                    reply.writeNoException();
                    if (_result4) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_copyDirectory /*{ENCODED_INT: 6}*/:
                    data.enforceInterface(DESCRIPTOR);
                    String _arg02 = data.readString();
                    String _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        _arg2 = true;
                    } else {
                        _arg2 = false;
                    }
                    boolean _result5 = copyDirectory(_arg02, _arg1, _arg2);
                    reply.writeNoException();
                    if (_result5) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result6 = deleteFile(data.readString());
                    reply.writeNoException();
                    if (_result6) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_makeDir /*{ENCODED_INT: 8}*/:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result7 = makeDir(data.readString(), data.readString());
                    reply.writeNoException();
                    if (_result7) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_hideNavigationBar /*{ENCODED_INT: 9}*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = true;
                    } else {
                        _arg0 = false;
                    }
                    hideNavigationBar(_arg0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getUserCount /*{ENCODED_INT: 10}*/:
                    data.enforceInterface(DESCRIPTOR);
                    int _result8 = getUserCount();
                    reply.writeNoException();
                    reply.writeInt(_result8);
                    return true;
                case TRANSACTION_setDefaultPreferredHomeApp /*{ENCODED_INT: 11}*/:
                    data.enforceInterface(DESCRIPTOR);
                    setDefaultPreferredHomeApp(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_clearDefaultPreferredApp /*{ENCODED_INT: 12}*/:
                    data.enforceInterface(DESCRIPTOR);
                    clearDefaultPreferredApp(data.readString());
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }

        private static class Proxy implements IDchaUtilService {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // jp.co.benesse.dcha.dchautilservice.IDchaUtilService
            public boolean setForcedDisplaySize(int width, int height) throws RemoteException {
                boolean _result = true;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(width);
                    _data.writeInt(height);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.dchautilservice.IDchaUtilService
            public int[] getDisplaySize() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createIntArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.dchautilservice.IDchaUtilService
            public int[] getLcdSize() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getLcdSize, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createIntArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.dchautilservice.IDchaUtilService
            public void sdUnmount() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_sdUnmount, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.dchautilservice.IDchaUtilService
            public boolean copyFile(String srcFilePath, String dstFilePath) throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(srcFilePath);
                    _data.writeString(dstFilePath);
                    this.mRemote.transact(Stub.TRANSACTION_copyFile, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.dchautilservice.IDchaUtilService
            public boolean copyDirectory(String srcDirPath, String dstDirPath, boolean makeTopDir) throws RemoteException {
                int i;
                boolean _result = true;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(srcDirPath);
                    _data.writeString(dstDirPath);
                    if (makeTopDir) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_copyDirectory, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.dchautilservice.IDchaUtilService
            public boolean deleteFile(String path) throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(path);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.dchautilservice.IDchaUtilService
            public boolean makeDir(String path, String dirname) throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(path);
                    _data.writeString(dirname);
                    this.mRemote.transact(Stub.TRANSACTION_makeDir, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.dchautilservice.IDchaUtilService
            public void hideNavigationBar(boolean hide) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (hide) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_hideNavigationBar, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.dchautilservice.IDchaUtilService
            public int getUserCount() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getUserCount, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.dchautilservice.IDchaUtilService
            public void setDefaultPreferredHomeApp(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(Stub.TRANSACTION_setDefaultPreferredHomeApp, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // jp.co.benesse.dcha.dchautilservice.IDchaUtilService
            public void clearDefaultPreferredApp(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(Stub.TRANSACTION_clearDefaultPreferredApp, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
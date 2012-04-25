/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/undergear/workspace/SuperCollider-Android/src/net/sf/supercollider/android/ISuperCollider.aidl
 */
package net.sf.supercollider.android;
/** Provide IPC to the SuperCollider service.
 *
 */
public interface ISuperCollider extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements net.sf.supercollider.android.ISuperCollider
{
private static final java.lang.String DESCRIPTOR = "net.sf.supercollider.android.ISuperCollider";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an net.sf.supercollider.android.ISuperCollider interface,
 * generating a proxy if needed.
 */
public static net.sf.supercollider.android.ISuperCollider asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof net.sf.supercollider.android.ISuperCollider))) {
return ((net.sf.supercollider.android.ISuperCollider)iin);
}
return new net.sf.supercollider.android.ISuperCollider.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_start:
{
data.enforceInterface(DESCRIPTOR);
this.start();
reply.writeNoException();
return true;
}
case TRANSACTION_stop:
{
data.enforceInterface(DESCRIPTOR);
this.stop();
reply.writeNoException();
return true;
}
case TRANSACTION_sendMessage:
{
data.enforceInterface(DESCRIPTOR);
net.sf.supercollider.android.OscMessage _arg0;
if ((0!=data.readInt())) {
_arg0 = net.sf.supercollider.android.OscMessage.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.sendMessage(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_openUDP:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.openUDP(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_closeUDP:
{
data.enforceInterface(DESCRIPTOR);
this.closeUDP();
reply.writeNoException();
return true;
}
case TRANSACTION_sendQuit:
{
data.enforceInterface(DESCRIPTOR);
this.sendQuit();
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements net.sf.supercollider.android.ISuperCollider
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
// Kick off the run loop, if not running.  SuperCollider is processor-intensive so try
// not to run it if you don't require audio  

public void start() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_start, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
// Terminate the run loop, if running.

public void stop() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_stop, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
// Send an OSC message

public void sendMessage(net.sf.supercollider.android.OscMessage oscMessage) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((oscMessage!=null)) {
_data.writeInt(1);
oscMessage.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_sendMessage, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
// Open a UDP listener for remote connections. Please remember to close it on exit.

public void openUDP(int port) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(port);
mRemote.transact(Stub.TRANSACTION_openUDP, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
// Close UDP listener.

public void closeUDP() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_closeUDP, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
// Gracefully quit the SC process and the Android audio loop

public void sendQuit() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_sendQuit, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_start = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_stop = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_sendMessage = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_openUDP = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_closeUDP = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_sendQuit = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
}
// Kick off the run loop, if not running.  SuperCollider is processor-intensive so try
// not to run it if you don't require audio  

public void start() throws android.os.RemoteException;
// Terminate the run loop, if running.

public void stop() throws android.os.RemoteException;
// Send an OSC message

public void sendMessage(net.sf.supercollider.android.OscMessage oscMessage) throws android.os.RemoteException;
// Open a UDP listener for remote connections. Please remember to close it on exit.

public void openUDP(int port) throws android.os.RemoteException;
// Close UDP listener.

public void closeUDP() throws android.os.RemoteException;
// Gracefully quit the SC process and the Android audio loop

public void sendQuit() throws android.os.RemoteException;
}

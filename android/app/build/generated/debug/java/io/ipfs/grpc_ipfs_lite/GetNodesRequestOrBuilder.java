// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: ipfs_lite.proto

package io.ipfs.grpc_ipfs_lite;

public interface GetNodesRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:ipfs_lite.GetNodesRequest)
    com.google.protobuf.MessageLiteOrBuilder {

  /**
   * <code>repeated string cids = 1;</code>
   * @return A list containing the cids.
   */
  java.util.List<java.lang.String>
      getCidsList();
  /**
   * <code>repeated string cids = 1;</code>
   * @return The count of cids.
   */
  int getCidsCount();
  /**
   * <code>repeated string cids = 1;</code>
   * @param index The index of the element to return.
   * @return The cids at the given index.
   */
  java.lang.String getCids(int index);
  /**
   * <code>repeated string cids = 1;</code>
   * @param index The index of the element to return.
   * @return The cids at the given index.
   */
  com.google.protobuf.ByteString
      getCidsBytes(int index);
}

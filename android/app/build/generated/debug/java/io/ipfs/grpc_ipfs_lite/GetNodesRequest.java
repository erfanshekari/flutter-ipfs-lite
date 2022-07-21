// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: ipfs_lite.proto

package io.ipfs.grpc_ipfs_lite;

/**
 * Protobuf type {@code ipfs_lite.GetNodesRequest}
 */
public  final class GetNodesRequest extends
    com.google.protobuf.GeneratedMessageLite<
        GetNodesRequest, GetNodesRequest.Builder> implements
    // @@protoc_insertion_point(message_implements:ipfs_lite.GetNodesRequest)
    GetNodesRequestOrBuilder {
  private GetNodesRequest() {
    cids_ = com.google.protobuf.GeneratedMessageLite.emptyProtobufList();
  }
  public static final int CIDS_FIELD_NUMBER = 1;
  private com.google.protobuf.Internal.ProtobufList<java.lang.String> cids_;
  /**
   * <code>repeated string cids = 1;</code>
   * @return A list containing the cids.
   */
  @java.lang.Override
  public java.util.List<java.lang.String> getCidsList() {
    return cids_;
  }
  /**
   * <code>repeated string cids = 1;</code>
   * @return The count of cids.
   */
  @java.lang.Override
  public int getCidsCount() {
    return cids_.size();
  }
  /**
   * <code>repeated string cids = 1;</code>
   * @param index The index of the element to return.
   * @return The cids at the given index.
   */
  @java.lang.Override
  public java.lang.String getCids(int index) {
    return cids_.get(index);
  }
  /**
   * <code>repeated string cids = 1;</code>
   * @param index The index of the value to return.
   * @return The bytes of the cids at the given index.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getCidsBytes(int index) {
    return com.google.protobuf.ByteString.copyFromUtf8(
        cids_.get(index));
  }
  private void ensureCidsIsMutable() {
    com.google.protobuf.Internal.ProtobufList<java.lang.String> tmp =
        cids_;  if (!tmp.isModifiable()) {
      cids_ =
          com.google.protobuf.GeneratedMessageLite.mutableCopy(tmp);
     }
  }
  /**
   * <code>repeated string cids = 1;</code>
   * @param index The index to set the value at.
   * @param value The cids to set.
   */
  private void setCids(
      int index, java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  ensureCidsIsMutable();
    cids_.set(index, value);
  }
  /**
   * <code>repeated string cids = 1;</code>
   * @param value The cids to add.
   */
  private void addCids(
      java.lang.String value) {
    java.lang.Class<?> valueClass = value.getClass();
  ensureCidsIsMutable();
    cids_.add(value);
  }
  /**
   * <code>repeated string cids = 1;</code>
   * @param values The cids to add.
   */
  private void addAllCids(
      java.lang.Iterable<java.lang.String> values) {
    ensureCidsIsMutable();
    com.google.protobuf.AbstractMessageLite.addAll(
        values, cids_);
  }
  /**
   * <code>repeated string cids = 1;</code>
   */
  private void clearCids() {
    cids_ = com.google.protobuf.GeneratedMessageLite.emptyProtobufList();
  }
  /**
   * <code>repeated string cids = 1;</code>
   * @param value The bytes of the cids to add.
   */
  private void addCidsBytes(
      com.google.protobuf.ByteString value) {
    checkByteStringIsUtf8(value);
    ensureCidsIsMutable();
    cids_.add(value.toStringUtf8());
  }

  public static io.ipfs.grpc_ipfs_lite.GetNodesRequest parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static io.ipfs.grpc_ipfs_lite.GetNodesRequest parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static io.ipfs.grpc_ipfs_lite.GetNodesRequest parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static io.ipfs.grpc_ipfs_lite.GetNodesRequest parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static io.ipfs.grpc_ipfs_lite.GetNodesRequest parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static io.ipfs.grpc_ipfs_lite.GetNodesRequest parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static io.ipfs.grpc_ipfs_lite.GetNodesRequest parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static io.ipfs.grpc_ipfs_lite.GetNodesRequest parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static io.ipfs.grpc_ipfs_lite.GetNodesRequest parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static io.ipfs.grpc_ipfs_lite.GetNodesRequest parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static io.ipfs.grpc_ipfs_lite.GetNodesRequest parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static io.ipfs.grpc_ipfs_lite.GetNodesRequest parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(io.ipfs.grpc_ipfs_lite.GetNodesRequest prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code ipfs_lite.GetNodesRequest}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        io.ipfs.grpc_ipfs_lite.GetNodesRequest, Builder> implements
      // @@protoc_insertion_point(builder_implements:ipfs_lite.GetNodesRequest)
      io.ipfs.grpc_ipfs_lite.GetNodesRequestOrBuilder {
    // Construct using io.ipfs.grpc_ipfs_lite.GetNodesRequest.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>repeated string cids = 1;</code>
     * @return A list containing the cids.
     */
    @java.lang.Override
    public java.util.List<java.lang.String>
        getCidsList() {
      return java.util.Collections.unmodifiableList(
          instance.getCidsList());
    }
    /**
     * <code>repeated string cids = 1;</code>
     * @return The count of cids.
     */
    @java.lang.Override
    public int getCidsCount() {
      return instance.getCidsCount();
    }
    /**
     * <code>repeated string cids = 1;</code>
     * @param index The index of the element to return.
     * @return The cids at the given index.
     */
    @java.lang.Override
    public java.lang.String getCids(int index) {
      return instance.getCids(index);
    }
    /**
     * <code>repeated string cids = 1;</code>
     * @param index The index of the value to return.
     * @return The bytes of the cids at the given index.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getCidsBytes(int index) {
      return instance.getCidsBytes(index);
    }
    /**
     * <code>repeated string cids = 1;</code>
     * @param index The index to set the value at.
     * @param value The cids to set.
     * @return This builder for chaining.
     */
    public Builder setCids(
        int index, java.lang.String value) {
      copyOnWrite();
      instance.setCids(index, value);
      return this;
    }
    /**
     * <code>repeated string cids = 1;</code>
     * @param value The cids to add.
     * @return This builder for chaining.
     */
    public Builder addCids(
        java.lang.String value) {
      copyOnWrite();
      instance.addCids(value);
      return this;
    }
    /**
     * <code>repeated string cids = 1;</code>
     * @param values The cids to add.
     * @return This builder for chaining.
     */
    public Builder addAllCids(
        java.lang.Iterable<java.lang.String> values) {
      copyOnWrite();
      instance.addAllCids(values);
      return this;
    }
    /**
     * <code>repeated string cids = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearCids() {
      copyOnWrite();
      instance.clearCids();
      return this;
    }
    /**
     * <code>repeated string cids = 1;</code>
     * @param value The bytes of the cids to add.
     * @return This builder for chaining.
     */
    public Builder addCidsBytes(
        com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.addCidsBytes(value);
      return this;
    }

    // @@protoc_insertion_point(builder_scope:ipfs_lite.GetNodesRequest)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new io.ipfs.grpc_ipfs_lite.GetNodesRequest();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "cids_",
          };
          java.lang.String info =
              "\u0000\u0001\u0000\u0000\u0001\u0001\u0001\u0000\u0001\u0000\u0001\u021a";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<io.ipfs.grpc_ipfs_lite.GetNodesRequest> parser = PARSER;
        if (parser == null) {
          synchronized (io.ipfs.grpc_ipfs_lite.GetNodesRequest.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<io.ipfs.grpc_ipfs_lite.GetNodesRequest>(
                      DEFAULT_INSTANCE);
              PARSER = parser;
            }
          }
        }
        return parser;
    }
    case GET_MEMOIZED_IS_INITIALIZED: {
      return (byte) 1;
    }
    case SET_MEMOIZED_IS_INITIALIZED: {
      return null;
    }
    }
    throw new UnsupportedOperationException();
  }


  // @@protoc_insertion_point(class_scope:ipfs_lite.GetNodesRequest)
  private static final io.ipfs.grpc_ipfs_lite.GetNodesRequest DEFAULT_INSTANCE;
  static {
    GetNodesRequest defaultInstance = new GetNodesRequest();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      GetNodesRequest.class, defaultInstance);
  }

  public static io.ipfs.grpc_ipfs_lite.GetNodesRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<GetNodesRequest> PARSER;

  public static com.google.protobuf.Parser<GetNodesRequest> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}

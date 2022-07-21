// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: ipfs_lite.proto

package io.ipfs.grpc_ipfs_lite;

/**
 * Protobuf type {@code ipfs_lite.AddFileRequest}
 */
public  final class AddFileRequest extends
    com.google.protobuf.GeneratedMessageLite<
        AddFileRequest, AddFileRequest.Builder> implements
    // @@protoc_insertion_point(message_implements:ipfs_lite.AddFileRequest)
    AddFileRequestOrBuilder {
  private AddFileRequest() {
  }
  private int payloadCase_ = 0;
  private java.lang.Object payload_;
  public enum PayloadCase {
    ADDPARAMS(1),
    CHUNK(2),
    PAYLOAD_NOT_SET(0);
    private final int value;
    private PayloadCase(int value) {
      this.value = value;
    }
    /**
     * @deprecated Use {@link #forNumber(int)} instead.
     */
    @java.lang.Deprecated
    public static PayloadCase valueOf(int value) {
      return forNumber(value);
    }

    public static PayloadCase forNumber(int value) {
      switch (value) {
        case 1: return ADDPARAMS;
        case 2: return CHUNK;
        case 0: return PAYLOAD_NOT_SET;
        default: return null;
      }
    }
    public int getNumber() {
      return this.value;
    }
  };

  @java.lang.Override
  public PayloadCase
  getPayloadCase() {
    return PayloadCase.forNumber(
        payloadCase_);
  }

  private void clearPayload() {
    payloadCase_ = 0;
    payload_ = null;
  }

  public static final int ADDPARAMS_FIELD_NUMBER = 1;
  /**
   * <code>.ipfs_lite.AddParams addParams = 1;</code>
   */
  @java.lang.Override
  public boolean hasAddParams() {
    return payloadCase_ == 1;
  }
  /**
   * <code>.ipfs_lite.AddParams addParams = 1;</code>
   */
  @java.lang.Override
  public io.ipfs.grpc_ipfs_lite.AddParams getAddParams() {
    if (payloadCase_ == 1) {
       return (io.ipfs.grpc_ipfs_lite.AddParams) payload_;
    }
    return io.ipfs.grpc_ipfs_lite.AddParams.getDefaultInstance();
  }
  /**
   * <code>.ipfs_lite.AddParams addParams = 1;</code>
   */
  private void setAddParams(io.ipfs.grpc_ipfs_lite.AddParams value) {
    value.getClass();
  payload_ = value;
    payloadCase_ = 1;
  }
  /**
   * <code>.ipfs_lite.AddParams addParams = 1;</code>
   */
  private void mergeAddParams(io.ipfs.grpc_ipfs_lite.AddParams value) {
    value.getClass();
  if (payloadCase_ == 1 &&
        payload_ != io.ipfs.grpc_ipfs_lite.AddParams.getDefaultInstance()) {
      payload_ = io.ipfs.grpc_ipfs_lite.AddParams.newBuilder((io.ipfs.grpc_ipfs_lite.AddParams) payload_)
          .mergeFrom(value).buildPartial();
    } else {
      payload_ = value;
    }
    payloadCase_ = 1;
  }
  /**
   * <code>.ipfs_lite.AddParams addParams = 1;</code>
   */
  private void clearAddParams() {
    if (payloadCase_ == 1) {
      payloadCase_ = 0;
      payload_ = null;
    }
  }

  public static final int CHUNK_FIELD_NUMBER = 2;
  /**
   * <code>bytes chunk = 2;</code>
   * @return Whether the chunk field is set.
   */
  @java.lang.Override
  public boolean hasChunk() {
    return payloadCase_ == 2;
  }
  /**
   * <code>bytes chunk = 2;</code>
   * @return The chunk.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString getChunk() {
    if (payloadCase_ == 2) {
      return (com.google.protobuf.ByteString) payload_;
    }
    return com.google.protobuf.ByteString.EMPTY;
  }
  /**
   * <code>bytes chunk = 2;</code>
   * @param value The chunk to set.
   */
  private void setChunk(com.google.protobuf.ByteString value) {
    java.lang.Class<?> valueClass = value.getClass();
  payloadCase_ = 2;
    payload_ = value;
  }
  /**
   * <code>bytes chunk = 2;</code>
   */
  private void clearChunk() {
    if (payloadCase_ == 2) {
      payloadCase_ = 0;
      payload_ = null;
    }
  }

  public static io.ipfs.grpc_ipfs_lite.AddFileRequest parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static io.ipfs.grpc_ipfs_lite.AddFileRequest parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static io.ipfs.grpc_ipfs_lite.AddFileRequest parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static io.ipfs.grpc_ipfs_lite.AddFileRequest parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static io.ipfs.grpc_ipfs_lite.AddFileRequest parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static io.ipfs.grpc_ipfs_lite.AddFileRequest parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static io.ipfs.grpc_ipfs_lite.AddFileRequest parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static io.ipfs.grpc_ipfs_lite.AddFileRequest parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static io.ipfs.grpc_ipfs_lite.AddFileRequest parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static io.ipfs.grpc_ipfs_lite.AddFileRequest parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static io.ipfs.grpc_ipfs_lite.AddFileRequest parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static io.ipfs.grpc_ipfs_lite.AddFileRequest parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(io.ipfs.grpc_ipfs_lite.AddFileRequest prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code ipfs_lite.AddFileRequest}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        io.ipfs.grpc_ipfs_lite.AddFileRequest, Builder> implements
      // @@protoc_insertion_point(builder_implements:ipfs_lite.AddFileRequest)
      io.ipfs.grpc_ipfs_lite.AddFileRequestOrBuilder {
    // Construct using io.ipfs.grpc_ipfs_lite.AddFileRequest.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }

    @java.lang.Override
    public PayloadCase
        getPayloadCase() {
      return instance.getPayloadCase();
    }

    public Builder clearPayload() {
      copyOnWrite();
      instance.clearPayload();
      return this;
    }


    /**
     * <code>.ipfs_lite.AddParams addParams = 1;</code>
     */
    @java.lang.Override
    public boolean hasAddParams() {
      return instance.hasAddParams();
    }
    /**
     * <code>.ipfs_lite.AddParams addParams = 1;</code>
     */
    @java.lang.Override
    public io.ipfs.grpc_ipfs_lite.AddParams getAddParams() {
      return instance.getAddParams();
    }
    /**
     * <code>.ipfs_lite.AddParams addParams = 1;</code>
     */
    public Builder setAddParams(io.ipfs.grpc_ipfs_lite.AddParams value) {
      copyOnWrite();
      instance.setAddParams(value);
      return this;
    }
    /**
     * <code>.ipfs_lite.AddParams addParams = 1;</code>
     */
    public Builder setAddParams(
        io.ipfs.grpc_ipfs_lite.AddParams.Builder builderForValue) {
      copyOnWrite();
      instance.setAddParams(builderForValue.build());
      return this;
    }
    /**
     * <code>.ipfs_lite.AddParams addParams = 1;</code>
     */
    public Builder mergeAddParams(io.ipfs.grpc_ipfs_lite.AddParams value) {
      copyOnWrite();
      instance.mergeAddParams(value);
      return this;
    }
    /**
     * <code>.ipfs_lite.AddParams addParams = 1;</code>
     */
    public Builder clearAddParams() {
      copyOnWrite();
      instance.clearAddParams();
      return this;
    }

    /**
     * <code>bytes chunk = 2;</code>
     * @return Whether the chunk field is set.
     */
    @java.lang.Override
    public boolean hasChunk() {
      return instance.hasChunk();
    }
    /**
     * <code>bytes chunk = 2;</code>
     * @return The chunk.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getChunk() {
      return instance.getChunk();
    }
    /**
     * <code>bytes chunk = 2;</code>
     * @param value The chunk to set.
     * @return This builder for chaining.
     */
    public Builder setChunk(com.google.protobuf.ByteString value) {
      copyOnWrite();
      instance.setChunk(value);
      return this;
    }
    /**
     * <code>bytes chunk = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearChunk() {
      copyOnWrite();
      instance.clearChunk();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:ipfs_lite.AddFileRequest)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new io.ipfs.grpc_ipfs_lite.AddFileRequest();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "payload_",
            "payloadCase_",
            io.ipfs.grpc_ipfs_lite.AddParams.class,
          };
          java.lang.String info =
              "\u0000\u0002\u0001\u0000\u0001\u0002\u0002\u0000\u0000\u0000\u0001<\u0000\u0002=" +
              "\u0000";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<io.ipfs.grpc_ipfs_lite.AddFileRequest> parser = PARSER;
        if (parser == null) {
          synchronized (io.ipfs.grpc_ipfs_lite.AddFileRequest.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<io.ipfs.grpc_ipfs_lite.AddFileRequest>(
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


  // @@protoc_insertion_point(class_scope:ipfs_lite.AddFileRequest)
  private static final io.ipfs.grpc_ipfs_lite.AddFileRequest DEFAULT_INSTANCE;
  static {
    AddFileRequest defaultInstance = new AddFileRequest();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      AddFileRequest.class, defaultInstance);
  }

  public static io.ipfs.grpc_ipfs_lite.AddFileRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<AddFileRequest> PARSER;

  public static com.google.protobuf.Parser<AddFileRequest> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}


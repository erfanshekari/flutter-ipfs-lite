// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: ipfs_lite.proto

package io.ipfs.grpc_ipfs_lite;

/**
 * Protobuf type {@code ipfs_lite.GetBlockResponse}
 */
public  final class GetBlockResponse extends
    com.google.protobuf.GeneratedMessageLite<
        GetBlockResponse, GetBlockResponse.Builder> implements
    // @@protoc_insertion_point(message_implements:ipfs_lite.GetBlockResponse)
    GetBlockResponseOrBuilder {
  private GetBlockResponse() {
  }
  public static final int BLOCK_FIELD_NUMBER = 1;
  private io.ipfs.grpc_ipfs_lite.Block block_;
  /**
   * <code>.ipfs_lite.Block block = 1;</code>
   */
  @java.lang.Override
  public boolean hasBlock() {
    return block_ != null;
  }
  /**
   * <code>.ipfs_lite.Block block = 1;</code>
   */
  @java.lang.Override
  public io.ipfs.grpc_ipfs_lite.Block getBlock() {
    return block_ == null ? io.ipfs.grpc_ipfs_lite.Block.getDefaultInstance() : block_;
  }
  /**
   * <code>.ipfs_lite.Block block = 1;</code>
   */
  private void setBlock(io.ipfs.grpc_ipfs_lite.Block value) {
    value.getClass();
  block_ = value;
    
    }
  /**
   * <code>.ipfs_lite.Block block = 1;</code>
   */
  @java.lang.SuppressWarnings({"ReferenceEquality"})
  private void mergeBlock(io.ipfs.grpc_ipfs_lite.Block value) {
    value.getClass();
  if (block_ != null &&
        block_ != io.ipfs.grpc_ipfs_lite.Block.getDefaultInstance()) {
      block_ =
        io.ipfs.grpc_ipfs_lite.Block.newBuilder(block_).mergeFrom(value).buildPartial();
    } else {
      block_ = value;
    }
    
  }
  /**
   * <code>.ipfs_lite.Block block = 1;</code>
   */
  private void clearBlock() {  block_ = null;
    
  }

  public static io.ipfs.grpc_ipfs_lite.GetBlockResponse parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static io.ipfs.grpc_ipfs_lite.GetBlockResponse parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static io.ipfs.grpc_ipfs_lite.GetBlockResponse parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static io.ipfs.grpc_ipfs_lite.GetBlockResponse parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static io.ipfs.grpc_ipfs_lite.GetBlockResponse parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static io.ipfs.grpc_ipfs_lite.GetBlockResponse parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static io.ipfs.grpc_ipfs_lite.GetBlockResponse parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static io.ipfs.grpc_ipfs_lite.GetBlockResponse parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static io.ipfs.grpc_ipfs_lite.GetBlockResponse parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static io.ipfs.grpc_ipfs_lite.GetBlockResponse parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static io.ipfs.grpc_ipfs_lite.GetBlockResponse parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static io.ipfs.grpc_ipfs_lite.GetBlockResponse parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(io.ipfs.grpc_ipfs_lite.GetBlockResponse prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code ipfs_lite.GetBlockResponse}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        io.ipfs.grpc_ipfs_lite.GetBlockResponse, Builder> implements
      // @@protoc_insertion_point(builder_implements:ipfs_lite.GetBlockResponse)
      io.ipfs.grpc_ipfs_lite.GetBlockResponseOrBuilder {
    // Construct using io.ipfs.grpc_ipfs_lite.GetBlockResponse.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>.ipfs_lite.Block block = 1;</code>
     */
    @java.lang.Override
    public boolean hasBlock() {
      return instance.hasBlock();
    }
    /**
     * <code>.ipfs_lite.Block block = 1;</code>
     */
    @java.lang.Override
    public io.ipfs.grpc_ipfs_lite.Block getBlock() {
      return instance.getBlock();
    }
    /**
     * <code>.ipfs_lite.Block block = 1;</code>
     */
    public Builder setBlock(io.ipfs.grpc_ipfs_lite.Block value) {
      copyOnWrite();
      instance.setBlock(value);
      return this;
      }
    /**
     * <code>.ipfs_lite.Block block = 1;</code>
     */
    public Builder setBlock(
        io.ipfs.grpc_ipfs_lite.Block.Builder builderForValue) {
      copyOnWrite();
      instance.setBlock(builderForValue.build());
      return this;
    }
    /**
     * <code>.ipfs_lite.Block block = 1;</code>
     */
    public Builder mergeBlock(io.ipfs.grpc_ipfs_lite.Block value) {
      copyOnWrite();
      instance.mergeBlock(value);
      return this;
    }
    /**
     * <code>.ipfs_lite.Block block = 1;</code>
     */
    public Builder clearBlock() {  copyOnWrite();
      instance.clearBlock();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:ipfs_lite.GetBlockResponse)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new io.ipfs.grpc_ipfs_lite.GetBlockResponse();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "block_",
          };
          java.lang.String info =
              "\u0000\u0001\u0000\u0000\u0001\u0001\u0001\u0000\u0000\u0000\u0001\t";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<io.ipfs.grpc_ipfs_lite.GetBlockResponse> parser = PARSER;
        if (parser == null) {
          synchronized (io.ipfs.grpc_ipfs_lite.GetBlockResponse.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<io.ipfs.grpc_ipfs_lite.GetBlockResponse>(
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


  // @@protoc_insertion_point(class_scope:ipfs_lite.GetBlockResponse)
  private static final io.ipfs.grpc_ipfs_lite.GetBlockResponse DEFAULT_INSTANCE;
  static {
    GetBlockResponse defaultInstance = new GetBlockResponse();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      GetBlockResponse.class, defaultInstance);
  }

  public static io.ipfs.grpc_ipfs_lite.GetBlockResponse getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<GetBlockResponse> PARSER;

  public static com.google.protobuf.Parser<GetBlockResponse> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}


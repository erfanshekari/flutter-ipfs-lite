// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: ipfs_lite.proto

package io.ipfs.grpc_ipfs_lite;

/**
 * Protobuf type {@code ipfs_lite.HasBlockResponse}
 */
public  final class HasBlockResponse extends
    com.google.protobuf.GeneratedMessageLite<
        HasBlockResponse, HasBlockResponse.Builder> implements
    // @@protoc_insertion_point(message_implements:ipfs_lite.HasBlockResponse)
    HasBlockResponseOrBuilder {
  private HasBlockResponse() {
  }
  public static final int HASBLOCK_FIELD_NUMBER = 1;
  private boolean hasBlock_;
  /**
   * <code>bool hasBlock = 1;</code>
   * @return The hasBlock.
   */
  @java.lang.Override
  public boolean getHasBlock() {
    return hasBlock_;
  }
  /**
   * <code>bool hasBlock = 1;</code>
   * @param value The hasBlock to set.
   */
  private void setHasBlock(boolean value) {
    
    hasBlock_ = value;
  }
  /**
   * <code>bool hasBlock = 1;</code>
   */
  private void clearHasBlock() {
    
    hasBlock_ = false;
  }

  public static io.ipfs.grpc_ipfs_lite.HasBlockResponse parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static io.ipfs.grpc_ipfs_lite.HasBlockResponse parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static io.ipfs.grpc_ipfs_lite.HasBlockResponse parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static io.ipfs.grpc_ipfs_lite.HasBlockResponse parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static io.ipfs.grpc_ipfs_lite.HasBlockResponse parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static io.ipfs.grpc_ipfs_lite.HasBlockResponse parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static io.ipfs.grpc_ipfs_lite.HasBlockResponse parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static io.ipfs.grpc_ipfs_lite.HasBlockResponse parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static io.ipfs.grpc_ipfs_lite.HasBlockResponse parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static io.ipfs.grpc_ipfs_lite.HasBlockResponse parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static io.ipfs.grpc_ipfs_lite.HasBlockResponse parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static io.ipfs.grpc_ipfs_lite.HasBlockResponse parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(io.ipfs.grpc_ipfs_lite.HasBlockResponse prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code ipfs_lite.HasBlockResponse}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        io.ipfs.grpc_ipfs_lite.HasBlockResponse, Builder> implements
      // @@protoc_insertion_point(builder_implements:ipfs_lite.HasBlockResponse)
      io.ipfs.grpc_ipfs_lite.HasBlockResponseOrBuilder {
    // Construct using io.ipfs.grpc_ipfs_lite.HasBlockResponse.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    /**
     * <code>bool hasBlock = 1;</code>
     * @return The hasBlock.
     */
    @java.lang.Override
    public boolean getHasBlock() {
      return instance.getHasBlock();
    }
    /**
     * <code>bool hasBlock = 1;</code>
     * @param value The hasBlock to set.
     * @return This builder for chaining.
     */
    public Builder setHasBlock(boolean value) {
      copyOnWrite();
      instance.setHasBlock(value);
      return this;
    }
    /**
     * <code>bool hasBlock = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearHasBlock() {
      copyOnWrite();
      instance.clearHasBlock();
      return this;
    }

    // @@protoc_insertion_point(builder_scope:ipfs_lite.HasBlockResponse)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new io.ipfs.grpc_ipfs_lite.HasBlockResponse();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = new java.lang.Object[] {
            "hasBlock_",
          };
          java.lang.String info =
              "\u0000\u0001\u0000\u0000\u0001\u0001\u0001\u0000\u0000\u0000\u0001\u0007";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<io.ipfs.grpc_ipfs_lite.HasBlockResponse> parser = PARSER;
        if (parser == null) {
          synchronized (io.ipfs.grpc_ipfs_lite.HasBlockResponse.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<io.ipfs.grpc_ipfs_lite.HasBlockResponse>(
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


  // @@protoc_insertion_point(class_scope:ipfs_lite.HasBlockResponse)
  private static final io.ipfs.grpc_ipfs_lite.HasBlockResponse DEFAULT_INSTANCE;
  static {
    HasBlockResponse defaultInstance = new HasBlockResponse();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      HasBlockResponse.class, defaultInstance);
  }

  public static io.ipfs.grpc_ipfs_lite.HasBlockResponse getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<HasBlockResponse> PARSER;

  public static com.google.protobuf.Parser<HasBlockResponse> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}


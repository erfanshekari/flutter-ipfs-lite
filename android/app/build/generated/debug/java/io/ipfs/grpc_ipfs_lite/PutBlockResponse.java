// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: ipfs_lite.proto

package io.ipfs.grpc_ipfs_lite;

/**
 * Protobuf type {@code ipfs_lite.PutBlockResponse}
 */
public  final class PutBlockResponse extends
    com.google.protobuf.GeneratedMessageLite<
        PutBlockResponse, PutBlockResponse.Builder> implements
    // @@protoc_insertion_point(message_implements:ipfs_lite.PutBlockResponse)
    PutBlockResponseOrBuilder {
  private PutBlockResponse() {
  }
  public static io.ipfs.grpc_ipfs_lite.PutBlockResponse parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static io.ipfs.grpc_ipfs_lite.PutBlockResponse parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static io.ipfs.grpc_ipfs_lite.PutBlockResponse parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static io.ipfs.grpc_ipfs_lite.PutBlockResponse parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static io.ipfs.grpc_ipfs_lite.PutBlockResponse parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data);
  }
  public static io.ipfs.grpc_ipfs_lite.PutBlockResponse parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, data, extensionRegistry);
  }
  public static io.ipfs.grpc_ipfs_lite.PutBlockResponse parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static io.ipfs.grpc_ipfs_lite.PutBlockResponse parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static io.ipfs.grpc_ipfs_lite.PutBlockResponse parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input);
  }
  public static io.ipfs.grpc_ipfs_lite.PutBlockResponse parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return parseDelimitedFrom(DEFAULT_INSTANCE, input, extensionRegistry);
  }
  public static io.ipfs.grpc_ipfs_lite.PutBlockResponse parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input);
  }
  public static io.ipfs.grpc_ipfs_lite.PutBlockResponse parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageLite.parseFrom(
        DEFAULT_INSTANCE, input, extensionRegistry);
  }

  public static Builder newBuilder() {
    return (Builder) DEFAULT_INSTANCE.createBuilder();
  }
  public static Builder newBuilder(io.ipfs.grpc_ipfs_lite.PutBlockResponse prototype) {
    return (Builder) DEFAULT_INSTANCE.createBuilder(prototype);
  }

  /**
   * Protobuf type {@code ipfs_lite.PutBlockResponse}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageLite.Builder<
        io.ipfs.grpc_ipfs_lite.PutBlockResponse, Builder> implements
      // @@protoc_insertion_point(builder_implements:ipfs_lite.PutBlockResponse)
      io.ipfs.grpc_ipfs_lite.PutBlockResponseOrBuilder {
    // Construct using io.ipfs.grpc_ipfs_lite.PutBlockResponse.newBuilder()
    private Builder() {
      super(DEFAULT_INSTANCE);
    }


    // @@protoc_insertion_point(builder_scope:ipfs_lite.PutBlockResponse)
  }
  @java.lang.Override
  @java.lang.SuppressWarnings({"unchecked", "fallthrough"})
  protected final java.lang.Object dynamicMethod(
      com.google.protobuf.GeneratedMessageLite.MethodToInvoke method,
      java.lang.Object arg0, java.lang.Object arg1) {
    switch (method) {
      case NEW_MUTABLE_INSTANCE: {
        return new io.ipfs.grpc_ipfs_lite.PutBlockResponse();
      }
      case NEW_BUILDER: {
        return new Builder();
      }
      case BUILD_MESSAGE_INFO: {
          java.lang.Object[] objects = null;java.lang.String info =
              "\u0000\u0000";
          return newMessageInfo(DEFAULT_INSTANCE, info, objects);
      }
      // fall through
      case GET_DEFAULT_INSTANCE: {
        return DEFAULT_INSTANCE;
      }
      case GET_PARSER: {
        com.google.protobuf.Parser<io.ipfs.grpc_ipfs_lite.PutBlockResponse> parser = PARSER;
        if (parser == null) {
          synchronized (io.ipfs.grpc_ipfs_lite.PutBlockResponse.class) {
            parser = PARSER;
            if (parser == null) {
              parser =
                  new DefaultInstanceBasedParser<io.ipfs.grpc_ipfs_lite.PutBlockResponse>(
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


  // @@protoc_insertion_point(class_scope:ipfs_lite.PutBlockResponse)
  private static final io.ipfs.grpc_ipfs_lite.PutBlockResponse DEFAULT_INSTANCE;
  static {
    PutBlockResponse defaultInstance = new PutBlockResponse();
    // New instances are implicitly immutable so no need to make
    // immutable.
    DEFAULT_INSTANCE = defaultInstance;
    com.google.protobuf.GeneratedMessageLite.registerDefaultInstance(
      PutBlockResponse.class, defaultInstance);
  }

  public static io.ipfs.grpc_ipfs_lite.PutBlockResponse getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static volatile com.google.protobuf.Parser<PutBlockResponse> PARSER;

  public static com.google.protobuf.Parser<PutBlockResponse> parser() {
    return DEFAULT_INSTANCE.getParserForType();
  }
}


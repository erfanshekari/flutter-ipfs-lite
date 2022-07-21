package io.ipfs.grpc_ipfs_lite;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.26.0)",
    comments = "Source: ipfs_lite.proto")
public final class IpfsLiteGrpc {

  private IpfsLiteGrpc() {}

  public static final String SERVICE_NAME = "ipfs_lite.IpfsLite";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<io.ipfs.grpc_ipfs_lite.AddFileRequest,
      io.ipfs.grpc_ipfs_lite.AddFileResponse> getAddFileMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "AddFile",
      requestType = io.ipfs.grpc_ipfs_lite.AddFileRequest.class,
      responseType = io.ipfs.grpc_ipfs_lite.AddFileResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
  public static io.grpc.MethodDescriptor<io.ipfs.grpc_ipfs_lite.AddFileRequest,
      io.ipfs.grpc_ipfs_lite.AddFileResponse> getAddFileMethod() {
    io.grpc.MethodDescriptor<io.ipfs.grpc_ipfs_lite.AddFileRequest, io.ipfs.grpc_ipfs_lite.AddFileResponse> getAddFileMethod;
    if ((getAddFileMethod = IpfsLiteGrpc.getAddFileMethod) == null) {
      synchronized (IpfsLiteGrpc.class) {
        if ((getAddFileMethod = IpfsLiteGrpc.getAddFileMethod) == null) {
          IpfsLiteGrpc.getAddFileMethod = getAddFileMethod =
              io.grpc.MethodDescriptor.<io.ipfs.grpc_ipfs_lite.AddFileRequest, io.ipfs.grpc_ipfs_lite.AddFileResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "AddFile"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  io.ipfs.grpc_ipfs_lite.AddFileRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  io.ipfs.grpc_ipfs_lite.AddFileResponse.getDefaultInstance()))
              .build();
        }
      }
    }
    return getAddFileMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.ipfs.grpc_ipfs_lite.GetFileRequest,
      io.ipfs.grpc_ipfs_lite.GetFileResponse> getGetFileMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetFile",
      requestType = io.ipfs.grpc_ipfs_lite.GetFileRequest.class,
      responseType = io.ipfs.grpc_ipfs_lite.GetFileResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<io.ipfs.grpc_ipfs_lite.GetFileRequest,
      io.ipfs.grpc_ipfs_lite.GetFileResponse> getGetFileMethod() {
    io.grpc.MethodDescriptor<io.ipfs.grpc_ipfs_lite.GetFileRequest, io.ipfs.grpc_ipfs_lite.GetFileResponse> getGetFileMethod;
    if ((getGetFileMethod = IpfsLiteGrpc.getGetFileMethod) == null) {
      synchronized (IpfsLiteGrpc.class) {
        if ((getGetFileMethod = IpfsLiteGrpc.getGetFileMethod) == null) {
          IpfsLiteGrpc.getGetFileMethod = getGetFileMethod =
              io.grpc.MethodDescriptor.<io.ipfs.grpc_ipfs_lite.GetFileRequest, io.ipfs.grpc_ipfs_lite.GetFileResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetFile"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  io.ipfs.grpc_ipfs_lite.GetFileRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  io.ipfs.grpc_ipfs_lite.GetFileResponse.getDefaultInstance()))
              .build();
        }
      }
    }
    return getGetFileMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.ipfs.grpc_ipfs_lite.HasBlockRequest,
      io.ipfs.grpc_ipfs_lite.HasBlockResponse> getHasBlockMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "HasBlock",
      requestType = io.ipfs.grpc_ipfs_lite.HasBlockRequest.class,
      responseType = io.ipfs.grpc_ipfs_lite.HasBlockResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.ipfs.grpc_ipfs_lite.HasBlockRequest,
      io.ipfs.grpc_ipfs_lite.HasBlockResponse> getHasBlockMethod() {
    io.grpc.MethodDescriptor<io.ipfs.grpc_ipfs_lite.HasBlockRequest, io.ipfs.grpc_ipfs_lite.HasBlockResponse> getHasBlockMethod;
    if ((getHasBlockMethod = IpfsLiteGrpc.getHasBlockMethod) == null) {
      synchronized (IpfsLiteGrpc.class) {
        if ((getHasBlockMethod = IpfsLiteGrpc.getHasBlockMethod) == null) {
          IpfsLiteGrpc.getHasBlockMethod = getHasBlockMethod =
              io.grpc.MethodDescriptor.<io.ipfs.grpc_ipfs_lite.HasBlockRequest, io.ipfs.grpc_ipfs_lite.HasBlockResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "HasBlock"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  io.ipfs.grpc_ipfs_lite.HasBlockRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  io.ipfs.grpc_ipfs_lite.HasBlockResponse.getDefaultInstance()))
              .build();
        }
      }
    }
    return getHasBlockMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.ipfs.grpc_ipfs_lite.AddNodeRequest,
      io.ipfs.grpc_ipfs_lite.AddNodeResponse> getAddNodeMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "AddNode",
      requestType = io.ipfs.grpc_ipfs_lite.AddNodeRequest.class,
      responseType = io.ipfs.grpc_ipfs_lite.AddNodeResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.ipfs.grpc_ipfs_lite.AddNodeRequest,
      io.ipfs.grpc_ipfs_lite.AddNodeResponse> getAddNodeMethod() {
    io.grpc.MethodDescriptor<io.ipfs.grpc_ipfs_lite.AddNodeRequest, io.ipfs.grpc_ipfs_lite.AddNodeResponse> getAddNodeMethod;
    if ((getAddNodeMethod = IpfsLiteGrpc.getAddNodeMethod) == null) {
      synchronized (IpfsLiteGrpc.class) {
        if ((getAddNodeMethod = IpfsLiteGrpc.getAddNodeMethod) == null) {
          IpfsLiteGrpc.getAddNodeMethod = getAddNodeMethod =
              io.grpc.MethodDescriptor.<io.ipfs.grpc_ipfs_lite.AddNodeRequest, io.ipfs.grpc_ipfs_lite.AddNodeResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "AddNode"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  io.ipfs.grpc_ipfs_lite.AddNodeRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  io.ipfs.grpc_ipfs_lite.AddNodeResponse.getDefaultInstance()))
              .build();
        }
      }
    }
    return getAddNodeMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.ipfs.grpc_ipfs_lite.AddNodesRequest,
      io.ipfs.grpc_ipfs_lite.AddNodesResponse> getAddNodesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "AddNodes",
      requestType = io.ipfs.grpc_ipfs_lite.AddNodesRequest.class,
      responseType = io.ipfs.grpc_ipfs_lite.AddNodesResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.ipfs.grpc_ipfs_lite.AddNodesRequest,
      io.ipfs.grpc_ipfs_lite.AddNodesResponse> getAddNodesMethod() {
    io.grpc.MethodDescriptor<io.ipfs.grpc_ipfs_lite.AddNodesRequest, io.ipfs.grpc_ipfs_lite.AddNodesResponse> getAddNodesMethod;
    if ((getAddNodesMethod = IpfsLiteGrpc.getAddNodesMethod) == null) {
      synchronized (IpfsLiteGrpc.class) {
        if ((getAddNodesMethod = IpfsLiteGrpc.getAddNodesMethod) == null) {
          IpfsLiteGrpc.getAddNodesMethod = getAddNodesMethod =
              io.grpc.MethodDescriptor.<io.ipfs.grpc_ipfs_lite.AddNodesRequest, io.ipfs.grpc_ipfs_lite.AddNodesResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "AddNodes"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  io.ipfs.grpc_ipfs_lite.AddNodesRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  io.ipfs.grpc_ipfs_lite.AddNodesResponse.getDefaultInstance()))
              .build();
        }
      }
    }
    return getAddNodesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.ipfs.grpc_ipfs_lite.GetNodeRequest,
      io.ipfs.grpc_ipfs_lite.GetNodeResponse> getGetNodeMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetNode",
      requestType = io.ipfs.grpc_ipfs_lite.GetNodeRequest.class,
      responseType = io.ipfs.grpc_ipfs_lite.GetNodeResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.ipfs.grpc_ipfs_lite.GetNodeRequest,
      io.ipfs.grpc_ipfs_lite.GetNodeResponse> getGetNodeMethod() {
    io.grpc.MethodDescriptor<io.ipfs.grpc_ipfs_lite.GetNodeRequest, io.ipfs.grpc_ipfs_lite.GetNodeResponse> getGetNodeMethod;
    if ((getGetNodeMethod = IpfsLiteGrpc.getGetNodeMethod) == null) {
      synchronized (IpfsLiteGrpc.class) {
        if ((getGetNodeMethod = IpfsLiteGrpc.getGetNodeMethod) == null) {
          IpfsLiteGrpc.getGetNodeMethod = getGetNodeMethod =
              io.grpc.MethodDescriptor.<io.ipfs.grpc_ipfs_lite.GetNodeRequest, io.ipfs.grpc_ipfs_lite.GetNodeResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetNode"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  io.ipfs.grpc_ipfs_lite.GetNodeRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  io.ipfs.grpc_ipfs_lite.GetNodeResponse.getDefaultInstance()))
              .build();
        }
      }
    }
    return getGetNodeMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.ipfs.grpc_ipfs_lite.GetNodesRequest,
      io.ipfs.grpc_ipfs_lite.GetNodesResponse> getGetNodesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetNodes",
      requestType = io.ipfs.grpc_ipfs_lite.GetNodesRequest.class,
      responseType = io.ipfs.grpc_ipfs_lite.GetNodesResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<io.ipfs.grpc_ipfs_lite.GetNodesRequest,
      io.ipfs.grpc_ipfs_lite.GetNodesResponse> getGetNodesMethod() {
    io.grpc.MethodDescriptor<io.ipfs.grpc_ipfs_lite.GetNodesRequest, io.ipfs.grpc_ipfs_lite.GetNodesResponse> getGetNodesMethod;
    if ((getGetNodesMethod = IpfsLiteGrpc.getGetNodesMethod) == null) {
      synchronized (IpfsLiteGrpc.class) {
        if ((getGetNodesMethod = IpfsLiteGrpc.getGetNodesMethod) == null) {
          IpfsLiteGrpc.getGetNodesMethod = getGetNodesMethod =
              io.grpc.MethodDescriptor.<io.ipfs.grpc_ipfs_lite.GetNodesRequest, io.ipfs.grpc_ipfs_lite.GetNodesResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetNodes"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  io.ipfs.grpc_ipfs_lite.GetNodesRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  io.ipfs.grpc_ipfs_lite.GetNodesResponse.getDefaultInstance()))
              .build();
        }
      }
    }
    return getGetNodesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.ipfs.grpc_ipfs_lite.RemoveNodeRequest,
      io.ipfs.grpc_ipfs_lite.RemoveNodeResponse> getRemoveNodeMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "RemoveNode",
      requestType = io.ipfs.grpc_ipfs_lite.RemoveNodeRequest.class,
      responseType = io.ipfs.grpc_ipfs_lite.RemoveNodeResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.ipfs.grpc_ipfs_lite.RemoveNodeRequest,
      io.ipfs.grpc_ipfs_lite.RemoveNodeResponse> getRemoveNodeMethod() {
    io.grpc.MethodDescriptor<io.ipfs.grpc_ipfs_lite.RemoveNodeRequest, io.ipfs.grpc_ipfs_lite.RemoveNodeResponse> getRemoveNodeMethod;
    if ((getRemoveNodeMethod = IpfsLiteGrpc.getRemoveNodeMethod) == null) {
      synchronized (IpfsLiteGrpc.class) {
        if ((getRemoveNodeMethod = IpfsLiteGrpc.getRemoveNodeMethod) == null) {
          IpfsLiteGrpc.getRemoveNodeMethod = getRemoveNodeMethod =
              io.grpc.MethodDescriptor.<io.ipfs.grpc_ipfs_lite.RemoveNodeRequest, io.ipfs.grpc_ipfs_lite.RemoveNodeResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "RemoveNode"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  io.ipfs.grpc_ipfs_lite.RemoveNodeRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  io.ipfs.grpc_ipfs_lite.RemoveNodeResponse.getDefaultInstance()))
              .build();
        }
      }
    }
    return getRemoveNodeMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.ipfs.grpc_ipfs_lite.RemoveNodesRequest,
      io.ipfs.grpc_ipfs_lite.RemoveNodesResponse> getRemoveNodesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "RemoveNodes",
      requestType = io.ipfs.grpc_ipfs_lite.RemoveNodesRequest.class,
      responseType = io.ipfs.grpc_ipfs_lite.RemoveNodesResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.ipfs.grpc_ipfs_lite.RemoveNodesRequest,
      io.ipfs.grpc_ipfs_lite.RemoveNodesResponse> getRemoveNodesMethod() {
    io.grpc.MethodDescriptor<io.ipfs.grpc_ipfs_lite.RemoveNodesRequest, io.ipfs.grpc_ipfs_lite.RemoveNodesResponse> getRemoveNodesMethod;
    if ((getRemoveNodesMethod = IpfsLiteGrpc.getRemoveNodesMethod) == null) {
      synchronized (IpfsLiteGrpc.class) {
        if ((getRemoveNodesMethod = IpfsLiteGrpc.getRemoveNodesMethod) == null) {
          IpfsLiteGrpc.getRemoveNodesMethod = getRemoveNodesMethod =
              io.grpc.MethodDescriptor.<io.ipfs.grpc_ipfs_lite.RemoveNodesRequest, io.ipfs.grpc_ipfs_lite.RemoveNodesResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "RemoveNodes"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  io.ipfs.grpc_ipfs_lite.RemoveNodesRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  io.ipfs.grpc_ipfs_lite.RemoveNodesResponse.getDefaultInstance()))
              .build();
        }
      }
    }
    return getRemoveNodesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.ipfs.grpc_ipfs_lite.ResolveLinkRequest,
      io.ipfs.grpc_ipfs_lite.ResolveLinkResponse> getResolveLinkMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ResolveLink",
      requestType = io.ipfs.grpc_ipfs_lite.ResolveLinkRequest.class,
      responseType = io.ipfs.grpc_ipfs_lite.ResolveLinkResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.ipfs.grpc_ipfs_lite.ResolveLinkRequest,
      io.ipfs.grpc_ipfs_lite.ResolveLinkResponse> getResolveLinkMethod() {
    io.grpc.MethodDescriptor<io.ipfs.grpc_ipfs_lite.ResolveLinkRequest, io.ipfs.grpc_ipfs_lite.ResolveLinkResponse> getResolveLinkMethod;
    if ((getResolveLinkMethod = IpfsLiteGrpc.getResolveLinkMethod) == null) {
      synchronized (IpfsLiteGrpc.class) {
        if ((getResolveLinkMethod = IpfsLiteGrpc.getResolveLinkMethod) == null) {
          IpfsLiteGrpc.getResolveLinkMethod = getResolveLinkMethod =
              io.grpc.MethodDescriptor.<io.ipfs.grpc_ipfs_lite.ResolveLinkRequest, io.ipfs.grpc_ipfs_lite.ResolveLinkResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ResolveLink"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  io.ipfs.grpc_ipfs_lite.ResolveLinkRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  io.ipfs.grpc_ipfs_lite.ResolveLinkResponse.getDefaultInstance()))
              .build();
        }
      }
    }
    return getResolveLinkMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.ipfs.grpc_ipfs_lite.TreeRequest,
      io.ipfs.grpc_ipfs_lite.TreeResponse> getTreeMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Tree",
      requestType = io.ipfs.grpc_ipfs_lite.TreeRequest.class,
      responseType = io.ipfs.grpc_ipfs_lite.TreeResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.ipfs.grpc_ipfs_lite.TreeRequest,
      io.ipfs.grpc_ipfs_lite.TreeResponse> getTreeMethod() {
    io.grpc.MethodDescriptor<io.ipfs.grpc_ipfs_lite.TreeRequest, io.ipfs.grpc_ipfs_lite.TreeResponse> getTreeMethod;
    if ((getTreeMethod = IpfsLiteGrpc.getTreeMethod) == null) {
      synchronized (IpfsLiteGrpc.class) {
        if ((getTreeMethod = IpfsLiteGrpc.getTreeMethod) == null) {
          IpfsLiteGrpc.getTreeMethod = getTreeMethod =
              io.grpc.MethodDescriptor.<io.ipfs.grpc_ipfs_lite.TreeRequest, io.ipfs.grpc_ipfs_lite.TreeResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Tree"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  io.ipfs.grpc_ipfs_lite.TreeRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.lite.ProtoLiteUtils.marshaller(
                  io.ipfs.grpc_ipfs_lite.TreeResponse.getDefaultInstance()))
              .build();
        }
      }
    }
    return getTreeMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static IpfsLiteStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<IpfsLiteStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<IpfsLiteStub>() {
        @java.lang.Override
        public IpfsLiteStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new IpfsLiteStub(channel, callOptions);
        }
      };
    return IpfsLiteStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static IpfsLiteBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<IpfsLiteBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<IpfsLiteBlockingStub>() {
        @java.lang.Override
        public IpfsLiteBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new IpfsLiteBlockingStub(channel, callOptions);
        }
      };
    return IpfsLiteBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static IpfsLiteFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<IpfsLiteFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<IpfsLiteFutureStub>() {
        @java.lang.Override
        public IpfsLiteFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new IpfsLiteFutureStub(channel, callOptions);
        }
      };
    return IpfsLiteFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class IpfsLiteImplBase implements io.grpc.BindableService {

    /**
     */
    public io.grpc.stub.StreamObserver<io.ipfs.grpc_ipfs_lite.AddFileRequest> addFile(
        io.grpc.stub.StreamObserver<io.ipfs.grpc_ipfs_lite.AddFileResponse> responseObserver) {
      return asyncUnimplementedStreamingCall(getAddFileMethod(), responseObserver);
    }

    /**
     */
    public void getFile(io.ipfs.grpc_ipfs_lite.GetFileRequest request,
        io.grpc.stub.StreamObserver<io.ipfs.grpc_ipfs_lite.GetFileResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getGetFileMethod(), responseObserver);
    }

    /**
     */
    public void hasBlock(io.ipfs.grpc_ipfs_lite.HasBlockRequest request,
        io.grpc.stub.StreamObserver<io.ipfs.grpc_ipfs_lite.HasBlockResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getHasBlockMethod(), responseObserver);
    }

    /**
     */
    public void addNode(io.ipfs.grpc_ipfs_lite.AddNodeRequest request,
        io.grpc.stub.StreamObserver<io.ipfs.grpc_ipfs_lite.AddNodeResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getAddNodeMethod(), responseObserver);
    }

    /**
     */
    public void addNodes(io.ipfs.grpc_ipfs_lite.AddNodesRequest request,
        io.grpc.stub.StreamObserver<io.ipfs.grpc_ipfs_lite.AddNodesResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getAddNodesMethod(), responseObserver);
    }

    /**
     */
    public void getNode(io.ipfs.grpc_ipfs_lite.GetNodeRequest request,
        io.grpc.stub.StreamObserver<io.ipfs.grpc_ipfs_lite.GetNodeResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getGetNodeMethod(), responseObserver);
    }

    /**
     */
    public void getNodes(io.ipfs.grpc_ipfs_lite.GetNodesRequest request,
        io.grpc.stub.StreamObserver<io.ipfs.grpc_ipfs_lite.GetNodesResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getGetNodesMethod(), responseObserver);
    }

    /**
     */
    public void removeNode(io.ipfs.grpc_ipfs_lite.RemoveNodeRequest request,
        io.grpc.stub.StreamObserver<io.ipfs.grpc_ipfs_lite.RemoveNodeResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getRemoveNodeMethod(), responseObserver);
    }

    /**
     */
    public void removeNodes(io.ipfs.grpc_ipfs_lite.RemoveNodesRequest request,
        io.grpc.stub.StreamObserver<io.ipfs.grpc_ipfs_lite.RemoveNodesResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getRemoveNodesMethod(), responseObserver);
    }

    /**
     */
    public void resolveLink(io.ipfs.grpc_ipfs_lite.ResolveLinkRequest request,
        io.grpc.stub.StreamObserver<io.ipfs.grpc_ipfs_lite.ResolveLinkResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getResolveLinkMethod(), responseObserver);
    }

    /**
     */
    public void tree(io.ipfs.grpc_ipfs_lite.TreeRequest request,
        io.grpc.stub.StreamObserver<io.ipfs.grpc_ipfs_lite.TreeResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getTreeMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getAddFileMethod(),
            asyncClientStreamingCall(
              new MethodHandlers<
                io.ipfs.grpc_ipfs_lite.AddFileRequest,
                io.ipfs.grpc_ipfs_lite.AddFileResponse>(
                  this, METHODID_ADD_FILE)))
          .addMethod(
            getGetFileMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                io.ipfs.grpc_ipfs_lite.GetFileRequest,
                io.ipfs.grpc_ipfs_lite.GetFileResponse>(
                  this, METHODID_GET_FILE)))
          .addMethod(
            getHasBlockMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                io.ipfs.grpc_ipfs_lite.HasBlockRequest,
                io.ipfs.grpc_ipfs_lite.HasBlockResponse>(
                  this, METHODID_HAS_BLOCK)))
          .addMethod(
            getAddNodeMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                io.ipfs.grpc_ipfs_lite.AddNodeRequest,
                io.ipfs.grpc_ipfs_lite.AddNodeResponse>(
                  this, METHODID_ADD_NODE)))
          .addMethod(
            getAddNodesMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                io.ipfs.grpc_ipfs_lite.AddNodesRequest,
                io.ipfs.grpc_ipfs_lite.AddNodesResponse>(
                  this, METHODID_ADD_NODES)))
          .addMethod(
            getGetNodeMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                io.ipfs.grpc_ipfs_lite.GetNodeRequest,
                io.ipfs.grpc_ipfs_lite.GetNodeResponse>(
                  this, METHODID_GET_NODE)))
          .addMethod(
            getGetNodesMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                io.ipfs.grpc_ipfs_lite.GetNodesRequest,
                io.ipfs.grpc_ipfs_lite.GetNodesResponse>(
                  this, METHODID_GET_NODES)))
          .addMethod(
            getRemoveNodeMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                io.ipfs.grpc_ipfs_lite.RemoveNodeRequest,
                io.ipfs.grpc_ipfs_lite.RemoveNodeResponse>(
                  this, METHODID_REMOVE_NODE)))
          .addMethod(
            getRemoveNodesMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                io.ipfs.grpc_ipfs_lite.RemoveNodesRequest,
                io.ipfs.grpc_ipfs_lite.RemoveNodesResponse>(
                  this, METHODID_REMOVE_NODES)))
          .addMethod(
            getResolveLinkMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                io.ipfs.grpc_ipfs_lite.ResolveLinkRequest,
                io.ipfs.grpc_ipfs_lite.ResolveLinkResponse>(
                  this, METHODID_RESOLVE_LINK)))
          .addMethod(
            getTreeMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                io.ipfs.grpc_ipfs_lite.TreeRequest,
                io.ipfs.grpc_ipfs_lite.TreeResponse>(
                  this, METHODID_TREE)))
          .build();
    }
  }

  /**
   */
  public static final class IpfsLiteStub extends io.grpc.stub.AbstractAsyncStub<IpfsLiteStub> {
    private IpfsLiteStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected IpfsLiteStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new IpfsLiteStub(channel, callOptions);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<io.ipfs.grpc_ipfs_lite.AddFileRequest> addFile(
        io.grpc.stub.StreamObserver<io.ipfs.grpc_ipfs_lite.AddFileResponse> responseObserver) {
      return asyncClientStreamingCall(
          getChannel().newCall(getAddFileMethod(), getCallOptions()), responseObserver);
    }

    /**
     */
    public void getFile(io.ipfs.grpc_ipfs_lite.GetFileRequest request,
        io.grpc.stub.StreamObserver<io.ipfs.grpc_ipfs_lite.GetFileResponse> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getGetFileMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void hasBlock(io.ipfs.grpc_ipfs_lite.HasBlockRequest request,
        io.grpc.stub.StreamObserver<io.ipfs.grpc_ipfs_lite.HasBlockResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getHasBlockMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void addNode(io.ipfs.grpc_ipfs_lite.AddNodeRequest request,
        io.grpc.stub.StreamObserver<io.ipfs.grpc_ipfs_lite.AddNodeResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getAddNodeMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void addNodes(io.ipfs.grpc_ipfs_lite.AddNodesRequest request,
        io.grpc.stub.StreamObserver<io.ipfs.grpc_ipfs_lite.AddNodesResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getAddNodesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getNode(io.ipfs.grpc_ipfs_lite.GetNodeRequest request,
        io.grpc.stub.StreamObserver<io.ipfs.grpc_ipfs_lite.GetNodeResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetNodeMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getNodes(io.ipfs.grpc_ipfs_lite.GetNodesRequest request,
        io.grpc.stub.StreamObserver<io.ipfs.grpc_ipfs_lite.GetNodesResponse> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getGetNodesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void removeNode(io.ipfs.grpc_ipfs_lite.RemoveNodeRequest request,
        io.grpc.stub.StreamObserver<io.ipfs.grpc_ipfs_lite.RemoveNodeResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getRemoveNodeMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void removeNodes(io.ipfs.grpc_ipfs_lite.RemoveNodesRequest request,
        io.grpc.stub.StreamObserver<io.ipfs.grpc_ipfs_lite.RemoveNodesResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getRemoveNodesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void resolveLink(io.ipfs.grpc_ipfs_lite.ResolveLinkRequest request,
        io.grpc.stub.StreamObserver<io.ipfs.grpc_ipfs_lite.ResolveLinkResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getResolveLinkMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void tree(io.ipfs.grpc_ipfs_lite.TreeRequest request,
        io.grpc.stub.StreamObserver<io.ipfs.grpc_ipfs_lite.TreeResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getTreeMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class IpfsLiteBlockingStub extends io.grpc.stub.AbstractBlockingStub<IpfsLiteBlockingStub> {
    private IpfsLiteBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected IpfsLiteBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new IpfsLiteBlockingStub(channel, callOptions);
    }

    /**
     */
    public java.util.Iterator<io.ipfs.grpc_ipfs_lite.GetFileResponse> getFile(
        io.ipfs.grpc_ipfs_lite.GetFileRequest request) {
      return blockingServerStreamingCall(
          getChannel(), getGetFileMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.ipfs.grpc_ipfs_lite.HasBlockResponse hasBlock(io.ipfs.grpc_ipfs_lite.HasBlockRequest request) {
      return blockingUnaryCall(
          getChannel(), getHasBlockMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.ipfs.grpc_ipfs_lite.AddNodeResponse addNode(io.ipfs.grpc_ipfs_lite.AddNodeRequest request) {
      return blockingUnaryCall(
          getChannel(), getAddNodeMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.ipfs.grpc_ipfs_lite.AddNodesResponse addNodes(io.ipfs.grpc_ipfs_lite.AddNodesRequest request) {
      return blockingUnaryCall(
          getChannel(), getAddNodesMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.ipfs.grpc_ipfs_lite.GetNodeResponse getNode(io.ipfs.grpc_ipfs_lite.GetNodeRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetNodeMethod(), getCallOptions(), request);
    }

    /**
     */
    public java.util.Iterator<io.ipfs.grpc_ipfs_lite.GetNodesResponse> getNodes(
        io.ipfs.grpc_ipfs_lite.GetNodesRequest request) {
      return blockingServerStreamingCall(
          getChannel(), getGetNodesMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.ipfs.grpc_ipfs_lite.RemoveNodeResponse removeNode(io.ipfs.grpc_ipfs_lite.RemoveNodeRequest request) {
      return blockingUnaryCall(
          getChannel(), getRemoveNodeMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.ipfs.grpc_ipfs_lite.RemoveNodesResponse removeNodes(io.ipfs.grpc_ipfs_lite.RemoveNodesRequest request) {
      return blockingUnaryCall(
          getChannel(), getRemoveNodesMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.ipfs.grpc_ipfs_lite.ResolveLinkResponse resolveLink(io.ipfs.grpc_ipfs_lite.ResolveLinkRequest request) {
      return blockingUnaryCall(
          getChannel(), getResolveLinkMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.ipfs.grpc_ipfs_lite.TreeResponse tree(io.ipfs.grpc_ipfs_lite.TreeRequest request) {
      return blockingUnaryCall(
          getChannel(), getTreeMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class IpfsLiteFutureStub extends io.grpc.stub.AbstractFutureStub<IpfsLiteFutureStub> {
    private IpfsLiteFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected IpfsLiteFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new IpfsLiteFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.ipfs.grpc_ipfs_lite.HasBlockResponse> hasBlock(
        io.ipfs.grpc_ipfs_lite.HasBlockRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getHasBlockMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.ipfs.grpc_ipfs_lite.AddNodeResponse> addNode(
        io.ipfs.grpc_ipfs_lite.AddNodeRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getAddNodeMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.ipfs.grpc_ipfs_lite.AddNodesResponse> addNodes(
        io.ipfs.grpc_ipfs_lite.AddNodesRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getAddNodesMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.ipfs.grpc_ipfs_lite.GetNodeResponse> getNode(
        io.ipfs.grpc_ipfs_lite.GetNodeRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetNodeMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.ipfs.grpc_ipfs_lite.RemoveNodeResponse> removeNode(
        io.ipfs.grpc_ipfs_lite.RemoveNodeRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getRemoveNodeMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.ipfs.grpc_ipfs_lite.RemoveNodesResponse> removeNodes(
        io.ipfs.grpc_ipfs_lite.RemoveNodesRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getRemoveNodesMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.ipfs.grpc_ipfs_lite.ResolveLinkResponse> resolveLink(
        io.ipfs.grpc_ipfs_lite.ResolveLinkRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getResolveLinkMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.ipfs.grpc_ipfs_lite.TreeResponse> tree(
        io.ipfs.grpc_ipfs_lite.TreeRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getTreeMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_GET_FILE = 0;
  private static final int METHODID_HAS_BLOCK = 1;
  private static final int METHODID_ADD_NODE = 2;
  private static final int METHODID_ADD_NODES = 3;
  private static final int METHODID_GET_NODE = 4;
  private static final int METHODID_GET_NODES = 5;
  private static final int METHODID_REMOVE_NODE = 6;
  private static final int METHODID_REMOVE_NODES = 7;
  private static final int METHODID_RESOLVE_LINK = 8;
  private static final int METHODID_TREE = 9;
  private static final int METHODID_ADD_FILE = 10;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final IpfsLiteImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(IpfsLiteImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_FILE:
          serviceImpl.getFile((io.ipfs.grpc_ipfs_lite.GetFileRequest) request,
              (io.grpc.stub.StreamObserver<io.ipfs.grpc_ipfs_lite.GetFileResponse>) responseObserver);
          break;
        case METHODID_HAS_BLOCK:
          serviceImpl.hasBlock((io.ipfs.grpc_ipfs_lite.HasBlockRequest) request,
              (io.grpc.stub.StreamObserver<io.ipfs.grpc_ipfs_lite.HasBlockResponse>) responseObserver);
          break;
        case METHODID_ADD_NODE:
          serviceImpl.addNode((io.ipfs.grpc_ipfs_lite.AddNodeRequest) request,
              (io.grpc.stub.StreamObserver<io.ipfs.grpc_ipfs_lite.AddNodeResponse>) responseObserver);
          break;
        case METHODID_ADD_NODES:
          serviceImpl.addNodes((io.ipfs.grpc_ipfs_lite.AddNodesRequest) request,
              (io.grpc.stub.StreamObserver<io.ipfs.grpc_ipfs_lite.AddNodesResponse>) responseObserver);
          break;
        case METHODID_GET_NODE:
          serviceImpl.getNode((io.ipfs.grpc_ipfs_lite.GetNodeRequest) request,
              (io.grpc.stub.StreamObserver<io.ipfs.grpc_ipfs_lite.GetNodeResponse>) responseObserver);
          break;
        case METHODID_GET_NODES:
          serviceImpl.getNodes((io.ipfs.grpc_ipfs_lite.GetNodesRequest) request,
              (io.grpc.stub.StreamObserver<io.ipfs.grpc_ipfs_lite.GetNodesResponse>) responseObserver);
          break;
        case METHODID_REMOVE_NODE:
          serviceImpl.removeNode((io.ipfs.grpc_ipfs_lite.RemoveNodeRequest) request,
              (io.grpc.stub.StreamObserver<io.ipfs.grpc_ipfs_lite.RemoveNodeResponse>) responseObserver);
          break;
        case METHODID_REMOVE_NODES:
          serviceImpl.removeNodes((io.ipfs.grpc_ipfs_lite.RemoveNodesRequest) request,
              (io.grpc.stub.StreamObserver<io.ipfs.grpc_ipfs_lite.RemoveNodesResponse>) responseObserver);
          break;
        case METHODID_RESOLVE_LINK:
          serviceImpl.resolveLink((io.ipfs.grpc_ipfs_lite.ResolveLinkRequest) request,
              (io.grpc.stub.StreamObserver<io.ipfs.grpc_ipfs_lite.ResolveLinkResponse>) responseObserver);
          break;
        case METHODID_TREE:
          serviceImpl.tree((io.ipfs.grpc_ipfs_lite.TreeRequest) request,
              (io.grpc.stub.StreamObserver<io.ipfs.grpc_ipfs_lite.TreeResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_ADD_FILE:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.addFile(
              (io.grpc.stub.StreamObserver<io.ipfs.grpc_ipfs_lite.AddFileResponse>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (IpfsLiteGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .addMethod(getAddFileMethod())
              .addMethod(getGetFileMethod())
              .addMethod(getHasBlockMethod())
              .addMethod(getAddNodeMethod())
              .addMethod(getAddNodesMethod())
              .addMethod(getGetNodeMethod())
              .addMethod(getGetNodesMethod())
              .addMethod(getRemoveNodeMethod())
              .addMethod(getRemoveNodesMethod())
              .addMethod(getResolveLinkMethod())
              .addMethod(getTreeMethod())
              .build();
        }
      }
    }
    return result;
  }
}

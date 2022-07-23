package threads.lite.crypto;

import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import crypto.pb.Crypto;
import threads.lite.LogUtils;

public interface Key {
    String TAG = Key.class.getSimpleName();

    @NonNull
    static byte[] marshalPublicKey(@NonNull PubKey pubKey) {
        return Crypto.PublicKey.newBuilder().setType(pubKey.getKeyType()).
                setData(ByteString.copyFrom(pubKey.raw())).build().toByteArray();
    }


    @NonNull
    static byte[] marshalPrivateKey(@NonNull PrivKey privKey) {
        return Crypto.PrivateKey.newBuilder().setType(privKey.getKeyType()).
                setData(ByteString.copyFrom(privKey.raw())).build().toByteArray();
    }


    static PubKey unmarshalPublicKey(byte[] data) throws InvalidProtocolBufferException {

        try {
            Crypto.PublicKey pms = Crypto.PublicKey.parseFrom(data);

            byte[] pubKeyData = pms.getData().toByteArray();

            switch (pms.getType()) {
                case RSA:
                    return Rsa.unmarshalRsaPublicKey(pubKeyData);
                case ECDSA:
                    return Ecdsa.unmarshalEcdsaPublicKey(pubKeyData);
                case Secp256k1:
                    return Secp256k1.unmarshalSecp256k1PublicKey(pubKeyData);
                case Ed25519:
                    return Ed25519.unmarshalEd25519PublicKey(pubKeyData);
                default:
                    throw new RuntimeException("BadKeyTypeException");
            }
        } catch (Throwable throwable) {
            LogUtils.error(TAG, throwable);
            throw throwable;
        }
    }

    @NonNull
    byte[] raw();

}



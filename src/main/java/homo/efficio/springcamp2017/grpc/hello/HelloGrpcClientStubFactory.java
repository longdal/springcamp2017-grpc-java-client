package homo.efficio.springcamp2017.grpc.hello;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.linecorp.location.protocol.LocationApisGrpc;

/**
 * @author homo.efficio@gmail.com
 *         created on 2017-04-12
 */
public class HelloGrpcClientStubFactory {

    private final Logger logger = Logger.getLogger(HelloGrpcClientStubFactory.class.getName());

    private final ManagedChannel channel;
    private LocationApisGrpc.LocationApisBlockingStub blockingStub;
    private final LocationApisGrpc.LocationApisStub asyncStub;
    private final LocationApisGrpc.LocationApisFutureStub futureStub;

    /*
     * private final HelloSpringCampGrpc.HelloSpringCampBlockingStub blockingStub;
     * private final HelloSpringCampGrpc.HelloSpringCampStub asyncStub; private
     * final HelloSpringCampGrpc.HelloSpringCampFutureStub futureStub;
     */
    //Base64.getEncoder().withoutPadding().encodeToString(digested)

    public String sha256Encode(final String clearText) throws NoSuchAlgorithmException {
        return new String(Base64.getEncoder().withoutPadding()
                .encode(MessageDigest.getInstance("SHA-256").digest(clearText.getBytes(StandardCharsets.UTF_8))));
    }

    public HelloGrpcClientStubFactory(String host, int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext(true).build();

        String beta_secret = "6oQPKzYUZEDNIlCBMXigKA";
        String beta_token = "YmWd6SI5rlGVbl1z7feoESxJK5oR0+/dSZHpplYGC+0";

        Metadata header = new Metadata();
        
        // String timestamp = Long.toString(System.currentTimeMillis());
        String timestamp = "1586482465712";
        Metadata.Key<String> timestampKey = Metadata.Key.of("X-Loc-Timestamp", Metadata.ASCII_STRING_MARSHALLER);
        header.put(timestampKey, timestamp);
        logger.info("timestamp:"+timestamp);

        Metadata.Key<String> tokenKey = Metadata.Key.of("X-Loc-Token", Metadata.ASCII_STRING_MARSHALLER);
        header.put(tokenKey, beta_token);

        String sigature = beta_token + timestamp + beta_secret;
        String shaSig;
        try {
            shaSig = this.sha256Encode(sigature);
            logger.info("sig:"+shaSig);
            Metadata.Key<String> signatureKey = Metadata.Key.of("X-Loc-Signature", Metadata.ASCII_STRING_MARSHALLER);
            header.put(signatureKey, shaSig);
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        this.blockingStub = LocationApisGrpc.newBlockingStub(channel);
        this.blockingStub = MetadataUtils.attachHeaders(this.blockingStub, header);

        this.asyncStub = LocationApisGrpc.newStub(channel);
        this.futureStub = LocationApisGrpc.newFutureStub(channel);

        /*
        this.blockingStub = HelloSpringCampGrpc.newBlockingStub(channel);
        this.asyncStub = HelloSpringCampGrpc.newStub(channel);
        this.futureStub = HelloSpringCampGrpc.newFutureStub(channel);
        */
    }

    public void shutdownChannel() throws InterruptedException {
        logger.info("gRPC Channel shutdown...");
        this.channel.shutdown().awaitTermination(2, TimeUnit.SECONDS);
    }

    public LocationApisGrpc.LocationApisBlockingStub getBlockingStub(){
        return blockingStub;
    } 

    public LocationApisGrpc.LocationApisStub getAsyncStub(){
        return asyncStub;
    }

    public LocationApisGrpc.LocationApisFutureStub getFutureStub(){
        return futureStub; 
    } 

    /*
    public HelloSpringCampGrpc.HelloSpringCampBlockingStub getBlockingStub() {
        return blockingStub;
    }

    public HelloSpringCampGrpc.HelloSpringCampStub getAsyncStub() {
        return asyncStub;
    }

    public HelloSpringCampGrpc.HelloSpringCampFutureStub getFutureStub() {
        return futureStub;
    }
    */
}

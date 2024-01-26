package graphstorage;



import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig  {
    @Bean
    public RestHighLevelClient Client() {
        return new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")));
    }
}


//
//
//import co.elastic.clients.elasticsearch.ElasticsearchClient;
//import co.elastic.clients.json.jackson.JacksonJsonpMapper;
//import co.elastic.clients.transport.ElasticsearchTransport;
//import co.elastic.clients.transport.rest_client.RestClientTransport;
//import org.apache.http.HttpHost;
//import org.apache.http.auth.AuthScope;
//import org.apache.http.auth.UsernamePasswordCredentials;
//import org.apache.http.client.CredentialsProvider;
//import org.apache.http.conn.ssl.TrustAllStrategy;
//import org.apache.http.impl.client.BasicCredentialsProvider;
//import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
//import org.elasticsearch.client.RestClient;
//import org.elasticsearch.client.RestClientBuilder;
//import org.springframework.stereotype.Component;
//
//
/////////////////
//import org.apache.http.HttpHost;
//import org.apache.http.ssl.SSLContextBuilder;
//import org.elasticsearch.client.RestClient;
////import org.elasticsearch.client.RestHighLevelClient;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.apache.http.ssl.SSLContexts;
//
//
//import javax.net.ssl.SSLContext;
//import java.security.KeyManagementException;
//import java.security.KeyStoreException;
//import java.security.NoSuchAlgorithmException;
//import java.security.UnrecoverableKeyException;
//import java.security.cert.CertificateException;
//import java.security.cert.X509Certificate;
//
//
//@Component
//public class ElasticsearchConfig {
//
//    private static ElasticsearchClient elasticsearchClient;
//
//    public static ElasticsearchClient getClient() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
//        if (elasticsearchClient == null) {
//            elasticsearchClient = getElasticClient();
//        }
//        return elasticsearchClient;
//
//    }
//    
//    private static ElasticsearchClient getElasticClient() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
//    	
//
//	    SSLContext sslContext = SSLContexts.custom()
//	            .loadTrustMaterial(new TrustAllStrategy())
//	            .build();
//    	
//        RestClient restClient = RestClient.builder(
//                new HttpHost("localhost", 9200,"https"))
//                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
//                    @Override
//                    public HttpAsyncClientBuilder customizeHttpClient(
//                            HttpAsyncClientBuilder httpClientBuilder) {
//                        return httpClientBuilder
//                                .setDefaultCredentialsProvider(getCredentialsProvider()).setSSLContext(sslContext);
//                    }
//                })
//                .build();
//
//        // Create the transport with a Jackson mapper
//        ElasticsearchTransport transport = new RestClientTransport(
//                restClient, new JacksonJsonpMapper());
//
//        // And create the API client
//        return new ElasticsearchClient(transport);
//    }
//
//    private static CredentialsProvider getCredentialsProvider() {
//        CredentialsProvider credentialsProvider =
//                new BasicCredentialsProvider();
//        credentialsProvider.setCredentials(AuthScope.ANY,
//        new UsernamePasswordCredentials("elastic", "XJHq6e=hrsC3__wjUPIf"));
//        return credentialsProvider;
//        }
//}

//import org.apache.http.HttpHost;
//import org.apache.http.auth.AuthScope;
//import org.apache.http.auth.UsernamePasswordCredentials;
//import org.apache.http.client.CredentialsProvider;
//import org.apache.http.conn.ssl.TrustAllStrategy;
//import org.apache.http.impl.client.BasicCredentialsProvider;
//import org.elasticsearch.client.RestClient;
//import org.elasticsearch.client.RestHighLevelClient;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//
////@Configuration
////public class ElasticsearchConfig {
////
////
////    @Bean
////    public RestHighLevelClient client() {
////        return new RestHighLevelClient(
////            RestClient.builder(
////                new HttpHost("localhost", 9200, "http")));
////    }
////}
//
//
//import org.apache.http.ssl.SSLContexts;
//
//
//import javax.net.ssl.SSLContext;
//import java.security.KeyManagementException;
//import java.security.KeyStoreException;
//import java.security.NoSuchAlgorithmException;
//
//
//@Configuration
//public class ElasticsearchConfig {
//	@Bean
//	public RestHighLevelClient client() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
//	    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
//	    credentialsProvider.setCredentials(AuthScope.ANY,
//	            new UsernamePasswordCredentials("elastic", "5=pNryQdAEsUTyrnmlH3"));
//
//	    SSLContext sslContext = SSLContexts.custom()
//	            .loadTrustMaterial(new TrustAllStrategy())
//	            .build();
//
//	    return new RestHighLevelClient(
//	            RestClient.builder(
//	                    new HttpHost("localhost", 9200, "https"))
//	                    .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
//	                            .setDefaultCredentialsProvider(credentialsProvider)
//	                            .setSSLContext(sslContext)
//	                    ));
//	}
//
//
//}
//

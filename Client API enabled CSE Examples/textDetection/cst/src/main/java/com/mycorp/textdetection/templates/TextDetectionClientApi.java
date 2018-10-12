package com.mycorp.textdetection.templates;

import static com.mycorp.textdetection.templates.TextDetectionConnectedSystemTemplate.API_KEY;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.appiancorp.connectedsystems.simplified.sdk.SimpleClientApi;
import com.appiancorp.connectedsystems.simplified.sdk.SimpleClientApiRequest;
import com.appiancorp.connectedsystems.templateframework.sdk.ClientApiResponse;
import com.appiancorp.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appiancorp.connectedsystems.templateframework.sdk.TemplateId;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.mycorp.textdetection.AnnotationResponse;
import com.mycorp.textdetection.OutputResponse;


/**
 * This is an example of a Client API that performs an operation when executed from a
 * Certified SAIL extension (CSE). The Client API accepts a data structure which contains
 * the CSE request payload as well as the data stored inside the Connected System object.
 * It uses both pieces to perform the operation.
 *
 * In this example, the Connected System stores a secret value, the Google API key, which
 * the Client API uses to submit a request to the Google Text Detection API. The Client API
 * then parses the response and returns a map of coordinates and the text found back to the CSE.
 */

@TemplateId(name = "TextDetectionClientApi")
public class TextDetectionClientApi extends SimpleClientApi {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().configure(
      DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  // Payload key in CSE request
  private static final String IMAGE_URL_KEY = "imageUrl";

  // Google Text Detection endpoint
  private static final String IMAGE_ANNOTATE_ENDPOINT = "https://vision.googleapis.com/v1/images:annotate";
  private static final String PARAMETER_KEY = "key";

  // Request body to use when POSTing a request to Google API
  private static final String REQUEST_ENTITY =
          "{" +
          "  \"requests\": [" +
          "    {" +
          "      \"image\": {" +
          "        \"source\": {" +
          "          \"imageUri\": \"%s\"" +
          "        }" +
          "      }," +
          "      \"features\": [" +
          "        {" +
          "          \"type\": \"TEXT_DETECTION\"" +
          "        }" +
          "      ]," +
          "     \"imageContext\": {" +
          "        \"languageHints\": [\"en-t-i0-handwrit\"]" +
          "      }" +
          "    }" +
          "  ]" +
          "}";

  @Override
  protected ClientApiResponse execute(
      SimpleClientApiRequest simpleClientApiRequest, ExecutionContext executionContext) {

    // API key to access Google Text Detection API
    String apiKey = simpleClientApiRequest.getConnectedSystemConfiguration().getValue(API_KEY);

    // Image URL provided by the CSE
    String imageUrl = (String)simpleClientApiRequest.getPayload().get(IMAGE_URL_KEY);

    Map<String,Object> resultMap;

    try {
      AnnotationResponse annotationResponse = detectProperties(imageUrl, apiKey);
      OutputResponse outputResponse = OutputResponse.fromAnnotationResponse(annotationResponse);
      resultMap = ImmutableMap.of("outputResponse", outputResponse);
    } catch (Exception e) {
      resultMap = ImmutableMap.of("error", e.getLocalizedMessage());
    }

    return new ClientApiResponse(resultMap);
  }

  /**
   * Makes the call out to the Google Text Detection endpoint via HTTP POST.
   *
   * Converts the HttpResponse into an AnnotationResponse.
   */
  private AnnotationResponse detectProperties(String imageUrl, String apiKey) throws Exception {
    HttpPost post = createRequest(imageUrl, apiKey);
    HttpClient httpClient = HttpClientBuilder.create().build();

    // Make a REST call to Google Text Detection endpoint
    HttpResponse httpResponse = httpClient.execute(post);

    if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
      String responseJson = EntityUtils.toString(httpResponse.getEntity());
      throw new RuntimeException(responseJson);
    }

    String responseJson = EntityUtils.toString(httpResponse.getEntity());
    AnnotationResponse annotationResponse = OBJECT_MAPPER.readValue(responseJson, AnnotationResponse.class);
    return annotationResponse;
  }

  private HttpPost createRequest(String imageUrl, String apiKey)
      throws URISyntaxException, UnsupportedEncodingException {
    URIBuilder builder = new URIBuilder(IMAGE_ANNOTATE_ENDPOINT);
    builder.addParameter(PARAMETER_KEY, apiKey);

    HttpPost httpPost = new HttpPost(builder.build());
    httpPost.setEntity(new StringEntity(String.format(REQUEST_ENTITY, imageUrl)));
    return httpPost;
  }

}

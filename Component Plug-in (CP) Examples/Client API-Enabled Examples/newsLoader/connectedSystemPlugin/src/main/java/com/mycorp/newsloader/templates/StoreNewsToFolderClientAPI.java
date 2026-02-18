package com.mycorp.newsloader.templates;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import com.appian.connectedsystems.simplified.sdk.SimpleClientApi;
import com.appian.connectedsystems.simplified.sdk.SimpleClientApiRequest;
import com.appian.connectedsystems.templateframework.sdk.ClientApiResponse;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appiancorp.suiteapi.content.ContentConstants;
import com.appiancorp.suiteapi.content.ContentService;
import com.appiancorp.suiteapi.content.ContentUploadOutputStream;
import com.appiancorp.suiteapi.knowledge.Document;
import com.appiancorp.suiteapi.type.TypedValue;
import com.appiancorp.type.AppianTypeLong;

/**
 * This is an example of a Client API that performs an operation when executed from a
 * Component Plug-in (CP). The Client API accepts a data structure which contains
 * the CP request payload as well as the data stored inside the Connected System object.
 * It uses both pieces to perform the operation.
 *
 * In this example, Client API will reviece news and store it as file in the defined folder
 * Returns ID of newly stored news file
 */

@TemplateId(name="StoreNewsClientApi")
public class StoreNewsToFolderClientAPI extends SimpleClientApi {
  private final ContentService contentService;

  public StoreNewsToFolderClientAPI(ContentService contentService){
    this.contentService = contentService;
  }
  @Override
  protected ClientApiResponse execute(SimpleClientApiRequest simpleClientApiRequest, ExecutionContext executionContext) {

    // News payload
    String newsPayload = (String)simpleClientApiRequest.getPayload().getOrDefault("news", "{}");

    // permission to add news (secured)
    Boolean canAddNewNews = (Boolean)simpleClientApiRequest.getSecuredPayload().getOrDefault("canAddNews", false);

    // folder to store news file (TypedValue)
    TypedValue folderCons = (TypedValue)simpleClientApiRequest.getSecuredPayload().getOrDefault("saveNewsToFolder", new TypedValue());

    if(folderCons.getValue() == null){
      throw new IllegalStateException("saveNewsToFolder should be null.");
    }

    if(!AppianTypeLong.FOLDER.equals(folderCons.getTypeRef().getId())){
      throw new IllegalStateException("saveNewsToFolder should be set to a constant pointing to a folder.");
    }

    if(canAddNewNews){
      Document newsDoc = new Document();
      newsDoc.setName(UUID.randomUUID().toString());
      newsDoc.setExtension("json");
      newsDoc.setParent((Long)folderCons.getValue());

      // uploading news document to the defined folder
      try (ContentUploadOutputStream cos = contentService.uploadDocument(newsDoc, ContentConstants.UNIQUE_NONE)) {
        cos.write(newsPayload.getBytes(StandardCharsets.UTF_8));
        Long newsDocId = cos.getContentId();

        return new ClientApiResponse(
            Map.of(
                "newsDocId", newsDocId
            )
        );
      } catch (Exception e) {
        throw new RuntimeException("Failed to store news to folder.");
      }

    }
    throw new IllegalStateException("You don't have permission to add news.");
  }
}

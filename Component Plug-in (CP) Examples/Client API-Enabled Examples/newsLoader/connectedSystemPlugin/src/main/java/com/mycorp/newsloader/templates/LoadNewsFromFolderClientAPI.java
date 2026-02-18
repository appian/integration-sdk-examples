package com.mycorp.newsloader.templates;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.appian.connectedsystems.simplified.sdk.SimpleClientApi;
import com.appian.connectedsystems.simplified.sdk.SimpleClientApiRequest;
import com.appian.connectedsystems.templateframework.sdk.ClientApiResponse;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appiancorp.suiteapi.common.exceptions.AppianStorageException;
import com.appiancorp.suiteapi.content.ContentFilter;
import com.appiancorp.suiteapi.content.ContentService;
import com.appiancorp.suiteapi.content.DocumentInputStream;
import com.appiancorp.suiteapi.content.exceptions.InvalidContentException;
import com.appiancorp.suiteapi.content.exceptions.InvalidTypeMaskException;
import com.appiancorp.suiteapi.type.TypedValue;
import com.appiancorp.type.AppianTypeLong;

/**
 * This is an example of a Client API that performs an operation when executed from a
 * Component Plug-in (CP). The Client API accepts a data structure which contains
 * the CP request payload as well as the data stored inside the Connected System object.
 * It uses both pieces to perform the operation.
 *
 * In this example, Client API gets folder reference (TypedValue) for the request payload
 * and return all the news stored in the files in that folder.
 */

@TemplateId(name = "LoadNewsClientApi")
public class LoadNewsFromFolderClientAPI extends SimpleClientApi {

  private final ContentService contentService;

  public LoadNewsFromFolderClientAPI(ContentService contentService){
    this.contentService = contentService;
  }

  @Override
  protected ClientApiResponse execute(SimpleClientApiRequest simpleClientApiRequest, ExecutionContext executionContext){

    // Folder reference provided in the request's secured payload.
    TypedValue folderCons = (TypedValue)simpleClientApiRequest.getSecuredPayload().getOrDefault("saveNewsToFolder", new TypedValue());
    if(folderCons.getValue() == null){
      throw new IllegalStateException("saveNewsToFolder should be null.");
    }

    if(!AppianTypeLong.FOLDER.equals(folderCons.getTypeRef().getId())){
      throw new IllegalStateException("saveNewsToFolder should be set to a constant pointing to a folder.");
    }

    Long folderId = (Long) folderCons.getValue();
    List<String> newsJson = new ArrayList<>();

    try {
      // Getting all the files IDs stored in that folder
      Long[] docIds = contentService.getAllChildrenIds(folderId, ContentFilter.DOCUMENTS, 0);

      for(Long id: docIds){
        // Reading file content via file ID.
        DocumentInputStream docStream = contentService.getDocumentInputStream(id);

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(docStream, StandardCharsets.UTF_8))) {
          String news = reader.lines().collect(Collectors.joining("\n"));
          newsJson.add(news);
        } catch ( IOException e) {
          throw new IllegalStateException("Cannot read files in saveNewsToFolder's folder.");
        }
      }

    } catch (InvalidContentException | InvalidTypeMaskException | AppianStorageException e) {
      throw new RuntimeException(e);
    }
    return new ClientApiResponse(Map.of(
        "news", newsJson
    ));
  }
}

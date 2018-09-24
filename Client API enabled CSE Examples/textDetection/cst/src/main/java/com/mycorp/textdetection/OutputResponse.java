package com.mycorp.textdetection;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Object representation of the response we want to pass back to the CSE
 */
public class OutputResponse {
  public List<String> textFound;
  public List<BoundingBox> boundingBoxes;

  public static class BoundingBox {
    public int x;
    public int y;
    public int width;
    public int height;

    private BoundingBox(int x, int y, int width, int height) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
    }

    private static BoundingBox of(int x, int y, int width, int height) {
      return new BoundingBox(x, y, width, height);
    }

    /**
     * Converts the Google bounding boxes to what the CSE expects
     * [(topLeft)[x,y], (topRight)[x,y], (bottomRight)[x,y], (bottomLeft)[x,y]] -> [left, top, width, height]
     */
    static BoundingBox fromVertices(List<AnnotationResponse.Vertex> vertices) {
      AnnotationResponse.Vertex topLeft = vertices.get(0);
      int x = topLeft.x;
      int y = topLeft.y;

      AnnotationResponse.Vertex bottomRight = vertices.get(2);
      int width = bottomRight.x - x;
      int height = bottomRight.y - y;

      return BoundingBox.of(x, y, width, height);
    }
  }

  private OutputResponse(
      List<String> textFound, List<BoundingBox> boundingBoxes) {
    this.textFound = textFound;
    this.boundingBoxes = boundingBoxes;
  }

  private static OutputResponse of(List<String> textFound, List<BoundingBox> boundingBoxes) {
    return new OutputResponse(textFound, boundingBoxes);
  }

  /**
   * Generates an OutputResponse from the AnnotationResponse
   */
  public static OutputResponse fromAnnotationResponse(AnnotationResponse annotationResponse) {
    List<String> textFound = Lists.newArrayList();
    List<BoundingBox> boundingBoxes = Lists.newArrayList();

    AnnotationResponse.TextResponse textResponse = annotationResponse.responses.get(0);

    // Ignore the first annotation as only the words should be relayed back and not the sentence
    textResponse.textAnnotations.stream().skip(1).forEach(
        textAnnotation -> {
          textFound.add(textAnnotation.description);
          boundingBoxes.add(BoundingBox.fromVertices(textAnnotation.boundingPoly.vertices));
        }
    );

    return OutputResponse.of(textFound, boundingBoxes);
  }

}

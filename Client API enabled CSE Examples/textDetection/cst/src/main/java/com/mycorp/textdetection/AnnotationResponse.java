package com.mycorp.textdetection;

import java.util.List;

/**
 * Object representation of the JSON that is returned from the Google Text Detection API
 */
public class AnnotationResponse {

  public List<TextResponse> responses;

  public static class Vertex {
    public int x;
    public int y;
  }

  public static class BoundingPoly {
    public List<Vertex> vertices;
  }

  public static class TextAnnotation {
    public String description;
    public BoundingPoly boundingPoly;
  }

  public static class TextResponse {
    public List<TextAnnotation> textAnnotations;
    public Error error;
  }

  public static class Error {
    public int code;
    public String message;
  }
}

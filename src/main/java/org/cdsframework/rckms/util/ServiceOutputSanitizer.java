package org.cdsframework.rckms.util;

import java.util.regex.Pattern;

/**
 * Removes sensitive data from the shared-service responses (service output) we receive with each submission.
 */
public class ServiceOutputSanitizer
{
  // For removal of the encoded VMR payloads that may be present in <input> or <output> elements of the shared-service responses.
  // Note that there can be multiple <output> elements (one for each jurisdiction).
  // <input> elements should generally not be present as they are only included in the shared-service response if the debug flag is
  // set in the request, but it is still removed here just in case.
  private static final Pattern INPUT_OUTPUT_PATTERN =
      Pattern.compile("(<input>.*?</input>)|(<output>.*?</output>)", Pattern.DOTALL);

  public ServiceOutputSanitizer()
  {
  }

  public String sanitize(String serviceOutput)
  {
    if (serviceOutput == null)
      return null;
    return INPUT_OUTPUT_PATTERN.matcher(serviceOutput).replaceAll("");
  }
}

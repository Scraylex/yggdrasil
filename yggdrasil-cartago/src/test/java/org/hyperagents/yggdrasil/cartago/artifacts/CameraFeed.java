package org.hyperagents.yggdrasil.cartago.artifacts;

import cartago.OPERATION;
import ch.unisg.ics.interactions.wot.td.security.NoSecurityScheme;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;

public class CameraFeed extends HypermediaArtifact {

  private static final int WAIT_TIME = 3000;
  private static final String PREFIX = "https://example.org/camera#"; //todo find a ontology dealing with video/camera
  private String cameraFeedUri;

  public void init(final String cameraFeedUri) {
    this.cameraFeedUri = cameraFeedUri;
  }

  @OPERATION
  public void getPicture() {
    //TODO Implement getPicture either with a workaround api or java opencv bindings
  }


  @Override
  protected void registerInteractionAffordances() {
    this.registerActionAffordance(
        PREFIX + "feed",
        "getPicture",
        "/getPicture"
    );

    // Add initial coordinates, these are currently hard-coded
    final var builder = new ModelBuilder();
    final var rdf = SimpleValueFactory.getInstance();

    final var coordinates = rdf.createBNode();
    builder.add(getArtifactUri(), rdf.createIRI("rdf:"), coordinates);
    builder.add(coordinates, rdf.createIRI(PREFIX + "coordX"), rdf.createLiteral(2.7));
    builder.add(coordinates, rdf.createIRI(PREFIX + "coordY"), rdf.createLiteral(-0.5));
    builder.add(coordinates, rdf.createIRI(PREFIX + "coordZ"), rdf.createLiteral(0.8));

    this.addMetadata(builder.build());

    this.setSecurityScheme(new NoSecurityScheme());
  }
}

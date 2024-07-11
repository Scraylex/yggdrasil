package org.hyperagents.yggdrasil.cartago.blocksworld;

import cartago.OPERATION;
import cartago.OpFeedbackParam;
import ch.unisg.ics.interactions.wot.td.schemas.StringSchema;
import ch.unisg.ics.interactions.wot.td.security.NoSecurityScheme;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.hyperagents.yggdrasil.cartago.artifacts.HypermediaArtifact;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Block extends HypermediaArtifact {
  private static final String PREFIX = "http://example.org/";
  private static final String BLOCK_TYPE = "http://example.org/Block";
  private static final String POSITION_TYPE = PREFIX + "Position";

  private enum Position {
    LEFT,
    RIGHT,
    CENTER
  }

  private Position position;

  public void init(final String pos) {
    this.position = Position.valueOf(pos.toUpperCase());
    this.defineObsProperty("position", position.name());
  }

  @OPERATION
  public void moveTo(final String position) {
    this.position = Position.valueOf(position.toUpperCase());
  }

  @OPERATION
  public void observe(final OpFeedbackParam<String> posParam) {
    this.log("block observes position");
    posParam.set(position.name());
    this.log("result of block: " + position.name());
  }

  @Override
  protected void registerInteractionAffordances() {
    final var StringSet = Arrays.stream(Position.values())
      .map(Enum::name)
      .collect(Collectors.toUnmodifiableSet());

    this.registerActionAffordance(
      "http://example.org#MoveTo",
      "moveTo",
      "/moveTo",
      new StringSchema.Builder()
        .addSemanticType(POSITION_TYPE)
        .addEnum(StringSet)
        .build());

    this.registerActionAffordance(
      "http://example.org#Position",
      "observe",
      "/observe");

    this.registerFeedbackParameter("observe");

    this.setSecurityScheme(new NoSecurityScheme());
    handleMetadata();
  }

  private void handleMetadata() {
    final var builder = new ModelBuilder();
    final var rdf = SimpleValueFactory.getInstance();

    final var position = rdf.createBNode();
    builder.add(position, RDF.TYPE, rdf.createIRI(POSITION_TYPE));
    builder.add(position, rdf.createIRI(POSITION_TYPE + "#valueOf"), rdf.createLiteral(this.position == null ? "" : this.position.name()));

    builder.subject(getArtifactUri())
      .add(rdf.createIRI(BLOCK_TYPE + "#hasPosition"), position);

    this.addMetadata(builder.build());
  }
}


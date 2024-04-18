package org.hyperagents.yggdrasil.cartago.artifacts.workshop;

import cartago.OPERATION;
import ch.unisg.ics.interactions.wot.td.security.NoSecurityScheme;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.hyperagents.yggdrasil.cartago.artifacts.HypermediaArtifact;

@SuppressFBWarnings("PI_DO_NOT_REUSE_PUBLIC_IDENTIFIERS_CLASS_NAMES")
public class LightSwitch extends HypermediaArtifact {

  private boolean isOn = false;
  private static final String LIGHT_PROP = "lightOn";
  private static final String URL = "http://example.org/LightSwitch/";


  public void init() {
    this.defineObsProperty(LIGHT_PROP, isOn);
  }

  @OPERATION
  public void flick() {
    if(isOn) {
      turnOff();
    } else {
      turnOn();
    }
    final var prop = this.getObsProperty(LIGHT_PROP);
    prop.updateValue(isOn);
    System.out.println("Light turned on");
  }

  private void turnOn() {
    isOn = true;
  }

  private void turnOff() {
    isOn = false;
  }

  @Override
  protected void registerInteractionAffordances() {
    this.registerActionAffordance(URL + "Flick", "flick", "/flick");

    final var builder = new ModelBuilder();
    final var rdf = SimpleValueFactory.getInstance();

    final var commentIRI = rdf.createIRI("http://www.w3.org/2000/01/rdf-schema#comment");
    final var commentLiteral = rdf.createLiteral("This is a comment");

    builder.add(getArtifactUri(), commentIRI, commentLiteral);
    this.addMetadata(builder.build());

    this.setSecurityScheme(new NoSecurityScheme());
  }
}

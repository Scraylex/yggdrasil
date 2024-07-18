package org.hyperagents.yggdrasil.utils.impl;

import ch.unisg.ics.interactions.wot.td.ThingDescription;
import ch.unisg.ics.interactions.wot.td.affordances.ActionAffordance;
import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.io.TDGraphWriter;
import ch.unisg.ics.interactions.wot.td.schemas.ArraySchema;
import ch.unisg.ics.interactions.wot.td.schemas.ObjectSchema;
import ch.unisg.ics.interactions.wot.td.schemas.StringSchema;
import ch.unisg.ics.interactions.wot.td.security.SecurityScheme;
import com.google.common.collect.ListMultimap;
import io.vertx.core.http.HttpMethod;

import java.util.Optional;
import java.util.Set;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.hyperagents.yggdrasil.model.Environment;
import org.hyperagents.yggdrasil.model.Workspace;
import org.hyperagents.yggdrasil.utils.EnvironmentConfig;
import org.hyperagents.yggdrasil.utils.HttpInterfaceConfig;
import org.hyperagents.yggdrasil.utils.RdfModelUtils;
import org.hyperagents.yggdrasil.utils.RepresentationFactory;

public final class RepresentationFactoryImpl implements RepresentationFactory {
  private static final String ARTIFACT_NAME_PARAM = "artifactName";

  private final HttpInterfaceConfig httpConfig;
  private final Environment environment;

  public RepresentationFactoryImpl(final HttpInterfaceConfig httpConfig, final Environment environment) {
    this.httpConfig = httpConfig;
    this.environment = environment;
  }

  @Override
  public String createPlatformRepresentation() {
    String uri = this.httpConfig.getBaseUri() + "/";

    final var model = this.environment.getTextInfo()
      .map(info -> RdfModelUtils.createCommentModel(uri, info))
      .orElse(new ModelBuilder().build());

    return serializeThingDescription(
      new ThingDescription
        .Builder("yggdrasil")
        .addThingURI(uri)
        .addSemanticType("https://purl.org/hmas/HypermediaMASPlatform")
        .addAction(
          new ActionAffordance.Builder(
              "createWorkspace",
              new Form.Builder(this.httpConfig.getWorkspacesUri() + "/")
                      .setMethodName(HttpMethod.POST.name())
                      .build()
          )
          .build()
        )
        .addGraph(model)
    );
  }



  @Override
  public String createWorkspaceRepresentation(
      final String workspaceName,
      final Set<String> artifactTemplates
  ) {
    final var uri = this.httpConfig.getWorkspaceUri(workspaceName);

    final var model = this.environment.getWorkspaces().stream()
      .filter(w -> w.getName().equals(workspaceName))
      .findFirst()
      .map(Workspace::getTextInfo)
      .flatMap(textInfo -> textInfo.map(text -> RdfModelUtils.createCommentModel(uri, text)))
      .orElse(new ModelBuilder().build());

    return serializeThingDescription(
      new ThingDescription
          .Builder(workspaceName)
          .addThingURI(uri)
          .addSemanticType("https://purl.org/hmas/Workspace")
          .addAction(
            new ActionAffordance.Builder(
                "makeArtifact",
                new Form.Builder(this.httpConfig.getArtifactsUri(workspaceName) + "/").build()
            )
            .addInputSchema(
              new ObjectSchema
                .Builder()
                .addProperty(
                  "artifactClass",
                  new StringSchema.Builder().addEnum(artifactTemplates).build()
                )
                .addProperty(ARTIFACT_NAME_PARAM, new StringSchema.Builder().build())
                .addProperty("initParams", new ArraySchema.Builder().build())
                .addRequiredProperties("artifactClass", ARTIFACT_NAME_PARAM)
                .build()
            )
            .build()
          )
          .addAction(
            new ActionAffordance.Builder(
                "joinWorkspace",
                new Form.Builder(uri + "/join")
                        .setMethodName(HttpMethod.POST.name())
                        .build()
            )
            .build()
          )
          .addAction(
            new ActionAffordance.Builder(
                "quitWorkspace",
                new Form.Builder(uri + "/leave")
                        .setMethodName(HttpMethod.POST.name())
                        .build()
            )
            .build()
          )
          .addAction(
            new ActionAffordance.Builder(
                "focus",
                new Form.Builder(uri + "/focus")
                        .setMethodName(HttpMethod.POST.name())
                        .build()
            )
            .addInputSchema(
              new ObjectSchema
                .Builder()
                .addProperty(ARTIFACT_NAME_PARAM, new StringSchema.Builder().build())
                .addProperty("callbackIri", new StringSchema.Builder().build())
                .addRequiredProperties(ARTIFACT_NAME_PARAM, "callbackIri")
                .build()
            )
            .build()
          )
          .addAction(
            new ActionAffordance.Builder(
                "createSubWorkspace",
                new Form.Builder(uri)
                        .setMethodName(HttpMethod.POST.name())
                        .build()
            )
            .build()
          )
        .addGraph(model)
    );
  }

  @Override
  public String createArtifactRepresentation(
      final String workspaceName,
      final String artifactName,
      final SecurityScheme securityScheme,
      final String semanticType,
      final Model metadata,
      final ListMultimap<String, ActionAffordance> actionAffordances
  ) {
    final var td =
        new ThingDescription.Builder(artifactName)
                            .addSecurityScheme(securityScheme)
                            .addSemanticType("https://purl.org/hmas/Artifact")
                            .addSemanticType(semanticType)
                            .addThingURI(this.httpConfig
                                             .getArtifactUri(workspaceName, artifactName))
                            .addGraph(metadata);
    actionAffordances.values().forEach(td::addAction);
    return serializeThingDescription(td);
  }

  @Override
  public String createBodyRepresentation(
      final String workspaceName,
      final String agentName,
      final SecurityScheme securityScheme,
      final Model metadata
  ) {
    final var td =
        new ThingDescription
          .Builder(agentName)
          .addSecurityScheme(securityScheme)
          .addSemanticType("https://purl.org/hmas/Artifact")
          .addSemanticType("https://example.org/Body")
          .addThingURI(this.httpConfig.getAgentBodyUri(workspaceName, agentName))
          .addGraph(metadata);
    return serializeThingDescription(td);
  }

  private String serializeThingDescription(final ThingDescription.Builder td) {
    return new TDGraphWriter(td.build())
      .setNamespace("td", "https://www.w3.org/2019/wot/td#")
      .setNamespace("htv", "http://www.w3.org/2011/http#")
      .setNamespace("hctl", "https://www.w3.org/2019/wot/hypermedia#")
      .setNamespace("wotsec", "https://www.w3.org/2019/wot/security#")
      .setNamespace("dct", "http://purl.org/dc/terms/")
      .setNamespace("js", "https://www.w3.org/2019/wot/json-schema#")
      .setNamespace("hmas", "https://purl.org/hmas/")
      .write();
  }
}

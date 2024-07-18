package org.hyperagents.yggdrasil.nlp.handlers.type;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.hyperagents.yggdrasil.nlp.IdentUtil;
import org.hyperagents.yggdrasil.nlp.RdfTypeHandler;

import java.util.stream.Collectors;

public class ActionAffordanceHandler implements RdfTypeHandler {
  @Override
  public String handleStatement(final Model model, final Resource subject, final int indentLevel) {
    final var stringBuilder = new StringBuilder();
    model.filter(subject, SimpleValueFactory.getInstance().createIRI("https://www.w3.org/2019/wot/td#name"), null).forEach(statement -> {
      String name = statement.getObject().stringValue();
      final var formatted = "Action affordance %s can be called with the following url".formatted(name);
      stringBuilder.append(IdentUtil.identString(indentLevel, formatted));
      stringBuilder.append("\n");
      final var inputStringBuilder = new StringBuilder();
      final var filteredModel = model.filter(statement.getSubject(), SimpleValueFactory.getInstance().createIRI("https://www.w3.org/2019/wot/td#hasInputSchema"), null);
      if (filteredModel.stream()
        .collect(Collectors.toUnmodifiableSet())
        .isEmpty()
      ) {
        inputStringBuilder.append(IdentUtil.identString(indentLevel + 1, "%s does not require parameters".formatted(name)));
      } else {
        inputStringBuilder.append(IdentUtil.identString(indentLevel + 1,
//          handleInputSchema(model, subject, indentLevel) TODO figure out
          "%s requires parameters".formatted(name)
        ));
      }
      stringBuilder.append(inputStringBuilder);
      stringBuilder.append("\n");
    });
    return stringBuilder.toString();
  }

  private String handleInputSchema(Model model, final Resource subject, int indentLevel) {
    final var subStringBuilder = new StringBuilder();
    model.forEach(inputSchema -> {
      model.filter(subject, SimpleValueFactory.getInstance().createIRI("https://www.w3.org/2019/wot/json-schema#ArraySchema"), null).forEach(inputSchemaStatement -> {
        handleArraySchema(model, (Resource) inputSchema.getObject(), indentLevel, subStringBuilder);
      });
      model.filter(subject, SimpleValueFactory.getInstance().createIRI("https://www.w3.org/2019/wot/json-schema#ObjectSchema"), null).forEach(inputSchemaStatement -> {
        handleObjectSchema(model, (Resource) inputSchema.getObject(), indentLevel, subStringBuilder);
      });
      handlePrimitiveDataSchema(model, subject, indentLevel, subStringBuilder);
    });
    return subStringBuilder.toString();
  }

  private void handleObjectSchema(final Model model, final Resource subject, final int indentLevel, StringBuilder subStringBuilder) {
    model.filter(subject, SimpleValueFactory.getInstance().createIRI("https://www.w3.org/2019/wot/json-schema#properties"), null)
      .forEach(enumValueStatement -> handleInputSchema(model, subject, indentLevel + 1));
    final var formatted = "Requires an object input of %s.".formatted(subStringBuilder);
    subStringBuilder.append(IdentUtil.identString(indentLevel + 1, formatted));
  }

  private void handleArraySchema(final Model model, final Resource subject, final int indentLevel, final StringBuilder subStringBuilder) {
    model.filter(subject, SimpleValueFactory.getInstance().createIRI("https://www.w3.org/2019/wot/json-schema#items"), null)
      .forEach(enumValueStatement -> handleInputSchema(model, subject,indentLevel + 1));

    final var formatted = "Requires an array input of %s.".formatted(subStringBuilder);
    subStringBuilder.append(IdentUtil.identString(indentLevel + 1, formatted));
  }

  private void handlePrimitiveDataSchema(final Model model, final Resource subject, final int indentLevel, final StringBuilder stringBuilder) {
    handleStringSchema(model, subject, indentLevel, stringBuilder);
    handleIntegerSchema(model, subject, indentLevel, stringBuilder);
    handleNumberSchema(model, subject, indentLevel, stringBuilder);
    handleBooleanSchema(model, subject, indentLevel, stringBuilder);
    handleNullSchema(model, subject, indentLevel, stringBuilder);
  }

  private static void handleStringSchema(Model model, Resource subject, int indentLevel, final StringBuilder stringBuilder) {
    model.filter(subject, SimpleValueFactory.getInstance().createIRI("https://www.w3.org/2019/wot/json-schema#StringSchema"), null).forEach(inputSchemaStatement -> {
      final var strSchemaStringBuilder = new StringBuilder();
      strSchemaStringBuilder.append(IdentUtil.identString(indentLevel + 1, "Requires a String input."));
      model.filter(inputSchemaStatement.getSubject(), SimpleValueFactory.getInstance().createIRI("https://www.w3.org/2019/wot/json-schema#enum"), null)
        .forEach(enumValueStatement -> strSchemaStringBuilder.append("possible values are %s.".formatted(enumValueStatement.getObject().stringValue())));
      model.filter(inputSchemaStatement.getSubject(), SimpleValueFactory.getInstance().createIRI("https://www.w3.org/2019/wot/json-schema#propertyName"), null)
        .forEach(enumValueStatement -> strSchemaStringBuilder.append("the parameter has to be called %s.".formatted(enumValueStatement.getObject().stringValue())));
      stringBuilder.append(strSchemaStringBuilder);
    });
  }

  private static void handleIntegerSchema(Model model, Resource subject, int indentLevel, StringBuilder stringBuilder) {
    model.filter(subject, SimpleValueFactory.getInstance().createIRI("https://www.w3.org/2019/wot/json-schema#IntegerSchema"), null).forEach(inputSchemaStatement -> {
      final var strSchemaStringBuilder = new StringBuilder();
      strSchemaStringBuilder.append(IdentUtil.identString(indentLevel + 1, "Requires a Integer input."));
      model.filter(inputSchemaStatement.getSubject(), SimpleValueFactory.getInstance().createIRI("https://www.w3.org/2019/wot/json-schema#propertyName"), null)
        .forEach(enumValueStatement -> strSchemaStringBuilder.append("the parameter has to be called %s.".formatted(enumValueStatement.getObject().stringValue())));
      stringBuilder.append(strSchemaStringBuilder);
    });
  }

  private static void handleNumberSchema(Model model, Resource subject, int indentLevel, StringBuilder stringBuilder) {
    model.filter(subject, SimpleValueFactory.getInstance().createIRI("https://www.w3.org/2019/wot/json-schema#NumberSchema"), null).forEach(inputSchemaStatement -> {
      final var strSchemaStringBuilder = new StringBuilder();
      strSchemaStringBuilder.append(IdentUtil.identString(indentLevel + 1, "Requires a Number input."));
      model.filter(inputSchemaStatement.getSubject(), SimpleValueFactory.getInstance().createIRI("https://www.w3.org/2019/wot/json-schema#propertyName"), null)
        .forEach(enumValueStatement -> strSchemaStringBuilder.append("the parameter has to be called %s.".formatted(enumValueStatement.getObject().stringValue())));
      stringBuilder.append(strSchemaStringBuilder);
    });
  }

  private static void handleBooleanSchema(Model model, Resource subject, int indentLevel, StringBuilder stringBuilder) {
    model.filter(subject, SimpleValueFactory.getInstance().createIRI("https://www.w3.org/2019/wot/json-schema#BooleanSchema"), null).forEach(inputSchemaStatement -> {
      final var strSchemaStringBuilder = new StringBuilder();
      strSchemaStringBuilder.append(IdentUtil.identString(indentLevel + 1, "Requires a Boolean input."));
      model.filter(inputSchemaStatement.getSubject(), SimpleValueFactory.getInstance().createIRI("https://www.w3.org/2019/wot/json-schema#propertyName"), null)
        .forEach(enumValueStatement -> strSchemaStringBuilder.append("the parameter has to be called %s.".formatted(enumValueStatement.getObject().stringValue())));
      stringBuilder.append(strSchemaStringBuilder);
    });
  }

  private static void handleNullSchema(Model model, Resource subject, int indentLevel, StringBuilder stringBuilder) {
    model.filter(subject, SimpleValueFactory.getInstance().createIRI("https://www.w3.org/2019/wot/json-schema#NullSchema"), null).forEach(inputSchemaStatement -> {
      final var strSchemaStringBuilder = new StringBuilder();
      strSchemaStringBuilder.append(IdentUtil.identString(indentLevel + 1, "Requires a Null input."));
      model.filter(inputSchemaStatement.getSubject(), SimpleValueFactory.getInstance().createIRI("https://www.w3.org/2019/wot/json-schema#propertyName"), null)
        .forEach(enumValueStatement -> strSchemaStringBuilder.append("the parameter has to be called %s.".formatted(enumValueStatement.getObject().stringValue())));
      stringBuilder.append(strSchemaStringBuilder);
    });
  }
}

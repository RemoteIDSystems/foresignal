//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'context_summary_narrative.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ContextSummaryNarrative {
  /// Returns a new [ContextSummaryNarrative] instance.
  ContextSummaryNarrative({

    required  this.text,

    required  this.textOrigin,
  });

  @JsonKey(
    
    name: r'text',
    required: true,
    includeIfNull: false,
  )


  final String text;



  @JsonKey(
    
    name: r'textOrigin',
    required: true,
    includeIfNull: false,
  )


  final ContextSummaryNarrativeTextOriginEnum textOrigin;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ContextSummaryNarrative &&
      other.text == text &&
      other.textOrigin == textOrigin;

    @override
    int get hashCode =>
        text.hashCode +
        textOrigin.hashCode;

  factory ContextSummaryNarrative.fromJson(Map<String, dynamic> json) => _$ContextSummaryNarrativeFromJson(json);

  Map<String, dynamic> toJson() => _$ContextSummaryNarrativeToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ContextSummaryNarrativeTextOriginEnum {
@JsonValue(r'llm')
llm(r'llm');

const ContextSummaryNarrativeTextOriginEnum(this.value);

final String value;

@override
String toString() => value;
}



//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'source_relevance_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SourceRelevanceRequest {
  /// Returns a new [SourceRelevanceRequest] instance.
  SourceRelevanceRequest({

    required  this.sourceIds,
  });

      /// Registered source ids, the candidate dataset of a rule (as many as `TriggerRuleInput.sources` takes)
  @JsonKey(
    
    name: r'sourceIds',
    required: true,
    includeIfNull: false,
  )


  final Set<String> sourceIds;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SourceRelevanceRequest &&
      other.sourceIds == sourceIds;

    @override
    int get hashCode =>
        sourceIds.hashCode;

  factory SourceRelevanceRequest.fromJson(Map<String, dynamic> json) => _$SourceRelevanceRequestFromJson(json);

  Map<String, dynamic> toJson() => _$SourceRelevanceRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}


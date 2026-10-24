//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'source_relevance_analysis_shared.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SourceRelevanceAnalysisShared {
  /// Returns a new [SourceRelevanceAnalysisShared] instance.
  SourceRelevanceAnalysisShared({

    required  this.kinds,

    required  this.entityTypes,
  });

  @JsonKey(
    
    name: r'kinds',
    required: true,
    includeIfNull: false,
  )


  final List<String> kinds;



      /// Entity identity types (descriptor entityKeys), such as icao24 or mmsi
  @JsonKey(
    
    name: r'entityTypes',
    required: true,
    includeIfNull: false,
  )


  final List<String> entityTypes;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SourceRelevanceAnalysisShared &&
      other.kinds == kinds &&
      other.entityTypes == entityTypes;

    @override
    int get hashCode =>
        kinds.hashCode +
        entityTypes.hashCode;

  factory SourceRelevanceAnalysisShared.fromJson(Map<String, dynamic> json) => _$SourceRelevanceAnalysisSharedFromJson(json);

  Map<String, dynamic> toJson() => _$SourceRelevanceAnalysisSharedToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}


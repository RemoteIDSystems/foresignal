//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/forecast_resolver.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'forecast_question_patch.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ForecastQuestionPatch {
  /// Returns a new [ForecastQuestionPatch] instance.
  ForecastQuestionPatch({

     this.title,

     this.tags,

     this.closesAt,

     this.resolutionCriteria,

     this.resolver,
  });

  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



  @JsonKey(
    
    name: r'tags',
    required: false,
    includeIfNull: false,
  )


  final List<String>? tags;



  @JsonKey(
    
    name: r'closesAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? closesAt;



  @JsonKey(
    
    name: r'resolutionCriteria',
    required: false,
    includeIfNull: false,
  )


  final String? resolutionCriteria;



  @JsonKey(
    
    name: r'resolver',
    required: false,
    includeIfNull: false,
  )


  final ForecastResolver? resolver;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ForecastQuestionPatch &&
      other.title == title &&
      other.tags == tags &&
      other.closesAt == closesAt &&
      other.resolutionCriteria == resolutionCriteria &&
      other.resolver == resolver;

    @override
    int get hashCode =>
        title.hashCode +
        tags.hashCode +
        closesAt.hashCode +
        resolutionCriteria.hashCode +
        resolver.hashCode;

  factory ForecastQuestionPatch.fromJson(Map<String, dynamic> json) => _$ForecastQuestionPatchFromJson(json);

  Map<String, dynamic> toJson() => _$ForecastQuestionPatchToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}


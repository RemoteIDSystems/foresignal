//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/question_links.dart';
import 'package:foresignal_api/src/model/question_outcomes.dart';
import 'package:foresignal_api/src/model/forecast_resolver.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'forecast_question_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ForecastQuestionInput {
  /// Returns a new [ForecastQuestionInput] instance.
  ForecastQuestionInput({

    required  this.title,

    required  this.type,

     this.outcomes,

    required  this.resolutionCriteria,

    required  this.resolver,

     this.opensAt,

    required  this.closesAt,

    required  this.resolveBy,

    required  this.category,

     this.tags,

     this.links,

     this.dedupKey,

     this.private = false,
  });

  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final ForecastQuestionInputTypeEnum type;



  @JsonKey(
    
    name: r'outcomes',
    required: false,
    includeIfNull: false,
  )


  final QuestionOutcomes? outcomes;



      /// Shown verbatim to every forecaster
  @JsonKey(
    
    name: r'resolutionCriteria',
    required: true,
    includeIfNull: false,
  )


  final String resolutionCriteria;



  @JsonKey(
    
    name: r'resolver',
    required: true,
    includeIfNull: false,
  )


  final ForecastResolver resolver;



      /// Default: now
  @JsonKey(
    
    name: r'opensAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? opensAt;



  @JsonKey(
    
    name: r'closesAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime closesAt;



  @JsonKey(
    
    name: r'resolveBy',
    required: true,
    includeIfNull: false,
  )


  final DateTime resolveBy;



  @JsonKey(
    
    name: r'category',
    required: true,
    includeIfNull: false,
  )


  final String category;



  @JsonKey(
    
    name: r'tags',
    required: false,
    includeIfNull: false,
  )


  final List<String>? tags;



  @JsonKey(
    
    name: r'links',
    required: false,
    includeIfNull: false,
  )


  final QuestionLinks? links;



  @JsonKey(
    
    name: r'dedupKey',
    required: false,
    includeIfNull: false,
  )


  final String? dedupKey;



  @JsonKey(
    defaultValue: false,
    name: r'private',
    required: false,
    includeIfNull: false,
  )


  final bool? private;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ForecastQuestionInput &&
      other.title == title &&
      other.type == type &&
      other.outcomes == outcomes &&
      other.resolutionCriteria == resolutionCriteria &&
      other.resolver == resolver &&
      other.opensAt == opensAt &&
      other.closesAt == closesAt &&
      other.resolveBy == resolveBy &&
      other.category == category &&
      other.tags == tags &&
      other.links == links &&
      other.dedupKey == dedupKey &&
      other.private == private;

    @override
    int get hashCode =>
        title.hashCode +
        type.hashCode +
        outcomes.hashCode +
        resolutionCriteria.hashCode +
        resolver.hashCode +
        opensAt.hashCode +
        closesAt.hashCode +
        resolveBy.hashCode +
        category.hashCode +
        tags.hashCode +
        links.hashCode +
        (dedupKey == null ? 0 : dedupKey.hashCode) +
        private.hashCode;

  factory ForecastQuestionInput.fromJson(Map<String, dynamic> json) => _$ForecastQuestionInputFromJson(json);

  Map<String, dynamic> toJson() => _$ForecastQuestionInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ForecastQuestionInputTypeEnum {
@JsonValue(r'binary')
binary(r'binary'),
@JsonValue(r'multiple')
multiple(r'multiple'),
@JsonValue(r'ordered')
ordered(r'ordered'),
@JsonValue(r'numeric')
numeric(r'numeric');

const ForecastQuestionInputTypeEnum(this.value);

final String value;

@override
String toString() => value;
}



//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'external_question_link.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExternalQuestionLink {
  /// Returns a new [ExternalQuestionLink] instance.
  ExternalQuestionLink({

    required  this.streamId,

    required  this.externalQuestionId,

    required  this.questionId,

     this.orientation,

    required  this.id,

    required  this.sourceId,

    required  this.forecasterId,

    required  this.createdAt,

     this.lastForecastAt,
  });

  @JsonKey(
    
    name: r'streamId',
    required: true,
    includeIfNull: false,
  )


  final String streamId;



  @JsonKey(
    
    name: r'externalQuestionId',
    required: true,
    includeIfNull: false,
  )


  final String externalQuestionId;



      /// ULID identifier
  @JsonKey(
    
    name: r'questionId',
    required: true,
    includeIfNull: false,
  )


  final String questionId;



  @JsonKey(
    
    name: r'orientation',
    required: false,
    includeIfNull: false,
  )


  final ExternalQuestionLinkOrientationEnum? orientation;



      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'sourceId',
    required: true,
    includeIfNull: false,
  )


  final String sourceId;



  @JsonKey(
    
    name: r'forecasterId',
    required: true,
    includeIfNull: false,
  )


  final String forecasterId;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'lastForecastAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastForecastAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExternalQuestionLink &&
      other.streamId == streamId &&
      other.externalQuestionId == externalQuestionId &&
      other.questionId == questionId &&
      other.orientation == orientation &&
      other.id == id &&
      other.sourceId == sourceId &&
      other.forecasterId == forecasterId &&
      other.createdAt == createdAt &&
      other.lastForecastAt == lastForecastAt;

    @override
    int get hashCode =>
        streamId.hashCode +
        externalQuestionId.hashCode +
        questionId.hashCode +
        orientation.hashCode +
        id.hashCode +
        sourceId.hashCode +
        forecasterId.hashCode +
        createdAt.hashCode +
        (lastForecastAt == null ? 0 : lastForecastAt.hashCode);

  factory ExternalQuestionLink.fromJson(Map<String, dynamic> json) => _$ExternalQuestionLinkFromJson(json);

  Map<String, dynamic> toJson() => _$ExternalQuestionLinkToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ExternalQuestionLinkOrientationEnum {
@JsonValue(r'same')
same(r'same'),
@JsonValue(r'inverse')
inverse(r'inverse');

const ExternalQuestionLinkOrientationEnum(this.value);

final String value;

@override
String toString() => value;
}



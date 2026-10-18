//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analyst_feedback.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalystFeedback {
  /// Returns a new [AnalystFeedback] instance.
  AnalystFeedback({

    required  this.id,

    required  this.target,

    required  this.targetId,

    required  this.rating,

     this.comment,

    required  this.by,

    required  this.at,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'target',
    required: true,
    includeIfNull: false,
  )


  final AnalystFeedbackTargetEnum target;



      /// ULID identifier
  @JsonKey(
    
    name: r'targetId',
    required: true,
    includeIfNull: false,
  )


  final String targetId;



  @JsonKey(
    
    name: r'rating',
    required: true,
    includeIfNull: false,
  )


  final AnalystFeedbackRatingEnum rating;



  @JsonKey(
    
    name: r'comment',
    required: false,
    includeIfNull: false,
  )


  final String? comment;



  @JsonKey(
    
    name: r'by',
    required: true,
    includeIfNull: false,
  )


  final String by;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalystFeedback &&
      other.id == id &&
      other.target == target &&
      other.targetId == targetId &&
      other.rating == rating &&
      other.comment == comment &&
      other.by == by &&
      other.at == at;

    @override
    int get hashCode =>
        id.hashCode +
        target.hashCode +
        targetId.hashCode +
        rating.hashCode +
        (comment == null ? 0 : comment.hashCode) +
        by.hashCode +
        at.hashCode;

  factory AnalystFeedback.fromJson(Map<String, dynamic> json) => _$AnalystFeedbackFromJson(json);

  Map<String, dynamic> toJson() => _$AnalystFeedbackToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AnalystFeedbackTargetEnum {
@JsonValue(r'note')
note(r'note'),
@JsonValue(r'proposal')
proposal(r'proposal');

const AnalystFeedbackTargetEnum(this.value);

final String value;

@override
String toString() => value;
}


enum AnalystFeedbackRatingEnum {
@JsonValue(r'useful')
useful(r'useful'),
@JsonValue(r'not_useful')
notUseful(r'not_useful'),
@JsonValue(r'wrong')
wrong(r'wrong');

const AnalystFeedbackRatingEnum(this.value);

final String value;

@override
String toString() => value;
}



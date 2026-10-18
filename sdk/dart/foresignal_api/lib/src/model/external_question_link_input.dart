//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'external_question_link_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExternalQuestionLinkInput {
  /// Returns a new [ExternalQuestionLinkInput] instance.
  ExternalQuestionLinkInput({

    required  this.streamId,

    required  this.externalQuestionId,

    required  this.questionId,

     this.orientation,
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


  final ExternalQuestionLinkInputOrientationEnum? orientation;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExternalQuestionLinkInput &&
      other.streamId == streamId &&
      other.externalQuestionId == externalQuestionId &&
      other.questionId == questionId &&
      other.orientation == orientation;

    @override
    int get hashCode =>
        streamId.hashCode +
        externalQuestionId.hashCode +
        questionId.hashCode +
        orientation.hashCode;

  factory ExternalQuestionLinkInput.fromJson(Map<String, dynamic> json) => _$ExternalQuestionLinkInputFromJson(json);

  Map<String, dynamic> toJson() => _$ExternalQuestionLinkInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ExternalQuestionLinkInputOrientationEnum {
@JsonValue(r'same')
same(r'same'),
@JsonValue(r'inverse')
inverse(r'inverse');

const ExternalQuestionLinkInputOrientationEnum(this.value);

final String value;

@override
String toString() => value;
}



//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'question_annulment.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class QuestionAnnulment {
  /// Returns a new [QuestionAnnulment] instance.
  QuestionAnnulment({

    required  this.code,

    required  this.reason,

    required  this.at,

    required  this.by,
  });

  @JsonKey(
    
    name: r'code',
    required: true,
    includeIfNull: false,
  )


  final QuestionAnnulmentCodeEnum code;



  @JsonKey(
    
    name: r'reason',
    required: true,
    includeIfNull: false,
  )


  final String reason;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;



  @JsonKey(
    
    name: r'by',
    required: true,
    includeIfNull: false,
  )


  final String by;





    @override
    bool operator ==(Object other) => identical(this, other) || other is QuestionAnnulment &&
      other.code == code &&
      other.reason == reason &&
      other.at == at &&
      other.by == by;

    @override
    int get hashCode =>
        code.hashCode +
        reason.hashCode +
        at.hashCode +
        by.hashCode;

  factory QuestionAnnulment.fromJson(Map<String, dynamic> json) => _$QuestionAnnulmentFromJson(json);

  Map<String, dynamic> toJson() => _$QuestionAnnulmentToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum QuestionAnnulmentCodeEnum {
@JsonValue(r'LOW_DETECTABILITY')
LOW_DETECTABILITY(r'LOW_DETECTABILITY'),
@JsonValue(r'COVERAGE_GAP')
COVERAGE_GAP(r'COVERAGE_GAP'),
@JsonValue(r'OPERATOR')
OPERATOR(r'OPERATOR');

const QuestionAnnulmentCodeEnum(this.value);

final String value;

@override
String toString() => value;
}



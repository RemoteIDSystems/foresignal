//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'key_assumption_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class KeyAssumptionInput {
  /// Returns a new [KeyAssumptionInput] instance.
  KeyAssumptionInput({

    required  this.text,

     this.linchpin = false,

     this.rationale,

     this.wouldBeWrongIf,

     this.status,

     this.indicatorIds,

     this.reviewDue,
  });

  @JsonKey(
    
    name: r'text',
    required: true,
    includeIfNull: false,
  )


  final String text;



  @JsonKey(
    defaultValue: false,
    name: r'linchpin',
    required: false,
    includeIfNull: false,
  )


  final bool? linchpin;



  @JsonKey(
    
    name: r'rationale',
    required: false,
    includeIfNull: false,
  )


  final String? rationale;



  @JsonKey(
    
    name: r'wouldBeWrongIf',
    required: false,
    includeIfNull: false,
  )


  final String? wouldBeWrongIf;



  @JsonKey(
    
    name: r'status',
    required: false,
    includeIfNull: false,
  )


  final KeyAssumptionInputStatusEnum? status;



  @JsonKey(
    
    name: r'indicatorIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? indicatorIds;



  @JsonKey(
    
    name: r'reviewDue',
    required: false,
    includeIfNull: false,
  )


  final DateTime? reviewDue;





    @override
    bool operator ==(Object other) => identical(this, other) || other is KeyAssumptionInput &&
      other.text == text &&
      other.linchpin == linchpin &&
      other.rationale == rationale &&
      other.wouldBeWrongIf == wouldBeWrongIf &&
      other.status == status &&
      other.indicatorIds == indicatorIds &&
      other.reviewDue == reviewDue;

    @override
    int get hashCode =>
        text.hashCode +
        linchpin.hashCode +
        (rationale == null ? 0 : rationale.hashCode) +
        (wouldBeWrongIf == null ? 0 : wouldBeWrongIf.hashCode) +
        status.hashCode +
        indicatorIds.hashCode +
        (reviewDue == null ? 0 : reviewDue.hashCode);

  factory KeyAssumptionInput.fromJson(Map<String, dynamic> json) => _$KeyAssumptionInputFromJson(json);

  Map<String, dynamic> toJson() => _$KeyAssumptionInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum KeyAssumptionInputStatusEnum {
@JsonValue(r'solid')
solid(r'solid'),
@JsonValue(r'caveated')
caveated(r'caveated'),
@JsonValue(r'unsupported')
unsupported(r'unsupported'),
@JsonValue(r'broken')
broken(r'broken');

const KeyAssumptionInputStatusEnum(this.value);

final String value;

@override
String toString() => value;
}



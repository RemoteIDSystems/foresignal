//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'key_assumption_patch.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class KeyAssumptionPatch {
  /// Returns a new [KeyAssumptionPatch] instance.
  KeyAssumptionPatch({

     this.text,

     this.linchpin,

     this.rationale,

     this.wouldBeWrongIf,

     this.status,

     this.indicatorIds,

     this.reviewDue,
  });

  @JsonKey(
    
    name: r'text',
    required: false,
    includeIfNull: false,
  )


  final String? text;



  @JsonKey(
    
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


  final KeyAssumptionPatchStatusEnum? status;



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
    bool operator ==(Object other) => identical(this, other) || other is KeyAssumptionPatch &&
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

  factory KeyAssumptionPatch.fromJson(Map<String, dynamic> json) => _$KeyAssumptionPatchFromJson(json);

  Map<String, dynamic> toJson() => _$KeyAssumptionPatchToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum KeyAssumptionPatchStatusEnum {
@JsonValue(r'solid')
solid(r'solid'),
@JsonValue(r'caveated')
caveated(r'caveated'),
@JsonValue(r'unsupported')
unsupported(r'unsupported'),
@JsonValue(r'broken')
broken(r'broken');

const KeyAssumptionPatchStatusEnum(this.value);

final String value;

@override
String toString() => value;
}



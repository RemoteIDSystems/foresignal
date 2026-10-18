//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'key_assumption.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class KeyAssumption {
  /// Returns a new [KeyAssumption] instance.
  KeyAssumption({

    required  this.id,

    required  this.setId,

    required  this.text,

    required  this.linchpin,

     this.rationale,

     this.wouldBeWrongIf,

    required  this.status,

     this.indicatorIds,

     this.reviewDue,

     this.updatedBy,

    required  this.updatedAt,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



      /// ULID identifier
  @JsonKey(
    
    name: r'setId',
    required: true,
    includeIfNull: false,
  )


  final String setId;



  @JsonKey(
    
    name: r'text',
    required: true,
    includeIfNull: false,
  )


  final String text;



  @JsonKey(
    
    name: r'linchpin',
    required: true,
    includeIfNull: false,
  )


  final bool linchpin;



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
    required: true,
    includeIfNull: false,
  )


  final KeyAssumptionStatusEnum status;



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



  @JsonKey(
    
    name: r'updatedBy',
    required: false,
    includeIfNull: false,
  )


  final String? updatedBy;



  @JsonKey(
    
    name: r'updatedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime updatedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is KeyAssumption &&
      other.id == id &&
      other.setId == setId &&
      other.text == text &&
      other.linchpin == linchpin &&
      other.rationale == rationale &&
      other.wouldBeWrongIf == wouldBeWrongIf &&
      other.status == status &&
      other.indicatorIds == indicatorIds &&
      other.reviewDue == reviewDue &&
      other.updatedBy == updatedBy &&
      other.updatedAt == updatedAt;

    @override
    int get hashCode =>
        id.hashCode +
        setId.hashCode +
        text.hashCode +
        linchpin.hashCode +
        (rationale == null ? 0 : rationale.hashCode) +
        (wouldBeWrongIf == null ? 0 : wouldBeWrongIf.hashCode) +
        status.hashCode +
        indicatorIds.hashCode +
        (reviewDue == null ? 0 : reviewDue.hashCode) +
        (updatedBy == null ? 0 : updatedBy.hashCode) +
        updatedAt.hashCode;

  factory KeyAssumption.fromJson(Map<String, dynamic> json) => _$KeyAssumptionFromJson(json);

  Map<String, dynamic> toJson() => _$KeyAssumptionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum KeyAssumptionStatusEnum {
@JsonValue(r'solid')
solid(r'solid'),
@JsonValue(r'caveated')
caveated(r'caveated'),
@JsonValue(r'unsupported')
unsupported(r'unsupported'),
@JsonValue(r'broken')
broken(r'broken');

const KeyAssumptionStatusEnum(this.value);

final String value;

@override
String toString() => value;
}



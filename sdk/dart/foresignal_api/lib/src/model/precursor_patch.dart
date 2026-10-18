//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'precursor_patch.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PrecursorPatch {
  /// Returns a new [PrecursorPatch] instance.
  PrecursorPatch({

    required  this.status,

    required  this.reason,
  });

  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final PrecursorPatchStatusEnum status;



  @JsonKey(
    
    name: r'reason',
    required: true,
    includeIfNull: false,
  )


  final String reason;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PrecursorPatch &&
      other.status == status &&
      other.reason == reason;

    @override
    int get hashCode =>
        status.hashCode +
        reason.hashCode;

  factory PrecursorPatch.fromJson(Map<String, dynamic> json) => _$PrecursorPatchFromJson(json);

  Map<String, dynamic> toJson() => _$PrecursorPatchToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PrecursorPatchStatusEnum {
@JsonValue(r'retired')
retired(r'retired'),
@JsonValue(r'validated')
validated(r'validated');

const PrecursorPatchStatusEnum(this.value);

final String value;

@override
String toString() => value;
}



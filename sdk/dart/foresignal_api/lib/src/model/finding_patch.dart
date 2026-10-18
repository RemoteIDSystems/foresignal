//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'finding_patch.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class FindingPatch {
  /// Returns a new [FindingPatch] instance.
  FindingPatch({

    required  this.status,

    required  this.reason,
  });

  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final FindingPatchStatusEnum status;



  @JsonKey(
    
    name: r'reason',
    required: true,
    includeIfNull: false,
  )


  final String reason;





    @override
    bool operator ==(Object other) => identical(this, other) || other is FindingPatch &&
      other.status == status &&
      other.reason == reason;

    @override
    int get hashCode =>
        status.hashCode +
        reason.hashCode;

  factory FindingPatch.fromJson(Map<String, dynamic> json) => _$FindingPatchFromJson(json);

  Map<String, dynamic> toJson() => _$FindingPatchToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum FindingPatchStatusEnum {
@JsonValue(r'dismissed')
dismissed(r'dismissed');

const FindingPatchStatusEnum(this.value);

final String value;

@override
String toString() => value;
}



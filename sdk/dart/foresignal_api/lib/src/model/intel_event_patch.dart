//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/severity.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'intel_event_patch.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IntelEventPatch {
  /// Returns a new [IntelEventPatch] instance.
  IntelEventPatch({

     this.title,

     this.status,

     this.severity,

     this.notes,
  });

  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



  @JsonKey(
    
    name: r'status',
    required: false,
    includeIfNull: false,
  )


  final IntelEventPatchStatusEnum? status;



      /// Override; null clears the override
  @JsonKey(
    
    name: r'severity',
    required: false,
    includeIfNull: false,
  )


  final Severity? severity;



  @JsonKey(
    
    name: r'notes',
    required: false,
    includeIfNull: false,
  )


  final String? notes;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IntelEventPatch &&
      other.title == title &&
      other.status == status &&
      other.severity == severity &&
      other.notes == notes;

    @override
    int get hashCode =>
        title.hashCode +
        status.hashCode +
        (severity == null ? 0 : severity.hashCode) +
        (notes == null ? 0 : notes.hashCode);

  factory IntelEventPatch.fromJson(Map<String, dynamic> json) => _$IntelEventPatchFromJson(json);

  Map<String, dynamic> toJson() => _$IntelEventPatchToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum IntelEventPatchStatusEnum {
@JsonValue(r'emerging')
emerging(r'emerging'),
@JsonValue(r'active')
active(r'active'),
@JsonValue(r'resolved')
resolved(r'resolved'),
@JsonValue(r'dismissed')
dismissed(r'dismissed');

const IntelEventPatchStatusEnum(this.value);

final String value;

@override
String toString() => value;
}



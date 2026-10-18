//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/situation_status.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'situation_changes_status.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SituationChangesStatus {
  /// Returns a new [SituationChangesStatus] instance.
  SituationChangesStatus({

     this.before,

    required  this.after,

    required  this.changed,
  });

  @JsonKey(
    
    name: r'before',
    required: false,
    includeIfNull: false,
  )


  final SituationStatus? before;



  @JsonKey(
    
    name: r'after',
    required: true,
    includeIfNull: false,
  )


  final SituationStatus after;



  @JsonKey(
    
    name: r'changed',
    required: true,
    includeIfNull: false,
  )


  final bool changed;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SituationChangesStatus &&
      other.before == before &&
      other.after == after &&
      other.changed == changed;

    @override
    int get hashCode =>
        (before == null ? 0 : before.hashCode) +
        after.hashCode +
        changed.hashCode;

  factory SituationChangesStatus.fromJson(Map<String, dynamic> json) => _$SituationChangesStatusFromJson(json);

  Map<String, dynamic> toJson() => _$SituationChangesStatusToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}


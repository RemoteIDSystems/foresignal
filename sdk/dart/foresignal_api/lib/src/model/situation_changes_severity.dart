//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/severity.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'situation_changes_severity.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SituationChangesSeverity {
  /// Returns a new [SituationChangesSeverity] instance.
  SituationChangesSeverity({

     this.before,

    required  this.after,

    required  this.changed,
  });

  @JsonKey(
    
    name: r'before',
    required: false,
    includeIfNull: false,
  )


  final Severity? before;



  @JsonKey(
    
    name: r'after',
    required: true,
    includeIfNull: false,
  )


  final Severity after;



  @JsonKey(
    
    name: r'changed',
    required: true,
    includeIfNull: false,
  )


  final bool changed;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SituationChangesSeverity &&
      other.before == before &&
      other.after == after &&
      other.changed == changed;

    @override
    int get hashCode =>
        (before == null ? 0 : before.hashCode) +
        after.hashCode +
        changed.hashCode;

  factory SituationChangesSeverity.fromJson(Map<String, dynamic> json) => _$SituationChangesSeverityFromJson(json);

  Map<String, dynamic> toJson() => _$SituationChangesSeverityToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}


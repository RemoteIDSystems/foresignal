//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'projected_impact_skipped_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ProjectedImpactSkippedInner {
  /// Returns a new [ProjectedImpactSkippedInner] instance.
  ProjectedImpactSkippedInner({

    required  this.impactFamily,

     this.regionClass,

     this.n,

    required  this.reason,
  });

  @JsonKey(
    
    name: r'impactFamily',
    required: true,
    includeIfNull: false,
  )


  final String impactFamily;



  @JsonKey(
    
    name: r'regionClass',
    required: false,
    includeIfNull: false,
  )


  final String? regionClass;



  @JsonKey(
    
    name: r'n',
    required: false,
    includeIfNull: false,
  )


  final int? n;



  @JsonKey(
    
    name: r'reason',
    required: true,
    includeIfNull: false,
  )


  final String reason;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ProjectedImpactSkippedInner &&
      other.impactFamily == impactFamily &&
      other.regionClass == regionClass &&
      other.n == n &&
      other.reason == reason;

    @override
    int get hashCode =>
        impactFamily.hashCode +
        (regionClass == null ? 0 : regionClass.hashCode) +
        (n == null ? 0 : n.hashCode) +
        reason.hashCode;

  factory ProjectedImpactSkippedInner.fromJson(Map<String, dynamic> json) => _$ProjectedImpactSkippedInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ProjectedImpactSkippedInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}


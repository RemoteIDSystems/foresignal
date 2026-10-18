//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'situation_evidence_member_counts.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SituationEvidenceMemberCounts {
  /// Returns a new [SituationEvidenceMemberCounts] instance.
  SituationEvidenceMemberCounts({

     this.byType,

     this.byFamily,
  });

      /// Count per member type
  @JsonKey(
    
    name: r'byType',
    required: false,
    includeIfNull: false,
  )


  final Map<String, int>? byType;



      /// Count per signal family
  @JsonKey(
    
    name: r'byFamily',
    required: false,
    includeIfNull: false,
  )


  final Map<String, int>? byFamily;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SituationEvidenceMemberCounts &&
      other.byType == byType &&
      other.byFamily == byFamily;

    @override
    int get hashCode =>
        byType.hashCode +
        byFamily.hashCode;

  factory SituationEvidenceMemberCounts.fromJson(Map<String, dynamic> json) => _$SituationEvidenceMemberCountsFromJson(json);

  Map<String, dynamic> toJson() => _$SituationEvidenceMemberCountsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}


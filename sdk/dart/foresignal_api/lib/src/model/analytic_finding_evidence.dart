//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analytic_finding_evidence.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalyticFindingEvidence {
  /// Returns a new [AnalyticFindingEvidence] instance.
  AnalyticFindingEvidence({

    required  this.seriesRefs,

    required  this.members,

     this.valuesWithheld,
  });

  @JsonKey(
    
    name: r'seriesRefs',
    required: true,
    includeIfNull: false,
  )


  final List<String> seriesRefs;



      /// Pinned refs (records with `@seq`, intel events, situations, findings)
  @JsonKey(
    
    name: r'members',
    required: true,
    includeIfNull: false,
  )


  final List<String> members;



      /// Raw values of `LOCAL_ONLY` series withheld; standardised statistics remain
  @JsonKey(
    
    name: r'valuesWithheld',
    required: false,
    includeIfNull: false,
  )


  final bool? valuesWithheld;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalyticFindingEvidence &&
      other.seriesRefs == seriesRefs &&
      other.members == members &&
      other.valuesWithheld == valuesWithheld;

    @override
    int get hashCode =>
        seriesRefs.hashCode +
        members.hashCode +
        valuesWithheld.hashCode;

  factory AnalyticFindingEvidence.fromJson(Map<String, dynamic> json) => _$AnalyticFindingEvidenceFromJson(json);

  Map<String, dynamic> toJson() => _$AnalyticFindingEvidenceToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}


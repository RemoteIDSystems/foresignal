//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'situation_evidence_corroboration.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SituationEvidenceCorroboration {
  /// Returns a new [SituationEvidenceCorroboration] instance.
  SituationEvidenceCorroboration({

    required  this.corroborated,

    required  this.uncorroborated,

     this.unknown,

     this.notApplicable,

    required  this.share,

     this.uncorroboratedOnly,
  });

  @JsonKey(
    
    name: r'corroborated',
    required: true,
    includeIfNull: false,
  )


  final int corroborated;



  @JsonKey(
    
    name: r'uncorroborated',
    required: true,
    includeIfNull: false,
  )


  final int uncorroborated;



  @JsonKey(
    
    name: r'unknown',
    required: false,
    includeIfNull: false,
  )


  final int? unknown;



  @JsonKey(
    
    name: r'notApplicable',
    required: false,
    includeIfNull: false,
  )


  final int? notApplicable;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'share',
    required: true,
    includeIfNull: true,
  )


  final num? share;



      /// True when the situation rests only on uncorroborated text reports
  @JsonKey(
    
    name: r'uncorroboratedOnly',
    required: false,
    includeIfNull: false,
  )


  final bool? uncorroboratedOnly;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SituationEvidenceCorroboration &&
      other.corroborated == corroborated &&
      other.uncorroborated == uncorroborated &&
      other.unknown == unknown &&
      other.notApplicable == notApplicable &&
      other.share == share &&
      other.uncorroboratedOnly == uncorroboratedOnly;

    @override
    int get hashCode =>
        corroborated.hashCode +
        uncorroborated.hashCode +
        unknown.hashCode +
        notApplicable.hashCode +
        (share == null ? 0 : share.hashCode) +
        uncorroboratedOnly.hashCode;

  factory SituationEvidenceCorroboration.fromJson(Map<String, dynamic> json) => _$SituationEvidenceCorroborationFromJson(json);

  Map<String, dynamic> toJson() => _$SituationEvidenceCorroborationToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}


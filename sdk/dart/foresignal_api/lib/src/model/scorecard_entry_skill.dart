//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/stat_interval.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'scorecard_entry_skill.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ScorecardEntrySkill {
  /// Returns a new [ScorecardEntrySkill] instance.
  ScorecardEntrySkill({

    required  this.value,

    required  this.reference,

    required  this.nShared,

     this.interval,
  });

  @JsonKey(
    
    name: r'value',
    required: true,
    includeIfNull: false,
  )


  final num value;



      /// Reference forecaster (`base-rate` or `climatology`)
  @JsonKey(
    
    name: r'reference',
    required: true,
    includeIfNull: false,
  )


  final String reference;



  @JsonKey(
    
    name: r'nShared',
    required: true,
    includeIfNull: false,
  )


  final int nShared;



  @JsonKey(
    
    name: r'interval',
    required: false,
    includeIfNull: false,
  )


  final StatInterval? interval;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ScorecardEntrySkill &&
      other.value == value &&
      other.reference == reference &&
      other.nShared == nShared &&
      other.interval == interval;

    @override
    int get hashCode =>
        value.hashCode +
        reference.hashCode +
        nShared.hashCode +
        (interval == null ? 0 : interval.hashCode);

  factory ScorecardEntrySkill.fromJson(Map<String, dynamic> json) => _$ScorecardEntrySkillFromJson(json);

  Map<String, dynamic> toJson() => _$ScorecardEntrySkillToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}


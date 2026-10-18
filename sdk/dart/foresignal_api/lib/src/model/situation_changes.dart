//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/situation_changes_status.dart';
import 'package:foresignal_api/src/model/situation_changes_severity.dart';
import 'package:foresignal_api/src/model/situation_changes_area.dart';
import 'package:foresignal_api/src/model/situation_changes_new_members.dart';
import 'package:foresignal_api/src/model/situation_changes_key_values_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'situation_changes.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SituationChanges {
  /// Returns a new [SituationChanges] instance.
  SituationChanges({

    required  this.situationId,

    required  this.since,

     this.baselineDay,

    required  this.now,

    required  this.status,

    required  this.severity,

    required  this.newMembers,

     this.area,

    required  this.regionsAdded,

     this.regionsRemoved,

     this.keyValues,

     this.manifestId,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'situationId',
    required: true,
    includeIfNull: false,
  )


  final String situationId;



      /// Requested time; the snapshot of its UTC day is the baseline
  @JsonKey(
    
    name: r'since',
    required: true,
    includeIfNull: false,
  )


  final DateTime since;



      /// Snapshot day used; null when the situation started after `since`
  @JsonKey(
    
    name: r'baselineDay',
    required: false,
    includeIfNull: false,
  )


  final DateTime? baselineDay;



  @JsonKey(
    
    name: r'now',
    required: true,
    includeIfNull: false,
  )


  final DateTime now;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final SituationChangesStatus status;



  @JsonKey(
    
    name: r'severity',
    required: true,
    includeIfNull: false,
  )


  final SituationChangesSeverity severity;



  @JsonKey(
    
    name: r'newMembers',
    required: true,
    includeIfNull: false,
  )


  final SituationChangesNewMembers newMembers;



  @JsonKey(
    
    name: r'area',
    required: false,
    includeIfNull: false,
  )


  final SituationChangesArea? area;



  @JsonKey(
    
    name: r'regionsAdded',
    required: true,
    includeIfNull: false,
  )


  final List<String> regionsAdded;



  @JsonKey(
    
    name: r'regionsRemoved',
    required: false,
    includeIfNull: false,
  )


  final List<String>? regionsRemoved;



  @JsonKey(
    
    name: r'keyValues',
    required: false,
    includeIfNull: false,
  )


  final List<SituationChangesKeyValuesInner>? keyValues;



      /// ULID identifier
  @JsonKey(
    
    name: r'manifestId',
    required: false,
    includeIfNull: false,
  )


  final String? manifestId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SituationChanges &&
      other.situationId == situationId &&
      other.since == since &&
      other.baselineDay == baselineDay &&
      other.now == now &&
      other.status == status &&
      other.severity == severity &&
      other.newMembers == newMembers &&
      other.area == area &&
      other.regionsAdded == regionsAdded &&
      other.regionsRemoved == regionsRemoved &&
      other.keyValues == keyValues &&
      other.manifestId == manifestId;

    @override
    int get hashCode =>
        situationId.hashCode +
        since.hashCode +
        (baselineDay == null ? 0 : baselineDay.hashCode) +
        now.hashCode +
        status.hashCode +
        severity.hashCode +
        newMembers.hashCode +
        area.hashCode +
        regionsAdded.hashCode +
        regionsRemoved.hashCode +
        keyValues.hashCode +
        (manifestId == null ? 0 : manifestId.hashCode);

  factory SituationChanges.fromJson(Map<String, dynamic> json) => _$SituationChangesFromJson(json);

  Map<String, dynamic> toJson() => _$SituationChangesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}


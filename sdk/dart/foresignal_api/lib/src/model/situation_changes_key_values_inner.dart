//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'situation_changes_key_values_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SituationChangesKeyValuesInner {
  /// Returns a new [SituationChangesKeyValuesInner] instance.
  SituationChangesKeyValuesInner({

    required  this.ref,

     this.before,

     this.after,

     this.withheld,
  });

      /// Series reference (Intelligence-Design §4.3): `series:{sourceId}/{seriesId}` (093/117 series; `series:imf-portwatch-chokepoints/{portId}` for PortWatch daily transits), `rollup:{hour|day}/{sourceId|*}/{kind}/{region|cell:h3}` (activity rollups; `*` is the cross-source event count with canonical events counted once) `rule:{ruleId}/{groupKey}` (100 anomaly rule samples) or `rollup:news/{topicId|situationId}/{bucket}` (133: daily news coverage in evidence groups by lean bucket left, center, right, unrated or state); or `social:{topic|place|entity}:{id}/{platform}/{metric}[/{language}]` (135: hourly social mention volume of a scope on one network, metric posts, uniqueAuthors, reposts or engagement, every language summed unless a language bucket is given; `social:all/{platform}/denominator[/{language}]` is the platform-language denominator; complete and paged collection only)
  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;



  @JsonKey(
    
    name: r'before',
    required: false,
    includeIfNull: false,
  )


  final num? before;



  @JsonKey(
    
    name: r'after',
    required: false,
    includeIfNull: false,
  )


  final num? after;



      /// LOCAL_ONLY value withheld
  @JsonKey(
    
    name: r'withheld',
    required: false,
    includeIfNull: false,
  )


  final bool? withheld;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SituationChangesKeyValuesInner &&
      other.ref == ref &&
      other.before == before &&
      other.after == after &&
      other.withheld == withheld;

    @override
    int get hashCode =>
        ref.hashCode +
        (before == null ? 0 : before.hashCode) +
        (after == null ? 0 : after.hashCode) +
        withheld.hashCode;

  factory SituationChangesKeyValuesInner.fromJson(Map<String, dynamic> json) => _$SituationChangesKeyValuesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$SituationChangesKeyValuesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}


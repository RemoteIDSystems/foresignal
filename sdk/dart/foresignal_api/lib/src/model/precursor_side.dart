//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/event_selector.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'precursor_side.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PrecursorSide {
  /// Returns a new [PrecursorSide] instance.
  PrecursorSide({

     this.selector,

     this.seriesRef,

     this.family,

    required  this.label,

     this.threshold,
  });

  @JsonKey(
    
    name: r'selector',
    required: false,
    includeIfNull: false,
  )


  final EventSelector? selector;



      /// Series reference (Intelligence-Design §4.3): `series:{sourceId}/{seriesId}` (093/117 series; `series:imf-portwatch-chokepoints/{portId}` for PortWatch daily transits), `rollup:{hour|day}/{sourceId|*}/{kind}/{region|cell:h3}` (activity rollups; `*` is the cross-source event count with canonical events counted once) `rule:{ruleId}/{groupKey}` (100 anomaly rule samples) or `rollup:news/{topicId|situationId}/{bucket}` (133: daily news coverage in evidence groups by lean bucket left, center, right, unrated or state); or `social:{topic|place|entity}:{id}/{platform}/{metric}[/{language}]` (135: hourly social mention volume of a scope on one network, metric posts, uniqueAuthors, reposts or engagement, every language summed unless a language bucket is given; `social:all/{platform}/denominator[/{language}]` is the platform-language denominator; complete and paged collection only)
  @JsonKey(
    
    name: r'seriesRef',
    required: false,
    includeIfNull: false,
  )


  final String? seriesRef;



      /// Event family or series family
  @JsonKey(
    
    name: r'family',
    required: false,
    includeIfNull: false,
  )


  final String? family;



      /// Display label
  @JsonKey(
    
    name: r'label',
    required: true,
    includeIfNull: false,
  )


  final String label;



      /// Series antecedents: standardised threshold that counts as an occurrence (default 2.0)
  @JsonKey(
    
    name: r'threshold',
    required: false,
    includeIfNull: false,
  )


  final num? threshold;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PrecursorSide &&
      other.selector == selector &&
      other.seriesRef == seriesRef &&
      other.family == family &&
      other.label == label &&
      other.threshold == threshold;

    @override
    int get hashCode =>
        (selector == null ? 0 : selector.hashCode) +
        (seriesRef == null ? 0 : seriesRef.hashCode) +
        (family == null ? 0 : family.hashCode) +
        label.hashCode +
        (threshold == null ? 0 : threshold.hashCode);

  factory PrecursorSide.fromJson(Map<String, dynamic> json) => _$PrecursorSideFromJson(json);

  Map<String, dynamic> toJson() => _$PrecursorSideToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}


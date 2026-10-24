//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/precursor_window_stats_reverse.dart';
import 'package:foresignal_api/src/model/stat_interval.dart';
import 'package:foresignal_api/src/model/time_range.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'precursor_window_stats.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PrecursorWindowStats {
  /// Returns a new [PrecursorWindowStats] instance.
  PrecursorWindowStats({

    required  this.window,

    required  this.n,

     this.hits,

     this.hitRate,

     this.hitRateInterval,

     this.baseRate,

     this.lift,

     this.correlation,

     this.bestLag,

     this.p,

     this.adjustedP,

    required  this.verdict,

     this.reverse,

     this.lowDetectabilityExcluded,

     this.coverageGapFraction,

     this.blockLength,

     this.reason,
  });

  @JsonKey(
    
    name: r'window',
    required: true,
    includeIfNull: false,
  )


  final TimeRange window;



      /// Antecedent occurrences (events) or overlapping points (series)
  @JsonKey(
    
    name: r'n',
    required: true,
    includeIfNull: false,
  )


  final int n;



  @JsonKey(
    
    name: r'hits',
    required: false,
    includeIfNull: false,
  )


  final int? hits;



  @JsonKey(
    
    name: r'hitRate',
    required: false,
    includeIfNull: false,
  )


  final num? hitRate;



  @JsonKey(
    
    name: r'hitRateInterval',
    required: false,
    includeIfNull: false,
  )


  final StatInterval? hitRateInterval;



      /// Hit rate expected by chance (circular-shift baseline)
  @JsonKey(
    
    name: r'baseRate',
    required: false,
    includeIfNull: false,
  )


  final num? baseRate;



  @JsonKey(
    
    name: r'lift',
    required: false,
    includeIfNull: false,
  )


  final num? lift;



      /// Series: cross-correlation at the best lag
  @JsonKey(
    
    name: r'correlation',
    required: false,
    includeIfNull: false,
  )


  final num? correlation;



      /// ISO-8601 duration
  @JsonKey(
    
    name: r'bestLag',
    required: false,
    includeIfNull: false,
  )


  final String? bestLag;



      /// Raw p-value
  @JsonKey(
    
    name: r'p',
    required: false,
    includeIfNull: false,
  )


  final num? p;



      /// BH or BY adjusted p within the batch
  @JsonKey(
    
    name: r'adjustedP',
    required: false,
    includeIfNull: false,
  )


  final num? adjustedP;



  @JsonKey(
    
    name: r'verdict',
    required: true,
    includeIfNull: false,
  )


  final PrecursorWindowStatsVerdictEnum verdict;



  @JsonKey(
    
    name: r'reverse',
    required: false,
    includeIfNull: false,
  )


  final PrecursorWindowStatsReverse? reverse;



      /// Antecedent occurrences excluded under `LOW_DETECTABILITY`
  @JsonKey(
    
    name: r'lowDetectabilityExcluded',
    required: false,
    includeIfNull: false,
  )


  final int? lowDetectabilityExcluded;



      /// Share of the window touched by coverage gaps (events: involved sources; series: missing buckets)
  @JsonKey(
    
    name: r'coverageGapFraction',
    required: false,
    includeIfNull: false,
  )


  final num? coverageGapFraction;



      /// Series: surrogate block length in buckets
  @JsonKey(
    
    name: r'blockLength',
    required: false,
    includeIfNull: false,
  )


  final int? blockLength;



      /// Why the window was insufficient
  @JsonKey(
    
    name: r'reason',
    required: false,
    includeIfNull: false,
  )


  final String? reason;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PrecursorWindowStats &&
      other.window == window &&
      other.n == n &&
      other.hits == hits &&
      other.hitRate == hitRate &&
      other.hitRateInterval == hitRateInterval &&
      other.baseRate == baseRate &&
      other.lift == lift &&
      other.correlation == correlation &&
      other.bestLag == bestLag &&
      other.p == p &&
      other.adjustedP == adjustedP &&
      other.verdict == verdict &&
      other.reverse == reverse &&
      other.lowDetectabilityExcluded == lowDetectabilityExcluded &&
      other.coverageGapFraction == coverageGapFraction &&
      other.blockLength == blockLength &&
      other.reason == reason;

    @override
    int get hashCode =>
        window.hashCode +
        n.hashCode +
        (hits == null ? 0 : hits.hashCode) +
        (hitRate == null ? 0 : hitRate.hashCode) +
        (hitRateInterval == null ? 0 : hitRateInterval.hashCode) +
        (baseRate == null ? 0 : baseRate.hashCode) +
        (lift == null ? 0 : lift.hashCode) +
        (correlation == null ? 0 : correlation.hashCode) +
        (bestLag == null ? 0 : bestLag.hashCode) +
        (p == null ? 0 : p.hashCode) +
        (adjustedP == null ? 0 : adjustedP.hashCode) +
        verdict.hashCode +
        (reverse == null ? 0 : reverse.hashCode) +
        lowDetectabilityExcluded.hashCode +
        (coverageGapFraction == null ? 0 : coverageGapFraction.hashCode) +
        (blockLength == null ? 0 : blockLength.hashCode) +
        (reason == null ? 0 : reason.hashCode);

  factory PrecursorWindowStats.fromJson(Map<String, dynamic> json) => _$PrecursorWindowStatsFromJson(json);

  Map<String, dynamic> toJson() => _$PrecursorWindowStatsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PrecursorWindowStatsVerdictEnum {
@JsonValue(r'insufficient')
insufficient(r'insufficient'),
@JsonValue(r'no-evidence-of-precedence')
noEvidenceOfPrecedence(r'no-evidence-of-precedence'),
@JsonValue(r'weak')
weak(r'weak'),
@JsonValue(r'notable')
notable(r'notable'),
@JsonValue(r'validated')
validated(r'validated'),
@JsonValue(r'failed')
failed(r'failed');

const PrecursorWindowStatsVerdictEnum(this.value);

final String value;

@override
String toString() => value;
}



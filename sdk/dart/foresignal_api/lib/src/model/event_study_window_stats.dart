//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/event_study_window_stats_placebo.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'event_study_window_stats.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EventStudyWindowStats {
  /// Returns a new [EventStudyWindowStats] instance.
  EventStudyWindowStats({

    required  this.window,

     this.caar,

     this.standardisedZ,

     this.standardisedP,

     this.corradoStat,

     this.corradoP,

     this.placeboP,

     this.placebo,
  });

      /// Offsets in buckets, inclusive
  @JsonKey(
    
    name: r'window',
    required: true,
    includeIfNull: false,
  )


  final List<int> window;



      /// Cumulative average abnormal value (standardised when values are withheld)
  @JsonKey(
    
    name: r'caar',
    required: false,
    includeIfNull: false,
  )


  final num? caar;



  @JsonKey(
    
    name: r'standardisedZ',
    required: false,
    includeIfNull: false,
  )


  final num? standardisedZ;



  @JsonKey(
    
    name: r'standardisedP',
    required: false,
    includeIfNull: false,
  )


  final num? standardisedP;



  @JsonKey(
    
    name: r'corradoStat',
    required: false,
    includeIfNull: false,
  )


  final num? corradoStat;



  @JsonKey(
    
    name: r'corradoP',
    required: false,
    includeIfNull: false,
  )


  final num? corradoP;



      /// Share of placebo windows at least as extreme
  @JsonKey(
    
    name: r'placeboP',
    required: false,
    includeIfNull: false,
  )


  final num? placeboP;



  @JsonKey(
    
    name: r'placebo',
    required: false,
    includeIfNull: false,
  )


  final EventStudyWindowStatsPlacebo? placebo;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EventStudyWindowStats &&
      other.window == window &&
      other.caar == caar &&
      other.standardisedZ == standardisedZ &&
      other.standardisedP == standardisedP &&
      other.corradoStat == corradoStat &&
      other.corradoP == corradoP &&
      other.placeboP == placeboP &&
      other.placebo == placebo;

    @override
    int get hashCode =>
        window.hashCode +
        (caar == null ? 0 : caar.hashCode) +
        (standardisedZ == null ? 0 : standardisedZ.hashCode) +
        (standardisedP == null ? 0 : standardisedP.hashCode) +
        (corradoStat == null ? 0 : corradoStat.hashCode) +
        (corradoP == null ? 0 : corradoP.hashCode) +
        (placeboP == null ? 0 : placeboP.hashCode) +
        placebo.hashCode;

  factory EventStudyWindowStats.fromJson(Map<String, dynamic> json) => _$EventStudyWindowStatsFromJson(json);

  Map<String, dynamic> toJson() => _$EventStudyWindowStatsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}


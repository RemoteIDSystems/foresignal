//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'frame_stats.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class FrameStats {
  /// Returns a new [FrameStats] instance.
  FrameStats({

     this.min,

     this.max,

     this.mean,

     this.coveragePct,

     this.codeCounts,

     this.flagMissing,
  });

  @JsonKey(
    
    name: r'min',
    required: false,
    includeIfNull: false,
  )


  final num? min;



  @JsonKey(
    
    name: r'max',
    required: false,
    includeIfNull: false,
  )


  final num? max;



  @JsonKey(
    
    name: r'mean',
    required: false,
    includeIfNull: false,
  )


  final num? mean;



      /// Share of grid cells with coverage
          // minimum: 0
          // maximum: 100
  @JsonKey(
    
    name: r'coveragePct',
    required: false,
    includeIfNull: false,
  )


  final num? coveragePct;



      /// Per-code counts for categorical products (raw code → cells)
  @JsonKey(
    
    name: r'codeCounts',
    required: false,
    includeIfNull: false,
  )


  final Map<String, int>? codeCounts;



      /// mrms-precip-rate: the PrecipFlag band was missing (committed after 4 min)
  @JsonKey(
    
    name: r'flagMissing',
    required: false,
    includeIfNull: false,
  )


  final bool? flagMissing;





    @override
    bool operator ==(Object other) => identical(this, other) || other is FrameStats &&
      other.min == min &&
      other.max == max &&
      other.mean == mean &&
      other.coveragePct == coveragePct &&
      other.codeCounts == codeCounts &&
      other.flagMissing == flagMissing;

    @override
    int get hashCode =>
        (min == null ? 0 : min.hashCode) +
        (max == null ? 0 : max.hashCode) +
        (mean == null ? 0 : mean.hashCode) +
        coveragePct.hashCode +
        codeCounts.hashCode +
        flagMissing.hashCode;

  factory FrameStats.fromJson(Map<String, dynamic> json) => _$FrameStatsFromJson(json);

  Map<String, dynamic> toJson() => _$FrameStatsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}


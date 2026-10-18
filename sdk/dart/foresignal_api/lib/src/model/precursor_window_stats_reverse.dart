//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'precursor_window_stats_reverse.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PrecursorWindowStatsReverse {
  /// Returns a new [PrecursorWindowStatsReverse] instance.
  PrecursorWindowStatsReverse({

     this.p,

     this.statistic,

    required  this.coMovement,
  });

  @JsonKey(
    
    name: r'p',
    required: false,
    includeIfNull: false,
  )


  final num? p;



  @JsonKey(
    
    name: r'statistic',
    required: false,
    includeIfNull: false,
  )


  final num? statistic;



      /// True when the reverse is as strong: co-movement, not precedence
  @JsonKey(
    
    name: r'coMovement',
    required: true,
    includeIfNull: false,
  )


  final bool coMovement;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PrecursorWindowStatsReverse &&
      other.p == p &&
      other.statistic == statistic &&
      other.coMovement == coMovement;

    @override
    int get hashCode =>
        (p == null ? 0 : p.hashCode) +
        (statistic == null ? 0 : statistic.hashCode) +
        coMovement.hashCode;

  factory PrecursorWindowStatsReverse.fromJson(Map<String, dynamic> json) => _$PrecursorWindowStatsReverseFromJson(json);

  Map<String, dynamic> toJson() => _$PrecursorWindowStatsReverseToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

